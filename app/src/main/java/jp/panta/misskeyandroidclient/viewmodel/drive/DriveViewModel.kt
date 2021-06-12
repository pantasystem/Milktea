package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
import java.util.*

class DriveViewModel(
    val selectableMaxSize: Int
) : ViewModel(){

    val currentDirectory = MutableLiveData<DirectoryViewData?>(DirectoryViewData(null))

    val hierarchyDirectory = MutableLiveData<List<DirectoryViewData>>()

    val openFileEvent = EventBus<FilePropertyDTO>()
    //val selectedFilesMap = HashMap<String, FileViewData>()
    val selectedFilesMapLiveData = if(selectableMaxSize > -1){
        MutableLiveData<Map<String, FileViewData>>()
    }else{
        null
    }


    init{
        val current = currentDirectory.value
        if(current != null){
            hierarchyDirectory.postValue(listOf(current))
        }
    }


    fun getSelectedFileIds(): List<String>?{
        return selectedFilesMapLiveData?.value?.values?.map{
            it.id.fileId
        }
    }

    fun getSelectedFileList(): List<FileProperty>?{
        return selectedFilesMapLiveData?.value?.values?.map{
            it.file
        }?.toList()
    }

    fun setSelectedFileList(files: List<FileProperty>){
        selectedFilesMapLiveData?.postValue(
        files.map{
            Pair<String, FileViewData>(it.id.fileId , FileViewData(it))
        }.toMap())
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

    fun openFile(fileProperty: FilePropertyDTO){
        openFileEvent.event = fileProperty
    }


}