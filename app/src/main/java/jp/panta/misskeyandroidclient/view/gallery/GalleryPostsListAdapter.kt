package jp.panta.misskeyandroidclient.view.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.databinding.ItemGalleryPostBinding
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostState

class GalleryPostsListAdapter(
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<GalleryPostState, GalleryPostViewHolder>(GalleryPostDiffItemCallback) {
    override fun onBindViewHolder(holder: GalleryPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryPostViewHolder {
        return GalleryPostViewHolder(ItemGalleryPostBinding.inflate(LayoutInflater.from(parent.context), parent, false), lifecycleOwner)
    }
}

class GalleryPostViewHolder(
    private val itemGalleryPostBinding: ItemGalleryPostBinding,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(itemGalleryPostBinding.root) {
    fun bind(galleryPostState: GalleryPostState) {
        itemGalleryPostBinding.galleryPostState = galleryPostState
        itemGalleryPostBinding.lifecycleOwner = lifecycleOwner
        itemGalleryPostBinding.executePendingBindings()
    }
}

object GalleryPostDiffItemCallback : DiffUtil.ItemCallback<GalleryPostState>() {
    override fun areContentsTheSame(oldItem: GalleryPostState, newItem: GalleryPostState): Boolean {
        return oldItem.galleryPost == newItem.galleryPost
    }

    override fun areItemsTheSame(oldItem: GalleryPostState, newItem: GalleryPostState): Boolean {
        return oldItem.galleryPost.id == newItem.galleryPost.id
    }
}