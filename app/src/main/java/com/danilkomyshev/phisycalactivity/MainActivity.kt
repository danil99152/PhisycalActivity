@file:Suppress("DEPRECATION")

package com.danilkomyshev.phisycalactivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class MainActivity:AppCompatActivity() {
    private lateinit var googleApiClient:GoogleApiClient
    private lateinit var frameLayout: FrameLayout
    private lateinit var activityData:ActivityData
    private lateinit var activityFragment:StepsFragment

    init{
    context = this
    }

    private fun subscribe() {
    activityData = ActivityData(
        googleApiClient,
        activityFragment
    )
    activityData.createSubscription()
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        Log.i(TAG, "RequestCode $requestCode")

        if (requestCode != RC_SIGN_IN) {
            Toast.makeText(this, "RequestCode: $requestCode", Toast.LENGTH_LONG).show()
            return
        }

        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

        if (!result.isSuccess) {
            Toast.makeText(this, "Unable to get information from account.", Toast.LENGTH_LONG).show()
            return
        }
        val acct = result.signInAccount
        Log.i(TAG, "ACCOUNT: " + acct!!.email!!)
        val personName = acct.displayName
//        val personGivenName = acct.givenName
//        val personFamilyName = acct.familyName
        val personEmail = acct.email
//        val personId = acct.id
//        val personPhoto = acct.photoUrl

        val mobile = ""
        val user = HashMap<String, Any>()
            user["name"] = personName.toString()
            user["email"] = personEmail.toString()
            user["mobile"] = mobile
            user["gender"] = "Male"

         // get the information about user from server
                // else store it in the database
                val requestQueue = Volley.newRequestQueue(this)
        val url = "https://jsonplaceholder.typicode.com/users"
        val jsonObject = JSONObject(user as Map<*, *>)

        val jsonRequest = JsonObjectRequest(url, jsonObject,
            Response.Listener<JSONObject> { response ->
                try {
                    if (response.getString("status") == "SUCCESS") {
                        saveUserId(response.getInt("id"))
                    }
                } catch (ex:JSONException) {
                    ex.printStackTrace()
                }
            }, Response.ErrorListener { error -> Log.e(TAG, "" + error) })

        requestQueue.add(jsonRequest)
    }

    private fun saveUserId(id:Int) {
    Log.i(TAG, "Saving user: $id")
    val sharedPreferences = getSharedPreferences(getString(R.string.preferences_file), Context.MODE_PRIVATE)
    sharedPreferences.edit().putInt("userId", id).apply()
    userId = id
    isReady = true
    saveActivitiesToServer()
    }

    private fun buildFitnessClient() {
    googleApiClient = GoogleApiClient.Builder(this)
    .addApi(Fitness.RECORDING_API)
    .addApi(Fitness.HISTORY_API)
    .addApi(Fitness.GOALS_API)
    .addScope(Scope(Scopes.FITNESS_ACTIVITY_READ))
    .addScope(Scope(Scopes.FITNESS_LOCATION_READ))
    .addConnectionCallbacks(
    object:GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(bundle:Bundle?) {
            Log.i(TAG, "Connected!!")
            subscribe()
        }

        override fun onConnectionSuspended(i:Int) {
            if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST)
            {
                 Log.w(TAG, "Connection lost!!")
            }
            else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED)
            {
                 Log.w(TAG, "Service disconnected!")
            }
        }
    })
    .enableAutoManage(this, 0
    ) { connectionResult ->
        Log.w(TAG, ("Google Play services connection failed. Cause: $connectionResult"))
        Toast.makeText(
            applicationContext, ("Exception while connecting to Google Play services: " + connectionResult.errorMessage!!),
            Toast.LENGTH_LONG).show()
    }.build()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestEmail()
    .build()

    val apiClient = GoogleApiClient.Builder(this)
    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
    .build()


    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient)
    this.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val view = layoutInflater.inflate(R.layout.list_header, null)
        frameLayout = findViewById(R.id.container)
        frameLayout.addView(view)

        buildFitnessClient()
        applicationContext.assets

        activityFragment = StepsFragment.newInstance(0L)
        val fragmentManager = fragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.content, activityFragment)
        transaction.commit()
    }

    companion object {
         const val TAG = "MyApp"

        private const val RC_SIGN_IN = 9001

        var isReady = false
        private var userId:Int? = 0
        private val requestQueue = ArrayDeque<Map<String, Any>>()
        private lateinit var context : Context

         fun saveToServer(name:String, dataType:String, value:Any) {
            Log.i(TAG, "Saving to server: $name")
            val activityInfo = HashMap<String, Any>()
                 activityInfo["activity"] = name
                 activityInfo["dataType"] = dataType
                 activityInfo["value"] = value

            requestQueue.add(activityInfo)
            if (isReady)
            {
                saveActivitiesToServer()
            }
        }

        private fun saveActivitiesToServer() {
            val q = Volley.newRequestQueue(context)


            while (!requestQueue.isEmpty())
            {
                val data = requestQueue.remove()
                val url = "https://jsonplaceholder.typicode.com/users"

                val body = JSONObject(data)
                val request = JsonObjectRequest(url, body,
                    Response.Listener<JSONObject> { response ->
                        try {
                            if (response.getString("status") == "SUCCESS") {
                                Log.i(TAG, "Successfully saved activity.")
                            } else {
                                Log.i(TAG, "Failed to save activity.")
                            }
                        } catch (e:JSONException) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener { error -> Log.e(TAG, "" + error) })
                q.add(request)
            }
        }
    }
}
