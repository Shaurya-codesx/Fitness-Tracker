package com.example.fitnessapp.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnessapp.Data.Model.LocationPoints
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OsmMapview(modifier : Modifier = Modifier, currentLocation : LocationPoints? = null) {
    val context = LocalContext.current

    val mapView = remember { // to reduce unnecessary recompositions and avoid loosing map state, zoom level etc
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true) // sets pinch to touch function in map
            controller.setZoom(15.0)
            if (currentLocation == null) {
                controller.setCenter(GeoPoint(77.4220, -152.0841))
            }else {
                controller.setCenter(GeoPoint(currentLocation.coordinates.latitude, currentLocation.coordinates.longitude))
            }
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
        modifier = modifier.fillMaxSize()
    )
}