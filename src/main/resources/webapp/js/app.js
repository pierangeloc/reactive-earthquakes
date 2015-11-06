
var initialize = function(map) {
    console.log("calling initMap");
    var events = new WebSocket("ws://localhost:9091/earthquakes");

    var eventsArray = new google.maps.MVCArray([]);

    var heatmap = new google.maps.visualization.HeatmapLayer({
        data: eventsArray
    });

    var eventLog = [];

    heatmap.setMap(map);

    var RETENTION = 100;
    var appendEvent = function(evt) {
        eventLog.unshift( (new Date(parseInt(evt.time))).toUTCString() + '-' +
                        evt.place + '-' +
                        evt.magnitude + '-');
        eventLog = eventLog.slice(0, RETENTION);
        document.getElementById('timeline').value = eventLog.join('\n');


    }

    events.onopen = function(evt) {
        console.log("ws connection accepted");
    };

    events.onmessage = function(msg){
        var event = JSON.parse(msg.data);
        console.log('event occurred: ' + JSON.stringify(event));
        appendEvent(event);

        eventsArray.push({location: new google.maps.LatLng(event.lat, event.long), weight: Math.ceil(event.magnitude)});

        //items.push({id: 7, content: 'item 7', start: '2015-04-27'})
        //let's drop also a marker that will vanish after a while:
        var marker = new google.maps.Marker({
            map: map,
            icon: getCircle(event.magnitude),
            draggable: false,
            animation: google.maps.Animation.DROP,
            position: {lat: event.lat, lng: event.long}
        });
        window.setTimeout(function() {
            removeMarker(marker);
        }, 10000);
    };

    var removeMarker = function(marker) {
       marker.setMap(null);
    };

    /**
     * See https://developers.google.com/maps/tutorials/visualizing/earthquakes
     */
    function getCircle(magnitude) {
        var circle = {
            path: google.maps.SymbolPath.CIRCLE,
            fillColor: 'red',
            fillOpacity: .5,
            scale: Math.pow(2, magnitude),
            strokeColor: 'white',
            strokeWeight: .7
        };
        return circle;
    }
};
