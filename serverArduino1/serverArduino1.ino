String command = "";
boolean end_command = false;


String name_arduino = "arduino1";

const int length_listCommands = 10;
String listCommands[length_listCommands] = {"name", "listCommand", "poweroff1", "ver1", "throat1", "neck1", "larmup1", "larmpit1",
  "lbicep1", "lspeed1"};

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
