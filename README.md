# Intelligent-Waste-Management-System
EcoSort is a smart waste management system that categorizes waste into wet and dry types, monitors waste levels, and notifies collectors via an Android app when the bin is full. The system uses a GPS module for location tracking, a Firebase database for real-time data synchronization, and various other sensors for waste classification and bin level monitoring. It enables a prompt response when the bin is full, thereby preventing overflow and maintaining cleanliness. This system plays a significant role in fostering sustainability.

# Required Hardware
  1. ESP32
  2. SG90 Servo Motor
  3. Soil Moisture Sensor
  4. Ultrasonic Sensor
  5. Neo6m GPS Module
  6. IR Sensor

# Required Software
  1. Android Studio (Kotlin)
  2. Arduino IDE (C++)

# Hardware Implementation Block Diagram
![image](https://github.com/mayankyadav06/Intelligent_Waste_Management_System/assets/140626220/4acea288-dc7e-4705-a914-84986301302e)

# To Run this Project
  1. Clone this Repository
  2. Connect the sensors with ESP32
  3. Open .ino file replace wifi ssid and password with your own
  4. Replace database URL and API key with your own API key and Database URL
  5. Uploade the .ino file (Smart_Waste_Management_System.ino) to ESP32
  6. Login to firebase
  7. Create your project using realtime database
  8. Create the firebase databse tree in the following manner:
       root
        |
        --- bin
            |
            --- distance1: Number
            --- distance2: Number
            --- latitude: Number
            --- longitude: Number
  9. Open the Android App Project in Android Studio
  10. Go to Tools -> Firebase -> Realtime Database -> Connect to Firebase -> Add the realtime database SDK to your app
  11. Upload the Android Application to your android phone


# Smar bin
![image](https://github.com/mayankyadav06/GreenWaste/assets/140626220/c210de36-82ff-482f-9c7b-d1c81f8022fd)
![image](https://github.com/mayankyadav06/GreenWaste/assets/140626220/d3911619-0684-4866-a808-3b852ab91c98)

# Android Application 
![image](https://github.com/mayankyadav06/GreenWaste/assets/140626220/85f2d721-e866-488c-929d-5a6ee6ad0a4e)
