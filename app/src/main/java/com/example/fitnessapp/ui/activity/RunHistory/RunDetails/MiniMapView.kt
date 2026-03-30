package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnessapp.Data.Model.LocationPoints
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


@Composable
fun miniMapView(modifier: Modifier = Modifier, routeList: List<LocationPoints> = emptyList()) {
    val context = LocalContext.current

    val route = remember(routeList) { // to prevent unnecessary mapping on ui recomposition
        routeList.map {
            GeoPoint(it.coordinates.latitude, it.coordinates.longitude)
        }
    }

    val miniMap = remember {
        MapView(context).apply {
            // map view configuration
            setTileSource(TileSourceFactory.MAPNIK) // sets the tiles
            setMultiTouchControls(true) // sets pinch to touch function in map
            controller.setZoom(19.0)


        }
    }

    AndroidView(
        factory = {miniMap},
        modifier = modifier.fillMaxSize(),
        update = { map ->
            if (routeList.isNotEmpty()) {
                val startingPoint = route.first()
                val endPoint = route.last()

                map.overlays.clear()

                val startMarker = Marker(map).apply {
                    position = startingPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Starting Point"
                }
                val endMarker = Marker(map).apply {
                    position = endPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "End Point"
                }

                // create polyline
                val polyline = Polyline(map).apply {
                    outlinePaint.color = Color.Green.toArgb()
                    outlinePaint.strokeWidth = 10f
                    setPoints(route)
                }

                map.overlays.add(polyline)
                map.overlays.add(startMarker)
                map.overlays.add(endMarker)

                // invalidate the map
                map.controller.setCenter(startingPoint)

            }
        }
    )

    LaunchedEffect(route) {
        if (route.isNotEmpty()) {miniMap.controller.setCenter(route.first()) }
    }

    // manage lifecycle
    DisposableEffect(Unit) {

        miniMap.onResume()

        onDispose {
            miniMap.onPause()
        }
    }
}