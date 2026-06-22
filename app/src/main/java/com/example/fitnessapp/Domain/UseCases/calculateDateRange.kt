package com.example.fitnessapp.Domain.UseCases

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class calculateDateRange @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke (filter: FilterRange, offset: Int) : Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()

        return when (filter) {
            FilterRange.WEEK -> {
                val targetWeek = today.plusWeeks(offset.toLong())
                // Assuming your week starts on Monday
                val startOfWeek = targetWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = startOfWeek.plusDays(6)
                Pair(startOfWeek, endOfWeek)
            }
            FilterRange.MONTH -> {
                val targetMonth = today.plusMonths(offset.toLong())
                val startOfMonth = targetMonth.withDayOfMonth(1)
                val endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth())
                Pair(startOfMonth, endOfMonth)
            }
            FilterRange.YEAR -> {
                val targetYear = today.plusYears(offset.toLong())
                val startOfYear = targetYear.withDayOfYear(1)
                val endOfYear = targetYear.withDayOfYear(targetYear.lengthOfYear())
                Pair(startOfYear, endOfYear)
            }
        }
    }
}