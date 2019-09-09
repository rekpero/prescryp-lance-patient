# Prescryp Lance Patient - Beta

Health App for booking ambulance services that uses your current location to get the nearest ambulance available for you and notify the ambulance about your location in realtime. You can book various types of ambulance depending on your requirement and get the price calculated beforehand based on your location and destination. Great UI similar to Uber App and easy to navigate in app. You can also request ambulance for other with just a phone number and the location. You can also view your previous booking.

This app is connected to Prescryp Lance Ambulance - Beta where the ambulance provider get their notification related to booking. You can view the ambulance location in realtime and can track them. Also all the details are given in the UI like phone number, Ambulance vehicle number, etc. You will also get in-app notifications when ambulance will accept and when ambulance will reach your location. Also there will be OTP generated when the ambulance pickup which is provided by the patient to the ambulance driver for verifications.

The app store data in realtime database in firebase and uses firebase functions to send realtime in-app notifications.

## Stack Used

**Frontend** : Android, Java, XML

**Backend**: Firebase SDK

**DB**: Realtime Database in Firebase

**Tools**: Android Studio, Sublime

## Other Modules

This app is depended on another module which takes care of ambulance system and realtime location tracker. The Github link to that app:

> ##### [Prescryp Lance Ambulance](https://github.com/mmitrasish/prescryp-lance-ambulance)
