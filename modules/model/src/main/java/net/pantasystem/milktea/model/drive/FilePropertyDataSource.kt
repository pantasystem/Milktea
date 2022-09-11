package net.pantasystem.milktea.model.drive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.model.AddResult

class FilePropertyNotFoundException(filePropertyId: FileProperty.Id) : NoSuchElementException("id:$filePropertyId　は存在しません")

data class FilePropertyDataSourceState(
    val map: Map<FileProperty.Id, FileProperty>
) {
    fun findIn(ids: List<FileProperty.Id>) : List<FileProperty>{
        return ids.mapNotNull {
            map[it]
        }
    }

    fun getOrNull(id: FileProperty.Id) : FileProperty? {
        return map[id]
    }
}

interface FilePropertyDataSource {

    val state: StateFlow<FilePropertyDataSourceState>

    suspend fun add(fileProperty: FileProperty) : Result<AddResult>

    suspend fun addAll(list: List<FileProperty>) : Result<List<AddResult>>

    suspend fun remove(fileProperty: FileProperty) : Result<Boolean>

    suspend fun find(filePropertyId: FileProperty.Id) : Result<FileProperty>

    suspend fun findIn(ids: List<FileProperty.Id>) : Result<List<FileProperty>> = runCatching {
        ids.mapNotNull {
            runCatching {
                find(it).getOrNull()
            }.getOrNull()
        }
    }

    fun observe(id: FileProperty.Id) : Flow<FileProperty?> {
        return state.map {
            it.map[id]
        }.distinctUntilChanged()
    }

    fun observeIn(ids: List<FileProperty.Id>): Flow<List<FileProperty>> {
        return state.map { state ->
            ids.mapNotNull { id ->
                state.map[id]
            }
        }
    }


}
