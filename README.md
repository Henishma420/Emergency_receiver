# Emergency_receiver
Built a companion app for emergency contacts to monitor real-time SOS alerts. When someone sends an SOS message, the contact can view the sender’s exact location, distance to the sender, and the shortest path marked using OSRM + OSMDroid (100% open-source). The app also shows driver details once a responder accepts the SOS request. (No API costs)

Emergency Contact App
Overview:
The Emergency Contact App serves as the third critical component in the SOS ecosystem. This app is designed for friends, family members, or emergency contacts who need to stay informed when an SOS request is triggered by the main user.

Core Features:
1. SOS Message Receiver:
When an SOS message is sent, this app fetches the data from Firebase Realtime Database.
By tapping the "Check SOS Message" button:
The app checks if any SOS alert is active for the contact.
If yes, it shows the exact location (latitude & longitude) of the person in distress on the map using OSMDroid.

2. Distance & Route Calculation:
The contact can view the real-time distance between their current location and the SOS sender’s location.
The shortest path is visualized on the map using:
OSRM (Open Source Routing Machine) for route calculation—free and open-source (no API key required).
The Polyline function, which draws the optimal route in red for easy visualization.

3. Driver Details Retrieval:
A second button labeled "Driver Info" allows the emergency contact to:
View the driver’s details (Name, Age, Phone Number, Vehicle Number) who accepted the SOS request.
Driver details are displayed only if a driver has accepted the SOS request in the Driver App.

Technology Stack:
Firebase Realtime Database – SOS data retrieval
OSMDroid – Map rendering (open-source)
OSRM – Route optimization (no API key needed)
Polyline Drawing – Shortest path display
Android (Java)
