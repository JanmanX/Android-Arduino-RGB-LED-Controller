#Android-Arduino RGB LED Controller
Android app and Arduino program for controlling a RGB LED strip remotely.
*(NOT TESTED WITH A STRIP YET, ONLY COMMUNICATION BETWEEN ANDROID-ARDUINO TESTED!)*

#How it works
[This](http://www.jerome-bernard.com/blog/2013/01/12/rgb-led-strip-controlled-by-an-arduino)
blog helped me setup an Arduino Uno to control the strip. I figured it would be nice to control my RGB LED strip directly from my Android phone.

This project is very basic:
The app sends a string of data to the arduino in this format:
"r**X**g**X**b**X**i**Y**f**Y**", where **X** is a number from 0 to 255 and **Y** is a number from 0 to 100.
When this string is received by the Arduino, it delimits the data and sets the
values for the output pins accordingly. (The Arduino also sends the string back,
which helps me debug for any connection errors, and to verify connection).

Note that you can connect to the Arduino using a telnet connection:
'telnet <IP> <PORT>'

Now you can change colors by sending strings with a letter for a color followed
by a number from 0 to 255.
For example, this string will change the LED strip color to green:
'r0g255b0'

#Setup
Nexus 4, Android version 5.0.1.

Arduino Uno R3 with Ethernet Shield W5100.

#TODO
- Display current status of Arduino in the App
- Improve UI (Better color schemes, color for SeekBars)

#Special thanks
Inspired from [Jerome Bernard Blog](http://www.jerome-bernard.com/blog/2013/01/12/rgb-led-strip-controlled-by-an-arduino), DEFINITELLY check it out!

Thanks to Pascal Cans and Justin Warner for their Android Color Picker, used in the Android app. Check out it out [here](https://github.com/yukuku/ambilwarna). (I could not import their module into the project with Android Studio, so I hand-copied every file into my project. Its bad :S )

#Screenshots
![](https://raw.githubusercontent.com/JanmanX/Android-Arduino-RGB-LED-Controller/master/examples/screenshot2.png)
