// Arduino Code
///////////////////////////////////////////////////// Serial /////////////////////////////////////////////////////

// Serial reference documents to talk with OpenMV camera
// Concepts from https://forum.arduino.cc/index.php?topic=396450
//  https://www.arduino.cc/reference/en/language/functions/communication/serial/
//  https://forum.arduino.cc/index.php?topic=396450
//  https://docs.openmv.io/_images/pinout.png
//
#include <Adafruit_SSD1306.h>
#define ledPin 11                      // Initialize pin 11 to drive LED
#define mvRx 4                         // Initialize pin 12 to receive signal from MV module
#define OLED_Address 0x3C              // 0x3C device address of I2C OLED. Few other OLED has 0x3D
Adafruit_SSD1306 oled(128, 64);        // create our screen object setting resolution to 128x64
int state = 0;                         // To read state of serial data from HC05 module
const byte Serial_numBytes = 60;       // receive buffer size
byte Serial_InBytes[Serial_numBytes];  // receive buffer
byte Serial_numReceived = 0;
boolean Serial_newData = false;  // flag to receive in progress
int a = 0;
int lasta = 0;
int lastb = 0;
int LastTime = 0;
int ThisTime;
bool BPMTiming = false;
bool BeatComplete = false;
int BPM = 0;
#define UpperThreshold 560
#define LowerThreshold 530

void Serial_recvBytes() {
  static boolean recvInProgress = false;
  static byte ndx = 0;
  byte startMarker = 0xFF;
  byte endMarker = 0xFE;
  byte rb;

  while (Serial1.available() > 0 && Serial_newData == false) {
    rb = Serial1.read();

    if (recvInProgress == true) {
      if (rb != endMarker) {
        Serial_InBytes[ndx] = rb;
        ndx++;
        if (ndx >= Serial_numBytes) {
          ndx = Serial_numBytes - 1;
        }
      } else {
        Serial_InBytes[ndx] = '\0';  // terminate the string
        recvInProgress = false;
        Serial_numReceived = ndx;  // save the number for use when printing
        ndx = 0;
        Serial_newData = true;
      }
    }

    else if (rb == startMarker) {
      recvInProgress = true;
    }
  }
}

void Serial_showNewData() {

  char output[100];
  if (Serial_newData == true) {
    for (byte n = 0; n < Serial_numReceived; n++) {
      sprintf(output, "%d ", Serial_InBytes[n]);
      Serial.println(output);
    }
    Serial_newData = false;
  }
}

void setup() {
  oled.begin(SSD1306_SWITCHCAPVCC, OLED_Address);
  oled.clearDisplay();
  oled.setTextSize(2);
  pinMode(ledPin, OUTPUT);  //Define ledpin as output
  digitalWrite(ledPin, LOW);
  Serial.begin(9600);     // Default communication rate of the Bluetooth module
  Serial1.begin(115200);  // link to OpenMV
}

void loop() {
  if (a > 127) {
    oled.clearDisplay();
    a = 0;
    lasta = a;
  }

  ThisTime = millis();
  int value = analogRead(0);
  oled.setTextColor(WHITE);
  int b = 60 - (value / 16);
  oled.writeLine(lasta, lastb, a, b, WHITE);
  lastb = b;
  lasta = a;

  if (value > UpperThreshold) {
    if (BeatComplete) {
      BPM = ThisTime - LastTime;
      BPM = int(60 / (float(BPM) / 1000));
      BPMTiming = false;
      BeatComplete = false;
      tone(8, 1000, 250);
    }
    if (BPMTiming == false) {
      LastTime = millis();
      BPMTiming = true;
    }
  }
  if ((value < LowerThreshold) & (BPMTiming))
    BeatComplete = true;

  oled.writeFillRect(0, 50, 128, 16, BLACK);
  oled.setCursor(0, 50);
  oled.print("BPM:");
  oled.print(BPM);

  oled.display();
  a++;
  // Regular Arduino code
  if (Serial.available() > 0) {  // Checks whether data is coming from the serial port
    state = Serial.read();       // Reads the data from the serial port
  }
  if (state == '0') {
    digitalWrite(ledPin, LOW);   // Turn LED OFF
    //Serial.println("LED: OFF");  // Send back, to the phone, the String "LED: ON"
    state = 0;
  }
  if (state == '1') {
    digitalWrite(ledPin, HIGH);
    //Serial.println("Drowsy driver detected! Alerting drowsy driver");  // Send back, to the phone, the String "LED: ON"
    state = 0;
  }
  Serial_recvBytes();    // Check for new data from OpenMV - non-blocking
  Serial_showNewData();  // Parse the data when a full set is received
}