package com.example.cs446_ece452_android_app.data

import com.example.cs446_ece452_android_app.data.model.Destination
import com.example.cs446_ece452_android_app.data.model.LatLngResponse
import com.example.cs446_ece452_android_app.data.model.PlaceDetailsResponse
import com.example.cs446_ece452_android_app.data.model.Route
import com.example.cs446_ece452_android_app.data.model.RouteResponse
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.CompletableFuture

class MapsApiClient {
    private val client: OkHttpClient = OkHttpClient()
    private val key = "AIzaSyC2E4JsKZl3y3fV0nDUGeZOWfCjrwb5ISw"
    private val gson = Gson()

    private fun <T> getResponse(request: Request, callback: (String?) -> T): CompletableFuture<T> {
        val result: CompletableFuture<T> = CompletableFuture()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    result.complete(callback(responseData))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
        })

        return result
    }

    fun getDestination(searchString: String): CompletableFuture<Destination> {
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${searchString.replace(' ', '+')}&key=$key"
        val request: Request = Request.Builder()
            .url(url)
            .build()

        return getResponse(request) { response ->
            val parsed = gson.fromJson(response, LatLngResponse::class.java)
            val result = parsed.results[0]
            Destination(name = searchString, lat = result.geometry.location.lat, lng = result.geometry.location.lng, address = result.address, placeId = result.placeId)
        }
    }

    fun getPlaceName(placeId: String): CompletableFuture<String> {
        val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=name&language=en&key=$key"
        val request: Request = Request.Builder()
            .url(url)
            .build()

        return getResponse(request) { response ->
            val parsed = gson.fromJson(response, PlaceDetailsResponse::class.java)
            parsed.result.name
        }
    }

    fun getRoute(start: Destination, end: Destination, stops: ArrayList<Destination>?, travelMode: String): CompletableFuture<Route> {
        var stopsFormatted = ""
        if (stops != null) {
            for (dest in stops) {
                stopsFormatted += """
                {
                    "location": {
                        "latLng": {
                            "latitude": ${dest.lat},
                            "longitude": ${dest.lng}
                        }
                    }
                },
            """
            }
            stopsFormatted = """
                "intermediates": [
                    $stopsFormatted
                ],
            """
        }

        val requestString = """
            {
                "origin": {
                    "location": {
                        "latLng": {
                            "latitude": ${start.lat},
                            "longitude": ${start.lng}
                        }
                    }
                },
                "destination": {
                    "location": {
                        "latLng": {
                            "latitude": ${end.lat},
                            "longitude": ${end.lng}
                        }
                    }
                },
                $stopsFormatted
                "optimizeWaypointOrder": "true",
                "travelMode": "$travelMode"
            }"""

        val body = requestString.toRequestBody("application/json".toMediaType())
        val request: Request = Request.Builder()
            .url("https://routes.googleapis.com/directions/v2:computeRoutes")
            .post(body)
            .header("X-Goog-Api-Key", key)
            .header("X-Goog-FieldMask", "routes.legs,routes.distanceMeters,routes.staticDuration,routes.polyline.encodedPolyline,routes.viewport,routes.optimizedIntermediateWaypointIndex")
            .build()

        return getResponse(request) { response ->
            val parsed = gson.fromJson(response, RouteResponse::class.java)
            parsed.routes[0]
        }
    }
}