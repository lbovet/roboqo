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
var network = function(){

    var eb;

    function init(oninit) {
        console.debug("connecting");
        state.server.connecting();
        if (window.location.protocol === "file:") {
            eb = new vertx.EventBus('http://localhost:8383/sock');
        } else {
            eb = new vertx.EventBus(window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/sock');
        }
        eb.onopen = function () {
            console.log("open");
            state.server.connected();
            eb.registerHandler("script-main", function (msg, replyTo) {
                switch(msg.status) {
                    case "CREATED":
                        $("#run").removeClass("disabled");
                        break;
                    case "RUNNING":
                        $("#run").addClass("active");
                        break;
                    case "INVALID":
                        $("#run").addClass("disabled");
                    default:
                        $("#run").removeClass("active");
                        state.reset();
                }
            });
            eb.registerHandler("controller-status", function (msg, replyTo) {
                state.update(msg);
            });
            network.eb = eb;
            oninit();
        };
        eb.onclose = function () {
            console.log("closed");
            state.server.disconnected();
            state.controller.connecting();
            setTimeout(function() { init(oninit) }, 5000);
        };
    }

    return {
        init: init
    };

}();
