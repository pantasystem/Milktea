package net.pantasystem.milktea.note.reaction.choices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemEmojiChoiceBinding


class EmojiChoicesAdapter(
    val emojiSelection: (EmojiType) -> Unit,
) : ListAdapter<EmojiType, EmojiChoicesAdapter.Holder>(
    DiffUtilItemCallback()
){
    class DiffUtilItemCallback : DiffUtil.ItemCallback<EmojiType>(){
        override fun areContentsTheSame(oldItem: EmojiType, newItem: EmojiType): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: EmojiType, newItem: EmojiType): Boolean {
            return oldItem == newItem
        }
    }
    class Holder(val binding : ItemEmojiChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            DataBindingUtil.inflate<ItemEmojiChoiceBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_emoji_choice,
                parent,
                false
            )
        return Holder(
            binding
        )
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        when(item) {
            is EmojiType.CustomEmoji -> {
                GlideApp.with(holder.binding.reactionImagePreview)
                    .load(item.emoji.url ?: item.emoji.uri)
                        // FIXME: webpの場合うまく表示できなくなる
//                    .centerCrop()
                    .into(holder.binding.reactionImagePreview)
                holder.binding.reactionStringPreview.isVisible = false
                holder.binding.reactionImagePreview.isVisible = true
            }
            is EmojiType.Legacy -> {
                holder.binding.reactionImagePreview.isVisible = false
                holder.binding.reactionStringPreview.isVisible = true
                holder.binding.reactionStringPreview.text = requireNotNull(LegacyReaction.reactionMap[item.type])
            }
            is EmojiType.UtfEmoji -> {
                holder.binding.reactionStringPreview.isVisible = true
                holder.binding.reactionImagePreview.isVisible = false
                holder.binding.reactionStringPreview.text = item.code
            }
        }
        holder.binding.root.setOnClickListener {
            emojiSelection(item)
        }
        holder.binding.executePendingBindings()
    }
}