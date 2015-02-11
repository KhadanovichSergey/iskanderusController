String command = "";
boolean end_command = false;


String name_arduino = "arduino3";

const int length_listCommands = 10;
String listCommands[length_listCommands] = {"name", "listCommand", "poweroff3", "ver3", "throat3", "neck3", "larmup3", "larmpit3",
  "lbicep3", "lspeed3"};

void setup() {
    Serial.begin(38400);
    Serial.flush();
}

void loop() {
     if (end_command) {
         command.replace(";", "");
         doSomeThing(command);
         
         command = "";
         end_command = false;
     }
}

void serialEvent() {
     while (Serial.available()) {
          char c = (char)Serial.read();
          command += c;
          if (c == ';')
              end_command = true;
     }
}

void runCommand(String _command) {
    //выполнение команды
    Serial.print(_command + " - return from " + name_arduino + ";");
}


void doSomeThing(String _command) {
     if (_command == listCommands[0]) Serial.print(name_arduino + ";");
     else if (_command == listCommands[1]) {
         String answer = "";
         for(int i = 2; i < length_listCommands - 1; i++)
             answer += listCommands[i] + ",";
         answer += listCommands[length_listCommands - 1] + ";";
         Serial.print(answer);
     } else {
         runCommand(_command);
     }
}
