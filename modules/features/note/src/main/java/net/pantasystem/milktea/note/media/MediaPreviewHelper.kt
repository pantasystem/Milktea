
package net.pantasystem.milktea.note.media

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common.glide.blurhash.BlurHashSource
import net.pantasystem.milktea.common_android.platform.isWifiConnected
import net.pantasystem.milktea.common_android.ui.MediaLayout
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.common_android.ui.haptic.HapticFeedbackController
import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemMediaPreviewBinding
import net.pantasystem.milktea.note.media.viewmodel.MediaViewData
import net.pantasystem.milktea.note.media.viewmodel.PreviewAbleFile
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter

object MediaPreviewHelper {


    @JvmStatic
    fun FrameLayout.setClickWhenShowMediaActivityListener(
        thumbnailView: ImageView,
        playButton: ImageButton,
        previewAbleFile: PreviewAbleFile?,
        previewAbleFileList: List<PreviewAbleFile>?,
        noteCardActionListenerAdapter: NoteCardActionListenerAdapter?,
        config: Config?,
    ) {

        if (previewAbleFileList.isNullOrEmpty()) {
            return
        }
        val listener = View.OnClickListener {
            HapticFeedbackController.performClickHapticFeedback(it)
            noteCardActionListenerAdapter?.onMediaPreviewClicked(
                previewAbleFile = previewAbleFile,
                files = previewAbleFileList,
                index = previewAbleFileList.indexOfFirst { f ->
                    f === previewAbleFile
                },
                thumbnailView = thumbnailView,
            )
        }
        thumbnailView.setOnClickListener(listener)
        playButton.setOnClickListener(listener)

        val holdListener = View.OnLongClickListener {
            noteCardActionListenerAdapter?.onMediaPreviewLongClicked(previewAbleFile)
            true
        }
        thumbnailView.setOnLongClickListener(holdListener)

        // NOTE: 実装の仕様上、サムネイル非表示時には親レイアウトにクリックイベントを伝播する必要がある

        if (previewAbleFile?.isHidingWithNetworkStateAndConfig(
            isMobileNetwork = !context.isWifiConnected(),
            mediaDisplayMode = config?.mediaDisplayMode ?: DefaultConfig.config.mediaDisplayMode
        ) == true) {
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
        val isHiding = file.isHidingWithNetworkStateAndConfig(
            isMobileNetwork = !context.isWifiConnected(),
            mediaDisplayMode = config.mediaDisplayMode
        )
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

        val isHiding = src.isHidingWithNetworkStateAndConfig(
            isMobileNetwork = !context.isWifiConnected(),
            mediaDisplayMode = config.mediaDisplayMode
        )
        setMemoVisibility(
            if (isHiding) {
                View.VISIBLE
            } else {
                View.GONE
            }
        )
        if (isHiding) {
            if (src.visibleType == PreviewAbleFile.VisibleType.SensitiveHide) {
                this.text = context.getString(R.string.sensitive_content)
            } else {
                this.text = context.getString(R.string.notes_media_click_to_load_image)
            }
        }
    }


    @JvmStatic
    @BindingAdapter("previewAbleList", "mediaViewData", "noteCardActionListenerAdapter")
    fun MediaLayout.setPreviewAbleList(
        previewAbleList: List<PreviewAbleFile>?,
        mediaViewData: MediaViewData?,
        noteCardActionListenerAdapter: NoteCardActionListenerAdapter?,
    ) {
        if (previewAbleList == null || mediaViewData == null) {
            this.visibility = View.GONE
            return
        }

        if (previewAbleList.isEmpty()) {
            this.visibility = View.GONE
            return
        }

        val isWifiConnected = context.isWifiConnected()

        withSuspendLayout {
            var count = this.childCount
            while (count > previewAbleList.size) {
                if (this.childCount > 4) {
                    this.removeViewAt(this.childCount - 1)
                } else {
                    this.getChildAt(count - 1).setMemoVisibility(View.GONE)
                }
                count --
            }

            val inflater = LayoutInflater.from(this.context)
            previewAbleList.forEachIndexed { index, previewAbleFile ->
                val existsView: View? = this.getChildAt(index)
                val binding = if (existsView == null) {
                    ItemMediaPreviewBinding.inflate(inflater, this, false)
                } else {
                    ItemMediaPreviewBinding.bind(existsView)
                }
                binding.root.setMemoVisibility(View.VISIBLE)

                binding.baseFrame.setClickWhenShowMediaActivityListener(
                    binding.thumbnail,
                    binding.actionButton,
                    previewAbleFile,
                    previewAbleList,
                    noteCardActionListenerAdapter,
                    mediaViewData.config,
                )
                binding.baseFrame.setOnClickListener {
                    HapticFeedbackController.performClickHapticFeedback(it)
                    if (previewAbleFile.visibleType == PreviewAbleFile.VisibleType.SensitiveHide) {
                        noteCardActionListenerAdapter?.onSensitiveMediaPreviewClicked(
                            mediaViewData,
                            index
                        )
                    } else {
                        mediaViewData.show(index)
                    }
                }

                binding.thumbnail.setPreview(previewAbleFile, mediaViewData.config)

                binding.actionButton.isVisible = previewAbleFile.isVisiblePlayButton
                binding.nsfwMessage.isVisible = previewAbleFile.isHiding
                binding.nsfwMessage.setHideImageMessage(previewAbleFile, mediaViewData.config)
                binding.toggleVisibilityButton.setImageResource(if (previewAbleFile.isHiding) R.drawable.ic_baseline_image_24 else R.drawable.ic_baseline_hide_image_24)
                binding.toggleVisibilityButton.setOnClickListener {
                    HapticFeedbackController.performClickHapticFeedback(it)
                    // NOTE: ここでのネットワークの状態はbind時のものを使う
                    // なぜなら表示状態はbindされた時のネットワークの状態を使っているから
                    mediaViewData.toggleVisibility(index, isMobileNetwork = !isWifiConnected, mediaDisplayMode = mediaViewData.config?.mediaDisplayMode ?: DefaultConfig.config.mediaDisplayMode)
                }

                if (existsView == null) {
                    this.addView(binding.root)
                }
            }
        }

        this.visibility = View.VISIBLE

    }


}


