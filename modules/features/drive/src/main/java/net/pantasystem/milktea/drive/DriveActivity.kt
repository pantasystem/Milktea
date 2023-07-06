package net.pantasystem.milktea.drive

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewTreeLifecycleOwner
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.drive.DriveState
import net.pantasystem.milktea.app_store.drive.DriveStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android.platform.PermissionUtil
import net.pantasystem.milktea.common_navigation.DriveNavigation
import net.pantasystem.milktea.common_navigation.DriveNavigationArgs
import net.pantasystem.milktea.common_navigation.EXTRA_ACCOUNT_ID
import net.pantasystem.milktea.common_navigation.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE
import net.pantasystem.milktea.common_navigation.EXTRA_SELECTED_FILE_PROPERTY_IDS
import net.pantasystem.milktea.drive.viewmodel.DirectoryViewModel
import net.pantasystem.milktea.drive.viewmodel.DriveSelectableMode
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel
import net.pantasystem.milktea.drive.viewmodel.FileViewModel
import net.pantasystem.milktea.drive.viewmodel.provideFactory
import net.pantasystem.milktea.drive.viewmodel.provideViewModel
import net.pantasystem.milktea.model.drive.DirectoryPath
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.SelectedFilePropertyIds
import javax.inject.Inject

class DriveNavigationImpl @Inject constructor(
    val activity: Activity
) : DriveNavigation {

    override fun newIntent(args: DriveNavigationArgs): Intent {
        return Intent(activity, DriveActivity::class.java)
            .putExtra(EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, args.selectableFileMaxSize)
            .putExtra(EXTRA_ACCOUNT_ID, args.accountId)
            .putExtra(
                EXTRA_SELECTED_FILE_PROPERTY_IDS,
                args.selectedFilePropertyIds?.let { ArrayList(it) })
    }
}

@AndroidEntryPoint
class DriveActivity : AppCompatActivity() {



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
            DriveSelectableMode(
                maxSize,
                selectedFileIds ?: emptyList(),
                aId
            )
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

    @Inject
    lateinit var fileViewModelFactory: FileViewModel.AssistedViewModelFactory

    private val _fileViewModel: FileViewModel by viewModels {
        FileViewModel.provideFactory(fileViewModelFactory, driveStore)
    }

    private val _driveViewModel: DriveViewModel by viewModels()

    @Inject
    lateinit var setTheme: ApplyTheme

    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalCoroutinesApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()

        ViewTreeLifecycleOwner.set(window.decorView, this)


        setContent {
            MdcTheme {
                DriveScreen(
                    driveViewModel = _driveViewModel,
                    fileViewModel = _fileViewModel,
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

        onBackPressedDispatcher.addCallback {
            if (!_driveViewModel.pop()) {
                remove()
                onBackPressedDispatcher.onBackPressed()
            }
        }


    }


    private fun createDirectoryDialog() {
        CreateFolderDialog().show(supportFragmentManager, "CreateFolder")
    }

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
        return PermissionUtil.checkReadStoragePermission(this)
    }

    private fun requestPermission() {
        if (!checkPermissions()) {
            if (Build.VERSION.SDK_INT >= 33) {
                requestReadMediasPermissionResult.launch(
                    PermissionUtil.getReadMediaPermissions().toTypedArray()
                )
            } else {
                registerForReadExternalStoragePermissionResult.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private val registerForOpenFileActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                uploadFile(uri)
            }
        }

    private val registerForReadExternalStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showFileManager()
            }
        }

    private val requestReadMediasPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.any { it.value }) {
                showFileManager()
            }
        }

    private fun uploadFile(uri: Uri) {
        _fileViewModel.uploadFile(uri.toAppFile(this))
    }



}
