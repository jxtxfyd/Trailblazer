package com.example.cs446_ece452_android_app.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.cs446_ece452_android_app.data.model.GoogleRouteInfo
import com.example.cs446_ece452_android_app.data.model.TravelMode
import java.util.concurrent.CompletableFuture

class RouteController(private val client: MapsApiClient) : ViewModel() {
    private lateinit var routeName: String
    private lateinit var location: String
    private lateinit var maxCost: String
    private var accessToCar: Boolean = false
    private lateinit var startDate: String
    private lateinit var endDate: String
    private lateinit var startDest: DestinationEntryStruct
    private lateinit var endDest: DestinationEntryStruct
    private var destinations: List<DestinationEntryStruct>? = null
    private lateinit var creatorEmail: String
    private var sharedEmails: List<String> = emptyList()
    private lateinit var createdDate : String
    private lateinit var lastModifiedDate : String

    var routeInfo: GoogleRouteInfo = GoogleRouteInfo()
        private set
    var dataLoaded by mutableStateOf(false)
        private set

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
        creatorEmail : String,
        sharedEmails : List<String>,
        createdDate : String,
        lastModifiedDate : String
    ) {
        dataLoaded = false

        this.routeName = routeName
        this.location = location
        this.maxCost = maxCost
        this.accessToCar = accessToCar
        this.startDate = startDate
        this.endDate = endDate
        this.startDest = startDest
        this.endDest = endDest
        this.destinations = destinations
        this.creatorEmail = creatorEmail
        this.sharedEmails = sharedEmails
        this.createdDate = createdDate
        this.lastModifiedDate = lastModifiedDate

        saveRouteEntry()
        calculateRoute()
    }

    private fun saveRouteEntry() {
        addRouteEntry(routeName, location, maxCost, accessToCar, startDate, endDate, startDest, endDest, destinations!!, creatorEmail, sharedEmails, createdDate, lastModifiedDate)
    }

    fun calculateRoute() {
        val startFuture = client.getDestination(startDest.destination)
        val endFuture = client.getDestination(endDest.destination)

        val destsFuture = destinations!!.map { client.getDestination(it.destination) }

        CompletableFuture.allOf(startFuture, endFuture, *destsFuture.toTypedArray()).thenRun {
            routeInfo.startDest = startFuture.join()
            routeInfo.endDest = endFuture.join()

            routeInfo.stopDests = destsFuture.mapTo(arrayListOf()) { it.join() }

            routeInfo.route = client.getRoute(routeInfo.startDest!!, routeInfo.endDest!!, routeInfo.stopDests, TravelMode.CAR).join()
        }.thenRun{
            dataLoaded = true
        }
    }
}