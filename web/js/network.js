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
