package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.SelectedFilePropertyIds
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
import java.util.*

class DriveViewModel(
    val selectableMaxSize: Int,
    val selectedFilePropertyIds: SelectedFilePropertyIds
) : ViewModel(){

    val currentDirectory = MutableLiveData<DirectoryViewData?>(DirectoryViewData(null))

    val hierarchyDirectory = MutableLiveData<List<DirectoryViewData>>()

    val openFileEvent = EventBus<FileProperty>()
    //val selectedFilesMap = HashMap<String, FileViewData>()


    private val _selectedFileIds = MutableLiveData<Set<FileProperty.Id>>()
    val selectedFileIds = _selectedFileIds as LiveData<Set<FileProperty.Id>>


    init{
        val current = currentDirectory.value
        if(current != null){
            hierarchyDirectory.postValue(listOf(current))
        }
    }


    fun getSelectedFileIds(): Set<FileProperty.Id>?{
        return selectedFileIds.value
    }






    fun setSelectedFileIds(fileIds: List<FileProperty.Id>) {
        _selectedFileIds.postValue(fileIds.toSet())
    }

    fun moveChildDirectory(childDirectory: FolderViewData){
        val current = DirectoryViewData(childDirectory.folderProperty)
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
        val arrayList = ArrayList<DirectoryViewData>(list)
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

    fun moveDirectory(directory: DirectoryViewData){
        val dirs = hierarchyDirectory.value
            ?:return
        val index = dirs.indexOf(directory)
        val current = dirs.get(index)
        val newDirs = dirs.subList(0, index + 1)
        currentDirectory.postValue(current)
        hierarchyDirectory.postValue(newDirs)
    }

    fun openFile(fileProperty: FileProperty){
        openFileEvent.event = fileProperty
    }


}