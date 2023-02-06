package net.pantasystem.milktea.note.reaction.choices

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common_android.resource.convertDp2Px
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.SegmentType
import net.pantasystem.milktea.note.databinding.ItemCategoryWithListBinding
import kotlin.math.max


class EmojiChoicesListAdapter(
    val onEmojiSelected: (EmojiType) -> Unit,
) : ListAdapter<SegmentType, SegmentViewHolder>(
    object : DiffUtil.ItemCallback<SegmentType>() {
        override fun areContentsTheSame(oldItem: SegmentType, newItem: SegmentType): Boolean {
            return when (oldItem) {
                is SegmentType.Category -> oldItem.name == (newItem as? SegmentType.Category)?.name
                        && oldItem.emojis == newItem.emojis
                is SegmentType.OftenUse -> oldItem.emojis == newItem.emojis
                is SegmentType.OtherCategory -> oldItem.emojis == newItem.emojis
                is SegmentType.RecentlyUsed -> oldItem.emojis == newItem.emojis
                is SegmentType.UserCustom -> oldItem.emojis == newItem.emojis
            }
        }

        override fun areItemsTheSame(oldItem: SegmentType, newItem: SegmentType): Boolean {
            return when (oldItem) {
                is SegmentType.Category -> oldItem.name == (newItem as? SegmentType.Category)?.name
                is SegmentType.OftenUse -> oldItem == newItem
                is SegmentType.OtherCategory -> oldItem == newItem
                is SegmentType.RecentlyUsed -> oldItem == newItem
                is SegmentType.UserCustom -> oldItem == newItem
            }
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
    private val onEmojiSelected: (EmojiType) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    var isSatLayoutManager = false

    fun onBind(segmentType: SegmentType) {
        val adapter = EmojiChoicesAdapter(onEmojiSelected)
        val label = segmentType.label.getString(binding.root.context)
        binding.categoryName.text = label

        if (!isSatLayoutManager) {
            val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val layoutManager =
                        GridLayoutManager(binding.root.context, max(calculateSpanCount(), 4))

                    binding.emojisView.layoutManager = layoutManager
                    binding.emojisView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    isSatLayoutManager = true
                }
            }
            binding.emojisView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        }

        if (!isSatLayoutManager) {
            val layoutManager =
                GridLayoutManager(binding.root.context, 4)

            binding.emojisView.layoutManager = layoutManager
        }


        adapter.submitList(segmentType.emojis)
        binding.emojisView.isNestedScrollingEnabled = false

        binding.emojisView.adapter = adapter
    }

    private fun calculateSpanCount(): Int {
        val viewWidth = binding.emojisView.measuredWidth
        val itemWidth = binding.root.context.convertDp2Px(54f).toInt()
        return viewWidth / itemWidth
    }

}