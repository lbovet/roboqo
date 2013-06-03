package li.chee.roboqo.runtime;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

/**
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class RuntimeVerticle extends Verticle {

    Runner runner = new Runner();

    public void start() throws Exception {
        final EventBus eb = vertx.eventBus();

        eb.registerHandler("runtime", new Handler<Message<JsonObject>>() {
            public void handle(final Message<JsonObject> message) {
                final String name = message.body.getString("name");
                switch(message.body.getString("command")) {
                    case "start":
                        runner.start(name);
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
                                eb.publish("script-"+name, status.toString());
                            }
                        });
                }
            }
        });
    }
}
