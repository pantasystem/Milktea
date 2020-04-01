package jp.panta.misskeyandroidclient

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.view.drive.DirListAdapter
import jp.panta.misskeyandroidclient.view.drive.DriveFragment
import jp.panta.misskeyandroidclient.viewmodel.drive.Directory
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import kotlinx.android.synthetic.main.activity_drive.*

class DriveActivity : AppCompatActivity() {
    companion object{
        //const val EXTRA_IS_FILE_SELECTABLE = "jp.panta.misskeyandroidclient.EXTRA_IS_FILE_SELECTABLE"
        const val EXTRA_INT_SELECTABLE_FILE_MAX_SIZE = "jp.panta.misskeyandroidclient.EXTRA_INT_SELECTABLE_FILE_SIZE"
        const val EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID = "jp.panta.misskeyandroiclient.EXTRA_STRING_ARRAY_LIST_SELECTED_FILES_ID"
        const val EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE = "jp.panta.misskeyandroiclient.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE"
    }

    private var mViewModel: DriveViewModel? = null
    private var mMenuOpen: MenuItem? = null

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
        miApplication.currentAccount.observe(this, Observer {
            val viewModel = ViewModelProvider(this, DriveViewModelFactory(maxSize)).get(DriveViewModel::class.java)
            mViewModel = viewModel

            if(selectedItem != null){
                viewModel.setSelectedFileList(selectedItem)
            }
            val adapter = DirListAdapter(diffUtilItemCallback, viewModel)
            dirListView.adapter = adapter
            viewModel.hierarchyDirectory.observe(this, Observer {dir ->
                Log.d("DriveActivity", "更新がありました: $dir")
                adapter.submitList(dir)
            })

            viewModel.selectedFilesMapLiveData?.observe(this, Observer{selected ->
                supportActionBar?.title = "選択済み ${selected.size}/${maxSize}"
                mMenuOpen?.isEnabled = selected.isNotEmpty() && selected.size <= maxSize
            })
        })

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.content_main, DriveFragment())
            ft.commit()
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
                val ids = mViewModel?.getSelectedFileIds()
                val files = mViewModel?.getSelectedFileList()
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
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val size = mViewModel?.hierarchyDirectory?.value?.size
        if(size != null && size > 1){
            mViewModel?.moveParentDirectory()
            return
        }
        mViewModel
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
}
