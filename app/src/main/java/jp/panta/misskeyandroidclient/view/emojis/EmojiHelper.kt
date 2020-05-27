package jp.panta.misskeyandroidclient.view.emojis

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.reaction.ReactionSelection
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionPreviewAdapter
import jp.panta.misskeyandroidclient.view.reaction.ReactionChoicesAdapter

object EmojiHelper{
    @JvmStatic
    @BindingAdapter("selectableEmojiList", "reactionSelection")
    fun RecyclerView.setEmojiList(emojiList: List<Emoji>?, reactionSelection: ReactionSelection){
        emojiList?: return

        if(layoutManager !is FlexboxLayoutManager){
            val flexBoxLayoutManager = FlexboxLayoutManager(this.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            layoutManager = flexBoxLayoutManager
        }

        val adapter = ReactionChoicesAdapter(reactionSelection)
        this.adapter = adapter
        adapter.submitList(emojiList.map{
            ":${it.name}:"
        })
    }

    @JvmStatic
    @BindingAdapter("customEmoji")
    fun ImageButton.setEmojiImage(customEmoji: Emoji){
        Glide.with(this)
            .load(customEmoji.url?: customEmoji.uri)
            .centerCrop()
            .into(this)
    }
}