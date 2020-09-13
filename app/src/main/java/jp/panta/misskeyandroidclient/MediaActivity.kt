package jp.panta.misskeyandroidclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.view.media.ImageFragment
import jp.panta.misskeyandroidclient.view.media.PlayerFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.android.synthetic.main.activity_media.*
import java.io.Serializable

class MediaActivity : AppCompatActivity() {

    private sealed class Media : Serializable{
        data class UriMedia(val uri: Uri): Media()
        data class FilePropertyMedia(val fileProperty: FileProperty) : Media()
        data class FileMedia(val file: File): Media()
    }


    companion object{
        const val TAG = "MediaActivity"
        const val EXTRA_FILE_PROPERTY = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_FILE_PROPERTY"
        const val EXTRA_FILE_PROPERTY_LIST = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_FILE_PROPERTY_LIST"
        const val EXTRA_FILE_PROPERTY_LIST_CURRENT_INDEX = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_INT_FILE_PROPERTY_LIST_CURRENT_INDEX"
        const val EXTRA_URI = "jp.panta.misskeyadnroidclient.MediaActivity.EXTRA_URI"
        const val EXTRA_FILE = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_FILE"

        fun newIntent(activity: AppCompatActivity, list: ArrayList<FileProperty>, currentIndex: Int) : Intent{
            return Intent(activity, MediaActivity::class.java).apply{
                putExtra(EXTRA_FILE_PROPERTY_LIST, list)
                putExtra(EXTRA_FILE_PROPERTY_LIST_CURRENT_INDEX, currentIndex)
            }

        }

        fun newIntent(activity: AppCompatActivity, fileProperty: FileProperty) : Intent{
            return Intent(activity, MediaActivity::class.java).apply{
                putExtra(EXTRA_FILE_PROPERTY, fileProperty)
            }
        }

        fun newIntent(activity: AppCompatActivity, uri: Uri) : Intent{
            return Intent(activity, MediaActivity::class.java).apply{
                putExtra(EXTRA_URI, uri.toString())
            }
        }

        fun newIntent(activity: AppCompatActivity, file: File) : Intent{
            return Intent(activity, MediaActivity::class.java).apply{
                putExtra(EXTRA_FILE, file)
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_media)
        setSupportActionBar(mediaToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fileProperty = intent.getSerializableExtra(EXTRA_FILE_PROPERTY) as FileProperty?

        val filePropertyList = (intent.getSerializableExtra(EXTRA_FILE_PROPERTY_LIST) as List<*>?)?.map{
            it as FileProperty?
        }?.filterNotNull()

        val filePropertyListCurrentIndex = intent.getIntExtra(EXTRA_FILE_PROPERTY_LIST_CURRENT_INDEX, 0)

        val file = intent.getSerializableExtra(EXTRA_FILE) as? File

        val extraUri: String? = intent.getStringExtra(EXTRA_URI)
        val uri = if(extraUri.isNullOrBlank()) null else Uri.parse(extraUri)

        val list = when{
            fileProperty != null ->{
                listOf<Media>(Media.FilePropertyMedia(fileProperty))
            }
            filePropertyList != null && filePropertyList.isNotEmpty() ->{
                filePropertyList.map{
                    Media.FilePropertyMedia(it)
                }
            }
            uri != null ->{
                listOf<Media>(Media.UriMedia(uri))
            }
            file != null ->{
                listOf<Media>(Media.FileMedia(file))
            }
            else ->{
                Log.e(TAG, "params must not null")
                throw IllegalArgumentException()
            }
        }

        val pagerAdapter = MediaPagerAdapter(list)
        mediaViewPager.adapter = pagerAdapter

        mediaViewPager.currentItem = filePropertyListCurrentIndex
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private inner class MediaPagerAdapter(
        private val list: List<Media>
    ) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Fragment {
            return when (val item = list[position]) {
                is Media.FilePropertyMedia -> createFragment(item.fileProperty, null, null)
                is Media.UriMedia -> createFragment(null, item.uri, null)
                is Media.FileMedia -> createFragment(null, null, item.file)
            }
        }
    }

    private fun createFragment(fileProperty: FileProperty?, uri: Uri?, file: File?): Fragment{
        val miCore = applicationContext as? MiCore
        val baseUrl = miCore?.getCurrentAccount()?.value?.instanceDomain?: ""
        if(fileProperty != null){
            return if(fileProperty.type?.contains("image") == true){
                ImageFragment.newInstance(fileProperty.getUrl(baseUrl))
            }else{
                PlayerFragment.newInstance(fileProperty.getUrl(baseUrl))
            }
        }
        if(uri != null){
            return if(contentResolver.getType(uri)?.contains("image") == true){
                ImageFragment.newInstance(uri)
            }else{
                PlayerFragment.newInstance(uri)
            }
        }
        if(file != null){
            return if(file.type?.contains("image") == true){
                ImageFragment.newInstance(file)
            }else{
                PlayerFragment.newInstance(file.path)
            }
        }
        throw NullPointerException("fileProperty xor uri must not null")
    }
}
