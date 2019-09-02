package com.danilkomyshev.phisycalactivity

import android.os.AsyncTask

class AsyncTaskRunner(val f: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        f()
        return null
    }
}

fun doAsyncTask(f: () -> Unit) {
    AsyncTaskRunner(f).execute()
}