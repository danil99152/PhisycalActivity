package com.danilkomyshev.phisycalactivity

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.Value

interface ActivityTrackerCallback {
    fun onStepsChanged(value: Value?)

    fun onDistanceMeasured(distance: Value?)

    fun onError(status: Status)

    fun onCaloriesChanged(data: Value?)
}

class ActivityData(private val googleApiClient: GoogleApiClient, private val activityTrackerCallback: ActivityTrackerCallback) {
    private fun buildFloatActivities(): List<ActivityTrackerConfig> {
        val activities = arrayListOf<ActivityTrackerConfig>()

        var activityFloat = ActivityTrackerConfig(DataType.TYPE_DISTANCE_DELTA, Field.FIELD_DISTANCE, ActivityListener())
        activityFloat.onChange {
                value -> activityTrackerCallback.onDistanceMeasured(value)
        }
        activityFloat.onError {
                status -> activityTrackerCallback.onError(status)
        }
        activities.add(activityFloat)

        activityFloat = ActivityTrackerConfig(DataType.TYPE_CALORIES_EXPENDED, Field.FIELD_CALORIES, ActivityListener())
        activityFloat.onChange {
                value -> activityTrackerCallback.onCaloriesChanged(value)
        }
        activityFloat.onError {
                status -> activityTrackerCallback.onError(status)
        }
        activities.add(activityFloat)

        return activities
    }

    private fun buildIntActivities(): List<ActivityTrackerConfig> {
        val activities = arrayListOf<ActivityTrackerConfig>()

        val activity = ActivityTrackerConfig(DataType.TYPE_STEP_COUNT_DELTA, Field.FIELD_STEPS, ActivityListener())
        activity.onChange {
                value -> activityTrackerCallback.onStepsChanged(value)
        }

        activity.onError {
                status -> activityTrackerCallback.onError(status)
        }
        activities.add(activity)

        return activities;
    }
    fun createSubscription() {
        val activities = buildFloatActivities()

        val activityTracker = ActivityTracker(googleApiClient)

        activityTracker.subscribeToFloatActivities(activities)
        val intActivities = buildIntActivities()
        activityTracker.subscribeToIntActivities(intActivities)
    }
}
