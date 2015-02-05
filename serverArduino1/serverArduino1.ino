String command = "";
boolean end_command = false;


String name_arduino = "left_arduino";

const int length_listCommands = 10;
String listCommands[length_listCommands] = {"name", "listCommand", "poweroff", "ver", "throat", "neck", "larmup", "larmpit",
  "lbicep", "lspeed"};

void setup() {
    Serial.begin(9600);
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
    Serial.print(_command + " - return from " + name_arduino + " arduino;");
}


void doSomeThing(String _command) {
     if (command == "name") Serial.print(name_arduino + ";");
     else if (command == "listCommand") {
         String answer = "";
         for(int i = 0; i < length_listCommands - 1; i++)
             answer += listCommands[i] + ",";
         answer += listCommands[length_listCommands - 1] + ";";
         Serial.print(answer);
     } else {
         runCommand(_command);
     }
}
