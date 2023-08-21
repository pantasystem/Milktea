package net.pantasystem.milktea.note.url

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import net.pantasystem.milktea.common_android.ui.CircleOutlineHelper.setCircleOutline
import net.pantasystem.milktea.common_android_ui.NavigationEntryPointForBinding
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.common_resource.R
import net.pantasystem.milktea.note.databinding.ItemFilePreviewBinding
import net.pantasystem.milktea.note.databinding.ItemUrlPreviewBinding
import net.pantasystem.milktea.note.url.UrlPreviewHelper.setSiteIcon
import net.pantasystem.milktea.note.url.UrlPreviewHelper.setUrlPreviewThumbnail
import net.pantasystem.milktea.note.viewmodel.Preview

class PreviewListAdapter : ListAdapter<Preview, RecyclerView.ViewHolder>(ItemCallback()){
    class ItemCallback : DiffUtil.ItemCallback<Preview>(){
        override fun areContentsTheSame(oldItem: Preview, newItem: Preview): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Preview, newItem: Preview): Boolean {
            return oldItem == newItem
        }
    }


    abstract class BaseHolder< T: Preview>(view: View) : RecyclerView.ViewHolder(view){
        abstract fun bind(preview: T)
    }

    class ViewHolder(val binding: ItemUrlPreviewBinding) : BaseHolder<Preview.UrlWrapper>(binding.root){

        override fun bind(preview: Preview.UrlWrapper) {
            val context = this.binding.urlPreviewView.context
            binding.urlPreviewView.setOnClickListener {
                context?.startActivity(
                    Intent(Intent.ACTION_VIEW).apply{
                        data = Uri.parse(preview.urlPreview.url)
                    }
                )
            }

            binding.urlPreviewView.setCircleOutline(7)
            binding.siteThumbnailView.setUrlPreviewThumbnail(preview.urlPreview.thumbnail)
            binding.siteTitleView.text = preview.urlPreview.title
            binding.siteDescription.text = preview.urlPreview.description
            binding.siteIconView.setSiteIcon(preview.urlPreview.icon)
            binding.siteNameView.text = preview.urlPreview.siteName

        }
    }
    class FilePreviewViewHolder(val binding: ItemFilePreviewBinding) : BaseHolder<Preview.FileWrapper>(binding.root){
        @SuppressLint("IntentReset")
        override fun bind(preview: Preview.FileWrapper) {
            val context = this.binding.filePropertyView.context
            binding.filePropertyView.setOnClickListener {
                if(preview.file.type.startsWith("audio")){
                    val activity = FragmentComponentManager.findActivity(binding.root.context)
                    if (activity is Activity) {
                        val accessor = EntryPointAccessors.fromActivity(activity, NavigationEntryPointForBinding::class.java)
                        val intent = accessor.mediaNavigation().newIntent(MediaNavigationArgs.AFile(
                            preview.file
                        ))
                        context?.startActivity(intent)
                    }

                }else{
                    try {
                        context?.startActivity(
                            Intent().apply{
                                data = Uri.parse(preview.file.path)
                                type = preview.file.type
                            }
                        )
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, context.getString(R.string.no_app_available_to_open_this_file), Toast.LENGTH_SHORT).show()
                    }

                }

            }

            binding.fileThumbnailView.setUrlPreviewThumbnail(preview.file.thumbnailUrl)
            binding.fileNameView.text = preview.file.name
            binding.fileTypeView.text = preview.file.type

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is Preview.UrlWrapper -> 0
            is Preview.FileWrapper -> 1
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder){
            is ViewHolder ->{
                holder.bind(getItem(position) as Preview.UrlWrapper)
            }
            is FilePreviewViewHolder ->{
                holder.bind(getItem(position) as Preview.FileWrapper)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            0 ->{
                ViewHolder(
                    ItemUrlPreviewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            1 ->{
                return FilePreviewViewHolder(
                    ItemFilePreviewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("0, 1しか想定されていません")
        }
    }
}