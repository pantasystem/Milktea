package net.pantasystem.milktea.model.drive

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.runCancellableCatching
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


    suspend fun add(fileProperty: FileProperty) : Result<AddResult>

    suspend fun addAll(list: List<FileProperty>) : Result<List<AddResult>>

    suspend fun remove(fileProperty: FileProperty) : Result<Boolean>

    suspend fun find(filePropertyId: FileProperty.Id) : Result<FileProperty>

    suspend fun clearUnusedCaches(): Result<Unit>

    suspend fun findIn(ids: List<FileProperty.Id>) : Result<List<FileProperty>> = runCancellableCatching {
        ids.mapNotNull {
            runCancellableCatching {
                find(it).getOrNull()
            }.getOrNull()
        }
    }

    fun observe(id: FileProperty.Id) : Flow<FileProperty?>

    fun observeIn(ids: List<FileProperty.Id>): Flow<List<FileProperty>>


}
