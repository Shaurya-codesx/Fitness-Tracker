package com.example.fitnessapp.Domain.UseCases

import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import java.util.Calendar
import javax.inject.Inject

class GetRunRangeUseCase @Inject constructor() {

        operator fun invoke(filter: RunFilter): Pair<Long, Long> {
            val now = System.currentTimeMillis()

            val calendar = Calendar.getInstance()

            return when (filter) {

                RunFilter.ALL -> {
                    0L to now
                }

                RunFilter.DAY -> {
                    calendar.timeInMillis = now
                    resetToStartOfDay(calendar) // Uses your existing helper function
                    calendar.timeInMillis to now
                }

                RunFilter.WEEK -> {
                    calendar.timeInMillis = now
                    // set to first day of week (Monday)
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    resetToStartOfDay(calendar)
                    calendar.timeInMillis to now
                }

                RunFilter.MONTH -> {
                    calendar.timeInMillis = now
                    // set to first day of month
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    resetToStartOfDay(calendar)
                    calendar.timeInMillis to now
                }
            }
        }

        private fun resetToStartOfDay(calendar: Calendar) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
    }