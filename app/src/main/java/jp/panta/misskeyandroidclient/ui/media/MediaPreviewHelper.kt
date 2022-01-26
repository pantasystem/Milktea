package jp.panta.misskeyandroidclient.ui.media

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.MediaViewData
import java.lang.IllegalArgumentException

object MediaPreviewHelper{


    @BindingAdapter("thumbnailView", "playButton", "fileViewData", "fileViewDataList")
    @JvmStatic
    fun FrameLayout.setClickWhenShowMediaActivityListener(thumbnailView: ImageView, playButton: ImageButton, fileViewData: FileViewData?, fileViewDataList: List<FileViewData>?) {
        setPreview(thumbnailView, playButton, fileViewData)
        fileViewData?: return

        if(fileViewDataList.isNullOrEmpty()) {
            return
        }
        val listener = View.OnClickListener {
            val context = it.context
            val intent = Intent(context, MediaActivity::class.java)
            intent.putExtra(MediaActivity.EXTRA_FILES, ArrayList(fileViewDataList.map{ fvd ->
                fvd.file
            }))
            intent.putExtra(MediaActivity.EXTRA_FILE_CURRENT_INDEX, fileViewDataList.indexOfFirst { f ->
                f === fileViewData
            })
            if(context is Activity){
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(context, thumbnailView, "image")
                context.startActivity(intent, compat.toBundle())

            }else{
                context.startActivity(intent)
            }
        }
        thumbnailView.setOnClickListener(listener)
        playButton.setOnClickListener(listener)
    }


    @BindingAdapter("thumbnailView", "playButton", "fileViewData")
    @JvmStatic
    fun FrameLayout.setPreview(thumbnailView: ImageView, playButton: ImageButton, fileViewData: FileViewData?){

        try{
            this@MediaPreviewHelper.setPreview(thumbnailView, playButton, fileViewData!!)
            this.visibility = View.VISIBLE

        }catch(e: Exception){
            this.visibility = View.GONE
        }
    }

    private fun setPreview(thumbnailView: ImageView, playButton: ImageButton, fileViewData: FileViewData){
        when(fileViewData.type){
            FileViewData.Type.IMAGE, FileViewData.Type.VIDEO -> {
                Glide.with(thumbnailView)
                    .load(fileViewData.file.thumbnailUrl)
                    .centerCrop()
                    .into(thumbnailView)

                when(fileViewData.type){
                    FileViewData.Type.IMAGE ->{
                        playButton.visibility = View.GONE
                    }
                    else ->{
                        playButton.visibility = View.VISIBLE
                        Glide.with(playButton)
                            .load(R.drawable.ic_play_circle_outline_black_24dp)
                            .centerInside()
                            .into(playButton)
                    }
                }
                //thumbnailView.visibility = View.VISIBLE

            }
            FileViewData.Type.SOUND -> {
                playButton.visibility = View.VISIBLE
                Glide.with(playButton.context)
                    .load(R.drawable.ic_music_note_black_24dp)
                    .centerInside()
                    .into(playButton)
            }
            else ->{
                throw IllegalArgumentException("this type 知らねー:${fileViewData.type}")
            }
        }
    }

    @BindingAdapter("leftMediaBase", "rightMediaBase", "mediaViewData")
    @JvmStatic
    fun ViewGroup.visibilityControl(
        leftMediaBase: LinearLayout,
        rightMediaBase: LinearLayout,
        mediaViewData: MediaViewData?
    ){
        when {

            mediaViewData == null || mediaViewData.files.isEmpty() -> {
                this.visibility = View.GONE
                leftMediaBase.visibility = View.GONE
                rightMediaBase.visibility = View.GONE
            }
            mediaViewData.files.size < 2 ->{
                leftMediaBase.visibility = View.VISIBLE
                rightMediaBase.visibility = View.GONE
            }
            else -> {
                leftMediaBase.visibility = View.VISIBLE
                rightMediaBase.visibility = View.VISIBLE
            }
        }
    }






}