#include <Wire.h>
#include <TinyGPS++.h>
#include <ESP32Servo.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

#define WIFI_SSID "XXXXX"
#define WIFI_PASSWORD "XXXX"
#define API_KEY "your API key "
#define DATABASE_URL your database url"

#define IR_SENSOR_PIN 35
#define moisture 34
#define trigPin1 12
#define echoPin1 13
#define trigPin2 14
#define echoPin2 25

Servo myservo;
int originalPosition = 90as;
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;
int count = 0;
bool signupok = false;
double latitude = 26.230465977751365;
double longitude = 78.20708762262421;

void setup() {
  Serial.begin(115200);
  pinMode(IR_SENSOR_PIN, INPUT);
  myservo.attach(26);
  myservo.write(originalPosition);
  pinMode(trigPin1, OUTPUT);
  pinMode(echoPin1, INPUT);
  pinMode(trigPin2, OUTPUT);
  pinMode(echoPin2, INPUT);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  /* Sign up */
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Signup ok");
    signupok = true;
  } else {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }
  config.token_status_callback = tokenStatusCallback;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

void loop() {
  int sensorValue = digitalRead(IR_SENSOR_PIN);

  if (sensorValue == HIGH) {
    Serial.println("No Object Detected");
    myservo.write(originalPosition);

  } else {
    Serial.println("Object Detected");
    int moisture_value = analogRead(moisture);

    Serial.print("Moisture: ");
    Serial.println(moisture_value);
    delay(2000);
    if (moisture_value < 3700) {
      myservo.write(0);  // move servo left
      delay(3000);
      myservo.write(originalPosition);
    } else {
      myservo.write(180);  // move servo right
      delay(3000);
      myservo.write(originalPosition);
    }
  }

  if (Firebase.ready() && signupok && (millis() - sendDataPrevMillis > 7500 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();
    digitalWrite(trigPin1, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin1, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin1, LOW);
    float duration1 = pulseIn(echoPin1, HIGH);
    int distance1 = (int)(duration1 * 0.034 / 2);

    digitalWrite(trigPin2, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin2, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin2, LOW);
    float duration2 = pulseIn(echoPin2, HIGH);
    int distance2 = (int)(duration2 * 0.034 / 2);

    Serial.print("Distance1: ");
    Serial.println(distance1);
    Serial.print("Distance2: ");
    Serial.println(distance2);
    if (Firebase.RTDB.setInt(&fbdo, "sensorData/distance1", distance1)) {
      Serial.println("Data written successfully!");
    } else {
      Serial.println("Failed to write to database");
      Serial.println("Reason: " + fbdo.errorReason());
    }
    if (Firebase.RTDB.setInt(&fbdo, "sensorData/distance2", distance2)) {
      Serial.println("Data written successfully!");
    } else {
      Serial.println("Failed to write to database");
      Serial.println("Reason: " + fbdo.errorReason());
    }
    // Location Data
    if (Firebase.RTDB.setDouble(&fbdo, "sensorData/latitude", latitude)) {
      Serial.println("Latitude data written successfully!");
    } else {
      Serial.println("Failed to write latitude to database");
      Serial.println("Reason: " + fbdo.errorReason());
    }
    if (Firebase.RTDB.setDouble(&fbdo, "sensorData/longitude", longitude)) {
      Serial.println("Longitude data written successfully!");
    } else {
      Serial.println("Failed to write longitude to database");
      Serial.println("Reason: " + fbdo.errorReason());
    }
    count++;
  }
  delay(1000);
}