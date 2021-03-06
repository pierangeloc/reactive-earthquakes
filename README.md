reactive-earthquakes
=========================

Using Akka-Streams to parse earthquakes events monthly dump and replaying them in a time-consistent way using websockets.

###N.B.
The file provided by USGS ([http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson](http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson)) 
contains events in a time descending order, from most recent to oldest, but we need to replay them in the opposite order, therefore we produce the `all_month_asc.geojson` file 
with a simple command:

OS  | command
------------- | -------------
UNIX  | `tac all_month.geojson > all_month_asc.geojson`
Mac  | `tail -r all_month.geojson > all_month_asc.geojson`

This file must be in our classpath, we saved it in the resources folder.



###How to run the app
- Run the `FileServer` application, adjusting the location of the `.geojson` file to serve. This simply serves the requested file as a chunked response
- Run the `EarthquakeServer` application
- Access the earthquakes visualization on [http://localhost:8080/world/events-display.html](http://localhost:8080/world/events-display.html)

