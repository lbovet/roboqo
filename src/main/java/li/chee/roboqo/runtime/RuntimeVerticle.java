/*
   Copyright 2013 Laurent Bovet <laurent.bovet@windmaster.ch>

   This file is part of Roboqo

   Roboqo is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
