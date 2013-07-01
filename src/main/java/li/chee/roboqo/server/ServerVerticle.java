package li.chee.roboqo.server;

import li.chee.vertx.reststorage.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

import java.util.Date;

/**
 * Entry point.
 *
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class ServerVerticle extends Verticle {

    int port;

    @Override
    public void start() throws Exception {
        port = 8383;
        final HttpServer server = vertx.createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();
        server.requestHandler(routeMatcher);
        EtagStore etagStore = new EmptyEtagStore();
        //EtagStore etagStore = new SharedMapEtagStore();

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
                                                container.getLogger().debug("Saved script " + name);
                                                request.response.statusCode = 200;
                                                request.response.statusMessage = "OK";
                                                break;
                                            case "error":
                                                container.getLogger().error("Bad script " + name);
                                                container.getLogger().debug("Bad script " + buffer.toString());
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
                        container.getLogger().debug("Created execution for " + name);
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
                        container.getLogger().debug("Stopping execution for " + name);
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
                container.getLogger().info("\nRoboqo started on http://localhost:" + port);
            }
        });
    }

    private String lastPart(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
}
