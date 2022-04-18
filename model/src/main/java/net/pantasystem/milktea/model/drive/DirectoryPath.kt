package net.pantasystem.milktea.model.drive


/**
 * Rootから現在にいる経路を表すPath
 */
data class DirectoryPath(
    val path: List<Directory>
) {

    fun push(directory: Directory) : DirectoryPath {
       require(path.lastOrNull()?.id == directory.parentId) {
           "親子関連のない不正なDirectory"
       }
       return this.copy(
           path = this.path.toMutableList().also {
               it.add(directory)
           }
       )
    }

    fun pop() : DirectoryPath {
        if(path.isEmpty()) {
            return this
        }
        return this.copy(path = path.subList(0, path.size - 1))
    }


    fun popUntil(directory: Directory?) : DirectoryPath {
        if(directory == null) {
            return this.clear()
        }
        val index = path.lastIndexOf(directory)
        if(index == -1) {
            return this
        }
        return this.copy(
            path = this.path.subList(0, index + 1)
        )
    }

    fun clear() : DirectoryPath {
        return this.copy(
            path = emptyList()
        )
    }


}