package com.danilkomyshev.phisycalactivity

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.Value

open class ActivityTrackerConfigBase(val dataType: DataType, val field: Field)

class ActivityTrackerConfig(dataType: DataType, field: Field, val activityListener: ActivityListener)
    : ActivityTrackerConfigBase(dataType, field) {
    fun onChange( value: (Value) -> Unit) {
        activityListener.onChange = value
    }

    fun onError( status: (Status) -> Unit) {
        activityListener.onError = status
    }
}

class ActivityTracker(googleApiClient: GoogleApiClient) {
    private val activitySubscriber = ActivitySubscriber(googleApiClient)

    private fun subscribe(activity: ActivityTrackerConfig) {
        activitySubscriber.subscribeToActivity(activity)
    }

    fun subscribeToFloatActivities(activities: List<ActivityTrackerConfig>) {
        for (activity in activities) {
            subscribe(activity)
        }
    }

    fun subscribeToIntActivities(activities: List<ActivityTrackerConfig>) {
        for (activity in activities) {
            subscribe(activity)
        }
    }
}
