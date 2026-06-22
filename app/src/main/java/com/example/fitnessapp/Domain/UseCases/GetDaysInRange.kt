package com.example.fitnessapp.Domain.UseCases

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.LocalDate
import javax.inject.Inject

class GetDaysInRange @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke (filter: FilterRange, offset: Int): Int {
        val today = LocalDate.now()
        return when (filter) {
            FilterRange.WEEK -> 7
            FilterRange.MONTH -> today.plusMonths(offset.toLong()).lengthOfMonth()
            FilterRange.YEAR -> today.plusYears(offset.toLong()).lengthOfYear()
        }
    }
}