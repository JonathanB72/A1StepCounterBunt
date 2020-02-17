# A1StepCounterBunt
A1 Ubicomp2020 Assignment
# Rice Honor Code
On my honor I have neither given nor recieved any unauthorized aid on this assessment

# Helping sites
https://github.com/comp-well-org/ubicomp2020Spring
https://examples.javacodegeeks.com/android/core/hardware/sensor/android-accelerometer-example/
https://www.geeksforgeeks.org/window-sliding-technique/
https://www.w3schools.com/java/
https://source.android.com/devices/sensors/
# Project analysis
I was able to successfully complete the shaking portion of the app without too much issue by using a siding window with a 
threshold crossing algorithm. The shakes were easy to detect since they were pretty uniform with large distances between the start
and end points. The walking was a ton more complicated since the movements were so much more miniscule but ultimately threshold 
crossing worked well enough here too.

The hardest part of the assignment was by far the algorithm design. Once my sliding window picked up on a shake or a step, I needed to
come up with a way to not count that same step again in the next pass through of the window. To fix this I decided to simply reset the
windows to 0 every time so as not to recount them, this backfired a bit though since any negative threshold would immediately could 49
steps after each reset and is something I want to keep reading on on how to fix.


# Extra Credit
I added a mistouch prevention system using the proximity sensor. If the phone detects that the users palm is covering the phone or
that the phone is in a pocket or armband, it hides the buttons so that no accidental pauses or resets are done while using the app to
count steps in real world situations.
