package net.pantasystem.milktea.model.drive


data class SelectedFilePropertyIds(
    val selectableMaxCount: Int,
    val selectedIds: Set<FileProperty.Id>
) {

    init {
        require(selectedIds.size <= selectableMaxCount) {
            "selectedIdsの個数はselectableMaxCount以下である必要があります。"
        }
    }

    val count: Int get() = selectedIds.size
    val isAddable: Boolean get() = selectedIds.size < selectableMaxCount

    fun addAndCopy(id: FileProperty.Id) : SelectedFilePropertyIds {
        val ids = this.selectedIds.let { ids ->
            ids.toMutableSet().also {
                it.add(id)
            }
        }

        return this.copy(selectedIds = ids)
    }

    fun removeAndCopy(id: FileProperty.Id) : SelectedFilePropertyIds {
        return this.copy(selectedIds = this.selectedIds.filterNot {
            it == id
        }.toSet())
    }

    fun exists(id: FileProperty.Id) : Boolean{
        return this.selectedIds.contains(id)
    }

    fun clearSelectedIdsAndCopy() : SelectedFilePropertyIds {
        return this.copy(selectedIds = emptySet())
    }




}


