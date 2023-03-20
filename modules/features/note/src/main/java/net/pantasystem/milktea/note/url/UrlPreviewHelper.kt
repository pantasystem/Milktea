package net.pantasystem.milktea.note.url

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.pantasystem.milktea.note.R
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