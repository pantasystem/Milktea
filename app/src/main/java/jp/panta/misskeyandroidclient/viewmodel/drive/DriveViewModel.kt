package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FolderProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFile
import jp.panta.misskeyandroidclient.model.drive.RequestFolder
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
import kotlinx.coroutines.launch
import java.util.*

class DriveViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI
) : ViewModel(){

    val currentDirectory = MutableLiveData<Directory>(Directory(null))

    val hierarchyDirectory = MutableLiveData<List<Directory>>()




    init{
        val current = currentDirectory.value
        if(current != null){
            hierarchyDirectory.postValue(listOf(current))
        }
    }




    fun moveChildDrectory(childDirectory: FolderViewData){
        val current = Directory(childDirectory.folderProperty)
        currentDirectory.postValue(current)
        val list = hierarchyDirectory.value
        val newList = if(list == null){
            listOf(current)
        }else{
            ArrayList(list).apply{
                add(current)
            }
        }
        hierarchyDirectory.postValue(newList)
    }

    fun moveParentDirectory(){
        val list = hierarchyDirectory.value
            ?: return
        val arrayList = ArrayList<Directory>(list)
        val lastIndex = arrayList.size - 1
        if(lastIndex < 0){
            return
        }else{
            val lastItem = arrayList.removeAt(lastIndex)
            currentDirectory.postValue(lastItem)
            hierarchyDirectory.postValue(arrayList)
        }
    }



}