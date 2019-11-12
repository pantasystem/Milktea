package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
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
    }

    private var mViewModel: DriveViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)

        setSupportActionBar(driveToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dirListView.layoutManager = layoutManager

        val maxSize = intent.getIntExtra(EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, 4)

        if(maxSize > 0){
            supportActionBar?.title = "ファイルを選択"
        }else{
            supportActionBar?.title = "ドライブ"
        }

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            val viewModel = ViewModelProvider(this, DriveViewModelFactory(it, miApplication, maxSize)).get(DriveViewModel::class.java)
            mViewModel = viewModel

            val adapter = DirListAdapter(diffUtilItemCallback, viewModel)
            dirListView.adapter = adapter
            viewModel.hierarchyDirectory.observe(this, Observer {dir ->
                Log.d("DriveActivity", "更新がありました: $dir")
                adapter.submitList(dir)
            })

            viewModel.selectedFilesMapLiveData?.observe(this, Observer{selected ->
                supportActionBar?.title = "選択済み ${selected.size}/${maxSize}"
            })
        })

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.content_main, DriveFragment())
            ft.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
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
