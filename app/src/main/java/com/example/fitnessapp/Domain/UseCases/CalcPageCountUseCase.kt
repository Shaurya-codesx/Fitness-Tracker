package com.example.fitnessapp.Domain.UseCases

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class CalculatePageCountUseCase @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun invoke(oldestRunMillis: Long?, filter: FilterRange): Int {
        // If they have no runs, there is only 1 page (Today)
        if (oldestRunMillis == null || oldestRunMillis == 0L) return 1

        val oldestDate = Instant.ofEpochMilli(oldestRunMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val today = LocalDate.now()

        return when (filter) {
            FilterRange.WEEK -> {
                // Snap both dates to their respective Mondays to count exact calendar weeks
                val startOfWeekOldest = oldestDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val startOfWeekToday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                ChronoUnit.WEEKS.between(startOfWeekOldest, startOfWeekToday).toInt() + 1
            }
            FilterRange.MONTH -> {
                // Calculate exact calendar months difference
                val yearsDiff = today.year - oldestDate.year
                val monthsDiff = today.monthValue - oldestDate.monthValue
                (yearsDiff * 12) + monthsDiff + 1
            }
            FilterRange.YEAR -> {
                // Calculate exact calendar years difference
                (today.year - oldestDate.year) + 1
            }
        }
    }
}