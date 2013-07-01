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
    private int[] sensors = new int[4];
    private int[] sensorsNew = new int[4];

    public HummingbirdController() {
        this.eventBus = VertxLocator.vertx.eventBus();
        VertxLocator.vertx.setPeriodic(2000, new Handler<Long>() {
            public void handle(Long event) {
                update(new JsonObject().putString("controller", controllerStatus));
            }
        });
        VertxLocator.vertx.setPeriodic(500, new Handler<Long>() {
            public void handle(Long event) {
                JsonObject info = new JsonObject();
                JsonObject sensorValues = new JsonObject();
                info.putObject("sensor", sensorValues);
                boolean update = false;
                for(int i=0; i<4; i++) {
                    if(sensorsNew[i] != sensors[i]) {
                        sensorValues.putNumber(""+(i+1), sensorsNew[i]);
                        sensors[i] = sensorsNew[i];
                        update=true;
                    }
                }
                if(update) {
                    update(info);
                }
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
        return sensors[id+1];
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
