
var initialize = function(map) {
    console.log("calling initMap");
    var events = new WebSocket("ws://localhost:9091/earthquakes");

    var eventsArray = new google.maps.MVCArray([]);

    var heatmap = new google.maps.visualization.HeatmapLayer({
        data: eventsArray
    });

    heatmap.setMap(map);

    events.onopen = function(evt) {
        console.log("ws connection accepted");
    };

    events.onmessage = function(msg){
        var event = JSON.parse(msg.data);
        console.log("event occurred: " + JSON.stringify(event));

        eventsArray.push({location: new google.maps.LatLng(event.lat, event.long), weight: Math.ceil(event.magnitude)});
        //var marker = new google.maps.Marker({
        //    map: map,
        //    draggable: true,
        //    animation: google.maps.Animation.DROP,
        //    position: {lat: event.lat, lng: event.long}
        //});
    };
};
