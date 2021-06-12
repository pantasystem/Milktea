package jp.panta.misskeyandroidclient.viewmodel.gallery

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.TaskExecutor
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.gallery.CreateGalleryPost
import jp.panta.misskeyandroidclient.model.gallery.GalleryPost
import jp.panta.misskeyandroidclient.model.gallery.GalleryRepository
import jp.panta.misskeyandroidclient.model.gallery.toTask
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener
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

class GalleryEditorViewModel(
    val editType: EditType,
    val galleryRepository: GalleryRepository,
    val filePropertyDataSource: FilePropertyDataSource,
    val accountRepository: AccountRepository,
    val taskExecutor: TaskExecutor,
    val logger: Logger,
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val editType: EditType, val miCore: MiCore) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GalleryEditorViewModel(
                editType,
                miCore.getGalleryRepository(),
                miCore.getFilePropertyDataSource(),
                miCore.getAccountRepository(),
                miCore.getTaskExecutor(),
                miCore.loggerFactory.create("GalleryEditorVM")
            ) as T
        }
    }

    private val _title = MutableLiveData<String>()
    val title = _title

    private val _description = MutableLiveData<String>()
    val description = _description

    private val _pickedImages = MutableLiveData<List<File>>()
    val pickedImages: LiveData<List<File>> = _pickedImages

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
                it.toFile()
            }
        )
    }

    fun detach(file: File) {
        _pickedImages.value = (_pickedImages.value?: emptyList()).filterNot {
            it == file
        }
    }

    fun addFileProperties(list: List<FileProperty>) {
        _pickedImages.value = (_pickedImages.value?: emptyList()).toMutableList().also { mutable ->
            mutable.addAll(
                list.map {
                    it.toFile()
                }
            )
        }
    }

    fun addFile(file: File) {
        _pickedImages.value = (_pickedImages.value?: emptyList()).toMutableList().also { mutable ->
            mutable.add(file)
        }
    }

    fun validate() : Boolean {
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