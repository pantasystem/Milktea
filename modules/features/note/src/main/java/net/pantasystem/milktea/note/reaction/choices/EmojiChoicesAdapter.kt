package net.pantasystem.milktea.note.reaction.choices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemEmojiChoiceBinding


class EmojiChoicesAdapter(
    val onEmojiSelected: (EmojiType) -> Unit,
    val onEmojiLongClicked: (EmojiType) -> Boolean,
    ) : ListAdapter<EmojiType, EmojiChoicesAdapter.Holder>(
    DiffUtilItemCallback()
){
    class DiffUtilItemCallback : DiffUtil.ItemCallback<EmojiType>(){
        override fun areContentsTheSame(oldItem: EmojiType, newItem: EmojiType): Boolean {
            return oldItem.areContentsTheSame(newItem)
        }

        override fun areItemsTheSame(oldItem: EmojiType, newItem: EmojiType): Boolean {
            return oldItem.areItemsTheSame(newItem)
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
                holder.binding.reactionStringPreview.setMemoVisibility(View.GONE)
                holder.binding.reactionImagePreview.setMemoVisibility(View.VISIBLE)
            }
            is EmojiType.Legacy -> {
                holder.binding.reactionImagePreview.setMemoVisibility(View.GONE)
                holder.binding.reactionStringPreview.setMemoVisibility(View.VISIBLE)
                holder.binding.reactionStringPreview.text = requireNotNull(LegacyReaction.reactionMap[item.type])
            }
            is EmojiType.UtfEmoji -> {
                holder.binding.reactionStringPreview.setMemoVisibility(View.VISIBLE)
                holder.binding.reactionImagePreview.setMemoVisibility(View.GONE)
                holder.binding.reactionStringPreview.text = item.code
            }
        }
        holder.binding.root.setOnClickListener {
            onEmojiSelected(item)
        }
        holder.binding.root.setOnLongClickListener {
            onEmojiLongClicked(item)
        }
        holder.binding.executePendingBindings()
    }
}