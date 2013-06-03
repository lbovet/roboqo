package li.chee.roboqo.runtime;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

/**
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class RunnerTest {
    @Test
    public void simpleScript() throws InterruptedException {
        final Runner r = new Runner();
        r.create("main", "log.info('hello')", new Handler<Runner.Status>() {
            public void handle(Runner.Status status) {
                LoggerFactory.getLogger("RunnerTest").info(status.toString());
                if(status== Runner.Status.STOPPED) {
                    synchronized(r) {
                        r.notify();
                    }
                }
            }
        });
        r.start("main");
        synchronized (r) {
            r.wait();
        }
    }
}
