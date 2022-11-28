package net.pantasystem.milktea.worker.note

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.worker.WorkerIdsModel
import net.pantasystem.milktea.worker.WorkerTags
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateNoteWorkerExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workerIdsModel: WorkerIdsModel,
){

    fun enqueue(draftNoteId: Long) {
        val request = CreateNoteWorker.createWorker(draftNoteId)
        workerIdsModel.add(request.id)
        WorkManager.getInstance(context)
            .enqueue(request)
    }

    /**
     * アプリ存在中に発生したCreateNote関連のWorkInfoのイベントを取得する
     */
    fun getCreateNoteWorkInfosInAppActives(): Flow<List<WorkInfo>> {
        val query =
            WorkQuery.Builder.fromTags(listOf(WorkerTags.CreateNote.name)).addStates(
                listOf(
                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.FAILED,
                )
            ).build()
        return WorkManager.getInstance(context)
            .getWorkInfosLiveData(query)
            .asFlow()
            .filterNotNull().map {
                workerIdsModel.filterActiveWorkInfoList(it)
            }
    }

    fun onHandled(id: UUID) {
        workerIdsModel.remove(id)
    }
}