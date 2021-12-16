package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.model.AddResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileNotFoundException

class FilePropertyNotFoundException(filePropertyId: FileProperty.Id) : NoSuchElementException("id:$filePropertyId　は存在しません")

data class FilePropertyDataSourceState(
    val map: Map<FileProperty.Id, FileProperty>
) {
    fun findIn(ids: List<FileProperty.Id>) : List<FileProperty>{
        return ids.mapNotNull {
            map[it]
        }
    }
}

interface FilePropertyDataSource {

    val state: StateFlow<FilePropertyDataSourceState>

    suspend fun add(fileProperty: FileProperty) : AddResult

    suspend fun addAll(list: List<FileProperty>) : List<AddResult>

    suspend fun remove(fileProperty: FileProperty) : Boolean

    suspend fun find(filePropertyId: FileProperty.Id) : FileProperty

    suspend fun findIn(ids: List<FileProperty.Id>) : List<FileProperty> {
        return ids.mapNotNull {
            runCatching {
                find(it)
            }.getOrNull()
        }
    }

}

class InMemoryFilePropertyDataSource : FilePropertyDataSource{
    private var map = mapOf<FileProperty.Id, FileProperty>()
    private var _state = MutableStateFlow(FilePropertyDataSourceState(map))
    override val state: StateFlow<FilePropertyDataSourceState> = _state

    private val lock = Mutex()

    override suspend fun add(fileProperty: FileProperty): AddResult {
        val result: AddResult
        lock.withLock {
            map = map.toMutableMap().also {
                result = if(it.put(fileProperty.id, fileProperty) == null) AddResult.CREATED else AddResult.UPDATED
            }
        }
        _state.value = _state.value.copy(
            map = map
        )
        return result
    }

    override suspend fun addAll(list: List<FileProperty>): List<AddResult> {
        return list.map {
            add(it)
        }
    }

    override suspend fun find(filePropertyId: FileProperty.Id): FileProperty {
        return map[filePropertyId]?: throw FilePropertyNotFoundException(filePropertyId)
    }

    override suspend fun remove(fileProperty: FileProperty): Boolean {
        val result: Boolean
        lock.withLock {
            map.toMutableMap().also {
                result = it.remove(fileProperty.id) != null
            }
        }
        _state.value = _state.value.copy(
            map = map
        )
        return result

    }



}