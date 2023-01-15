package net.pantasystem.milktea.note.reaction.choices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.SegmentType
import net.pantasystem.milktea.note.databinding.ItemCategoryWithListBinding

class EmojiChoicesListAdapter(
    val onEmojiSelected: (EmojiType) -> Unit,
) : ListAdapter<SegmentType, SegmentViewHolder>(
    object : DiffUtil.ItemCallback<SegmentType>() {
        override fun areContentsTheSame(oldItem: SegmentType, newItem: SegmentType): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: SegmentType, newItem: SegmentType): Boolean {
            return oldItem.label == newItem.label && oldItem.javaClass == newItem.javaClass
        }
    }
) {

    override fun onBindViewHolder(holder: SegmentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SegmentViewHolder {
        return SegmentViewHolder(
            ItemCategoryWithListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onEmojiSelected
        )
    }
}

class SegmentViewHolder(
    val binding: ItemCategoryWithListBinding,
    onEmojiSelected: (EmojiType) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    val adapter = EmojiChoicesAdapter(onEmojiSelected)
    fun onBind(segmentType: SegmentType) {
        val label = segmentType.label.getString(binding.root.context)
        binding.categoryName.text = label

        val flexBoxLayoutManager = FlexboxLayoutManager(binding.root.context)
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        binding.emojisView.layoutManager = flexBoxLayoutManager
        adapter.submitList(segmentType.emojis)
        binding.emojisView.isNestedScrollingEnabled = false

        binding.emojisView.adapter = adapter
    }
}