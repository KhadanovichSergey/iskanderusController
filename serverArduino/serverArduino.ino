String command = "";
boolean end_command = false;


String name_arduino = "left_arduino";

const int length_listCommands = 11;
String listCommands[length_listCommands] = {"name", "listCommand", "headUpDown", "headLeftRight",
  "handFrontUpDown", "handSideUpDown", "fingerLeft0", "fingerLeft1", "fingerLeft2",
  "fingerLeft3", "fingerLeft4"};

void setup() {
    Serial.begin(9600);
    Serial.flush();
}

void loop() {
     if (end_command) {
         doSomeThing(command);
         
         command = "";
         end_command = false;
     }
}

void serialEvent() {
     while (Serial.available()) {
          char c = (char)Serial.read();
          command += c;
          if (c == ']')
              end_command = true;
     }
}

void parseStringToCommand(String strForParsing, String &name, int &value) {
    strForParsing.replace("]", "");
    strForParsing.replace("[", "");
    name = strForParsing.substring(0, strForParsing.indexOf('='));
    value = strForParsing.substring(strForParsing.indexOf('=') + 1).toInt();
}

boolean check(String name) {
  boolean result = false; 
  for(int i = 0; i < length_listCommands && !result; ++i)
      if (listCommands[i] == name)
          result = true;
  return result;
}

void runCommand(String name, int value) {
    if (check(name)) {
         //выполнение команды
        Serial.print("[" + name + "=" + value + ":rfa]");
    } else Serial.print("[unknownCommand]");
}

void doSomeThing(String _command) {
     if (command == "[name]") Serial.print("[" + name_arduino + "]");
     else if (command == "[listCommand]") {
         String lc = "[";
         for(int i = 0; i < length_listCommands - 1; ++i)
             lc += listCommands[i] + ",";
         Serial.print(lc + "]");
     } else {
         String name;
         int value;
         parseStringToCommand(command, name, value);
         runCommand(name, value);
     }
}
