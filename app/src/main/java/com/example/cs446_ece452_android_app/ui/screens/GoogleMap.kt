package com.example.cs446_ece452_android_app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.cs446_ece452_android_app.ui.components.BottomNavigationBar
import com.example.cs446_ece452_android_app.R
import com.example.cs446_ece452_android_app.data.RouteController
import com.example.cs446_ece452_android_app.data.model.Destination
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PinConfig
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import com.example.cs446_ece452_android_app.data.model.Route

@Composable
fun MapScreen(navController: NavController, rc: RouteController) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val mapId = stringResource(R.string.map_id)

    val cameraPosition = rememberCameraPositionState()

    LaunchedEffect(rc.routeInfoLoaded) {
        if (rc.routeInfoLoaded) {
            val low = rc.routeInfo.route!!.viewport!!.low!!
            val high = rc.routeInfo.route!!.viewport!!.high!!
            val boundsBuilder = LatLngBounds.builder()
            boundsBuilder.include(LatLng(low.lat, low.lng))
            boundsBuilder.include((LatLng(high.lat, high.lng)))
            val bounds = boundsBuilder.build()
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            cameraPosition.move(cameraUpdate)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPosition,
                googleMapOptionsFactory = { GoogleMapOptions().mapId(mapId) },
                onMapLoaded = { isMapLoaded = true },
                content = {
                    if (rc.routeInfoLoaded && rc.routeEntryLoaded) {
                        if (rc.routeEntry.accessToCar) {
                            CarRouteContent(
                                start = rc.routeInfo.startDest!!,
                                end = rc.routeInfo.endDest!!,
                                stops = rc.routeInfo.stopDests,
                                poly = rc.routeInfo.route!!.polyline!!.encodedPolyline
                            )
                        } else {
                            TransitRouteContent(
                                transitRoutes = rc.routeInfo.transitRoute!!,
                                start = rc.routeInfo.startDest!!,
                                end = rc.routeInfo.endDest!!,
                                stops = rc.routeInfo.stopDests
                            )
                        }
                    }
                }
            )
            if(rc.routeEntry.accessToCar == false){
                Button(
                    //Need to pass in rc.transitRouteInfo
                    onClick = { navController.navigate("Directions") },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text("Transit Directions")
                }
            }
            Button(
                onClick = { navController.navigate("EditRoute") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Edit Route")
            }
        }

        if (!isMapLoaded) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Loading Map")
                }
            }
        }
    }
}

@Composable
@GoogleMapComposable
fun CarRouteContent(start: Destination, end: Destination, stops: List<Destination>?, poly: String) {
    StartEndMarker(start)
    if (start.address != end.address)
        StartEndMarker(end)
    if (stops != null)
        StopMarkers(stops)
    Route(poly)
}

@Composable
@GoogleMapComposable
fun TransitRouteContent(transitRoutes: List<Route>, start: Destination, end: Destination, stops: List<Destination>?) {
    StartEndMarker(destination = start)
    if (start.address != end.address)
        StartEndMarker(end)
    if (stops != null)
        StopMarkers(stops)

    transitRoutes.forEach { route ->
        route.legs!!.forEach { leg ->
            val decoded = decodePolyline(leg.polyline!!.encodedPolyline)
            Polyline(
                points = decoded,
                color = Color.Red
            )
        }
    }
}

@Composable
@GoogleMapComposable
fun StartEndMarker(destination: Destination) {
    Marker(
        state = rememberMarkerState(position = LatLng(destination.lat, destination.lng)),
        title = destination.name
    )
}

@Composable
@GoogleMapComposable
fun StopMarkers(stops: List<Destination>) {
    stops.forEachIndexed { i, stop ->
        AdvancedMarker(
            state = rememberMarkerState(position = LatLng(stop.lat, stop.lng)),
            title = stop.name,
            pinConfig = configPin((i + 1).toString())
        )
    }
}

private fun configPin(s: String): PinConfig {
    return with(PinConfig.builder()) {
        setGlyph(PinConfig.Glyph(s, android.graphics.Color.WHITE))
        setBackgroundColor(android.graphics.Color.MAGENTA)
        setBorderColor(android.graphics.Color.MAGENTA)
        build()
    }
}

@Composable
@GoogleMapComposable
fun Route(poly: String) {
    val decoded = decodePolyline(poly)

    Polyline(
        points = decoded,
        color = Color.Blue
    )
}

private fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val finalLat = lat / 1E5
        val finalLng = lng / 1E5
        poly.add(LatLng(finalLat, finalLng))
    }

    return poly
}
