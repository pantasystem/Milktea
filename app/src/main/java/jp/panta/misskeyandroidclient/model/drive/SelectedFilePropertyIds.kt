package jp.panta.misskeyandroidclient.model.drive

import kotlinx.coroutines.flow.*

class SelectedFilePropertyIds(
    val selectableMaxCount: Int,
    selectedIds: List<FileProperty.Id>
) {

    private val _state = MutableStateFlow<Set<FileProperty.Id>>(emptySet())
    val state: StateFlow<Set<FileProperty.Id>> get() = _state

    init {
        val set = selectedIds.toSet()
        require(set.size <= selectableMaxCount) {
            "selectedIdsの個数はselectableMaxCount以下である必要があります。"
        }
        _state.value = selectedIds.toSet()
    }
    fun add(id: FileProperty.Id) : Boolean{
        val ids = this.state.value
        if(ids.size >= selectableMaxCount) {
            return false
        }
        this._state.value = ids.toMutableSet().also {
            add(id)
        }
        return true
    }

    fun remove(id: FileProperty.Id) {
        this._state.value = this.state.value.filterNot {
            id == it
        }.toSet()
    }

    fun exists(id: FileProperty.Id) : Boolean{
        return this.state.value.contains(id)
    }

    fun clear() {
        this._state.value = emptySet()
    }

    fun count() : Int{
        return this.state.value.size
    }
}


fun SelectedFilePropertyIds.watchExists(id: FileProperty.Id) : Flow<Boolean>{
    return this.state.map {
        it.contains(id)
    }
}

fun SelectedFilePropertyIds.watchCount() : Flow<Int> {
    return this.state.map {
        it.size
    }
}