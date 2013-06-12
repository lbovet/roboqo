var Blockly;

function initBlockly(blockly) {
    Blockly = blockly;
}


$(document).ready(function () {

    // Layout
    var layout;
    layout = $('body').layout({
        spacing_open: 0,
        west__size: 160,
        west__minSize: 120,
        west__maxSize: 256,
        west__slidable: false,
        west__resizable: false,
        west__initClosed: true,
        west__spacing_closed: 32,
        west__togglerLength_closed: 32,
        west__togglerAlign_closed: "top",
        west__togglerContent_closed: "<i class='icon-folder-close'></i>",
        west__togglerTip_closed: "Open & Pin Menu",
        west__sliderTip: "Slide Open Menu",
        center__maskContents: true,
        east__size: 64,
        east__minSize: 64,
        east__maxSize: 200,
        east__closable: false
    });
    layout.addCloseBtn( "#folder", "west" );

    $('#center-pane').layout({
        spacing_open: 0,
        west__size: 64,
        west__slidable: false,
        center__maskContents: true
    });

    $("#project-new").click(function() {

    });

    $(".tool").click(function(e) {
        var toolName=e.currentTarget.id.split("-")[0];
        Blockly.Toolbox.flyout_.show(Blockly.Toolbox.tree_.getChildAt(toolName).blocks);
    });

    // Event Bus
    var eb;

    function init() {
        console.debug("connecting");
        if (window.location.protocol === "file:") {
            eb = new vertx.EventBus('http://localhost:8383/sock');
        } else {
            eb = new vertx.EventBus(window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/sock');
        }
        eb.onopen = function () {
            console.log("open");
            $("#feedback").addClass("active");
            eb.registerHandler("script-main", function (msg, replyTo) {
                $("#feedback").addClass("active");
                $("#feedback").html(msg.status)
            });

        };

        eb.onclose = function () {
            $("#feedback").removeClass("active");
            console.log("closed");
            setTimeout(init, 5000);
        };
    }

    init();

    console.log("hello");

    $("#run").click(function () {
        eb.send('runtime', {
            command: 'create',
            name: 'main',
            script: $("#script").val()
        }, function (reply) {
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
