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

    var isSatLayoutManager = false

    val adapter = EmojiChoicesAdapter(onEmojiSelected)
    fun onBind(segmentType: SegmentType) {
        val label = segmentType.label.getString(binding.root.context)
        binding.categoryName.text = label

        if (!isSatLayoutManager) {
            val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val layoutManager = GridLayoutManager(binding.root.context, calculateSpanCount())

                    binding.emojisView.layoutManager = layoutManager
                    binding.emojisView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    isSatLayoutManager = true
                }
            }
            binding.emojisView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        }


        adapter.submitList(segmentType.emojis)
        binding.emojisView.isNestedScrollingEnabled = false

        binding.emojisView.adapter = adapter
    }

    private fun calculateSpanCount(): Int {
        val viewWidth = binding.emojisView.measuredWidth
        val itemWidth =  binding.root.context.convertDp2Px(54f).toInt()
        return viewWidth / itemWidth
    }

}