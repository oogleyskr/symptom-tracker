package com.symptomtracker.ui.onboarding

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    var hasSeenOnboarding: Boolean
        get() = prefs.getBoolean("seen", false)
        set(value) = prefs.edit().putBoolean("seen", value).apply()
}
