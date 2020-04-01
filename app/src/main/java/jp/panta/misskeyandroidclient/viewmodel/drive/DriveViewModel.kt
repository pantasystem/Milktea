package jp.panta.misskeyandroidclient.viewmodel.drive

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.drive.UploadFile
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DriveViewModel(
    val selectableMaxSize: Int
) : ViewModel(){

    val currentDirectory = MutableLiveData<Directory>(Directory(null))

    val hierarchyDirectory = MutableLiveData<List<Directory>>()

    val openFileEvent = EventBus<FileProperty>()
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
            it.id
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
            Pair<String, FileViewData>(it.id , FileViewData(it))
        }.toMap())
    }

    fun moveChildDirectory(childDirectory: FolderViewData){
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
        val newDirs = dirs.subList(0, index + 1)
        currentDirectory.postValue(current)
        hierarchyDirectory.postValue(newDirs)
    }

    fun openFile(fileProperty: FileProperty){
        openFileEvent.event = fileProperty
    }

    fun uploadFile(uploadFile: UploadFile, fileUploader: FileUploader){
        uploadFile.folderId = currentDirectory.value?.id
        viewModelScope.launch(Dispatchers.IO) {
            try{
                fileUploader.upload(uploadFile)
            }catch(e: Exception){
                Log.d("DriveViewModel", "ファイルアップロードに失敗した")
            }
        }
    }

    fun createFolder(folderName: String){

    }


}