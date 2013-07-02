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
Blockly.Language.controls_wait = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(120);
        this.appendValueInput("TIME")
            .setCheck("Number")
            .appendTitle("wait");
        this.appendDummyInput()
            .appendTitle("seconds");
        this.setInputsInline(true);
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setTooltip('');
    }
};

Blockly.Language.movement_motor = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(50);
        this.appendValueInput("SPEED")
            .setCheck("Number")
            .appendTitle("motor")
            .appendTitle(new Blockly.FieldDropdown([["1", "1"], ["2", "2"]]), "ID")
            .appendTitle("speed");
        this.appendDummyInput()
            .appendTitle("reverse")
            .appendTitle(new Blockly.FieldCheckbox("FALSE"), "REVERSE");
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setTooltip('');
    }
};

Blockly.Language.movement_servo = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(50);
        this.appendValueInput("POSITION")
            .setCheck("Number")
            .appendTitle("servo")
            .appendTitle(new Blockly.FieldDropdown([["1", "1"], ["2", "2"]]), "ID")
            .appendTitle("position");
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setTooltip('');
    }
};

Blockly.Language.movement_vibration = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(50);
        this.appendValueInput("SPEED")
            .setCheck("Number")
            .appendTitle("vibration")
            .appendTitle(new Blockly.FieldDropdown([["1", "1"], ["2", "2"]]), "ID")
            .appendTitle("speed");
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setTooltip('');
    }
};

Blockly.Language.sensor_raw = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(180);
        this.appendDummyInput()
            .appendTitle("sensor")
            .appendTitle(new Blockly.FieldDropdown([["1", "1"], ["2", "2"], ["3", "3"], ["4", "4"]]), "ID");
        this.setOutput(true, "Number");
        this.setTooltip('');
    }
};

Blockly.Language.light_led = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(20);
        this.appendValueInput("LIGHTNESS")
            .setCheck("Number")
            .appendTitle("LED")
            .appendTitle(new Blockly.FieldDropdown([["1", "1"], ["2", "2"], ["3", "3"], ["4", "4"]]), "ID");
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setTooltip('');
    }
};

Blockly.Language.light_triled = {
    helpUrl: 'http://www.example.com/',
    init: function() {
        this.setColour(20);
        this.appendValueInput("COLOUR")
            .setCheck("Colour")
            .appendTitle("tri-LED")
            .appendTitle(new Blockly.FieldDropdown([["1", "1"], ["2", "2"], ["3", "3"], ["4", "4"]]), "ID");
        this.setPreviousStatement(true);
        this.setNextStatement(true);
        this.setTooltip('');
    }
};

Blockly.Language.controls_button = {
    helpUrl: 'http://www.example.com/',
    category: null,
    init: function() {
        this.setColour(120);
        this.appendDummyInput()
            .appendTitle("button")
            .appendTitle(new Blockly.FieldTextInput(""), "NAME");
        this.appendStatementInput("DO")
            .appendTitle("do");
        this.setTooltip('');
    }
};
