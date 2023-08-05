
package net.pantasystem.milktea.note.media

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import jp.wasabeef.glide.transformations.BlurTransformation
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common.glide.blurhash.BlurHashSource
import net.pantasystem.milktea.common_android.platform.isWifiConnected
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.common_android_ui.NavigationEntryPointForBinding
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.MediaDisplayMode
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.media.viewmodel.MediaViewData
import net.pantasystem.milktea.note.media.viewmodel.PreviewAbleFile

object MediaPreviewHelper {


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
            val activity = FragmentComponentManager.findActivity(it.context) as Activity
            val intent = EntryPointAccessors.fromActivity(
                activity,
                NavigationEntryPointForBinding::class.java
            )
                .mediaNavigation().newIntent(
                    MediaNavigationArgs.Files(
                        files = previewAbleFileList.map { fvd ->
                            fvd.source
                        },
                        index = previewAbleFileList.indexOfFirst { f ->
                            f === previewAbleFile
                        })
                )
            val context = it.context

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
            val title = previewAbleFile?.source?.name
            val altText = previewAbleFile?.source?.comment
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

        // NOTE: 実装の仕様上、サムネイル非表示時には親レイアウトにクリックイベントを伝播する必要がある
        if (previewAbleFile?.visibleType == PreviewAbleFile.VisibleType.SensitiveHide
            || previewAbleFile?.visibleType == PreviewAbleFile.VisibleType.HideWhenMobileNetwork
        ) {
            thumbnailView.setOnClickListener {
                this.performClick()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @BindingAdapter("thumbnailView", "config")
    @JvmStatic
    fun ImageView.setPreview(file: PreviewAbleFile?, config: Config?) {
        file ?: return
        config ?: return
        val isHiding = when(file.visibleType) {
            PreviewAbleFile.VisibleType.Visible -> false
            PreviewAbleFile.VisibleType.HideWhenMobileNetwork -> {
                if (config.mediaDisplayMode == MediaDisplayMode.ALWAYS_HIDE_WHEN_MOBILE_NETWORK) {
                    !context.isWifiConnected()
                } else config.mediaDisplayMode == MediaDisplayMode.ALWAYS_HIDE
            }
            PreviewAbleFile.VisibleType.SensitiveHide -> true
        }
        if (isHiding) {
            Glide.with(this)
                .let {
                    when (val blurhash = file.source.blurhash) {
                        null -> it.load(file.source.thumbnailUrl)
                            .transform(BlurTransformation(32, 4), CenterCrop())
                        else -> it.load(
                            BlurHashSource(blurhash)
                        )
                    }
                }
                .into(this)
        } else {
            Glide.with(this)
                .load(file.source.thumbnailUrl)
                .thumbnail(GlideApp.with(this).load(
                    file.source.blurhash?.let {
                        BlurHashSource(it)
                    }
                ))
                .centerCrop()
                .into(this)
        }

    }

    @JvmStatic
    @BindingAdapter("hideImageMessageImageSource", "config")
    fun TextView.setHideImageMessage(src: PreviewAbleFile?, config: Config?) {
        src ?: return
        config ?: return
        when(src.visibleType) {
            PreviewAbleFile.VisibleType.Visible -> {
                this.setMemoVisibility(View.GONE)
                return
            }
            PreviewAbleFile.VisibleType.HideWhenMobileNetwork -> {
                this.text = context.getString(R.string.notes_media_click_to_load_image)
                if (context.isWifiConnected()) {
                    if (config.mediaDisplayMode == MediaDisplayMode.ALWAYS_HIDE) {
                        this.setMemoVisibility(View.VISIBLE)
                    } else {
                        this.setMemoVisibility(View.GONE)
                    }
                } else {
                    this.setMemoVisibility(View.VISIBLE)
                }
            }
            PreviewAbleFile.VisibleType.SensitiveHide -> {
                this.setMemoVisibility(View.VISIBLE)
                this.text = context.getString(R.string.sensitive_content)
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
        layoutManager.recycleChildrenOnDetach = true
        this.layoutManager = layoutManager

        adapter.submitList(previewAbleList)
        this.visibility = View.VISIBLE

    }


}


