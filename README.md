# OliverWeatherShare
Delivery repo for skills assessment

Overview - Unable to pull from JSON server due to missing APIKey. Completed with another weather provider. 

OliverWeather
Skills test (1 of 2) for Oliver

Client Description:Question 1

Please create an app to show a weather forecast. The app should automatically use the device's current location when querying the weather service. Please use the Dark Sky Forecast API as the app's datasource (https://developer.forecast.io/docs/v2 ). Present each day as an item in a scrolling list with today at the top. Please follow AOS best practice wherever possible. Spend no more than 2 hours on this exercise.

Tasks:

- Create Prefab UI

Poss - use scrollable view
- Fetch Location Data from device

Create Callback/Listener
Create Trigger
Set Manifest for coarse/fine permisions
- Pull JSON

Set Manifest for internet permision
Examine Dark Sky Forecast API
Specify location
Pull Data
Parse/Prettify Data
- Create/Populate ListView

Create Adapter (probably use out of box)
Populate ListView, sort if necessary
Test

Check rotation
Check on 2nd device
Areas of concern. 2 async tasks, location and JSON. Location must be set before JSON fetch.

Initial Assesment: 2 hours, high confidence. Begin immediately after conf call at 11am. Deliver by ~1pm 12/23/15
