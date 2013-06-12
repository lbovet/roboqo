package li.chee.roboqo.runtime;

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

/**
 * Runs the statusHandlers in their own threads.
 *
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class Runner {

    public static final org.slf4j.Logger log = LoggerFactory.getLogger(Runner.class);

    enum Status {
        CREATED,
        INVALID,
        RUNNING,
        DONE,
        STOPPED
    }

    private Map<String,Control> controls = new HashMap<>();
    private Executor executor = Executors.newCachedThreadPool();

    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("JavaScript");

    public class Control {

        public Handler<Status> statusHandler;

        public Thread thread;

        public boolean shouldStop = false;

        public void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
            }
            if(shouldStop) {
                throw new StoppedException();
            }
        }
    }

    public void setGlobal(String variable, Object object) {
        engine.put(variable, object);
    }

    public void create(String name, String script, Handler<Status> statusHandler) {
        Control control = new Control();
        control.statusHandler = statusHandler;
        controls.put(name, control);
        report(name, Status.CREATED);
        try {
            engine.eval("function "+name+"(__log__, __control__) {\n"+script+"\n}");
        } catch (ScriptException e) {
            report(name, Status.INVALID);
            log.error("Could not create function "+name, e);
        }
    }

    public void start(final String name) {
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
                    control.thread = null;
                }
            }
        });
    }

    public void stop(String name) {
        final Control control = controls.get(name);
        if(control == null) {
            log.error("Script was not created " + name);
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
    }

    private void report(String name, Status status) {
        controls.get(name).statusHandler.handle(status);
    }

}
