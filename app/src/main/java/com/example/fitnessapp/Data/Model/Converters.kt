package com.example.fitnessapp.Data.Model

import androidx.room.TypeConverter
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint?): String? {
        if (geoPoint == null) return null
        // Store as a simple map/json so it's readable
        val data = mapOf("lat" to geoPoint.latitude, "lng" to geoPoint.longitude)
        return gson.toJson(data)
    }

    @TypeConverter
    fun toGeoPoint(geoPointString: String?): GeoPoint? {
        if (geoPointString == null) return null
        val mapType = object : TypeToken<Map<String, Double>>() {}.type
        val data: Map<String, Double> = gson.fromJson(geoPointString, mapType)
        return GeoPoint(data["lat"] ?: 0.0, data["lng"] ?: 0.0)
    }

    @TypeConverter
    fun fromLocationPointsList(value: List<LocationPoints>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLocationPointsList(value: String?): List<LocationPoints>? {
        val listType = object : TypeToken<List<LocationPoints>>() {}.type
        return gson.fromJson(value, listType)
    }
}