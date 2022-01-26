package jp.panta.misskeyandroidclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeLifecycleOwner
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.composethemeadapter.MdcTheme
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.ui.drive.DriveScreen
import jp.panta.misskeyandroidclient.util.file.toAppFile
import jp.panta.misskeyandroidclient.ui.drive.CreateFolderDialog
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveSelectableMode
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class DriveActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_INT_SELECTABLE_FILE_MAX_SIZE = "jp.panta.misskeyandroidclient.EXTRA_INT_SELECTABLE_FILE_SIZE"
        const val EXTRA_SELECTED_FILE_PROPERTY_IDS = "jp.panta.misskeyandroiclient.EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID"
        const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"
    }
    enum class Type{
        FILE
    }

    private lateinit var _driveViewModel: DriveViewModel
    @ExperimentalCoroutinesApi
    private lateinit var _fileViewModel: FileViewModel
    private lateinit var _directoryViewModel: DirectoryViewModel



    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setTheme()
        ViewTreeLifecycleOwner.set(window.decorView, this)

        val maxSize = intent.getIntExtra(EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, -1)
        val selectedFileIds = (intent.getSerializableExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS) as? ArrayList<*>)?.map {
            it as FileProperty.Id
        }
        val accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1).let {
            if(it == -1L) null else it
        }
        val accountIds = selectedFileIds?.map { it.accountId }?.distinct()?: emptyList()
        require(selectedFileIds == null || accountIds.size <= 1) {
            "選択したFilePropertyの所有者は全て同一のアカウントである必要があります。ids:${accountIds}"
        }
        val miCore = applicationContext as MiCore
        val driveSelectableMode: DriveSelectableMode? = if(intent.action == Intent.ACTION_OPEN_DOCUMENT) {
            val aId = accountId?: accountIds.lastOrNull()?:  miCore.getCurrentAccount().value?.accountId
            requireNotNull(aId)
            DriveSelectableMode(maxSize, selectedFileIds ?: emptyList(), aId)
        }else{
            null
        }

        _driveViewModel = ViewModelProvider(this, DriveViewModelFactory(driveSelectableMode))[DriveViewModel::class.java]
        _fileViewModel = ViewModelProvider(this, FileViewModelFactory(
            accountId?: accountIds.lastOrNull(),
            miCore,
            _driveViewModel.driveStore
        ))[FileViewModel::class.java]
        _directoryViewModel = ViewModelProvider(this, DirectoryViewModelFactory(
            accountId?: accountIds.lastOrNull(), miCore, _driveViewModel.driveStore
        )
        )[DirectoryViewModel::class.java]

        _fileViewModel = ViewModelProvider(this, FileViewModelFactory(
            accountId?: accountIds.lastOrNull(),
            miCore,
            _driveViewModel.driveStore
        ))[FileViewModel::class.java]

        _directoryViewModel = ViewModelProvider(this, DirectoryViewModelFactory(
            accountId?: accountIds.lastOrNull(), miCore, _driveViewModel.driveStore
        )
        )[DirectoryViewModel::class.java]

        setContent {
            MdcTheme {
                DriveScreen(
                    driveViewModel = _driveViewModel,
                    fileViewModel = _fileViewModel,
                    directoryViewModel = _directoryViewModel,
                    onNavigateUp = { finish() },
                    onFixSelected = {
                        val ids = _driveViewModel.getSelectedFileIds()
                        if(ids.isNullOrEmpty()) {
                            setResult(RESULT_CANCELED)
                        }else{
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



    private fun createDirectoryDialog(){

        CreateFolderDialog().show(supportFragmentManager, "CreateFolder")
    }

    @ExperimentalCoroutinesApi
    private fun showFileManager(){
        if(checkPermissions()){
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            registerForOpenFileActivityResult.launch(intent)
        }else{
            requestPermission()
        }
    }

    private fun checkPermissions(): Boolean{
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    @ExperimentalCoroutinesApi
    private fun requestPermission(){
        if(! checkPermissions()){
            registerForReadExternalStoragePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @ExperimentalCoroutinesApi
    val registerForOpenFileActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if(uri != null) {
            uploadFile(uri)
        }
    }

    @ExperimentalCoroutinesApi
    val registerForReadExternalStoragePermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it) {
            showFileManager()
        }
    }




    @ExperimentalCoroutinesApi
    private fun uploadFile(uri: Uri){
        _fileViewModel.uploadFile(uri.toAppFile(this))
    }

    override fun onBackPressed() {
        if(_driveViewModel.pop()) {
            return
        }
        super.onBackPressed()
    }



}
