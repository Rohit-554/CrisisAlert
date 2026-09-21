package io.jadu.crisisprotect.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.jadu.crisisprotect.domain.model.RefreshResult
import io.jadu.crisisprotect.domain.repository.DisasterRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DisasterRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {
    private val repository: DisasterRepository by inject()
    override suspend fun doWork(): Result = when (repository.refreshEvents()) {
        RefreshResult.Success -> Result.success()
        RefreshResult.Failure -> Result.retry()
    }
}
