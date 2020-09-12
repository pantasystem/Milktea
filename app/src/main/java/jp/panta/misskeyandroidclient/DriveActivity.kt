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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.util.file.toFile
import jp.panta.misskeyandroidclient.view.drive.CreateFolderDialog
import jp.panta.misskeyandroidclient.view.drive.DirListAdapter
import jp.panta.misskeyandroidclient.view.drive.DriveFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.drive.Directory
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewModelFactory
import kotlinx.android.synthetic.main.activity_drive.*

class DriveActivity : AppCompatActivity() {
    companion object{
        //const val EXTRA_IS_FILE_SELECTABLE = "jp.panta.misskeyandroidclient.EXTRA_IS_FILE_SELECTABLE"
        const val EXTRA_INT_SELECTABLE_FILE_MAX_SIZE = "jp.panta.misskeyandroidclient.EXTRA_INT_SELECTABLE_FILE_SIZE"
        const val EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID = "jp.panta.misskeyandroiclient.EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID"
        const val EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE = "jp.panta.misskeyandroiclient.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE"

        private const val OPEN_DOCUMENT_RESULT_CODE = 113
        private const val READ_STORAGE_PERMISSION_REQUEST_CODE = 112
    }
    enum class Type{
        FOLDER, FILE
    }

    private var mDriveViewModel: DriveViewModel? = null
    private var mFileViewModel: FileViewModel? = null
    private var mFolderViewModel: FolderViewModel? = null

    private var mMenuOpen: MenuItem? = null

    private var mCurrentFragmentType: Type = Type.FOLDER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_drive)

        setSupportActionBar(driveToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dirListView.layoutManager = layoutManager

        val maxSize = intent.getIntExtra(EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, -1)
        val selectedItem = (intent.getSerializableExtra(EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE) as List<*>?)?.map{
            it as FileProperty
        }

        if(maxSize > -1){
            supportActionBar?.title = getString(R.string.select_file)
        }else{
            supportActionBar?.title = getString(R.string.drive)
        }

        val miApplication = applicationContext as MiApplication
        miApplication.mCurrentAccount.observe(this, Observer {
            val driveViewModel = ViewModelProvider(this, DriveViewModelFactory(maxSize)).get(DriveViewModel::class.java)
            mDriveViewModel = driveViewModel
            mFileViewModel = ViewModelProvider(this, FileViewModelFactory(
                it,
                miApplication,
                driveViewModel.selectedFilesMapLiveData,
                maxSelectableItemSize = driveViewModel.selectableMaxSize,
                folderId = null)
            )[FileViewModel::class.java]
            mFolderViewModel = ViewModelProvider(this, FolderViewModelFactory(
                it, miApplication, null
            ))[FolderViewModel::class.java]

            if(selectedItem != null){
                driveViewModel.setSelectedFileList(selectedItem)
            }
            val adapter = DirListAdapter(diffUtilItemCallback, driveViewModel)
            dirListView.adapter = adapter
            driveViewModel.hierarchyDirectory.observe(this, Observer { dir ->
                Log.d("DriveActivity", "更新がありました: $dir")
                adapter.submitList(dir)
            })

            driveViewModel.selectedFilesMapLiveData?.observe(this, Observer{ selected ->
                supportActionBar?.title = "${getString(R.string.selected)} ${selected.size}/${maxSize}"
                mMenuOpen?.isEnabled = selected.isNotEmpty() && selected.size <= maxSize
            })

            driveViewModel.openFileEvent.observe(this, Observer {
                // TODO ファイルの詳細を開く
            })
        })

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.content_main, DriveFragment())
            ft.commit()
        }

        addItemButton.setOnClickListener {
            if(mCurrentFragmentType == Type.FILE){
                showFileManager()
            }else{
                createDirectoryDialog()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
            R.id.action_open ->{
                val ids = mDriveViewModel?.getSelectedFileIds()
                val files = mDriveViewModel?.getSelectedFileList()
                if(ids != null && files != null){
                    intent.putStringArrayListExtra(EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID, ArrayList(ids))
                    intent.putExtra(EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE, ArrayList<FileProperty>(files))
                    setResult(RESULT_OK, intent)
                    finish()

                }else{
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
        item?: return false
        return super.onOptionsItemSelected(item)
    }

    private fun createDirectoryDialog(){

        CreateFolderDialog().show(supportFragmentManager, "CreateFolder")
    }

    private fun showFileManager(){
        if(checkPermissions()){
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, OPEN_DOCUMENT_RESULT_CODE)
        }else{
            requestPermission()
        }
    }

    private fun checkPermissions(): Boolean{
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        if(! checkPermissions()){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == OPEN_DOCUMENT_RESULT_CODE){
            if(resultCode == RESULT_OK){
                data?.data?.let{ uri ->
                    uploadFile(uri)
                }
            }
        }
        if(requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK){
            showFileManager()
        }
    }

    private fun uploadFile(uri: Uri){
        val miCore = application as MiCore
        miCore.currentAccount.value?.getCurrentConnectionInformation()?.let{ ci ->
            val uploader = OkHttpDriveFileUploader(this, ci, GsonFactory.create(), miCore.getEncryption())
            mFileViewModel?.uploadFile(uri.toFile(this), uploader)
        }

    }

    override fun onBackPressed() {
        val size = mDriveViewModel?.hierarchyDirectory?.value?.size
        if(size != null && size > 1){
            mDriveViewModel?.moveParentDirectory()
            return
        }
        mDriveViewModel
        super.onBackPressed()
    }

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<Directory>(){
        override fun areContentsTheSame(oldItem: Directory, newItem: Directory): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Directory, newItem: Directory): Boolean {
            return oldItem.id == newItem.id

        }
    }

    fun setCurrentFragment(type: Type){
        mCurrentFragmentType = type
        Log.d("DriveActivity", "currentFragmentType:$type")
    }
}
