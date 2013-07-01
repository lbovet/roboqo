package li.chee.roboqo.controller;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.impl.VertxLocator;

/**
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class HummingbirdController implements Controller {

    private final EventBus eventBus;
    private String controllerStatus="connecting";


    public HummingbirdController() {
        this.eventBus = VertxLocator.vertx.eventBus();
        VertxLocator.vertx.setPeriodic(2000, new Handler<Long>() {
            public void handle(Long event) {
                update(new JsonObject().putString("controller", controllerStatus));
            }
        });
    }

    public void motor(int id, int speed) {
        update(new JsonObject().putObject("motor",
                new JsonObject().putNumber("" + id, speed)));
    }

    public void servo(int id, int position) {
        update(new JsonObject().putObject("servo",
                new JsonObject().putNumber("" + id, position)));
    }

    public void vibration(int id, int speed) {
        update(new JsonObject().putObject("vibration",
                new JsonObject().putNumber("" + id, speed)));
    }

    public int sensor(int id) {
        return 0;
    }


    public void led(int id, int lightness) {
        update(new JsonObject().putObject("led",
                new JsonObject().putNumber("" + id, lightness)));
    }

    public void triled(int id, String color) {
        update(new JsonObject().putObject("triled",
                new JsonObject().putString("" + id, color)));
    }

    @Override
    public void init() {
        VertxLocator.vertx.setTimer(2000, new Handler<Long>() {
            public void handle(Long event) {
                controllerStatus = "connected";
            }
        });
    }

    @Override
    public void stop() {

    }

    private void update(JsonObject status) {
        eventBus.publish("controller-status", status);
    }

}
