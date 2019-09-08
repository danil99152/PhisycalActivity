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
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.Status
import com.google.android.gms.fitness.data.Value
import org.json.JSONException

@Suppress("DEPRECATION")
class StepsFragment : Fragment(), ActivityTrackerCallback, View.OnClickListener {

    private var steps: String? = "0"
    private lateinit var stepsView: TextView
    private lateinit var distanceView: TextView
    private lateinit var caloriesView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var linearLayout: LinearLayout
    private var distance = "0"
    private var calories = "0"
    private lateinit var mListener: OnFragmentInteractionListener

    private fun setSteps(steps: Int) {
        this.steps = steps.toString()
        stepsView.text = steps.toString()
    }

    override fun onCaloriesChanged(data: Value?) {
        val calString = String.format("%2.0f", data?.asFloat())
        setCalories(calString)
        if (data != null) {
            MainActivity.saveToServer("calories", "float", data.asFloat())
        }
    }

    override fun onDistanceMeasured(distance: Value?) {
        var dist = distance?.asFloat()
        var unit = "m"
        if (dist != null) {
            if (dist - 1000 > 0) {
                dist /= 1000
                unit = "km"
            }
        }

        val disString = String.format("%2.2f", dist)
        setDistance(disString, unit)

        if (dist != null) {
            MainActivity.saveToServer("distance", "float", dist.toFloat())
        }
    }

    override fun onStepsChanged(value: Value?) {
        setSteps(steps.toString().toInt())

        MainActivity.saveToServer("steps", "int", steps!!.toInt())
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
        val str = SpannableString(distance + "km")
        str.setSpan(ForegroundColorSpan(Color.GRAY), distance.length, str.length, distance.length)
        distanceView.text = str

        caloriesView = getView()!!.findViewById(R.id.calories) as TextView
        caloriesView.text = calories

        getView()!!.findViewById(R.id.steps_desc) as TextView
        linearLayout = getView()!!.findViewById(R.id.linear_layout) as LinearLayout

        val requestQueue = Volley.newRequestQueue(activity)
        val url = "https://jsonplaceholder.typicode.com/users"

        val request = JsonObjectRequest(Request.Method.GET, url,
            Response.Listener { response ->
                val user: User?
                try {
                    user = User.fromJSON(response)
                    Log.i("Hello:", user.toString())
                    handleUser()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                Log.i("ERROR", error.toString())
                Toast.makeText(activity, "Could not get user info.", Toast.LENGTH_SHORT).show()
            })

        (view.findViewById(R.id.share_button) as ImageButton).setOnClickListener(this)

        requestQueue.add(request)
    }

    private fun handleUser() {
        progressBar.visibility = ProgressBar.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_steps, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } //else {
            //            throw new RuntimeException(context.toString()
            //                    + " must implement OnFragmentInteractionListener");
      //  }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     **/
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

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param steps Parameter 1.
         * @return A new instance of fragment StepsFragment.
         */
        fun newInstance(steps: Long): StepsFragment {
            val fragment = StepsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, steps.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
