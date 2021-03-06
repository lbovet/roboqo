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
package li.chee.roboqo.controller;

import edu.cmu.ri.createlab.hummingbird.Hummingbird;
import edu.cmu.ri.createlab.hummingbird.HummingbirdFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.impl.VertxLocator;

import java.awt.*;

/**
 * @author Laurent Bovet (laurent.bovet@windmaster.ch)
 */
public class HummingbirdController implements Controller {

    private final EventBus eventBus;
    private String controllerStatus="connecting";
    private int[] sensors = new int[4];
    private int[] sensorsNew = new int[4];

    private Hummingbird hummingbird;
    private boolean disableMotors=false;

    private final static Logger log = LoggerFactory.getLogger(HummingbirdController.class);

    public HummingbirdController() {
        disableMotors = System.getProperty("motors", "on").equals("off");
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
        if(hummingbird!=null && !disableMotors) {
            hummingbird.setMotorVelocity(id-1, speed);
        }
        update(new JsonObject().putObject("motor",
                new JsonObject().putNumber("" + id, speed)));
    }

    public void servo(int id, int position) {
        if(hummingbird!=null) {
            hummingbird.setServoPosition(id-1, position);
        }
        update(new JsonObject().putObject("servo",
                new JsonObject().putNumber("" + id, position)));
    }

    public void vibration(int id, int speed) {
        if(hummingbird!=null) {
            hummingbird.setVibrationMotorSpeed(id-1, speed);
        }
        update(new JsonObject().putObject("vibration",
                new JsonObject().putNumber("" + id, speed)));
    }

    public int sensor(int id) {
        if(hummingbird!=null) {
            return hummingbird.getAnalogInputValue(id-1);
        } else {
            return 0;
        }
    }

    public void led(int id, int lightness) {
        if(hummingbird!=null) {
            hummingbird.setLED(id-1, lightness);
        }
        update(new JsonObject().putObject("led",
                new JsonObject().putNumber("" + id, lightness)));
    }

    public void triled(int id, String colorString) {
        if(hummingbird!=null) {
            Color color = Color.decode(colorString);
            hummingbird.setFullColorLED(id-1, colorComponent(color.getRed()), colorComponent(color.getGreen()), colorComponent(color.getBlue()));
        }
        update(new JsonObject().putObject("triled",
                new JsonObject().putString("" + id, colorString)));
    }

    private int colorComponent(int c) {
        return (int)(Math.pow(c/255.0,3)*255)/2;
    }

    @Override
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!System.getProperty("controller", "").equals("off")) {
                    VertxLocator.vertx.setPeriodic(500, new Handler<Long>() {
                        public void handle(Long event) {
                            if(hummingbird!=null) {
                                Hummingbird.HummingbirdState state = hummingbird.getState();
                                if(state!=null) {
                                    for(int i=0; i<4; i++) {
                                       sensorsNew = state.getAnalogInputValues();
                                    }
                                } else {
                                    log.debug("Lost Hummingbird controller");
                                    if(!controllerStatus.equals("connecting")) {
                                        log.debug("Trigger reconnection");
                                        synchronized (hummingbird) {
                                            hummingbird.notify();
                                        }
                                    }
                                    controllerStatus = "connecting";
                                }
                            }
                        }
                    });
                    while(true) {
                        controllerStatus = "connecting";
                        hummingbird = HummingbirdFactory.create();
                        log.debug("Connected to Hummingbird controller");
                        controllerStatus = "connected";
                        synchronized (hummingbird) {
                            try {
                                hummingbird.wait();
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        hummingbird.disconnect();
                    }
                }
            }
        }, "Initializer").start();
    }

    @Override
    public void stop() {
        if(hummingbird!=null) {
            hummingbird.emergencyStop();
        }
    }

    private void update(JsonObject status) {
        eventBus.publish("controller-status", status);
    }

}
