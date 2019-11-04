package jp.panta.misskeyandroidclient.view.notes

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.lang.IndexOutOfBoundsException
/*
object ImagePreviewHelper {


    @BindingAdapter("imagePreviewTopLeft", "imagePreviewTopRight", "imagePreviewBottomLeft", "imagePreviewBottomRight", "previewFiles")
    @JvmStatic
    fun ViewGroup.setImagePreview(imagePreviewTopLeft: ImageView, imagePreviewTopRight: ImageView, imagePreviewBottomLeft: ImageView, imagePreviewBottomRight: ImageView, previewFiles: List<FileProperty>?){
        /*if(imagePreviewTopLeft != null){
            Log.d("ImagePreview", "${imagePreviewTopLeft::class.java}")
        }*/
        //Log.d("ImagePreview", "画面サイズ ${this.width}")
        //this.parent
        val a = this.parent

        val previews = listOf(imagePreviewTopLeft, imagePreviewTopRight, imagePreviewBottomLeft, imagePreviewBottomRight)

        if(previewFiles.isNullOrEmpty()){
            previews.forEach{
                it.visibility = View.GONE
            }
            return
        }

        for(n in 0.until(previews.size)){
            try{
                val file = previewFiles[n]
                val view = previews[n]
                view.visibility = View.VISIBLE
                Glide
                    .with(this.context)
                    .load(file.thumbnailUrl)
                    .error(R.drawable.ic_cloud_off_black_24dp)
                    .centerCrop()
                    .into(view)
            }catch(e: IndexOutOfBoundsException){
                previews[n].visibility = View.GONE
            }
        }
        //for(n in 0.until(previewFiles?.size?: 0))
    }

}*/