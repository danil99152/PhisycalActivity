package com.danilkomyshev.phisycalactivity

import android.Manifest
import android.text.format.DateFormat
import android.util.Log
import com.danilkomyshev.phisycalactivity.MainActivity.Companion.TAG
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.fitness.Fitness
import java.util.concurrent.TimeUnit

fun readData(activity: ActivityTrackerConfig, googleApiClient: GoogleApiClient) {
    val result = Fitness.HistoryApi.readDailyTotal(googleApiClient, activity.dataType)
    result.setResultCallback {
            dailyTotalResult ->
        run {
            if (dailyTotalResult.status.isSuccess) {
                if (dailyTotalResult.total?.dataPoints?.isEmpty()!!) {
                    Log.i(TAG, "$dailyTotalResult is empty.")
                    return@run
                }
                val dataPoints = dailyTotalResult.total?.dataPoints?.get(0)
                if (dataPoints != null) {
                    Log.i(TAG, "${dataPoints.dataType.name} from "
                            + "${dataPoints.dataSource.name} during "
                            + "${DateFormat.format("", dataPoints.getStartTime(TimeUnit.MILLISECONDS))} to"
                            + "${DateFormat.format("", dataPoints.getEndTime(TimeUnit.MILLISECONDS))}"
                    )
                    activity.activityListener.onChange(dataPoints.getValue(activity.field))
                }
            } else {
                Log.i(TAG, dailyTotalResult.status.toString())
                activity.activityListener.onError(dailyTotalResult.status)
            }
        }
    }
}

private val LOCATION_PERMS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
private const val LOCATION_REQUEST = 255

class ActivitySubscriber(private val googleApiClient: GoogleApiClient) {
    fun subscribeToActivity(activityTrackerConfig: ActivityTrackerConfig) {
//        if (ContextCompat.checkSelfPermission(
//                MainActivity() as Activity,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(
//                    MainActivity() as Activity,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            ){
//                Toast.makeText(MainActivity(), "Без этого разрешения я работать не буду",Toast.LENGTH_LONG).show()
//            } else {
//                ActivityCompat.requestPermissions(MainActivity() as Activity, LOCATION_PERMS, LOCATION_REQUEST)
//            }
//        }
        Fitness.RecordingApi.subscribe(googleApiClient, activityTrackerConfig.dataType)
            .setResultCallback {
                    status ->
                run {
                    if (status.isSuccess) {
                        Log.i(TAG, "Successfully subscribed to activity ${activityTrackerConfig.dataType.name}")

                        doAsyncTask {
                            readData(activityTrackerConfig, googleApiClient)
                        }
                    } else {
                        activityTrackerConfig.activityListener.onError(status)
                    }
                }
            }
    }
}
