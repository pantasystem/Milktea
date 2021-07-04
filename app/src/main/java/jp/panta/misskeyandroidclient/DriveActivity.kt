package jp.panta.misskeyandroidclient

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.composethemeadapter.MdcTheme
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.ui.drive.DriveScreen
import jp.panta.misskeyandroidclient.util.file.toFile
import jp.panta.misskeyandroidclient.view.drive.CreateFolderDialog
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.drive.PathViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveSelectableMode
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

class DriveActivity : AppCompatActivity() {
    companion object{
        //const val EXTRA_IS_FILE_SELECTABLE = "jp.panta.misskeyandroidclient.EXTRA_IS_FILE_SELECTABLE"
        const val EXTRA_INT_SELECTABLE_FILE_MAX_SIZE = "jp.panta.misskeyandroidclient.EXTRA_INT_SELECTABLE_FILE_SIZE"
        const val EXTRA_SELECTED_FILE_PROPERTY_IDS = "jp.panta.misskeyandroiclient.EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID"
        const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"
    }
    enum class Type{
        FOLDER, FILE
    }

    private lateinit var _driveViewModel: DriveViewModel
    @ExperimentalCoroutinesApi
    private lateinit var _fileViewModel: FileViewModel
    private lateinit var _directoryViewModel: DirectoryViewModel

    private var mMenuOpen: MenuItem? = null

    private var mCurrentFragmentType: Type = Type.FOLDER

    //private lateinit var mBinding: ActivityDriveBinding

    @ExperimentalCoroutinesApi
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
        val accountIds = selectedFileIds?.map { it.accountId }?.toSet()
        require(selectedFileIds == null || accountIds!!.size == 1) {
            "選択したFilePropertyの所有者は全て同一のアカウントである必要があります。"
        }
        val miCore = applicationContext as MiCore
        val driveSelectableMode: DriveSelectableMode? = if(intent.action == Intent.ACTION_OPEN_DOCUMENT) {
            val aId = accountId?: accountIds?.lastOrNull()?:  miCore.getCurrentAccount().value?.accountId
            requireNotNull(aId)
            DriveSelectableMode(maxSize, selectedFileIds ?: emptyList(), aId)
        }else{
            null
        }

        _driveViewModel = ViewModelProvider(this, DriveViewModelFactory(driveSelectableMode))[DriveViewModel::class.java]
        _fileViewModel = ViewModelProvider(this, FileViewModelFactory(
            accountId?: accountIds?.lastOrNull(),
            miCore,
            _driveViewModel.driveStore
        ))[FileViewModel::class.java]
        _directoryViewModel = ViewModelProvider(this, DirectoryViewModelFactory(
            accountId?: accountIds?.lastOrNull(), miCore, _driveViewModel.driveStore
        )
        )[DirectoryViewModel::class.java]

        _fileViewModel = ViewModelProvider(this, FileViewModelFactory(
            accountId?: accountIds?.lastOrNull(),
            miCore,
            _driveViewModel.driveStore
        ))[FileViewModel::class.java]

        _directoryViewModel = ViewModelProvider(this, DirectoryViewModelFactory(
            accountId?: accountIds?.lastOrNull(), miCore, _driveViewModel.driveStore
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
                            finish()
                        }else{
                            intent.putExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS, ArrayList(ids))
                            setResult(RESULT_OK, intent)
                        }
                    }
                )
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_drive, menu)
        val openMenu = menu?.findItem(R.id.action_open)
        mMenuOpen = openMenu
        val maxSize = intent.getIntExtra(EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, 0)
        //openMenu?.isCheckable = true
        //openMenu?.isEnabled = false
        openMenu?.isVisible = maxSize > 0

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
            R.id.action_open ->{
                val ids = _driveViewModel.getSelectedFileIds()
                if(ids != null){
                    intent.putExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS, ArrayList(ids))
                    setResult(RESULT_OK, intent)
                    finish()

                }else{
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
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
        _fileViewModel.uploadFile(uri.toFile(this))
    }

    override fun onBackPressed() {
        if(_driveViewModel.pop()) {
            return
        }
        super.onBackPressed()
    }

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<PathViewData>(){
        override fun areContentsTheSame(oldItem: PathViewData, newItem: PathViewData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: PathViewData, newItem: PathViewData): Boolean {
            return oldItem.id == newItem.id

        }
    }

    fun setCurrentFragment(type: Type){
        mCurrentFragmentType = type
        Log.d("DriveActivity", "currentFragmentType:$type")
    }
}
