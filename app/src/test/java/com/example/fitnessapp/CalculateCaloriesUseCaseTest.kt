package com.example.fitnessapp

import com.example.fitnessapp.Domain.UseCases.CalculateCaloriesUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateCaloriesUseCaseTest {

    // The actual object we are testing
    private lateinit var calculateCalories: CalculateCaloriesUseCase

    @Before
    fun setup() {
        // we can just create a real, standard instance of it.
        calculateCalories = CalculateCaloriesUseCase()
    }

    @Test
    fun `when user runs at normal pace, calories are calculated correctly`() {
        // GIVEN: A 70kg user runs 5km in 30 minutes.
        val distance = 5000f // 5000 meters
        val duration = 1800000L // 30 minutes in milliseconds
        val weight = 70f

        /* * Let's do the math manually to see what we EXPECT:
         * 30 mins = 0.5 hours.
         * Speed = 5km / 0.5h = 10 km/h.
         * In code, 10 km/h falls into the "< 11.3f" bracket, so MET = 9.8f.
         * Formula: 9.8 (MET) * 70 (kg) * 0.5 (hours) = 343.0 calories.
         */

        // WHEN: We push these numbers through your use case
        val result = calculateCalories(distance, duration, weight)

        // THEN: The result must match our manual math exactly
        // The third parameter (0.01f) is the "delta". Because Floats can be weird (e.g., 343.0000001),
        // we tell JUnit "As long as it is within 0.01 of 343.0, it passes."
        assertEquals(343.0f, result, 0.01f)
    }

    @Test
    fun `when duration is zero, guard clause triggers and returns zero`() {
        // GIVEN: A glitch happens and duration is 0
        val distance = 5000f
        val duration = 0L // ZERO
        val weight = 70f

        // WHEN
        val result = calculateCalories(distance, duration, weight)

        // THEN: Your guard clause should catch it and return 0f to prevent a crash
        assertEquals(0f, result, 0.0f)
    }
}