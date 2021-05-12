package jp.panta.misskeyandroidclient

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import jp.panta.misskeyandroidclient.databinding.ActivityMediaBinding
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.view.media.ImageFragment
import jp.panta.misskeyandroidclient.view.media.PlayerFragment
import java.io.Serializable

class MediaActivity : AppCompatActivity() {

    private sealed class Media : Serializable{
        data class UriMedia(val uri: Uri): Media()
        data class FilePropertyMedia(val fileProperty: FileProperty) : Media()
        data class FileMedia(val file: File): Media()
    }


    companion object{
        const val TAG = "MediaActivity"
        const val EXTRA_URI = "jp.panta.misskeyadnroidclient.MediaActivity.EXTRA_URI"
        const val EXTRA_FILE = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_FILE"
        const val EXTRA_FILES = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_FILES"
        const val EXTRA_FILE_CURRENT_INDEX = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_FILES_CURRENT_INDEX"




        fun newIntent(activity: AppCompatActivity, file: File) : Intent{
            return Intent(activity, MediaActivity::class.java).apply{
                putExtra(EXTRA_FILE, file)
            }
        }

        fun newInstance(activity: AppCompatActivity, files: List<File>, index: Int) : Intent{
            return Intent(activity, MediaActivity::class.java).apply{
                putExtra(EXTRA_FILES, ArrayList(files))
                putExtra(EXTRA_FILE_CURRENT_INDEX, index)
            }
        }

    }

    private var mCurrentMedia: Media? = null
    private var mMedias: List<Media>? = null
    private lateinit var mBinding: ActivityMediaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_media)
        setSupportActionBar(mBinding.mediaToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val file = intent.getSerializableExtra(EXTRA_FILE) as File?


        val files = (intent.getSerializableExtra(EXTRA_FILES) as List<*>?)?.mapNotNull {
            it as File?
        }


        val fileCurrentIndex = intent.getIntExtra(EXTRA_FILE_CURRENT_INDEX, 0)


        val extraUri: String? = intent.getStringExtra(EXTRA_URI)
        val uri = if(extraUri.isNullOrBlank()) null else Uri.parse(extraUri)

        val list = when{

            files != null && files.isNotEmpty() ->{
                files.map{
                    Media.FileMedia(it)
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

        mMedias = list

        val pagerAdapter = MediaPagerAdapter(list)
        mBinding.mediaViewPager.adapter = pagerAdapter

        mBinding.mediaViewPager.currentItem = fileCurrentIndex
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
            R.id.download_file ->{
                mCurrentMedia?.let{ m ->
                    val file = if(m is Media.FileMedia){
                        m.file
                    }else if(m is Media.FilePropertyMedia){
                        m.fileProperty.toFile()
                    }else{
                        null
                    }?: return false
                    downloadFile(file)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let{ m ->
            menuInflater.inflate(R.menu.menu_media, m)
        }
        val media = mCurrentMedia
        if(media is Media.FileMedia){
            menu?.findItem(R.id.download_file)?.isVisible = media.file.path.startsWith("http")
        }
        return super.onCreateOptionsMenu(menu)
    }

    fun setCurrentFileIndex(index: Int){
        try{
            mCurrentMedia = mMedias?.get(index)
            Log.d(TAG, "現在の画像:$mCurrentMedia")
        }catch(e: IndexOutOfBoundsException){
            Log.d(TAG, "お探しのメディアは存在しません。", e)
        }
    }

    private fun downloadFile(file: File){
        Log.d(TAG, "ダウンロードを開始します:$file")
        Toast.makeText(this, String.format(getString(R.string.start_downloading_placeholder, file.name)), Toast.LENGTH_LONG).show()
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        val uri = Uri.parse(file.path)

        val request = DownloadManager.Request(uri)
            .setMimeType(file.type)
            .setTitle(file.name)
            //.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, file.name)

            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val id = downloadManager.enqueue(request)

        Log.d(TAG, getStatus(downloadManager, id))

    }

    private inner class MediaPagerAdapter(
        private val list: List<Media>
    ) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Fragment {
            return when (val item = list[position]) {
                is Media.FilePropertyMedia -> createFragment(position, item.fileProperty, null, null)
                is Media.UriMedia -> createFragment(position, null, item.uri, null)
                is Media.FileMedia -> createFragment(position,null, null, item.file)
            }
        }
    }

    private fun createFragment(index: Int, fileProperty: FileProperty?, uri: Uri?, file: File?): Fragment{
        if(fileProperty != null){
            return if(fileProperty.type.contains("image")){
                ImageFragment.newInstance(index, fileProperty.url)
            }else{
                PlayerFragment.newInstance(index, fileProperty.url)
            }
        }
        if(uri != null){
            return if(contentResolver.getType(uri)?.contains("image") == true){
                ImageFragment.newInstance(index, uri)
            }else{
                PlayerFragment.newInstance(index, uri)
            }
        }
        if(file != null){
            return if(file.type?.contains("image") == true){
                ImageFragment.newInstance(index, file)
            }else{
                PlayerFragment.newInstance(index, file.path)
            }
        }
        throw NullPointerException("fileProperty xor uri must not null")
    }

    private fun getStatus(downloadManager: DownloadManager, id: Long): String {
        val query: DownloadManager.Query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        if (!cursor.moveToFirst()) {
            Log.e(TAG, "Empty row")
            return "Wrong downloadId"
        }

        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnIndex)
        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(columnReason)
        val statusText: String

        statusText = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Successful"
            DownloadManager.STATUS_FAILED -> {
                "Failed: $reason"
            }
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Running"
            DownloadManager.STATUS_PAUSED-> {
                "Paused: $reason"
            }
            else -> "Unknown"
        }

        return statusText
    }
}
