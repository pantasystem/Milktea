package jp.panta.misskeyandroidclient.model.drive

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Rootから現在にいる経路を表すPath
 */
class DirectoryPath {

    private val _route = MutableStateFlow<List<Directory>>(emptyList())
    val route: StateFlow<List<Directory>> get() = _route

    fun push(directory: Directory) {
        if(route.value.lastOrNull()?.id != directory.parentId) {
            throw IllegalArgumentException("不正なDirectory")
        }
        _route.value = _route.value.toMutableList().also {
            it.add(directory)
        }
    }

    fun pop() : Boolean {
        val list = _route.value
        if(list.isEmpty()) {
            return false
        }
        this._route.value = list.subList(0, list.size - 1)
        return true
    }

    fun popUntil(directory: Directory) : Boolean{
        val list = _route.value
        val index = list.lastIndexOf(directory)
        if(index == -1) {
            return false
        }
        this._route.value = this.route.value.subList(0, index)
        return true
    }

    fun isCurrentRoot() : Boolean{
        return this.route.value.isEmpty()
    }

}