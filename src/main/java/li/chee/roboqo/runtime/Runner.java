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

    enum Status {
        CREATED,
        INVALID,
        RUNNING,
        STOPPED
    }

    public final static String PREFIX = "__roboqo_";
    public final static String STOP_FLAG = PREFIX+"_stop_";

    private Map<String,Handler<Status>> statusHandlers = new HashMap<>();
    private Executor executor = Executors.newCachedThreadPool();

    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("JavaScript");

    public Runner() {
        engine.put("log", LoggerFactory.getLogger("script"));
    }

    public void setGlobal(String variable, Object object) {
        engine.put(variable, object);
    }

    public void create(String name, String script, Handler<Status> statusHandler) {
        statusHandlers.put(name, statusHandler);
        report(name, Status.CREATED);
        try {
            engine.eval("function "+PREFIX+name+"() {\n"+script+"\n}");
        } catch (ScriptException e) {
            report(name, Status.INVALID);
            LoggerFactory.getLogger(Runner.class).error("Could not create function "+name, e);
        }
    }

    public void start(final String name) {
        final Invocable inv = (Invocable) engine;
        report(name, Status.RUNNING);
        engine.put(STOP_FLAG+name, false);
        executor.execute(new Runnable() {
            public void run() {
                try {
                    inv.invokeFunction(PREFIX+name);
                } catch (ScriptException | NoSuchMethodException e) {
                    LoggerFactory.getLogger(Runner.class).error("Could not run function " + name, e);
                } finally {
                    report(name, Status.STOPPED);
                }
            }
        });
    }

    public void stop(String name) {
        engine.put(STOP_FLAG+name, true);
    }

    private void report(String name, Status status) {
        Handler<Status> statusHandler = this.statusHandlers.get(name);
        statusHandler.handle(status);
    }

}
