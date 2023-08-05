package net.pantasystem.milktea.common_android_ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction

object ReactionViewHelper {
    @BindingAdapter("reactionImageView", "reactionStringView", "reaction")
    @JvmStatic
    fun LinearLayout.setReaction(
        reactionImageView: ImageView,
        reactionStringView: TextView,
        reaction: String
    ) {
        setReaction(this.context, reactionImageView, reactionStringView, reaction)

    }

    @BindingAdapter("reactionImageView", "reactionStringView", "reaction")
    @JvmStatic
    fun FrameLayout.setReaction(
        reactionImageView: ImageView,
        reactionStringView: TextView,
        reaction: String
    ) {
        setReaction(this.context, reactionImageView, reactionStringView, reaction)
    }

    @BindingAdapter("emojis", "reaction")
    @JvmStatic
    fun ImageView.setCustomEmoji(
        emojis: List<Emoji>?,
        reaction: String?,
    ) {
        reaction ?: return
        val emoji = emojis?.firstOrNull {
            ":${it.name}:" == reaction
        } ?: return

        GlideApp.with(this)
            .load(emoji.url)
            .centerCrop()
            .into(this)
    }


    private fun setReaction(
        context: Context,
        reactionImageView: ImageView,
        reactionStringView: TextView,
        reaction: String
    ) {

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BindingProvider::class.java
        )
        val cache = entryPoint.customEmojiRepository()
        val accountStore = entryPoint.accountStore()


        //Log.d("ReactionViewHelper", "reaction $reaction")
        if (reaction.startsWith(":") && reaction.endsWith(":")) {
            val account = accountStore.currentAccount
            val emojis = if (account?.getHost() != null) {
                cache.getAndConvertToMap(account.getHost()) ?: emptyMap()
            } else {
                emptyMap()
            }

            val emoji = emojis[reaction.replace(":", "")]

            if (emoji != null) {
                //Log.d("ReactionViewHelper", "カスタム絵文字を発見した: ${emoji}")
                if (emoji.cachePath == null) {
                    GlideApp.with(reactionImageView.context)
                        .load(emoji.url ?: emoji.uri)
                        .into(reactionImageView)
                } else {
                    GlideApp.with(reactionImageView.context)
                        .load(emoji.cachePath)
                        .error(
                            GlideApp.with(reactionImageView.context)
                                .load(emoji.url ?: emoji.uri)
                        )
                        .into(reactionImageView)
                }

                reactionImageView.setMemoVisibility(View.VISIBLE)
                reactionStringView.setMemoVisibility(View.GONE)
                return
            } else {
                reactionImageView.setMemoVisibility(View.GONE)
                reactionStringView.setMemoVisibility(View.VISIBLE)
            }

        }

        val constantReaction = LegacyReaction.reactionMap[reaction]
        if (constantReaction != null) {

            reactionStringView.text = constantReaction
            reactionImageView.setMemoVisibility(View.GONE)
            reactionStringView.setMemoVisibility(View.VISIBLE)
        } else {
            //Log.d("ReactionViewHelper", "どれにも当てはまらなかった")
            reactionStringView.text = reaction
            reactionImageView.setMemoVisibility(View.GONE)
            reactionStringView.setMemoVisibility(View.VISIBLE)
        }

    }



}