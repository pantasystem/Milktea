package jp.panta.misskeyandroidclient.view.notes

import android.databinding.BindingAdapter
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import jp.panta.misskeyandroidclient.model.files.FileProperty

object ImagePreviewHelper {


    @BindingAdapter("imagePreviewTopLeft", "imagePreviewTopRight", "imagePreviewBottomLeft", "imagePreviewBottomRight", "previewFiles")
    @JvmStatic
    fun ViewGroup.setImagePreview(imagePreviewTopLeft: ImageView, imagePreviewTopRight: ImageView, imagePreviewBottomLeft: ImageView, imagePreviewBottomRight: ImageView, previewFiles: List<FileProperty>?){
        /*if(imagePreviewTopLeft != null){
            Log.d("ImagePreview", "${imagePreviewTopLeft::class.java}")
        }*/


    }

}