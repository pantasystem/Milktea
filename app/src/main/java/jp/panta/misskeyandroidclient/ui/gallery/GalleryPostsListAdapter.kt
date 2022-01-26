package jp.panta.misskeyandroidclient.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemGalleryPhotoBinding
import jp.panta.misskeyandroidclient.databinding.ItemGalleryPostBinding
import jp.panta.misskeyandroidclient.ui.ViewDataBindingSimpleRecyclerViewAdapter
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostState
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostsViewModel

class GalleryPostsListAdapter(
    val lifecycleOwner: LifecycleOwner,
    private val galleryPostsViewModel: GalleryPostsViewModel
) : ListAdapter<GalleryPostState, GalleryPostViewHolder>(GalleryPostDiffItemCallback) {
    override fun onBindViewHolder(holder: GalleryPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryPostViewHolder {
        return GalleryPostViewHolder(ItemGalleryPostBinding.inflate(LayoutInflater.from(parent.context), parent, false), lifecycleOwner, galleryPostsViewModel)
    }
}

class GalleryPostViewHolder(
    private val itemGalleryPostBinding: ItemGalleryPostBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val galleryPostsViewModel: GalleryPostsViewModel
) : RecyclerView.ViewHolder(itemGalleryPostBinding.root) {
    fun bind(galleryPostState: GalleryPostState) {
        itemGalleryPostBinding.galleryPostState = galleryPostState
        itemGalleryPostBinding.lifecycleOwner = lifecycleOwner

        itemGalleryPostBinding.galleryImagePager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        val adapter = ViewDataBindingSimpleRecyclerViewAdapter<FileViewData, ItemGalleryPhotoBinding>(
            onBind = { b, f ->
                b.fileViewData = f
                b.fileViewDataList = galleryPostState.fileViewDataList
            },
            R.layout.item_gallery_photo,
            key = {
                it.file.path to it.file.remoteFileId
            },
            onDeepEqual = { new, old ->
                new.file == old.file
                        && new.isHiding == old.isHiding
            },
            lifecycleOwner = lifecycleOwner
        )

        adapter.submitList(galleryPostState.fileViewDataList)
        itemGalleryPostBinding.galleryPostsViewModel = galleryPostsViewModel

        itemGalleryPostBinding.galleryImagePager.adapter = adapter
        TabLayoutMediator(itemGalleryPostBinding.imagesTab, itemGalleryPostBinding.galleryImagePager) { _, _ ->

        }.attach()

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