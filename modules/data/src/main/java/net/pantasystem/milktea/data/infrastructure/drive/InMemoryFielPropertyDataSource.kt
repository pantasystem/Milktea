package net.pantasystem.milktea.data.infrastructure.drive

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.FilePropertyDataSourceState
import net.pantasystem.milktea.model.drive.FilePropertyNotFoundException
import javax.inject.Inject


class InMemoryFilePropertyDataSource @Inject constructor(): FilePropertyDataSource {
    private var map = mapOf<FileProperty.Id, FileProperty>()
    private var _state = MutableStateFlow(FilePropertyDataSourceState(map))
    override val state: StateFlow<FilePropertyDataSourceState> = _state

    private val lock = Mutex()

    override suspend fun add(fileProperty: FileProperty): Result<AddResult> {
        val result: AddResult
        lock.withLock {
            map = map.toMutableMap().also {
                result = if(it.put(fileProperty.id, fileProperty) == null) AddResult.Created else AddResult.Updated
            }
        }
        _state.value = _state.value.copy(
            map = map
        )
        return Result.success(result)
    }

    override suspend fun addAll(list: List<FileProperty>): Result<List<AddResult>> = runCatching {
        list.map {
            add(it).getOrElse {
                AddResult.Canceled
            }
        }
    }

    override suspend fun find(filePropertyId: FileProperty.Id): Result<FileProperty> = runCatching {
        map[filePropertyId]?: throw FilePropertyNotFoundException(filePropertyId)
    }

    override suspend fun remove(fileProperty: FileProperty): Result<Boolean> {
        val result: Boolean
        lock.withLock {
            map = map.toMutableMap().also {
                result = it.remove(fileProperty.id) != null
            }
        }
        _state.value = _state.value.copy(
            map = map
        )
        return Result.success(result)

    }





}