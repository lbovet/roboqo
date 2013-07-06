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
package li.chee.roboqo.server;

import li.chee.vertx.reststorage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Entry point.
 *
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class ServerVerticle extends Verticle {

    int port;
    private Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    @Override
    public void start() throws Exception {
        port = 8383;
        final HttpServer server = vertx.createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();
        server.requestHandler(routeMatcher);
        EtagStore etagStore;

        if(System.getProperty("cache", "on").equals("on")) {
            etagStore = new SharedMapEtagStore(vertx);
        } else {
            etagStore = new EmptyEtagStore();
        }

        // Stores the projects and the blocks
        RestStorageHandler dataStorageHandler =
                new RestStorageHandler(new FileSystemStorage("./data"), etagStore, "/data", null);
        routeMatcher.allWithRegEx("/data/.*", dataStorageHandler);

        // Webapp
        RestStorageHandler appStorageHandler =
                new RestStorageHandler(new FileSystemStorage("./web"), etagStore, "", null);
        routeMatcher.get("/", new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest request) {
                request.response.statusCode = 302;
                request.response.headers().put("Location", "/index.html");
                request.response.end();
            }
        });
        routeMatcher.getWithRegEx("/.*", appStorageHandler);

        // Scripts
        routeMatcher.putWithRegEx("/scripts/.*", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                final String name = lastPart(request.uri);
                request.bodyHandler(new Handler<Buffer>() {
                    public void handle(final Buffer buffer) {
                        vertx.eventBus().send("runtime",
                                new JsonObject().putString("command", "create").putString("name", name).putString("script", buffer.toString()),
                                new Handler<Message<JsonObject>>() {
                                    @Override
                                    public void handle(Message<JsonObject> reply) {
                                        switch (reply.body.getString("status")) {
                                            case "ok":
                                                logger.debug("Saved script " + name);
                                                request.response.statusCode = 200;
                                                request.response.statusMessage = "OK";
                                                break;
                                            case "error":
                                                logger.error("Bad script " + name);
                                                logger.debug("Bad script " + buffer.toString());
                                                request.response.statusCode = 400;
                                                request.response.statusMessage = "Bad script";
                                                break;
                                        }
                                        request.response.end();
                                    }
                                });
                    }
                });
            }
        });

        // Executions
        routeMatcher.putWithRegEx("/executions/.*", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                final String name = lastPart(request.uri);
                request.bodyHandler(new Handler<Buffer>() {
                    public void handle(Buffer buffer) {
                        vertx.eventBus().send("runtime", new JsonObject().putString("command", "start").putString("name", name));
                        logger.debug("Created execution for " + name);
                        request.response.statusCode = 202;
                        request.response.statusMessage = "Accepted";
                        request.response.end();
                    }
                });
            }
        });

        routeMatcher.deleteWithRegEx("/executions/.*", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest request) {
                final String name = lastPart(request.uri);
                request.bodyHandler(new Handler<Buffer>() {
                    public void handle(Buffer buffer) {
                        vertx.eventBus().send("runtime", new JsonObject().putString("command", "stop").putString("name", name));
                        logger.debug("Stopping execution for " + name);
                        request.response.statusCode = 200;
                        request.response.statusMessage = "OK";
                        request.response.end();
                    }
                });
            }
        });

        // Event-bus extended to the client
        JsonObject config = new JsonObject().putString("prefix", "/sock");
        JsonArray permitted = new JsonArray();
        permitted.add(new JsonObject());
        vertx.createSockJSServer(server).bridge(config, permitted, permitted);

        container.deployVerticle("li.chee.roboqo.runtime.RuntimeVerticle", null, 1, new Handler<String>() {
            public void handle(String event) {
                server.listen(port);
                Properties props = new Properties();
                InputStream in=null;
                try {
                    in = getClass().getClassLoader().getResourceAsStream("build.properties");
                    props.load(in);
                } catch (IOException e) {
                    logger.error("Could not load build.properties", e);
                } finally {
                    if(in!=null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
                System.err.println("Roboqo "+props.getProperty("version")+" started on http://localhost:" + port);
            }
        });
    }

    private String lastPart(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
