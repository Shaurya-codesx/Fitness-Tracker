package com.example.fitnessapp.Domain

import com.example.fitnessapp.Data.Model.LocationPoints
import kotlinx.coroutines.flow.Flow

interface LocationDataSource { // this defines how the data is emitted, like it simply tells what this can do without getting into how android does it

    val locationDataStream : Flow<LocationPoints> // a flow of location data observable by others


}