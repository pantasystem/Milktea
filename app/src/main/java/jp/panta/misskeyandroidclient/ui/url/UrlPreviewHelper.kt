package jp.panta.misskeyandroidclient.ui.url

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.viewmodel.notes.Preview

object UrlPreviewHelper {

    @JvmStatic
    @BindingAdapter("previewList")
    fun RecyclerView.setUrlPreviewList(previewList: List<Preview>?){

        if(previewList.isNullOrEmpty()){
            this.visibility = View.GONE

        }else{
            this.visibility = View.VISIBLE
            val adapter = PreviewListAdapter()
            adapter.submitList(previewList)
            this.layoutManager = LinearLayoutManager(this.context)
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