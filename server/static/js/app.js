var MAP, DIALOG, FORM;
var MARKERS = {};


function initMap() {
    MAP = new google.maps.Map(document.getElementById('map'), {});

    // Create the DIV to hold the control and call the CenterControl()
    // constructor passing in this DIV.
    var centerControlDiv = document.createElement('div');
    var centerControl = new CenterControl(centerControlDiv, MAP);

    centerControlDiv.index = 1;
    MAP.controls[google.maps.ControlPosition.TOP_CENTER].push(centerControlDiv);

    fitMarkers(POSITIONS);
}


function fitMarkers(positions) {
    if (POSITIONS.length > 0) {
        var shouldFitBounds = false;

        //create empty LatLngBounds object
        var bounds = new google.maps.LatLngBounds();

        for (var i in positions) {
            
            var newLatLng = new google.maps.LatLng(positions[i]['lat'], positions[i]['lng']);
            bounds.extend(newLatLng);

            if (positions[i]['token'] in MARKERS) {

                MARKERS[positions[i]['token']].setPosition(newLatLng);

            } else {

                MARKERS[positions[i]['token']] = new google.maps.Marker({
                    position: newLatLng,
                    map: MAP,
                    icon: '/static/img/m1.png',  // https://developers.google.com/maps/documentation/javascript/marker-clustering
                    title: positions[i]['name'],
                    text: positions[i]['name']
                })

                shouldFitBounds = true;
            }
        }

        if (shouldFitBounds) {
            MAP.fitBounds(bounds);
            
            // Zooms out a bit if there's only one position
            if (positions.length == 1) {
                var listener = google.maps.event.addListener(MAP, "idle", function () {
                    MAP.setZoom(17);
                    google.maps.event.removeListener(listener);
                });
            }
        }



    } else if (positions.length == 0) {  // Initializes empti map centered in Australia
        MAP.setCenter({lat: -25.363, lng: 131.044}); // uluru
        MAP.setZoom(4);

    }
}


function CenterControl(controlDiv, map) {
    // Set CSS for the control border.
    var controlUI = document.createElement('div');
    controlUI.style.backgroundColor = '#fff';
    controlUI.style.border = '2px solid #fff';
    controlUI.style.borderRadius = '3px';
    controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
    controlUI.style.cursor = 'pointer';
    controlUI.style.marginBottom = '22px';
    controlUI.style.textAlign = 'center';
    controlUI.title = 'Click to add mobile to monitor';
    controlDiv.appendChild(controlUI);

    // Set CSS for the control interior.
    var controlText = document.createElement('div');
    controlText.style.color = 'rgb(25,25,25)';
    controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
    controlText.style.fontSize = '16px';
    controlText.style.lineHeight = '38px';
    controlText.style.paddingLeft = '5px';
    controlText.style.paddingRight = '5px';
    controlText.innerHTML = 'Add mobile';
    controlUI.appendChild(controlText);


    controlUI.addEventListener('click', function() {
        $("#addMobileDialog").dialog("open");
    });
}


// function getParameterFromUrl(url, param) {
//     var tokens;
//     try {
//         var url = new URL(url);
//         tokens = url.searchParams.get(param);
        
//     } catch (e) {
//         tokens = url
//     }

//     return (tokens != null && tokens.length > 0) ? tokens.split(',') : ([])
// }


function addMobile() {
    var token = $("#token").val();
    
    if (TOKENS.indexOf(token) < 0) {
        TOKENS.push(token);
        pollForPositions();
    } else {
        // TODO tell client token's already included
    }

}


function pollForPositions() {
    if (TOKENS.length > 0) {
        $.ajax({
            url: '/monitor/position',
            data: {token:TOKENS.join(',')},
            success: function(response) {
                POSITIONS = response;
                fitMarkers(POSITIONS);
            },
            error: function(xhr) {}
        });    
    }
}


function setUI() {
    DIALOG = $("#addMobileDialog").dialog({
        autoOpen: false,
        height: 400,
        width: 350,
        modal: true,
        buttons: {
            "Add": function () {
                addMobile();
                DIALOG.dialog( "close" );
            },
            "Cancel": function() {
                DIALOG.dialog( "close" );
            }
        },
        close: function() {
            FORM[0].reset();
            TOKENFIELD.removeClass( "ui-state-error" );
        }
    });
    FORM = DIALOG.find("form").on("submit", function(event) {
        event.preventDefault();
        addMobile();
    });
}


$(document).ready(function () {

    setUI();

    setInterval(pollForPositions, 3000);

});
