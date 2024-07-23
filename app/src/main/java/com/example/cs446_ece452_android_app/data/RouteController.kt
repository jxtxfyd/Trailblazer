package com.example.cs446_ece452_android_app.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.cs446_ece452_android_app.data.model.DestinationEntryStruct
import com.example.cs446_ece452_android_app.data.model.RouteInfo
import com.example.cs446_ece452_android_app.data.model.Route
import com.example.cs446_ece452_android_app.data.model.RouteEntry
import com.example.cs446_ece452_android_app.data.model.TravelMode
import java.util.concurrent.CompletableFuture

class RouteController(private val client: MapsApiClient) : ViewModel() {
    lateinit var routeEntry: RouteEntry
    var routeEntryLoaded by mutableStateOf(false)
        private set

    var routeInfo: RouteInfo = RouteInfo()
        private set
    var transitRouteInfo: MutableList<Route> = mutableListOf()
        private set
    var routeInfoLoaded by mutableStateOf(false)
        private set
    lateinit var currentRoute: String
    fun getRoute(routeId: String) {
        currentRoute = routeId
        routeEntryLoaded = false
        routeInfoLoaded = false
        getRouteEntryFromDb(routeId) {
            routeEntry = it
            routeEntryLoaded = true
        }
        getRouteFromDb(routeId) {
            routeInfo = it
            routeInfoLoaded = true
        }
    }

    fun getRoute(
        routeName: String,
        location: String,
        maxCost: String,
        accessToCar: Boolean,
        startDate: String,
        endDate: String,
        startDest: DestinationEntryStruct,
        endDest: DestinationEntryStruct,
        destinations: List<DestinationEntryStruct>,
        creatorEmail: String,
        sharedEmails: List<String>,
        createdDate: String,
        lastModifiedDate: String
    ) {
        routeEntryLoaded = false
        routeInfoLoaded = false
        routeEntry = RouteEntry(routeName, location, maxCost, accessToCar, startDate, endDate, startDest, endDest, destinations, creatorEmail, sharedEmails, createdDate, lastModifiedDate)
        routeEntryLoaded = true
        calculateRoute()
    }

    fun hasCarAccess(): Boolean {
        return routeEntry.accessToCar
    }

    private fun calculateRoute() {
        val carAccess = routeEntry.accessToCar

        val startFuture = client.getDestination(routeEntry.startDest!!.destination)
        val endFuture = client.getDestination(routeEntry.endDest!!.destination)
        val destsFuture = routeEntry.destinations!!.map { client.getDestination(it.destination) }

        CompletableFuture.allOf(startFuture, endFuture, *destsFuture.toTypedArray()).thenRun {
            routeInfo.startDest = startFuture.join()
            routeInfo.endDest = endFuture.join()
            routeInfo.stopDests = destsFuture.mapTo(arrayListOf()) { it.join() }

            routeInfo.route = client.getRoute(routeInfo.startDest!!, routeInfo.endDest!!, routeInfo.stopDests, TravelMode.CAR).join()

            if (!carAccess) {
                val routesFuture = mutableListOf<CompletableFuture<Route>>()
                for (leg in routeInfo.route?.legs ?: listOf()) {
                    val startLat = leg.start!!.latLng!!.lat
                    val startLng = leg.start.latLng!!.lng
                    val endLat = leg.end!!.latLng!!.lat
                    val endLng = leg.end.latLng!!.lng

                    val requestString = """
                        {
                            "origin": {
                                "location": {
                                    "latLng": {
                                        "latitude": $startLat,
                                        "longitude": $startLng
                                    }
                                }
                            },
                            "destination": {
                                "location": {
                                    "latLng": {
                                        "latitude": $endLat,
                                        "longitude": $endLng
                                    }
                                }
                            },
                            "travelMode": "TRANSIT"
                        }"""
                    routesFuture.add(client.getRoute(requestString))
                }

                CompletableFuture.allOf(*routesFuture.toTypedArray()).thenRun {
                    routesFuture.forEach { route ->
                        transitRouteInfo.add(route.join())
                    }
                }
            }
        }.thenRun {
            routeInfoLoaded = true
            addRouteEntryToDb(routeEntry) { id ->
                addRouteToDb(id, routeInfo)
            }
        }
    }
}