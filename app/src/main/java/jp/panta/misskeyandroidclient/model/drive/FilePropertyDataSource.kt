package jp.panta.misskeyandroidclient.model.drive

import jp.panta.misskeyandroidclient.model.AddResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.FileNotFoundException

class FilePropertyNotFoundException(filePropertyId: FileProperty.Id) : NoSuchElementException("id:$filePropertyId　は存在しません")

interface FilePropertyDataSource {

    suspend fun add(fileProperty: FileProperty) : AddResult

    suspend fun addAll(list: List<FileProperty>) : List<AddResult>

    suspend fun remove(fileProperty: FileProperty) : Boolean

    suspend fun find(filePropertyId: FileProperty.Id) : FileProperty
}

class InMemoryFilePropertyDataSource : FilePropertyDataSource{
    private var map = mapOf<FileProperty.Id, FileProperty>()
    private val lock = Mutex()

    override suspend fun add(fileProperty: FileProperty): AddResult {
        lock.withLock {
            val result: AddResult
            map = map.toMutableMap().also {
                result = if(it.put(fileProperty.id, fileProperty) == null) AddResult.CREATED else AddResult.UPDATED
            }
            return result
        }
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
        lock.withLock {
            val result: Boolean
            map.toMutableMap().also {
                result = it.remove(fileProperty.id) != null
            }
            return result
        }
    }

}