package net.pantasystem.milktea.note.reaction.choices

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.note.EmojiListItemType
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemEmojiChoiceBinding
import net.pantasystem.milktea.note.databinding.ItemEmojiListItemHeaderBinding
import net.pantasystem.milktea.note.reaction.CustomEmojiImageViewSizeHelper.applySizeByAspectRatio
import net.pantasystem.milktea.note.reaction.ImageAspectRatioCache
import net.pantasystem.milktea.note.reaction.SaveImageAspectRequestListener

class EmojiListItemsAdapter(
    private val isApplyImageAspectRatio: Boolean,
    private val onEmojiSelected: (EmojiType) -> Unit,
    private val onEmojiLongClicked: (EmojiType) -> Boolean,
    private val baseItemSizeDp: Int = 28,
) : ListAdapter<EmojiListItemType, EmojiListItemsAdapter.VH>(
    DiffUtilItemCallback()
) {
    class DiffUtilItemCallback : DiffUtil.ItemCallback<EmojiListItemType>() {


        override fun areContentsTheSame(
            oldItem: EmojiListItemType,
            newItem: EmojiListItemType,
        ): Boolean {
            if (oldItem is EmojiListItemType.EmojiItem && newItem is EmojiListItemType.EmojiItem) {
                return oldItem.emoji.areContentsTheSame(newItem.emoji)
            }
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: EmojiListItemType,
            newItem: EmojiListItemType,
        ): Boolean {
            if (oldItem is EmojiListItemType.EmojiItem && newItem is EmojiListItemType.EmojiItem) {
                return oldItem.emoji.areItemsTheSame(newItem.emoji)
            }
            return oldItem == newItem
        }

    }

    sealed class VH(view: View) : RecyclerView.ViewHolder(view)
    class EmojiVH(
        val binding: ItemEmojiChoiceBinding,
        private val isApplyImageAspectRatio: Boolean,
        private val baseItemSizeDp: Int,
    ) : VH(binding.root) {

        fun onBind(
            item: EmojiType, onEmojiSelected: (EmojiType) -> Unit,
            onEmojiLongClicked: (EmojiType) -> Boolean,
        ) {
            when (item) {
                is EmojiType.CustomEmoji -> {
                    if (isApplyImageAspectRatio) {
                        binding.reactionImagePreview.applySizeByAspectRatio<LinearLayout.LayoutParams>(
                            baseItemSizeDp,
                            item.emoji.aspectRatio ?: ImageAspectRatioCache.get(
                                item.emoji.url ?: item.emoji.uri
                            )
                        )
                    }
                    Log.d("EmojiListAdapter", "source:${item.emoji.cachePath ?: item.emoji.url ?: item.emoji.uri}")
                    GlideApp.with(binding.reactionImagePreview)
                        .load(item.emoji.cachePath ?: item.emoji.url ?: item.emoji.uri)
                        // FIXME: webpの場合うまく表示できなくなる
//                    .centerCrop()
                        .addListener(
                            SaveImageAspectRequestListener(
                                item.emoji,
                                binding.root.context
                            )
                        )
                        .into(binding.reactionImagePreview)

                    binding.reactionStringPreview.setMemoVisibility(View.GONE)
                    binding.reactionImagePreview.setMemoVisibility(View.VISIBLE)
                }
                is EmojiType.Legacy -> {
                    binding.reactionStringPreview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, baseItemSizeDp * 0.8f)
                    binding.reactionImagePreview.setMemoVisibility(View.GONE)
                    binding.reactionStringPreview.setMemoVisibility(View.VISIBLE)
                    binding.reactionStringPreview.text =
                        requireNotNull(LegacyReaction.reactionMap[item.type])
                }
                is EmojiType.UtfEmoji -> {
                    binding.reactionStringPreview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, baseItemSizeDp * 0.8f)
                    binding.reactionStringPreview.setMemoVisibility(View.VISIBLE)
                    binding.reactionImagePreview.setMemoVisibility(View.GONE)
                    binding.reactionStringPreview.text = item.code
                }
            }
            binding.root.setOnClickListener {
                onEmojiSelected(item)
            }
            binding.root.setOnLongClickListener {
                onEmojiLongClicked(item)
            }
            binding.executePendingBindings()
        }
    }

    class HeaderVH(val binding: ItemEmojiListItemHeaderBinding) : VH(binding.root)


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EmojiListItemType.EmojiItem -> ItemType.Emoji.ordinal
            is EmojiListItemType.Header -> ItemType.Header.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        when (ItemType.values()[viewType]) {
            ItemType.Header -> {
                val binding = ItemEmojiListItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return HeaderVH(binding)
            }
            ItemType.Emoji -> {
                val binding =
                    DataBindingUtil.inflate<ItemEmojiChoiceBinding>(
                        LayoutInflater.from(parent.context),
                        R.layout.item_emoji_choice,
                        parent,
                        false
                    )
                return EmojiVH(
                    binding,
                    isApplyImageAspectRatio,
                    baseItemSizeDp
                )
            }
        }

    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (val item = getItem(position)) {
            is EmojiListItemType.EmojiItem -> {
                (holder as EmojiVH).onBind(
                    item.emoji,
                    onEmojiLongClicked = onEmojiLongClicked,
                    onEmojiSelected = onEmojiSelected
                )
            }
            is EmojiListItemType.Header -> {
                (holder as HeaderVH).binding.categoryName.text =
                    item.label.getString(holder.binding.root.context)
            }
        }

    }

    enum class ItemType {
        Header, Emoji
    }

}