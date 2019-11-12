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

    private var mViewModel: DriveViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)

        setSupportActionBar(driveToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dirListView.layoutManager = layoutManager

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            val viewModel = ViewModelProvider(this, DriveViewModelFactory(it, miApplication)).get(DriveViewModel::class.java)
            mViewModel = viewModel

            val adapter = DirListAdapter(diffUtilItemCallback, viewModel)
            viewModel.hierarchyDirectory.observe(this, Observer {dir ->
                Log.d("DriveActivity", "更新がありました: $dir")
                adapter.submitList(dir)
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
