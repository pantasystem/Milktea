@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.media

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.MediaNavigation
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.common_navigation.MediaNavigationKeys
import net.pantasystem.milktea.media.databinding.ActivityMediaBinding
import net.pantasystem.milktea.model.file.AboutMediaType
import net.pantasystem.milktea.model.file.FilePreviewSource
import java.io.Serializable
import javax.inject.Inject

class MediaNavigationImpl @Inject constructor(
    val activity: Activity
) : MediaNavigation {
    override fun newIntent(args: MediaNavigationArgs): Intent {
        return when (args) {
            is MediaNavigationArgs.AFile -> {
                val intent = Intent(activity, MediaActivity::class.java)
                intent.putExtra(MediaNavigationKeys.EXTRA_FILE, when(val f = args.file) {
                    is FilePreviewSource.Local -> f.file.toFile()
                    is FilePreviewSource.Remote -> File.from(f.fileProperty)
                })
            }
            is MediaNavigationArgs.Files -> {
                val intent = Intent(activity, MediaActivity::class.java)
                intent.putExtra(MediaNavigationKeys.EXTRA_FILES, ArrayList(args.files.map {
                    when(val f = it) {
                        is FilePreviewSource.Local -> f.file.toFile()
                        is FilePreviewSource.Remote -> File.from(f.fileProperty)
                    }
                }))
                intent.putExtra(
                    MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX,
                    args.index
                )
            }
        }
    }
}

@AndroidEntryPoint
class MediaActivity : AppCompatActivity() {

    private sealed class Media : Serializable {
        data class FileMedia(val file: File) : Media()
    }


    companion object {


        fun newInstance(activity: FragmentActivity, files: List<File>, index: Int): Intent {
            return Intent(activity, MediaActivity::class.java).apply {
                putExtra(MediaNavigationKeys.EXTRA_FILES, ArrayList(files))
                putExtra(MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX, index)
            }
        }

    }

    private var mCurrentMedia: Media? = null
    private var mMedias: List<Media>? = null
    private lateinit var mBinding: ActivityMediaBinding

    @Inject
    lateinit var setTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_media)
        setSupportActionBar(mBinding.mediaToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val file = intent.getSerializableExtra(MediaNavigationKeys.EXTRA_FILE) as File?

        val files =
            (intent.getSerializableExtra(MediaNavigationKeys.EXTRA_FILES) as List<*>?)?.mapNotNull {
                it as File?
            }


        val fileCurrentIndex = intent.getIntExtra(MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX, 0)
        val list = when {
            files != null && files.isNotEmpty() -> {
                files.map {
                    Media.FileMedia(it)
                }
            }
            file != null -> {
                listOf<Media>(Media.FileMedia(file))
            }
            else -> {
                Log.e(MediaNavigationKeys.TAG, "params must not null")
                throw IllegalArgumentException()
            }
        }

        mMedias = list

        val pagerAdapter = MediaPagerAdapter(list)
        mBinding.mediaViewPager.adapter = pagerAdapter

        mBinding.mediaViewPager.currentItem = fileCurrentIndex
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.download_file -> {
                mCurrentMedia?.let { m ->
                    val file = (m as Media.FileMedia).file
                    downloadFile(file)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_media, menu)
        val media = mCurrentMedia
        if (media is Media.FileMedia) {
            menu.findItem(R.id.download_file)?.isVisible =
                media.file.path?.startsWith("http") == true
        }
        return super.onCreateOptionsMenu(menu)
    }

    fun setCurrentFileIndex(index: Int) {
        try {
            mCurrentMedia = mMedias?.get(index)
            Log.d(MediaNavigationKeys.TAG, "現在の画像:$mCurrentMedia")
        } catch (e: IndexOutOfBoundsException) {
            Log.d(MediaNavigationKeys.TAG, "お探しのメディアは存在しません。", e)
        }
    }

    private fun downloadFile(file: File) {
        Log.d(MediaNavigationKeys.TAG, "ダウンロードを開始します:$file")
        Toast.makeText(
            this,
            getString(R.string.start_downloading_placeholder, file.name),
            Toast.LENGTH_LONG
        ).show()
        WorkManager.getInstance(this)
            .enqueue(
                RemoteFileDownloadWorkManager.createWorkRequest(
                    name = file.name,
                    type = when (file.aboutMediaType) {
                        AboutMediaType.VIDEO -> DownloadContentType.Video
                        AboutMediaType.IMAGE -> DownloadContentType.Image
                        AboutMediaType.SOUND -> DownloadContentType.Audio
                        AboutMediaType.OTHER -> return
                    },
                    url = file.path ?: "",
                )
            )
    }

    private inner class MediaPagerAdapter(
        private val list: List<Media>
    ) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return list.size
        }

        override fun getItem(position: Int): Fragment {
            return when (val item = list[position]) {
                is Media.FileMedia -> createFragment(position, item.file)
            }
        }
    }

    private fun createFragment(index: Int, file: File): Fragment {

        return if (file.type?.contains("image") == true) {
            ImageFragment.newInstance(index, file)
        } else {
            PlayerFragment.newInstance(index, file.path!!)
        }
    }

}
