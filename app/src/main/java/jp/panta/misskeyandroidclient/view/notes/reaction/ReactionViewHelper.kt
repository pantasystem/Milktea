package jp.panta.misskeyandroidclient.view.notes.reaction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.caverock.androidsvg.SVG
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.emoji.ConstantEmoji
import jp.panta.misskeyandroidclient.util.svg.SvgDecoder
import jp.panta.misskeyandroidclient.util.svg.SvgDrawableTranscoder
import jp.panta.misskeyandroidclient.util.svg.SvgSoftwareLayerSetter
import kotlinx.android.synthetic.main.item_reaction.view.*
import  com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import jp.panta.misskeyandroidclient.util.svg.GlideApp
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionViewHelper.setReaction

object ReactionViewHelper {
    @BindingAdapter("reactionImageView", "reactionStringView", "reaction")
    @JvmStatic
    fun LinearLayout.setReaction(reactionImageView: ImageView, reactionStringView: TextView, reaction: String) {
        setReaction(this.context, reactionImageView, reactionStringView, reaction)
        //Log.d("ReactionViewHelper", "reaction $reaction")
        /*if(reaction.startsWith(":") && reaction.endsWith(":")){
            val miApplication = this.context.applicationContext as MiApplication
            val emoji = miApplication.nowInstanceMeta?.emojis?.firstOrNull{
                it.name == reaction.replace(":", "")
            }

            if(emoji != null){
                //Log.d("ReactionViewHelper", "カスタム絵文字を発見した: ${emoji}")
                if(emoji.type?.contains("svg") == true || emoji.url?.contains("svg") == true|| emoji.uri?.contains("svg") == true){
                    /*GlideApp.with(this.context)
                        .`as`(PictureDrawable::class.java)
                        .listener(SvgSoftwareLayerSetter())
                        .load(emoji.url?: emoji.uri)
                        .centerCrop()
                        .transition(withCrossFade())
                        .into(reactionImageView)*/
                    GlideApp.with(this.context)
                        .`as`(Bitmap::class.java)
                        //.listener(SvgSoftwareLayerSetter())
                        //.transition(withCrossFade())
                        .load(emoji.url?: emoji.url)
                        .into(reactionImageView)


                    //Log.d("ReactionViewHolder", "svgを読み込みました")
                }else{
                    Glide.with(reactionImageView.context)
                        .load(emoji.url?: emoji.uri)
                        .centerCrop()
                        .into(reactionImageView)

                }
                reactionImageView.visibility = View.VISIBLE
                reactionStringView.visibility = View.GONE
                return
            }else{
                Log.d("ReactionViewHelper", "emoji not found")
                reactionImageView.visibility = View.GONE
                reactionStringView.visibility = View.GONE
            }

        }

        val reactionResourceId = ReactionResourceMap.reactionDrawableMap[reaction]
        if(reactionResourceId != null){

            Glide.with(reactionImageView)
                .load(reactionResourceId)
                .centerCrop()
                .into(reactionImageView)
            reactionImageView.visibility = View.VISIBLE
            reactionStringView.visibility = View.GONE
        }else{
            //Log.d("ReactionViewHelper", "どれにも当てはまらなかった")
            reactionStringView.text = reaction
            reactionImageView.visibility = View.GONE
            reactionStringView.visibility = View.VISIBLE
        }*/

    }

    fun setReaction(context: Context, reactionImageView: ImageView, reactionStringView: TextView, reaction: String) {
        //Log.d("ReactionViewHelper", "reaction $reaction")
        if(reaction.startsWith(":") && reaction.endsWith(":")){
            val miApplication = context.applicationContext as MiApplication
            val emoji = miApplication.nowInstanceMeta?.emojis?.firstOrNull{
                it.name == reaction.replace(":", "")
            }

            if(emoji != null){
                //Log.d("ReactionViewHelper", "カスタム絵文字を発見した: ${emoji}")
                if(emoji.type?.contains("svg") == true || emoji.url?.contains("svg") == true|| emoji.uri?.contains("svg") == true){
                    /*GlideApp.with(this.context)
                        .`as`(PictureDrawable::class.java)
                        .listener(SvgSoftwareLayerSetter())
                        .load(emoji.url?: emoji.uri)
                        .centerCrop()
                        .transition(withCrossFade())
                        .into(reactionImageView)*/
                    GlideApp.with(context)
                        .`as`(Bitmap::class.java)
                        //.listener(SvgSoftwareLayerSetter())
                        //.transition(withCrossFade())
                        .load(emoji.url?: emoji.url)
                        .into(reactionImageView)


                    //Log.d("ReactionViewHolder", "svgを読み込みました")
                }else{
                    Glide.with(reactionImageView.context)
                        .load(emoji.url?: emoji.uri)
                        .centerCrop()
                        .into(reactionImageView)

                }
                reactionImageView.visibility = View.VISIBLE
                reactionStringView.visibility = View.GONE
                return
            }else{
                Log.d("ReactionViewHelper", "emoji not found")
                reactionImageView.visibility = View.GONE
                reactionStringView.visibility = View.GONE
            }

        }

        val reactionResourceId = ReactionResourceMap.reactionDrawableMap[reaction]
        if(reactionResourceId != null){

            Glide.with(reactionImageView)
                .load(reactionResourceId)
                .centerCrop()
                .into(reactionImageView)
            reactionImageView.visibility = View.VISIBLE
            reactionStringView.visibility = View.GONE
        }else{
            //Log.d("ReactionViewHelper", "どれにも当てはまらなかった")
            reactionStringView.text = reaction
            reactionImageView.visibility = View.GONE
            reactionStringView.visibility = View.VISIBLE
        }

    }
}