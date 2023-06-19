package net.pantasystem.milktea.worker.instance

//import android.content.Context
//import androidx.hilt.work.HiltWorker
//import androidx.work.CoroutineWorker
//import androidx.work.PeriodicWorkRequest
//import androidx.work.PeriodicWorkRequestBuilder
//import androidx.work.WorkerParameters
//import dagger.assisted.Assisted
//import dagger.assisted.AssistedInject
//import net.pantasystem.milktea.model.instance.InstanceInfoRepository
//import java.util.concurrent.TimeUnit
//
//@HiltWorker
//class SyncInstanceInfoWorker @AssistedInject constructor(
//    @Assisted context: Context,
//    @Assisted params: WorkerParameters,
//    private val instanceInfoRepository: InstanceInfoRepository,
//) : CoroutineWorker(context, params) {
//
//    companion object {
//        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
//            return PeriodicWorkRequestBuilder<SyncInstanceInfoWorker>(7, TimeUnit.DAYS)
//                .build()
//        }
//    }
//
//    override suspend fun doWork(): Result {
//        return instanceInfoRepository.sync().fold(
//            onSuccess = {
//                Result.success()
//            },
//            onFailure = {
//                Result.failure()
//            }
//        )
//    }
//
//}