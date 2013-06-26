var network = function(){

    var eb;

    function init() {
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
                $("#feedback").addClass("active");
                $("#feedback").html(msg.status)
            });
            eb.registerHandler("controller-status", function (msg, replyTo) {
                state.update(msg);
            });
            network.eb = eb;
        };
        eb.onclose = function () {
            console.log("closed");
            state.server.disconnected();
            setTimeout(init, 5000);
        };
    }

    return {
        init: init
    };

}();
