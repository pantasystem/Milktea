package jp.panta.misskeyandroidclient.view.media

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.lang.IndexOutOfBoundsException

object MediaPreviewHelper{

    @BindingAdapter(
        "thumbnailTopLeft",
        "thumbnailTopRight",
        "thumbnailBottomLeft",
        "thumbnailBottomRight",
        "actionButtonTopLeft",
        "actionButtonTopRight",
        "actionButtonBottomLeft",
        "actionButtonBottomRight",
        "baseFrameTopLeft",
        "baseFrameTopRight",
        "baseFrameBottomLeft",
        "baseFrameBottomRight",
        "previewMediaFiles")
    @JvmStatic
    fun ViewGroup.setMediaPreview(
        thumbnailTopLeft: ImageView,
        thumbnailTopRight: ImageView,
        thumbnailBottomLeft: ImageView,
        thumbnailBottomRight: ImageView,
        actionButtonTopLeft: ImageButton,
        actionButtonTopRight: ImageButton,
        actionButtonBottomLeft: ImageButton,
        actionButtonBottomRight: ImageButton,
        baseFrameTopLeft: FrameLayout,
        baseFrameTopRight: FrameLayout,
        baseFrameBottomLeft: FrameLayout,
        baseFrameBottomRight: FrameLayout,
        previewMediaFiles: List<FileProperty>
    ){
        val thumbnailViews = listOf(thumbnailTopLeft, thumbnailTopRight, thumbnailBottomLeft, thumbnailBottomRight)
        val actionButtons = listOf(actionButtonTopLeft, actionButtonTopRight, actionButtonBottomLeft, actionButtonBottomRight)
        val frames = listOf(baseFrameTopLeft, baseFrameTopRight, baseFrameBottomLeft, baseFrameBottomRight)

        for(n in 0.until(4)){
            val thumbnail = thumbnailViews[n]
            val actionButton = actionButtons[n]
            val frame = frames[n]
            try{
                val file = previewMediaFiles[n]
                setPreview(thumbnail, actionButton, file)

            }catch(e: IndexOutOfBoundsException){
                frame.visibility = View.GONE
                thumbnail.visibility = View.GONE
                actionButton.visibility = View.GONE

            }
        }
    }

    fun setPreview(thumbnail: ImageView, actionButton: ImageButton, file: FileProperty){
        //VISIBLEにしかしない
        thumbnail.visibility = View.VISIBLE
        if(file.type?.contains("audio") == true){
            Glide.with(thumbnail)
                .load(R.drawable.ic_music_note_black_24dp)
                .centerCrop()
                .error(R.drawable.ic_cloud_off_black_24dp)
                .into(thumbnail)
        }else{
            Glide.with(thumbnail)
                .load(file.thumbnailUrl)
                .centerCrop()
                .error(R.drawable.ic_cloud_off_black_24dp)
                .into(thumbnail)
        }

        if(file.type?.contains("image") == true){
            actionButton.visibility = View.INVISIBLE
        }else{
            actionButton.visibility = View.VISIBLE
            Glide.with(actionButton)
                .load(R.drawable.ic_play_circle_outline_black_24dp)
                .centerCrop()
                .into(actionButton)
        }



    }


}