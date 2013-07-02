package li.chee.roboqo.runtime;

import li.chee.roboqo.controller.Controller;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Runs the statusHandlers in their own threads.
 *
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class Runner {

    public static final org.slf4j.Logger log = LoggerFactory.getLogger(Runner.class);

    private Controller controller;

    enum Status {
        CREATED,
        INVALID,
        RUNNING,
        DONE,
        STOPPED
    }

    private Map<String,Control> controls = new HashMap<>();
    private Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            }
    });

    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("JavaScript");

    public Runner(Controller controller) {
        if(controller!=null) {
            controller.init();
        }
        this.controller = controller;
        setGlobal("controller", controller);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                log.debug("Shutting down pool");
                ((ThreadPoolExecutor)executor).shutdownNow();
            }
        }));
    }

    public class Control {

        public Handler<Status> statusHandler;

        public Thread thread;

        public boolean shouldStop = false;

        public void sleep(long millis) {
            long remains=millis;
            while(remains > 0) {
                long step = Math.min(remains, 500);
                remains = remains-step;
                    try {
                        Thread.sleep(step);
                    } catch (InterruptedException e) {
                }
                checkStop();
            }
        }

        public void checkStop() {
            if(shouldStop) {
                throw new StoppedException();
            }
        }
    }

    public void setGlobal(String variable, Object object) {
        engine.put(variable, object);
    }

    public void create(String name, String script, Handler<Status> statusHandler) {
        stop(name);
        Control control = new Control();
        control.statusHandler = statusHandler;
        controls.put(name, control);
        report(name, Status.CREATED);
        try {
            engine.eval("function "+name+"(log, control) {\n"+script+"\n}");
        } catch (ScriptException e) {
            report(name, Status.INVALID);
            log.warn("Could not create function " + name +": "+e.getMessage());
        }
    }

    public void start(final String name) {
        log.debug("Starting " + name);
        final Invocable inv = (Invocable) engine;
        final Control control = controls.get(name);
        control.shouldStop = false;
        if(control == null) {
            log.error("Script was not created " + name);
            report(name, Status.INVALID);
            return;
        }
        report(name, Status.RUNNING);
        controls.put(name, control);
        executor.execute(new Runnable() {
            public void run() {
                control.thread = Thread.currentThread();
                try {
                    inv.invokeFunction(name, LoggerFactory.getLogger("script-"+name), control);
                    report(name, Status.DONE);
                } catch (ScriptException | NoSuchMethodException e) {
                    Throwable cause=e;
                    while(cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    if(cause instanceof StoppedException) {
                        log.debug("Script stopped " + name);
                        report(name, Status.STOPPED);
                    } else {
                        log.error("Could not run function " + name, e);
                        report(name, Status.INVALID);
                    }
                } finally {
                    if(controller!=null) {
                        controller.stop();
                    }
                    control.thread = null;
                }
            }
        });
    }

    public void stop(String name) {
        final Control control = controls.get(name);
        if(control == null) {
            report(name, Status.INVALID);
            return;
        }
        control.shouldStop = true;
        try {
            // If the thread is sleeping
            control.thread.interrupt();
        } catch(NullPointerException e) {
            // Ignore. The thread can have finished already.
        }
        controller.stop();
    }

    private void report(String name, Status status) {
        Control control = controls.get(name);
        if(control!=null) {
            control.statusHandler.handle(status);
        }
    }

}
