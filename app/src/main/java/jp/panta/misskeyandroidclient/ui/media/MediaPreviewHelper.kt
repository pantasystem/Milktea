@file:Suppress("unused")

package jp.panta.misskeyandroidclient.ui.media

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.notes.view.media.PreviewAbleFileListAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.MediaViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.PreviewAbleFile
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import net.pantasystem.milktea.common_navigation.MediaNavigationKeys
import net.pantasystem.milktea.media.MediaActivity
import net.pantasystem.milktea.model.file.File

object MediaPreviewHelper {


    @BindingAdapter("thumbnailView", "playButton", "fileViewData", "fileViewDataList")
    @JvmStatic
    fun FrameLayout.setClickWhenShowMediaActivityListener(
        thumbnailView: ImageView,
        playButton: ImageButton,
        fileViewData: FileViewData?,
        fileViewDataList: List<FileViewData>?
    ) {
        //setPreview(thumbnailView, playButton, fileViewData?.file)
        fileViewData ?: return

        if (fileViewDataList.isNullOrEmpty()) {
            return
        }
        val listener = View.OnClickListener {
            val context = it.context
            val intent = Intent(context, MediaActivity::class.java)
            intent.putExtra(MediaNavigationKeys.EXTRA_FILES, ArrayList(fileViewDataList.map { fvd ->
                fvd.file
            }))
            intent.putExtra(
                MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX,
                fileViewDataList.indexOfFirst { f ->
                    f === fileViewData
                })
            if (context is Activity) {
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context,
                    thumbnailView,
                    "image"
                )
                context.startActivity(intent, compat.toBundle())

            } else {
                context.startActivity(intent)
            }
        }
        thumbnailView.setOnClickListener(listener)
        playButton.setOnClickListener(listener)
    }

    @BindingAdapter("thumbnailView", "playButton", "previewAbleFile", "previewAbleFileList")
    @JvmStatic
    fun FrameLayout.setClickWhenShowMediaActivityListener(
        thumbnailView: ImageView,
        playButton: ImageButton,
        previewAbleFile: PreviewAbleFile?,
        previewAbleFileList: List<PreviewAbleFile>?
    ) {

        if (previewAbleFileList.isNullOrEmpty()) {
            return
        }
        val listener = View.OnClickListener {
            val context = it.context
            val intent = Intent(context, MediaActivity::class.java)
            intent.putExtra(MediaNavigationKeys.EXTRA_FILES, ArrayList(previewAbleFileList.map { fvd ->
                fvd.file
            }))
            intent.putExtra(
                MediaNavigationKeys.EXTRA_FILE_CURRENT_INDEX,
                previewAbleFileList.indexOfFirst { f ->
                    f === previewAbleFile
                })
            if (context is Activity) {
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context,
                    thumbnailView,
                    "image"
                )
                context.startActivity(intent, compat.toBundle())

            } else {
                context.startActivity(intent)
            }
        }
        thumbnailView.setOnClickListener(listener)
        playButton.setOnClickListener(listener)

        val holdListener = View.OnLongClickListener {
            val context = it.context
            val title = previewAbleFile?.file?.name
            val altText = previewAbleFile?.file?.comment
            val alertDialog = MaterialAlertDialogBuilder(context)
            alertDialog.setTitle(title)
            alertDialog.setMessage(altText)
            alertDialog.setNeutralButton("Exit") { intf, _ ->
                intf.cancel()
            }
            alertDialog.show()
            true
        }
        thumbnailView.setOnLongClickListener(holdListener)
    }


    @BindingAdapter("thumbnailView", "playButton", "fileViewData")
    @JvmStatic
    fun FrameLayout.setPreview(
        thumbnailView: ImageView,
        playButton: ImageButton,
        file: File?
    ) {

        try {
            this@MediaPreviewHelper.setPreview(thumbnailView, playButton, file!!)
            this.visibility = View.VISIBLE

        } catch (e: Exception) {
            this.visibility = View.GONE
        }
    }

    @BindingAdapter("thumbnailView")
    @JvmStatic
    fun ImageView.setPreview(file: PreviewAbleFile?) {
        file ?: return
        Glide.with(this)
            .load(file.file.thumbnailUrl)
            .centerCrop()
            .into(this)
    }

    @BindingAdapter("thumbnailView")
    @JvmStatic
    fun ImageView.setPreview(file: FileViewData?) {
        Log.d("MediaPreviewHelper", "setPreview:${file?.file}")
        if (this.visibility == View.GONE || file == null) {
            this.setImageResource(0)
            return
        }
        Glide.with(this)
            .load(file.file.thumbnailUrl)
            .centerCrop()
            .into(this)
    }

    private fun setPreview(
        thumbnailView: ImageView,
        playButton: ImageButton,
        file: File
    ) {
        when (file.aboutMediaType) {
            File.AboutMediaType.IMAGE, File.AboutMediaType.VIDEO -> {
                Glide.with(thumbnailView)
                    .load(file.thumbnailUrl)
                    .centerCrop()
                    .into(thumbnailView)

                when (file.aboutMediaType) {
                    File.AboutMediaType.IMAGE -> {
                        playButton.visibility = View.GONE
                    }
                    else -> {
                        playButton.visibility = View.VISIBLE
                        Glide.with(playButton)
                            .load(R.drawable.ic_play_circle_outline_black_24dp)
                            .centerInside()
                            .into(playButton)
                    }
                }
                //thumbnailView.visibility = View.VISIBLE

            }
            File.AboutMediaType.SOUND -> {
                playButton.visibility = View.VISIBLE
                Glide.with(playButton.context)
                    .load(R.drawable.ic_music_note_black_24dp)
                    .centerInside()
                    .into(playButton)
            }
            else -> {
                throw IllegalArgumentException("this type 知らねー:${file.type}")
            }
        }
    }


    @JvmStatic
    @BindingAdapter("previewAbleList", "mediaViewData")
    fun RecyclerView.setPreviewAbleList(
        previewAbleList: List<PreviewAbleFile>?,
        mediaViewData: MediaViewData?
    ) {
        if (previewAbleList == null || mediaViewData == null) {
            this.visibility = View.GONE
            return
        }

        if (previewAbleList.isEmpty() || previewAbleList.size <= 4) {
            this.visibility = View.GONE
            return
        }
        isNestedScrollingEnabled = false
        this.itemAnimator = null

        val adapter = this.adapter as? PreviewAbleFileListAdapter
            ?: PreviewAbleFileListAdapter(mediaViewData)
        this.adapter = adapter
        val layoutManager = this.layoutManager as? GridLayoutManager
            ?: GridLayoutManager(context, 2)
        this.layoutManager = layoutManager

        adapter.submitList(previewAbleList)
        this.visibility = View.VISIBLE

    }


}