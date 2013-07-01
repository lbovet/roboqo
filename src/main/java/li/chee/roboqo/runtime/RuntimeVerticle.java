package li.chee.roboqo.runtime;

import li.chee.roboqo.controller.HummingbirdController;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

/**
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class RuntimeVerticle extends Verticle {

    public void start() throws Exception {
        final EventBus eb = vertx.eventBus();
        final Runner runner = new Runner(new HummingbirdController());

        eb.registerHandler("runtime", new Handler<Message<JsonObject>>() {
            public void handle(final Message<JsonObject> message) {
                final String name = message.body.getString("name");
                LoggerFactory.getLogger(Runner.class).debug("Got " + message.body);
                switch(message.body.getString("command")) {
                    case "start":
                        runner.start(name);
                        break;
                    case "stop":
                        runner.stop(name);
                        break;
                    case "create":
                        runner.create(name, message.body.getString("script"), new Handler<Runner.Status>() {
                            public void handle(Runner.Status status) {
                                switch(status) {
                                    case CREATED:
                                        message.reply(new JsonObject().putString("status", "ok"));
                                        break;
                                    case INVALID:
                                        message.reply(new JsonObject().putString("status", "error"));
                                        break;
                                }
                                eb.publish("script-"+name, new JsonObject().putString("status", status.toString()));
                            }
                        });
                }
            }
        });
    }
}
