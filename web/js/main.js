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
var Blockly;

var flyoutOpen=false;
var blocklyReady = $.Deferred();

var unselectTools = function() {
    $(".tool").removeClass().addClass("tool");
    $(".tool").removeClass("glow");
    flyoutOpen = false;
}

var currentProject = null;

window.alert = console.log

function load(project) {
    $(".project").removeClass("active-project");
    project.addClass("active-project");
    currentProject = project.html();

    localStorage.setItem("currentProject", project.html());

    return $.ajax({url: "/data/projects/"+currentProject+"/workspace.xml", dataType:"text"})
        .done(function(data) {
            var xml;
            try {
                xml = Blockly.Xml.textToDom(data);
            } catch (e) {
                console.error(e+ '\nxml: ' + xml);
                return;
            }
            // Clear the workspace to avoid merge.
            Blockly.mainWorkspace.clear();
            Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
        })
}

function save(xmlText) {
    if(currentProject) {
        if(!xmlText) {
            xmlText = Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Blockly.mainWorkspace));
        }
        return $.ajax({url: "/data/projects/"+currentProject+"/workspace.xml", method:"PUT", data:xmlText})
            .fail(function(a, b, c) {
                console.error(a, b, c);
            });
    } else {
        return $.Deferred().reject().promise();
    }
}

function pushCode() {
    return $.ajax({url: "/scripts/main", method:"PUT", data:Blockly.Generator.workspaceToCode('JavaScript')})
        .fail(function(a, b, c) {
            console.error(a, b, c);
        });
}

function list() {
    console.log("list");
    var d= $.Deferred();
    $.ajax({url: "/data/projects/", dataType:"json"})
        .done(function(data){
            var projects = $("#project-list");
            projects.empty();
            $.each(data.projects, function(k,v) {
                var name = v.split("/")[0];
                var elt = $("<li></li>").html(name);
                elt.addClass("project").bind("vclick", function(e) {
                    var project = $(e.currentTarget);
                    if(!project.hasClass("active-project")) {
                        load(project);
                    }
                });
                projects.append(elt);
            })
            if(!data.projects.length) {
                newProject(true);
            } else {
                if($(".active-project").length ==0) {
                    var p = findProject(localStorage.getItem("currentProject"));
                    if(!p) {
                        p = $(".project").first();
                    }
                    return load(p);
                }
            }
            d.resolve();
        })
        .fail(function(a, b, c) {
            console.error(a, b, c);
            d.resolve();
        });
    return d.promise();
}

function initBlockly(blockly) {
    Blockly = blockly;
    var originalHide = Blockly.Toolbox.flyout_.hide;
    Blockly.JavaScript.INFINITE_LOOP_TRAP = 'control.checkStop();\n';
    Blockly.JavaScript.addReservedWords(['main', 'control', 'controller', 'log']);
    Blockly.Toolbox.flyout_.hide = function() {
        if(flyoutOpen) {
            unselectTools();
        }
        originalHide.apply(Blockly.Toolbox.flyout_);
    }
        var startXmlText = Blockly.Xml.domToText(Blockly.Xml.workspaceToDom(Blockly.mainWorkspace));
        function change() {
            console.log("changed");
            var xmlDom = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
            var xmlText = Blockly.Xml.domToText(xmlDom);
            if (startXmlText != xmlText) {
                save(xmlText);
                startXmlText = xmlText;
                pushCode();
            }
        }
        Blockly.addChangeListener(change);
    blocklyReady.resolve();
}

function findProject(name) {
    var project=null;
    $(".project").each(function() {
        if($(this).html() == name) {
            project = $(this);
        }
    });
    return project;
}

function newProject(force) {
    var d = $.Deferred();
    var name;
    do {
        name = window.prompt("New project");
    } while(force && !name)
    if(name && name.trim() !== "") {
        currentProject = name
        var project = findProject(name);
        if(!project) {
            Blockly.mainWorkspace.clear();
            save().then(list).always(function() {
                project = findProject(name);
                $(".project").removeClass("active-project");
                project.addClass("active-project");
                d.resolve();
            });
        } else {
            load(project).always(d.resolve);
        }
    } else {
        d.reject();
    }
    return d.promise();
}
$(document).bind('pageinit', function() {
    console.log("pageinit");
});

$(document).ready(function () {

    blocklyReady.then(list).done(function() {
        network.init(pushCode);
    });

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
        west__togglerContent_closed: "<div class='toggler-icon'><i class='icon-c-box'></i></div>",
        center__maskContents: true,
        east__size: 64,
        east__minSize: 64,
        east__maxSize: 200,
        east__closable: false,
        south__size: 36,
        south__minsize: 36,
        south__closable: false
    });
    layout.addCloseBtn( "#folder", "west" );

    $('#center-pane').layout({
        spacing_open: 0,
        west__size: 64,
        west__slidable: false,
        center__maskContents: true
    });

    $("#project-new").bind("vclick", function(e) {
        $("#project-new").toggleClass("active-project");
        e.preventDefault();
        newProject().always(function() {
            $("#project-new").toggleClass("active-project");
        });
    });

    $("#project-del").bind("vclick", function(e) {
        $("#project-del").toggleClass("active-project");
        e.preventDefault();
        if(window.confirm("Delete "+currentProject+" ?")) {
            $.ajax({url:"/data/projects/"+currentProject, method:"DELETE"})
                .then(list).always(function() {
                    $("#project-del").toggleClass("active-project");
                });
        } else {
            $("#project-del").toggleClass("active-project");
        }
    });

    // Tools

    $(".tool").bind("vclick", function(e) {
        var toolName=e.currentTarget.id.split("-")[0];
        if(!$(e.currentTarget).hasClass("selected-tool")) {
            unselectTools();
            $(e.currentTarget).addClass("selected-tool").addClass("selected-"+toolName+"-tool");
            $(e.currentTarget).addClass("glow");
            $("#blockly-frame").focus();
            setTimeout(function() {
                Blockly.Toolbox.flyout_.show(Blockly.Toolbox.tree_.getChildAt(toolName).blocks);
                flyoutOpen=true;
            }, 1);
        } else {
            unselectTools();
            Blockly.Toolbox.flyout_.hide();
        }
    });

    $("#run").bind("vclick", function () {
        if(!$("#run").hasClass("disabled")) {
            network.eb.send('runtime', {
                command: "stop",
                name: "main"
            });
            network.eb.send('runtime', {
                command: "start",
                name: "main"
            });
        }
    });

    $("#stop").bind("vclick", function () {
        network.eb.send('runtime', {
            command: "stop",
            name: "main"
        });
    });

    network.init(pushCode);
});
