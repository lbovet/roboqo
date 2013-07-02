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
Blockly.JavaScript.controls_wait = function() {
    var time = Blockly.JavaScript.valueToCode(this, 'TIME', Blockly.JavaScript.ORDER_ATOMIC);
    var code = 'control.sleep('+time+"*1000);\n"
    return code;
};

Blockly.JavaScript.movement_motor = function() {
    var id = this.getTitleValue("ID");
    var speed = Blockly.JavaScript.valueToCode(this, 'SPEED', Blockly.JavaScript.ORDER_ATOMIC);
    var reverse = this.getTitleValue("REVERSE")==="TRUE";
    var code = 'controller.motor('+id+", "+(reverse?"-":"")+speed+");\n";
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
    var code = 'buttons["'+name+'"] = function() {\n' +block+"};\n";
    return code;
};
