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
var state = function(){

    var server = {
        disconnected : function() {
            $("#server-status").removeClass();
            $("#server-status").addClass("status-icon").addClass("icon-c-plug").addClass("problem");
        },
        connecting : function() {
            $("#server-status").removeClass();
            $("#server-status").addClass("status-icon").addClass("icon-c-plug").addClass("inactive");
        },
        connected : function() {
            $("#server-status").removeClass();
            $("#server-status").addClass("status-icon").addClass("icon-c-ok").addClass("ok");
        }
    }

    var controller = {
        disconnected : function() {
            $(".activable").removeClass("active").addClass("inactive");
            $("#controller-status").removeClass();
            $("#controller-status").addClass("status-icon").addClass("icon-c-plug").addClass("problem");
            reset();
        },
        connecting : function() {
            $(".activable").removeClass("active").addClass("inactive");
            $("#controller-status").removeClass();
            $("#controller-status").addClass("status-icon").addClass("icon-c-plug").addClass("inactive");
            reset();
        },
        connected : function() {
            $("#controller-status").removeClass();
            $("#controller-status").addClass("status-icon").addClass("icon-c-ok").addClass("ok");
        }
    }

    function servo(id, value) {
        var servo = $("#servo-"+id);
        servo.removeClass("inactive").addClass("active");
        servo.css("-webkit-transform", "rotate("+(180*value)/255+"deg)");
    }

    function motor(id, value) {
        var motor = $("#motor-"+id);
        motor.removeClass("inactive").addClass("active");
        if(value!=0) {
            var duration = ((255-Math.abs(value))/255)*3+0.5;
            motor.css("-webkit-animation-duration", duration+"s");
            motor.css("-webkit-animation-play-state", "running");
            motor.css("-moz-animation-duration", duration+"s");
            motor.css("-moz-animation-play-state", "running");
            if(value>0) {
                motor.css("webkit-animation-name", "rotate");
                motor.css("-moz-animation-name", "rotate");

            } else {
                motor.css("-webkit-animation-name", "rotate-reverse");
                motor.css("-moz-animation-name", "rotate-reverse");
            }
        } else {
            motor.css("-webkit-animation-play-state", "paused");
            motor.css("-moz-animation-play-state", "paused");
        }
    }

    function vibration(id, value) {
        var vibration = $("#vibration-"+id);
        vibration.removeClass("inactive").addClass("active");
        if(value!=0) {
            var duration = ((255-Math.abs(value))/1024)+0.05;
            vibration.css("-webkit-animation-duration", duration+"s");
            vibration.css("-webkit-animation-play-state", "running");
            vibration.css("-moz-animation-duration", duration+"s");
            vibration.css("-moz-animation-play-state", "running");

        } else {
            vibration.css("-webkit-animation-play-state", "paused");
            vibration.css("-moz-animation-play-state", "paused");
        }
    }

    function led(id, value) {
        var led = $("#led-"+id);
        led.removeClass("inactive-border").addClass("active-border");
        led.css("background", "hsl(56,100%,"+(90*value/255)+"%)");
    }

    function triled(id, color) {
        var triled = $("#triled-"+id);
        triled.removeClass("inactive-border").addClass("active-border");
        triled.css("background", color);
    }

    function sensor(id, value) {
        var sensor = $("#sensor-"+id);
        sensor.removeClass("inactive-border").addClass("active-border");
        sensor.children().first().css("width", (32*value/255)+"px");
    }

    function reset() {
        motor(1, 0);
        motor(2, 0);
        servo(1, 0);
        servo(2, 0);
        servo(3, 0);
        servo(4, 0);
        vibration(1, 0);
        vibration(2, 0);
        sensor(1, 0);
        sensor(2, 0);
        sensor(3, 0);
        sensor(4, 0);
        $(".activable").removeClass("active").addClass("inactive");
        $(".activable-border").css("background","").removeClass("active-border").addClass("inactive-border");
    }

    function update(status) {
        $.each(status, function(k, v) {
            if(k === "controller") {
                switch(v) {
                    case "connecting": controller.connecting(); break;
                    case "connected": controller.connected(); break;
                    case "disconnected": controller.disconnected(); break;
                }
            } else {
                $.each(v, function(id,value) {
                    functions[k](id, value);
                });
            }
        });
    };

     var functions = {
        server: server,
        controller: controller,
        servo: servo,
        motor: motor,
        vibration: vibration,
        led: led,
        triled: triled,
        sensor: sensor,
        update: update,
        reset: reset
    };

    return functions;
}();
