package dk.mhr.radio8podcast.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import dk.mhr.radio8podcast.presentation.DEBUG_LOG

class PlayerWorker(appContext: Context, workerParameters: WorkerParameters):
    Worker(appContext, workerParameters) {

    override fun doWork(): Result {
        Log.i(DEBUG_LOG, "okay okay....Now we have a worker for exo player")
        return Result.success()
    }
}