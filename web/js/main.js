$(document).ready(function() {

    var eb;
    function init() {
        console.debug("connecting");
        if(window.location.protocol==="file:") {
            eb = new vertx.EventBus('http://localhost:8383/sock');
        } else {
            eb = new vertx.EventBus(window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/sock');
        }
        eb.onopen = function() {
            console.log("open");
            $("#feedback").addClass("active");
            eb.registerHandler("script-main", function(msg, replyTo) {
                $("#feedback").addClass("active");
                $("#feedback").html(msg.status)
            });

        };

        eb.onclose = function() {
            $("#feedback").removeClass("active");
            console.log("closed");
            setTimeout(init, 5000);
        };
    }

    init();

    console.log("hello");

    $("#run").click(function() {
        console.log($("#script").val());
        eb.send('runtime', {
                command : 'create',
                name: 'main',
                script: $("#script").val()
            }, function(reply) {
            if (reply.status === 'ok') {
                console.log('Created');
                eb.send('runtime', {
                    command: "start",
                    name: "main"
                });
            } else {
                console.error('Failed to send task');
            }
        });
    });
});
