#define IR A0
#define model 430


#define TRIG_PIN A1
#define ECHO_PIN A2
// Replace sensor numbers with Names
#include <SharpIR.h>
#include<Servo.h>
#include <NewPing.h>
NewPing sonar(TRIG_PIN, ECHO_PIN, 250);
SharpIR SharpIR(IR, model);
Servo front_servo;
Servo rear_servo;
#define RELAY1 10
#define RELAY2 11
Servo ESC;
Servo steer;


void setup() {
  Serial.begin(9600); 
  front_servo.attach(7);
  pinMode(RELAY1, OUTPUT);
  pinMode(RELAY2, OUTPUT);
  ESC.attach(9);
  steer.attach(3);
  ESC.write(0);
  steer.write(110);
  Serial.begin(9600);
  front_servo.write(125);
  pinMode(4, OUTPUT);

}

void loop() {
  Serial.flush();
  if (Serial.available() > 0)
  {
    
    int x = Serial.read();
    Serial.print(x);
    int distanceR = 0; //
    int distanceL = 0;
    
    int USdis = sonar.ping_cm();            //Distance measured from ultrasonic sensor
    if (USdis == 0)                         //If distance is greater than maximum,
    { //ultrasonic sensor reads it as 0. Hence it is made 250
      USdis = 250;
    }
    switch (x)
    {
      case 1:
        goLeft();
        break;
      case 0:
        goStraight();
        break;
      case 2:
        goRight();
        break;
      case -1:
        goStop();
        break;
    }

    if (USdis < 50)                         //We have kept distance here as 50cm. But as the car is in motion(high speed), the car will stop when the distance is close to 20cm. 50-30 is the threshold.
    {
reverse:
      digitalWrite(6, LOW);                 //switches off buzzer
      steer.write(110);                     //steering is made straight
     
      ESC.write(0);                         //Motor is stopped
      digitalWrite(RELAY1, HIGH);           // Turns ON Relays - Motor in reverse direction
      digitalWrite(RELAY2, HIGH);
//      delay(15);
      ESC.write(70);                         //Motor is running in reverse direction
      delay(500);
      ESC.write(0);
      digitalWrite(4, HIGH);                //Lights are switched on as the car has stopped

      front_servo.write(50);                //Ultrasonic sensor mounted Servo is moved right to scan right side
      delay(500);
      distanceR = sonar.ping_cm();          //Right side distance is measured
      if (distanceR == 0)
      {
        distanceR = 250;
      }
      Serial.println(distanceR);
      delay(1000);
      front_servo.write(125);

      front_servo.write(170);               //Ultrasonic sensor mounted Servo is moved left to scan left side
      delay(500);
      distanceL = sonar.ping_cm();          //Left side distance is measured
      if (distanceL == 0)
      {
        distanceL = 250;
      }
      Serial.println(distanceL);
      delay(1000);
      front_servo.write(125);
      digitalWrite(4, LOW);
      int dis;
      if (distanceR > distanceL)            //Condition to check if right side distance is greater tha left side distance
      {
        dis = sonar.ping_cm();
        if (dis == 0)
        {
          dis = 250;
        }
        delay(500);
        if (dis > 50)
        {
          goRight();
          delay(1000);
        }

        while (dis < 50)
        {
          dis = sonar.ping_cm();
          if (dis == 0)
          {
            dis = 250;
          }
          delay(500);

          goRight();
          Serial.println("inLoop");
          if (dis < 15)                     //Car cannot make a turn if obstacle distance is less than 15
          {
            goto reverse;                   //So the car moves back a little, and then completes the turn
          }

        }

      }
      else
      {
        dis = sonar.ping_cm();
        if (dis == 0)
        {
          dis = 250;
        }
        delay(500);
        if (dis > 50)
        {
          goLeft();
          delay(1000);
        }
        while (dis < 50)
        {
          goLeft();
          Serial.println("inLoop");
          if (dis < 15)                     //Car cannot make a turn if obstacle distance is less than 15
          {
            goto reverse;                   //So the car moves back a little, and then completes the turn
          }
        }
      }
    }
   /* int IRdis = SharpIR.distance();
    if (IRdis < 10)
    {
      Serial.print("IR");
      goStop();
    }*/

  }
  Serial.flush();
}

void goLeft()
{
  digitalWrite(4, LOW);
  Serial.println("Left");
  digitalWrite(RELAY1, LOW);        // Turns OFF Relays - Motor in forward direction
  digitalWrite(RELAY2, LOW);
  ESC.write(68);
  steer.write(85);
//  delay(500);
}
void goRight()
{
  digitalWrite(4, LOW);
  Serial.println("Right");
  digitalWrite(RELAY1, LOW);        // Turns OFF Relays - Motor in forward direction
  digitalWrite(RELAY2, LOW);
  ESC.write(68);
  steer.write(135);
//  delay(500);
}

void goStraight()
{ digitalWrite(RELAY1, LOW);        // Turns OFF Relays - Motor in forward direction
  digitalWrite(RELAY2, LOW);
  delay(15);
  digitalWrite(4, LOW);
  Serial.println("RUN");
  ESC.write(68);
  steer.write(110);
//  delay(500);
}
void goStop()
{
  digitalWrite(4, HIGH);
  Serial.println("STOP");
  ESC.write(0);
  steer.write(110);
  delay(1000);

}
