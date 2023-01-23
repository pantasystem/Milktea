package net.pantasystem.milktea.worker

import androidx.work.WorkInfo
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 作成されたWorkerがアプリ生存中であるかそうでないかを判定するためのModel
 * アプリがKillされればメモリ上からSingletonなオブジェクトの状態も解放されるだろうという考え方。
 */
@Singleton
class WorkerIdsModel @Inject constructor() {
    private var workerIds: Set<UUID> = emptySet()

    /**
     * UI上などで処理する必要性があればここにWorkerのIDを入れる
     */
    fun add(id: UUID) {
        synchronized(workerIds) {
            workerIds = workerIds + id
        }
    }

    /**
     * UI上などで処理する必要性がなくなればこれを呼び出す
     */
    fun remove(id: UUID) {
        synchronized(workerIds) {
            workerIds = workerIds - id
        }
    }

    fun contains(id: UUID): Boolean {
        return synchronized(workerIds) {
            workerIds.contains(id)
        }
    }

    fun filterActiveWorkInfoList(list: List<WorkInfo>): List<WorkInfo> {
        return list.filter { info ->
            workerIds.any { memId ->
                memId == info.id
            }
        }
    }
}