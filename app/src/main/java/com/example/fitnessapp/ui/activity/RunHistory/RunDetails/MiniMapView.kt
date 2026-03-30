package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnessapp.Data.Model.LocationPoints
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


@Composable
fun miniMapView(routeList: List<LocationPoints> = emptyList()) {
    val context = LocalContext.current


    // has two bugs which need to be fixed,
    // 1. if any run with a single point in the route list, it shows very far from that point in the mini map
    // 2. the mini map stutters on touch, fights user interaction

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
//            controller.setZoom(19.0)


        }
    }

    LaunchedEffect(route) {
        if (route.isEmpty()) return@LaunchedEffect

        miniMap.overlays.clear()

        val startingPoint = route.first()
        val endPoint = route.last()

        val startMarker = Marker(miniMap).apply {
            position = startingPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Starting Point"
        }
        val endMarker = Marker(miniMap).apply {
            position = endPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "End Point"
        }

        // create polyline
        val polyline = Polyline(miniMap).apply {
            outlinePaint.color = Color.Green.toArgb()
            outlinePaint.strokeWidth = 10f
            setPoints(route)
        }

        miniMap.overlays.add(polyline)
        miniMap.overlays.add(startMarker)
        miniMap.overlays.add(endMarker)

        if (route.size >= 2) {
            miniMap.post {
                try {
                        val boundingBox = BoundingBox.fromGeoPoints(route)
                        // Padding of 120 pixels
                        miniMap.zoomToBoundingBox(boundingBox, false, 120)
                    Log.d("map view", "this nigga also running")
                } catch (e: Exception) {
                    // Fallback if the bounding box calculation fails
                    miniMap.controller.setCenter(endPoint)
                }
            }
        }else {
            miniMap.controller.setCenter(endPoint)
            miniMap.controller.setZoom(18.0)
        }
        miniMap.invalidate()
    }


    val modifier = Modifier
    AndroidView(
        factory = {miniMap},
        modifier = modifier.fillMaxSize(),
        update = {
        }
    )


    // manage lifecycle
    DisposableEffect(Unit) {

        miniMap.onResume()

        onDispose {
            miniMap.onPause()
        }
    }
}