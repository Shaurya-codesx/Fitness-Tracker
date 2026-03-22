package com.example.fitnessapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnessapp.Data.Location.androidLocationProvider
import com.example.fitnessapp.Data.Model.LocationPoints
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OsmMapview(modifier : Modifier = Modifier, Route : List<LocationPoints> = emptyList()) {
    val context = LocalContext.current

    val locationMarker = remember { mutableStateOf<Marker?>(null) }
    val routePolyline = remember { mutableStateOf<Polyline?>(null) }

    val mapView = remember { // to reduce unnecessary recompositions and avoid loosing map state, zoom level etc
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true) // sets pinch to touch function in map
            controller.setZoom(20.0)

            // set Marker
            val marker = Marker(this)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            overlays.add(marker)
            locationMarker.value = marker

            // set Polyline
            val polyline = Polyline()
            polyline.outlinePaint.color = Color.Blue.toArgb()
            polyline.outlinePaint.strokeWidth = 10f
            overlays.add(polyline)
            routePolyline.value = polyline

        }

    }

    // Step 2 - manage lifecycle
    DisposableEffect(Unit) {

        mapView.onResume()

        onDispose {
            mapView.onPause()
        }
    }

    AndroidView( // lambda that renders the map onto the map view
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = {
        }
    )

    LaunchedEffect(
        Route
    ) {
        if (Route.isEmpty()) return@LaunchedEffect

        val lastPoint = Route.last()
        val center = GeoPoint(lastPoint.coordinates.latitude, lastPoint.coordinates.longitude)

        // Update Marker and camera
        locationMarker.value?.position = center
        mapView.controller.animateTo(center)

        // Polyline Sync/Append Logic
        val polyline = routePolyline.value
        if (polyline != null) {
            // IF the polyline in the Map is empty but our Route list HAS data
            // (This happens after a screen rotation/recomposition)
            if (polyline.actualPoints.isEmpty() && Route.isNotEmpty()) {
                // Sync the entire history once
                val history = Route.map {
                    GeoPoint(it.coordinates.latitude, it.coordinates.longitude)
                }
                polyline.setPoints(history)
            } else {
                // Otherwise, just append the newest point (Efficient)
                polyline.addPoint(center)
            }
        }

        mapView.invalidate()
        Log.d("uitestingmap", "Map updated with point: ${center.latitude}")

    }
}