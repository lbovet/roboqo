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
