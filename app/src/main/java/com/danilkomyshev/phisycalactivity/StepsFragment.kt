package com.danilkomyshev.phisycalactivity

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.data.Value

@Suppress("DEPRECATION")
class StepsFragment : Fragment(), ActivityTrackerCallback, View.OnClickListener {

    private var steps: String? = "0"
    private lateinit var stepsView: TextView
    private lateinit var distanceView: TextView
    private lateinit var caloriesView: TextView
    private lateinit var linearLayout: LinearLayout
    private var distance = "0"
    private var calories = "0"
    private lateinit var mListener: OnFragmentInteractionListener

    private fun setSteps(steps: Int) {
        this.steps = steps.toString()
        stepsView.text = steps.toString()
    }

    override fun onCaloriesChanged(calories: Value?) {
        val calString = String.format("%2.0f", calories?.asFloat())
        setCalories(calString)
        if (calories != null) {
            MainActivity.saveToServer("calories", "float", calories.asFloat())
        }
    }

    override fun onDistanceMeasured(distance: Value?) {
        var dist = distance?.asFloat()
        var unit = "м"
        if (dist != null) {
            if (dist - 1000 > 0) {
                dist /= 1000
                unit = "км"
            }
        }

        val disString = String.format("%2.2f", dist)
        setDistance(disString, unit)

        if (dist != null) {
            MainActivity.saveToServer("distance", "float", dist.toFloat())
        }
    }

    override fun onStepsChanged(steps: Value?) {
        setSteps(steps.toString().toInt())

        MainActivity.saveToServer("steps", "int", steps!!)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onError(status: Status) {
        Toast.makeText(context, status.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            steps = arguments!!.getString(ARG_PARAM1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stepsView = getView()!!.findViewById(R.id.steps) as TextView
        stepsView.text = steps!!.toString()

        distanceView = getView()!!.findViewById(R.id.distance) as TextView
        val str = SpannableString(distance + "км")
        str.setSpan(ForegroundColorSpan(Color.GRAY), distance.length, str.length, distance.length)
        distanceView.text = str

        caloriesView = getView()!!.findViewById(R.id.calories) as TextView
        caloriesView.text = calories

        getView()!!.findViewById(R.id.steps_desc) as TextView
        linearLayout = getView()!!.findViewById(R.id.linear_layout) as LinearLayout

//        val requestQueue = Volley.newRequestQueue(activity)
//        val url = "https://jsonplaceholder.typicode.com/users"
//
//        val request = JsonObjectRequest(Request.Method.GET, url,
//            Response.Listener { response ->
//                val user: User?
//                try {
//                    user = User.fromJSON(response)
//                    Log.i("Hello:", user.toString())
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }, Response.ErrorListener { error ->
//                Log.i("ERROR", error.toString())
//                Toast.makeText(activity, "Could not get user info.", Toast.LENGTH_SHORT).show()
//            })
//        requestQueue.add(request)

        (view.findViewById(R.id.share_button) as ImageButton).setOnClickListener(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_steps, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        }
    }

    private fun setDistance(distance: String, unit: String) {
        this.distance = distance
        val str = SpannableString(distance + unit)
        str.setSpan(
            ForegroundColorSpan(Color.GRAY),
            distance.length,
            str.length,
            distance.length
        )
        distanceView.text = str
    }

    private fun setCalories(calories: String) {
        this.calories = calories
        caloriesView.text = calories
    }

    interface OnFragmentInteractionListener

    private fun shareScreenshot() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Отчёт по моей Двигательной Активности \nСегодняшнее количество шагов: " + steps!!
        )
        intent.type = "text/plain"
        startActivity(intent)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.share_button -> {
                Log.i(MainActivity.TAG, "Share button clicked.")
                shareScreenshot()
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "steps"

        fun newInstance(steps: Long): StepsFragment {
            val fragment = StepsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, steps.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
