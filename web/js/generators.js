Blockly.JavaScript.controls_wait = function() {
    var time = Blockly.JavaScript.valueToCode(this, 'TIME', Blockly.JavaScript.ORDER_ATOMIC);

    var code = 'script.sleep('+time+"*1000);\n"
    return code;
};

Blockly.JavaScript.movement_motor = function() {
    var id = this.getTitleValue("ID");
    var speed = Blockly.JavaScript.valueToCode(this, 'SPEED', Blockly.JavaScript.ORDER_ATOMIC);

    var code = 'controller.motor('+id+", "+speed+");\n";
    return code;
};

Blockly.JavaScript.movement_servo = function() {
    var id = this.getTitleValue("ID");
    var position = Blockly.JavaScript.valueToCode(this, 'POSITION', Blockly.JavaScript.ORDER_ATOMIC);

    var code = 'controller.servo('+id+", "+position+");\n";
    return code;
};

Blockly.JavaScript.movement_vibration = function() {
    var id = this.getTitleValue("ID");
    var speed = Blockly.JavaScript.valueToCode(this, 'SPEED', Blockly.JavaScript.ORDER_ATOMIC);

    var code = 'controller.vibration('+id+", "+speed+");\n";
    return code;
};

Blockly.JavaScript.sensor_raw = function() {
    var id = this.getTitleValue("ID");
    var code = 'controller.sensor('+id+")";
    return [code, Blockly.JavaScript.ORDER_FUNCTION_CALL];
};

Blockly.JavaScript.light_led = function() {
    var id = this.getTitleValue("ID");
    var lightness= Blockly.JavaScript.valueToCode(this, 'LIGHTNESS', Blockly.JavaScript.ORDER_ATOMIC);

    var code = 'controller.led('+id+", "+lightness+");\n";
    return code;
};

Blockly.JavaScript.light_triled = function() {
    var id = this.getTitleValue("ID");
    var colour= Blockly.JavaScript.valueToCode(this, 'COLOUR', Blockly.JavaScript.ORDER_ATOMIC);

    var code = 'controller.triled('+id+", "+colour+");\n";
    return code;
};


Blockly.JavaScript.controls_button = function() {
    var block = Blockly.JavaScript.statementToCode(this, 'DO');
    var name = this.getTitleValue('NAME');
    var code = 'buttons.'+name+' = function() {\n' +block+"};\n";
    return code;
};
