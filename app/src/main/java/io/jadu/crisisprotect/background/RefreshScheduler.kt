package io.jadu.crisisprotect.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RefreshScheduler {
    private const val UniqueWorkName = "disaster-source-refresh"
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<DisasterRefreshWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UniqueWorkName, ExistingPeriodicWorkPolicy.KEEP, request,
        )
    }
}
