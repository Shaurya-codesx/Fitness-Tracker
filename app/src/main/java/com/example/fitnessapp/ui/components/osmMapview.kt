package com.example.fitnessapp.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OsmMapview(modifier : Modifier = Modifier, currentLocation : LocationPoints? = null) {
    val context = LocalContext.current

    val locationMarker = remember { mutableStateOf<Marker?>(null) }

    val mapView = remember { // to reduce unnecessary recompositions and avoid loosing map state, zoom level etc
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true) // sets pinch to touch function in map
            controller.setZoom(20.0)

            val marker = Marker(this)
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            overlays.add(marker)
            locationMarker.value = marker
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
        currentLocation
    ) {
        currentLocation?.let {
            val center = GeoPoint(currentLocation.coordinates.latitude, currentLocation.coordinates.longitude)
            locationMarker.value?.position = center
            locationMarker.value?.title = "Current Location"
            mapView.controller.animateTo(center)
            Log.d("uitestingmap", "map center updated NOW")
            mapView.invalidate()
        }
    }


}