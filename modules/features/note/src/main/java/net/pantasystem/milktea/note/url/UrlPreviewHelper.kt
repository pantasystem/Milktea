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
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import net.pantasystem.milktea.common_android_ui.NavigationEntryPointForBinding
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemUrlOrFilePreviewBinding
import net.pantasystem.milktea.note.viewmodel.Preview

object UrlPreviewHelper {

    @JvmStatic
    @BindingAdapter("previewList")
    fun RecyclerView.setUrlPreviewList(previewList: List<Preview>?){

        if(previewList.isNullOrEmpty()){
            this.visibility = View.GONE

        }else{
            this.isNestedScrollingEnabled = false
            this.visibility = View.VISIBLE
            val adapter = (this.adapter as? PreviewListAdapter)?: PreviewListAdapter().also {
                adapter = it
            }

            val lm = (this.layoutManager as? LinearLayoutManager) ?: LinearLayoutManager(this.context).also {
                layoutManager = it
            }
            lm.recycleChildrenOnDetach = true
            adapter.submitList(previewList)

        }

    }

    @SuppressLint("IntentReset")
    @JvmStatic
    @BindingAdapter("previewList")
    fun LinearLayout.setUrlPreviewList(previewList: List<Preview>?) {
        this.visibility = View.GONE
        if (previewList.isNullOrEmpty()) {
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
                    view.urlPreviewView.isVisible = false
                    view.filePreviewView.isVisible = true
                    view.filePreviewView.setOtherFile(preview.file)

                    view.filePreviewView.setOnClickListener {

                        if (preview.file.type.startsWith("audio")) {
                            val activity = FragmentComponentManager.findActivity(view.root.context)
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
                                context?.startActivity(intent)
                            }
                        } else {
                            try {
                                context?.startActivity(
                                    Intent().apply {
                                        data = Uri.parse(preview.file.path)
                                        type = preview.file.type
                                    }
                                )
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    context.getString(net.pantasystem.milktea.common_resource.R.string.no_app_available_to_open_this_file),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
                is Preview.UrlWrapper -> {
                    view.urlPreviewView.isVisible = true
                    view.filePreviewView.isVisible = false
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