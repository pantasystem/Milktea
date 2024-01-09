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
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android.platform.PermissionUtil
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.DriveNavigation
import net.pantasystem.milktea.common_navigation.DriveNavigationArgs
import net.pantasystem.milktea.common_navigation.EXTRA_ACCOUNT_ID
import net.pantasystem.milktea.common_navigation.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE
import net.pantasystem.milktea.common_navigation.EXTRA_SELECTED_FILE_PROPERTY_IDS
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel
import net.pantasystem.milktea.model.setting.LocalConfigRepository
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


    private val _driveViewModel: DriveViewModel by viewModels()

    @Inject
    lateinit var setTheme: ApplyTheme

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalCoroutinesApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()



        setContent {
            MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                DriveScreen(
                    driveViewModel = _driveViewModel,
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
        CreateFolderDialog().show(supportFragmentManager, CreateFolderDialog.FRAGMENT_TAG)
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
        _driveViewModel.uploadFile(uri)
    }



}
