package net.pantasystem.milktea.note.url

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
            val adapter = this.adapter as? PreviewListAdapter
                ?: PreviewListAdapter()
            adapter.submitList(previewList)

            val layoutManager = this.layoutManager as? LinearLayoutManager
                ?: LinearLayoutManager(this.context)
            this.layoutManager = layoutManager
            this.adapter = adapter
        }

    }

    @JvmStatic
    @BindingAdapter("urlPreviewThumbnailUrl")
    fun ImageView.setUrlPreviewThumbnail(url: String?){
        url?: return
        Glide.with(this)
            .load(url)
            .centerCrop()
            .into(this)
    }

    @JvmStatic
    @BindingAdapter("siteIconUrl")
    fun ImageView.setSiteIcon(url: String?){
        url?: return
        Glide.with(this)
            .load(url)
            .centerCrop()
            .into(this)
    }
}