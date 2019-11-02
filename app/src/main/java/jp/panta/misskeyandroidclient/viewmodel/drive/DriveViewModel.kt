package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
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
        val currentIndex = arrayList.size - 1
        if(currentIndex < 1){
            return
        }else{
            arrayList.removeAt(currentIndex)
            //val lastItem = arrayList.removeAt(currentIndex)
            val parentItem = arrayList.lastOrNull()
            currentDirectory.postValue(parentItem)
            hierarchyDirectory.postValue(arrayList)
        }
    }

    fun moveDirectory(directory: Directory){
        val dirs = hierarchyDirectory.value
            ?:return
        val index = dirs.indexOf(directory)
        val current = dirs.get(index)
        val newDirs = dirs.subList(0, index)
        currentDirectory.postValue(current)
        hierarchyDirectory.postValue(newDirs)
    }


}