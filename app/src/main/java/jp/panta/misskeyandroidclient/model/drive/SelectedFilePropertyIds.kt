package jp.panta.misskeyandroidclient.model.drive

import kotlinx.coroutines.flow.*

class SelectedFilePropertyIds {

    private val _state = MutableStateFlow<Set<FileProperty.Id>>(emptySet())
    val state: StateFlow<Set<FileProperty.Id>> get() = _state

    fun add(id: FileProperty.Id) {
        val ids = this.state.value
        this._state.value = ids.toMutableSet().also {
            add(id)
        }
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