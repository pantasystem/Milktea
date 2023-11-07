package net.pantasystem.milktea.note.url

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.common_android_ui.NavigationEntryPointForBinding
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemUrlOrFilePreviewBinding
import net.pantasystem.milktea.note.viewmodel.Preview

object UrlPreviewHelper {

    @SuppressLint("IntentReset")
    @JvmStatic
    @BindingAdapter("previewList")
    fun LinearLayout.setUrlPreviewList(previewList: List<Preview>?) {
        if (previewList.isNullOrEmpty()) {
            this.setMemoVisibility(View.GONE)
            return
        }

        while(this.childCount > previewList.size){
            this.removeViewAt(this.childCount - 1)
        }

        previewList.forEachIndexed { index, preview ->
            val existsView: ItemUrlOrFilePreviewBinding? = this.getChildAt(index)?.let {
                ItemUrlOrFilePreviewBinding.bind(it)
            }

            val view = existsView ?: ItemUrlOrFilePreviewBinding.inflate(LayoutInflater.from(this.context), this, false)

            when(preview) {
                is Preview.FileWrapper -> {
                    view.urlPreviewView.setMemoVisibility(View.GONE)
                    view.filePreviewView.setMemoVisibility(View.VISIBLE)
                    view.filePreviewView.setOtherFile(preview.file)

                    view.filePreviewView.setOnClickListener { v ->

                        if (preview.file.type.startsWith("audio")) {
                            val activity = FragmentComponentManager.findActivity(v.context)
                            if (activity is Activity) {
                                val accessor = EntryPointAccessors.fromActivity(
                                    activity,
                                    NavigationEntryPointForBinding::class.java
                                )
                                val intent = accessor.mediaNavigation().newIntent(
                                    MediaNavigationArgs.AFile(
                                        preview.file
                                    )
                                )
                                v.context?.startActivity(intent)
                            }
                        } else {
                            try {
                                v.context?.startActivity(
                                    Intent().apply {
                                        data = Uri.parse(preview.file.path)
                                        type = preview.file.type
                                    }
                                )
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    v.context,
                                    context.getString(net.pantasystem.milktea.common_resource.R.string.no_app_available_to_open_this_file),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
                is Preview.UrlWrapper -> {
                    view.urlPreviewView.setMemoVisibility(View.VISIBLE)
                    view.filePreviewView.setMemoVisibility(View.GONE)
                    view.urlPreviewView.setUrlPreview(preview.urlPreview)


                    view.urlPreviewView.setOnClickListener {
                        context?.startActivity(
                            Intent(Intent.ACTION_VIEW).apply{
                                data = Uri.parse(preview.urlPreview.url)
                            }
                        )
                    }
                }
            }

            if(existsView == null){
                this.addView(view.root)
            }
        }

        this.visibility = View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("urlPreviewThumbnailUrl")
    fun ImageView.setUrlPreviewThumbnail(url: String?){
        Glide.with(this)
            .load(url)
            .centerCrop()
            .error(R.drawable.ic_cloud_off_black_24dp)
            .into(this)
    }

    @JvmStatic
    @BindingAdapter("siteIconUrl")
    fun ImageView.setSiteIcon(url: String?){
        Glide.with(this)
            .load(url)
            .error(R.drawable.ic_cloud_off_black_24dp)
            .centerCrop()
            .into(this)
    }
}