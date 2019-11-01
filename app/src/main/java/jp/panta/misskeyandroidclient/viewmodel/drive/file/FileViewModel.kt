package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.io.File

class FileViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI,
    isSelectable: Boolean,
    private val maxSelectableItemSize: Int
) : ViewModel(){

    val filesLiveData = MutableLiveData<List<FileViewData>>()
    val isSelectable = MutableLiveData<Boolean>(isSelectable)

    private val selectedItemMap = HashMap<String, FileViewData>()

    fun getSelectedItems(): List<FileViewData>{
        return selectedItemMap.values.toList()
    }

    fun changeSelectItemState(fileViewData: FileViewData){
        if(selectedItemMap.size >= maxSelectableItemSize)
            return

        //nullはfalseとして扱う
        val isSelect = fileViewData.isSelect.value
        if(isSelect == null){
            fileViewData.isSelect.value = true
            selectedItemMap[fileViewData.id] = fileViewData
            if(selectedItemMap.size >= maxSelectableItemSize){
                allDisabledSelect()
            }
            return
        }

        if(isSelect){
            selectedItemMap.remove(fileViewData.id)
            fileViewData.isSelect.value = false
            if(selectedItemMap.size < maxSelectableItemSize){
                allEnabledSelect()
            }
        }else{
            selectedItemMap[fileViewData.id] = fileViewData
            fileViewData.isSelect.value = true
            if(selectedItemMap.size >= maxSelectableItemSize){
                allDisabledSelect()
            }
        }
    }

    private fun allDisabledSelect(){
        filesLiveData.value?.forEach{
            val item = selectedItemMap[it.id]
            if(item == null){
                it.isEnabledSelect.value = false
            }
        }
    }

    private fun allEnabledSelect(){
        filesLiveData.value?.forEach{
            it.isEnabledSelect.value = true
        }
    }


}