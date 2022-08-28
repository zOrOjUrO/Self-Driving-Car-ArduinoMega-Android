# Self-Driving Car
Distance &amp; Proximity Sensor Based Self-Driving Autonomous Vehicle

![The Prototype - Front View](https://user-images.githubusercontent.com/68921071/187082344-fc1982cd-111d-4922-b196-334ec33c8f7e.jpg)*The Prototype - Front View*

The car runs on an Arduino Mega, commands for navigation are sent by an android app in which the destination is set and received using a bluetooth module. Any obstacles are sensed and avoided to continue with the original path.


[Link to Model Working](https://youtu.be/2O3NMSFHBNA) | [Link to Build Explanation](https://youtu.be/IGRs5LYzIB4)


**Components Used :**
>  - Arduino Mega 2560
>  - HC-05 bluetooth module
>  - HC-SR04 Ultrasonic sensor
>  - SharpIR sensor
>  - Single BLDC motor with transmission
>  - ESC and dual-channel 5V Relay

**Key Android Concepts:**
> - The Client-side app can be used to set the Destination for the navigation. Not just the destination but also the waypoints to help navigation of the car. All these GPS coordinates are saved to firebase cloud in Realtime, which will then be retrieved by the app on the car and thus start navigating in order to reach the destination.
> - As a counterpart, the Car Navigator shows the current location of the car as a puck on the map. It also sends the Car’s current GPS Coordinates to Firebase Cloud, which will then be used by the Client-side App. Fetches and Draws the best feasible route from car’s location to destination. Starts Navigation on Button Click from current location to Destination GPS coordinates. During Navigation, the app sends appropriate values via Bluetooth to the Bluetooth module on the Car for the AV to manoeuvre.
> - On clicking the start button, the Navigation starts and the app sends data to the Bluetooth module which is paired with the android device. 
> - Both the apps use location, internet and sensor permissions including a Bluetooth permission on the RC Navigator app.
> - As the apps are cloud based, the destination could be set from anywhere around the world and the car could technically travel there.
> - ***Note: *** we are using mapbox sdk to acheieve the navigation as well as map visualization. [Reference](https://docs.mapbox.com/android/maps/guides/install/)

* Team incudes : 
  - [Harikiran G](https://github.com/harikiran27)
  - [Gokul Raj S](https://github.com/zOrOjUrO)
  - [Darshan J](https://github.com/Darshan-j-24)


**Android Clients**
| Location Client : Caller | Car Navigator : Driver |
| ----------- | ----------- |
![LocationClient](https://user-images.githubusercontent.com/68921071/187082395-9a562c52-5a72-4be8-8ddd-b8bffeb8b1ad.png) | ![RCNavigator](https://user-images.githubusercontent.com/68921071/187082426-491e4492-5a41-4eec-b2b9-29e9c839a570.png)


*Note: The Android Codes maynot fully correspond to the complete app as we are uploading an old project and few files might be missing.*



  
  <a href = "https://github.com/Tanu-N-Prabhu/Python/graphs/contributors">
    <img src = "https://contrib.rocks/image?repo = zOrOjUrO/Self-Driving-Car-ArduinoMega-Android"/>
  </a>

Made with [contributors-img](https://contrib.rocks).
