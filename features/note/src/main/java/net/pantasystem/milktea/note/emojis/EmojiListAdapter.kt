package net.pantasystem.milktea.note.emojis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemCustomEmojiChoiceBinding
import net.pantasystem.milktea.note.databinding.ItemEmojisCategoryBinding
import net.pantasystem.milktea.note.databinding.ItemTextEmojiChoiceBinding
import net.pantasystem.milktea.note.emojis.viewmodel.EmojiSelection
import net.pantasystem.milktea.note.emojis.viewmodel.Emojis

class EmojiListAdapter(
    private val emojiSelection: EmojiSelection,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<Emojis, EmojiListAdapter.VH>(ItemCallback()){

    abstract class VH(view: View) : RecyclerView.ViewHolder(view){
        abstract fun bind(emoji: Emojis)
        open fun setSelection(emojiSelection: EmojiSelection) = Unit
        open fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) = Unit
    }
    class CategoryVH(val binding: ItemEmojisCategoryBinding) : VH(binding.root){
        override fun bind(emoji: Emojis) {
            if(emoji is Emojis.EmojiCategory){
                binding.emojiCategory = emoji
            }
        }
    }

    class CustomEmojiVH(val binding: ItemCustomEmojiChoiceBinding) : VH(binding.root){
        override fun bind(emoji: Emojis) {
            if(emoji is Emojis.CustomEmoji){
                binding.emoji = emoji.emoji
            }
        }

        override fun setSelection(emojiSelection: EmojiSelection) {
            binding.customEmojiSelection = emojiSelection
        }

        override fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
            binding.lifecycleOwner = lifecycleOwner
        }
    }

    class TextEmojiVH(val binding: ItemTextEmojiChoiceBinding) : VH(binding.root){
        override fun bind(emoji: Emojis) {
            if(emoji is Emojis.TextEmoji){
                binding.emoji = emoji.text
            }
        }

        override fun setSelection(emojiSelection: EmojiSelection) {
            binding.emojiSelection = emojiSelection
        }

        override fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
            binding.lifecycleOwner = lifecycleOwner
        }
    }

    class ItemCallback : DiffUtil.ItemCallback<Emojis>(){

        override fun areContentsTheSame(oldItem: Emojis, newItem: Emojis): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Emojis, newItem: Emojis): Boolean {
            return oldItem == newItem
        }
    }

    private enum class ViewType{
        CUSTOM,
        TEXT,
        CATEGORY
    }
    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is Emojis.TextEmoji -> ViewType.TEXT.ordinal
            is Emojis.CustomEmoji -> ViewType.CUSTOM.ordinal
            is Emojis.EmojiCategory -> ViewType.CATEGORY.ordinal
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when(val item = getItem(position)){
            is Emojis.TextEmoji -> {
                holder.bind(item)
                holder.setSelection(emojiSelection)
            }
            is Emojis.CustomEmoji -> {
                holder.bind(item)
                holder.setSelection(emojiSelection)
            }
            is Emojis.EmojiCategory -> {
                holder.bind(item)
            }
        }
        holder.setLifecycleOwner(lifecycleOwner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            ViewType.CUSTOM.ordinal ->{
                CustomEmojiVH(
                    DataBindingUtil.inflate(inflater, R.layout.item_custom_emoji_choice, parent, false)
                )
            }
            ViewType.CATEGORY.ordinal ->{
                CategoryVH(
                    DataBindingUtil.inflate(inflater, R.layout.item_emojis_category, parent, false)
                )
            }
            ViewType.TEXT.ordinal ->{
                TextEmojiVH(
                    DataBindingUtil.inflate(inflater, R.layout.item_text_emoji_choice, parent, false)
                )
            }
            else -> throw IllegalArgumentException("実装されていないViewTypeです")
        }
    }


}