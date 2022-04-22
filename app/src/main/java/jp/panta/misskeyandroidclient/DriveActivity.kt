package jp.panta.misskeyandroidclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeLifecycleOwner
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.account.AccountStore
import jp.panta.misskeyandroidclient.ui.drive.DriveScreen
import jp.panta.misskeyandroidclient.util.file.toAppFile
import jp.panta.misskeyandroidclient.ui.drive.CreateFolderDialog
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.*
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.file.FileViewModel
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.file.FileViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.model.drive.*
import javax.inject.Inject

@AndroidEntryPoint
class DriveActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_INT_SELECTABLE_FILE_MAX_SIZE =
            "jp.panta.misskeyandroidclient.EXTRA_INT_SELECTABLE_FILE_SIZE"
        const val EXTRA_SELECTED_FILE_PROPERTY_IDS =
            "jp.panta.misskeyandroiclient.EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID"
        const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"
    }

    private lateinit var _driveViewModel: DriveViewModel

    @ExperimentalCoroutinesApi
    private lateinit var _fileViewModel: FileViewModel


    @Inject
    lateinit var accountStore: AccountStore

    private val accountId: Long? by lazy {
        intent.getLongExtra(EXTRA_ACCOUNT_ID, -1).let {
            if (it == -1L) null else it
        }
    }


    private val selectedFileIds: List<FileProperty.Id>? by lazy {
        (intent.getSerializableExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS) as? ArrayList<*>)?.map {
            it as FileProperty.Id
        }
    }

    private val accountIds: List<Long> by lazy {
        val accountIds = selectedFileIds?.map { it.accountId }?.distinct() ?: emptyList()
        require(selectedFileIds == null || accountIds.size <= 1) {
            "選択したFilePropertyの所有者は全て同一のアカウントである必要があります。ids:${accountIds}"
        }
        accountIds
    }


    private val driveSelectableMode: DriveSelectableMode? by lazy {

        val maxSize = intent.getIntExtra(EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, -1)
        if (intent.action == Intent.ACTION_OPEN_DOCUMENT) {
            val aId = accountId ?: accountIds.lastOrNull() ?: accountStore.currentAccountId
            requireNotNull(aId)
            DriveSelectableMode(maxSize, selectedFileIds ?: emptyList(), aId)
        } else {
            null
        }
    }

    private val driveStore: DriveStore by lazy {
        val selectable = driveSelectableMode
        DriveStore(DriveState(
            accountId = selectable?.accountId,
            path = DirectoryPath(emptyList()),
            selectedFilePropertyIds = selectable?.let {
                SelectedFilePropertyIds(
                    selectableMaxCount = it.selectableMaxSize,
                    selectedIds = it.selectedFilePropertyIds.toSet()
                )
            }
        ))
    }
    @Inject
    lateinit var directoryViewModelFactory: DirectoryViewModel.ViewModelAssistedFactory
    private val _directoryViewModel: DirectoryViewModel by viewModels {
        DirectoryViewModel.provideViewModel(directoryViewModelFactory, driveStore)
    }



    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalCoroutinesApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setTheme()
        ViewTreeLifecycleOwner.set(window.decorView, this)



        val miCore = applicationContext as MiCore


        _driveViewModel = ViewModelProvider(
            this,
            DriveViewModelFactory(driveSelectableMode)
        )[DriveViewModel::class.java]
        _fileViewModel = ViewModelProvider(
            this, FileViewModelFactory(
                accountId ?: accountIds.lastOrNull(),
                miCore,
                driveStore
            )
        )[FileViewModel::class.java]

        _fileViewModel = ViewModelProvider(
            this, FileViewModelFactory(
                accountId ?: accountIds.lastOrNull(),
                miCore,
                driveStore
            )
        )[FileViewModel::class.java]



        setContent {
            MdcTheme {
                DriveScreen(
                    driveViewModel = _driveViewModel,
                    fileViewModel = _fileViewModel,
                    directoryViewModel = _directoryViewModel,
                    onNavigateUp = { finish() },
                    onFixSelected = {
                        val ids = _driveViewModel.getSelectedFileIds()
                        if (ids.isNullOrEmpty()) {
                            setResult(RESULT_CANCELED)
                        } else {
                            intent.putExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS, ArrayList(ids))
                            setResult(RESULT_OK, intent)
                        }
                        finish()

                    },
                    onShowLocalFilePicker = {
                        showFileManager()
                    },
                    onShowCreateDirectoryEditor = {
                        createDirectoryDialog()
                    }
                )
            }
        }


    }


    private fun createDirectoryDialog() {

        CreateFolderDialog().show(supportFragmentManager, "CreateFolder")
    }

    @ExperimentalCoroutinesApi
    private fun showFileManager() {
        if (checkPermissions()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            registerForOpenFileActivityResult.launch(intent)
        } else {
            requestPermission()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    @ExperimentalCoroutinesApi
    private fun requestPermission() {
        if (!checkPermissions()) {
            registerForReadExternalStoragePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @ExperimentalCoroutinesApi
    val registerForOpenFileActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                uploadFile(uri)
            }
        }

    @ExperimentalCoroutinesApi
    val registerForReadExternalStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showFileManager()
            }
        }


    @ExperimentalCoroutinesApi
    private fun uploadFile(uri: Uri) {
        _fileViewModel.uploadFile(uri.toAppFile(this))
    }

    override fun onBackPressed() {
        if (_driveViewModel.pop()) {
            return
        }
        super.onBackPressed()
    }


}
