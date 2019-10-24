# Prescryp Lance Patient - Beta

<img src="https://github.com/mmitrasish/prescryp-lance-patient/blob/master/app/src/main/res/drawable/logo_lance.png" width="100" height="100">

**Check Releases for apk.** <br/>
[Release](https://github.com/mmitrasish/prescryp-lance-patient/releases/tag/Beta-1.0.0)

Health App for booking ambulance services that uses your current location to get the nearest ambulance available for you and notify the ambulance about your location in realtime. You can book various types of ambulance depending on your requirement and get the price calculated beforehand based on your location and destination. Great UI similar to Uber App and easy to navigate in app. You can also request ambulance for other with just a phone number and the location. You can also view your previous booking.

This app is connected to Prescryp Lance Ambulance - Beta where the ambulance provider get their notification related to booking. You can view the ambulance location in realtime and can track them. Also all the details are given in the UI like phone number, Ambulance vehicle number, etc. You will also get in-app notifications when ambulance will accept and when ambulance will reach your location. Also there will be OTP generated when the ambulance pickup which is provided by the patient to the ambulance driver for verifications.

The app store data in realtime database in firebase and uses firebase functions to send realtime in-app notifications.

## Stack Used

**Frontend** : Android, Java, XML

**Backend**: Firebase SDK

**DB**: Realtime Database in Firebase

**Tools**: Android Studio, Sublime

## Features

This app is built on Android Native (Java) which has various features:

- Authentication is done with simple phone number and OTP using firebase authentication.
- Store data in Firebase Realtime Database.
- Can track ambulance location in realtime and provide insights like distance and time taken for the ambulance to reach the location.
- Also have feature like routing and slick animation for realtime tracking of ambulance in realtime.
- Can view all the previous bookings with all detail regarding the trip.
- Can calculate price for ambulance depending on the source and destination location distance.
- Great UI similar to OLA/Uber.
- Have used various apis related to google map sdk like geolocation, geocoding, routing, gps.

## Other Modules

This app is depended on another module which takes care of ambulance system and realtime location tracker. The Github link to that app:

> ##### [Prescryp Lance Ambulance](https://github.com/mmitrasish/prescryp-lance-ambulance)
