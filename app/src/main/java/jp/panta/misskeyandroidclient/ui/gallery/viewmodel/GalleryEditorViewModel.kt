package jp.panta.misskeyandroidclient.ui.gallery.viewmodel

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.model.CreateGalleryTaskExecutor
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.account.AccountRepository
import net.pantasystem.milktea.data.model.drive.DriveFileRepository
import net.pantasystem.milktea.data.model.drive.FileProperty
import net.pantasystem.milktea.data.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.data.model.file.AppFile
import net.pantasystem.milktea.data.model.gallery.CreateGalleryPost
import net.pantasystem.milktea.data.model.gallery.GalleryPost
import net.pantasystem.milktea.data.model.gallery.GalleryRepository
import net.pantasystem.milktea.data.model.gallery.toTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Serializable


sealed class EditType : Serializable{
    data class Update(
        val postId: GalleryPost.Id
    ) : EditType()

    data class Create(
        val accountId: Long?
    ) : EditType()
}

class GalleryEditorViewModel @AssistedInject constructor(
    private val galleryRepository: GalleryRepository,
    val filePropertyDataSource: FilePropertyDataSource,
    val accountRepository: AccountRepository,
    private val taskExecutor: CreateGalleryTaskExecutor,
    private val driveFileRepository: DriveFileRepository,
    loggerFactory: net.pantasystem.milktea.common.Logger.Factory,
    @Assisted private val editType: EditType,
    ) : ViewModel(){

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(type: EditType): GalleryEditorViewModel
    }
    companion object

    val logger = loggerFactory.create("GalleryEditorVM")

    private val _title = MutableLiveData<String>()
    val title = _title

    private val _description = MutableLiveData<String>()
    val description = _description

    private val _pickedImages = MutableLiveData<List<AppFile>>()
    val pickedImages: LiveData<List<AppFile>> = _pickedImages

    val isSensitive = MutableLiveData(false)


    init {
        viewModelScope.launch(Dispatchers.IO) {
            if(editType is EditType.Update) {
                runCatching {
                    fetchWithApply(editType.postId)
                }.onFailure {
                    logger.debug("Update用のGalleryPostの取得に失敗した", e = it)
                }

            }
        }
    }

    private suspend fun fetchWithApply(postId: GalleryPost.Id) {
        val galleryPost = galleryRepository.find(postId)
        _title.postValue(galleryPost.title)
        _description.postValue(galleryPost.description)
        val files = filePropertyDataSource.findIn(galleryPost.fileIds)

        _pickedImages.postValue(
            files.map {
                AppFile.Remote(it.id)
            }
        )
    }

    fun detach(file: AppFile) {
        _pickedImages.value = (_pickedImages.value?: emptyList()).filterNot {
            it == file
        }
    }

    fun toggleSensitive(file: AppFile) {
        when(file) {
            is AppFile.Local -> {
                _pickedImages.value = _pickedImages.value?.map {
                    if(it === file) {
                        it.copy(isSensitive = !file.isSensitive)
                    }else{
                        it
                    }
                }
            }
            is AppFile.Remote -> {
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        driveFileRepository.toggleNsfw(file.id)
                    }.onFailure {
                        logger.info("sensitiveの切り替えに失敗しました。", e = it)
                    }
                }
            }
        }
    }


    fun addFilePropertyIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = filePropertyDataSource.findIn(ids)

            val list = (_pickedImages.value?: emptyList()).toMutableList().also { list ->
                list.addAll(
                    files.map {
                        AppFile.Remote(it.id)
                    }
                )
            }
            _pickedImages.postValue(list)
        }
    }

    fun addFile(file: AppFile) {
        _pickedImages.value = (_pickedImages.value?: emptyList()).toMutableList().also { mutable ->
            mutable.add(file)
        }
    }

    fun validate() : Boolean {
        logger.debug("title:${this.title.value}, images:${pickedImages.value}")
        return this.pickedImages.value?.isNotEmpty() == true && this.title.value?.isNotBlank() == true
    }
    suspend fun save(){
        val files = this.pickedImages.value?: emptyList()
        val title = this.title.value?: ""
        val description = this.description.value?: ""
        val isSensitive = this.isSensitive.value?: false
        if(validate()) {
            val create = CreateGalleryPost(
                title,
                getAccount(),
                files,
                description,
                isSensitive
            )
            taskExecutor.dispatch(create.toTask(galleryRepository))
        }

    }

    private var _accountId: Long? = null
    private val _accountLock = Mutex()
    private suspend fun getAccount() : Account{
        _accountLock.withLock {
            if(_accountId == null) {
                return accountRepository.getCurrentAccount().also {
                    _accountId = it.accountId
                }
            }
            return accountRepository.get(_accountId!!)
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun GalleryEditorViewModel.Companion.provideFactory(
    assistedFactory: GalleryEditorViewModel.ViewModelAssistedFactory,
    type: EditType,
) : ViewModelProvider.Factory = object  : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(type) as T
    }
}