String command = "";
boolean start_command = false;
boolean evailable = false;

String name_arduino = "arduino1";

const int length_listCommands = 10;
String listCommands[length_listCommands] = {"name", "listCommand", "poweroff1", "ver1", "throat1", "neck1", "larmup1", "larmpit1",
  "lbicep1", "lspeed1"};

void setup() {
    Serial.begin(38400);
    Serial.flush();
}

void loop() {}

void spSent(String s) {
    Serial.print("[" + s + "]");
}
void spRead() {
      while (Serial.available() > 0 && !evailable) {
          char c = (char)Serial.read();
          if (c == '[') {
              start_command = true;
              command = "";
              evailable = false;
          } else if (c == ']' && start_command) {
              evailable = true;
              start_command = false;
              break;
          } else {
              if (start_command)
                  command += c; 
          }
     }
}

void serialEvent() {
     spRead();
     if (evailable) {
         doSomeThing(); 
         start_command = evailable = false;
     }
}

void doSomeThing() {
     if (command == listCommands[0]) spSent(name_arduino);
     else if (command == listCommands[1]) {
         String answer = "";
         for(int i = 2; i < length_listCommands - 1; i++)
             answer += listCommands[i] + ",";
         answer += listCommands[length_listCommands - 1];
         spSent(answer);
     } else {
         spSent(command + ", return from arduino with name " + name_arduino);
     }
}
