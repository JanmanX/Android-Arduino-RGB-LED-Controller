// TODO: Implement color intensity and flashing

#include <SPI.h>
#include <Ethernet.h>

#define BLUEPIN 3
#define REDPIN 5
#define GREENPIN 6

// Network
byte mac[] = { 
  0xDE, 0xAD, 0xBE, 0xEF, 0xFF, 0xFF };  
byte ip[] = { 
  192, 168, 1, 51 };    
int port = 31337;
EthernetServer server = EthernetServer(port);

String buffer = ""; // String buffer
char t; // Temporary character buffer

unsigned int red, green, blue;  // Colors
unsigned int intensity, flashing;

void setup()  
{
  pinMode(REDPIN, OUTPUT);
  pinMode(GREENPIN, OUTPUT);
  pinMode(BLUEPIN, OUTPUT);

  Serial.begin(9600);

  // Retry
  while(Ethernet.begin(mac) != 1)
    ;
    
  Serial.print("Connected\n");
}

void loop () 
{
  EthernetClient client = server.available();
  if(client == true) {
    while((t = client.read()) != -1) {
      if(t == '\n' || t == '\r') continue;   
      buffer += t;    
    }

    readColors(buffer);

    Serial.print("red = ");
    Serial.println(red);
    Serial.print("green = ");
    Serial.println(green);
    Serial.print("blue = ");
    Serial.println(blue);
    Serial.print("intensity = ");
    Serial.println(intensity);
    Serial.print("flashing = ");
    Serial.println(flashing);
    Serial.print("full message = ");
    Serial.print(buffer);

    // Respond to client
    client.println("Received: " + buffer);
    
    buffer = "Configuration: ";
    buffer += "r" + String(red);
    buffer += "g" + String(green);
   buffer += "b" + String(blue);
  buffer += "i" + String(intensity);
   buffer += "f" + String(flashing);
   
    client.println(buffer);
    buffer = "";

    // Light
    set_light();
  }
  
  // Give the arduino a break :)
  delay(200);

}

void set_light() 
{
  analogWrite(REDPIN, red);
  analogWrite(BLUEPIN, blue);
  analogWrite(GREENPIN, green); 
}

void readColors(String str)
{
  for(int i = 0; i < str.length(); i++)
  {
    switch(str.charAt(i)) {
    case 'r':
      red = str.substring(i+1).toInt() % 256;
      break;
    case 'g':
      green = str.substring(i+1).toInt() % 256;
      break;
    case 'b':
      blue = str.substring(i+1).toInt() % 256;
      break;
    case 'i':
      intensity = str.substring(i+1).toInt() % 101;
      break;
    case 'f':
      flashing = str.substring(i+1).toInt() % 101;
      break;   
    default:
      continue;
    } 
  }
}



