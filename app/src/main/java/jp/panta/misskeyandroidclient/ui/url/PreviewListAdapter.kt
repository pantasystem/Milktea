package jp.panta.misskeyandroidclient.ui.url

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemFilePreviewBinding
import jp.panta.misskeyandroidclient.databinding.ItemUrlPreviewBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.Preview

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
            binding.urlPreview = preview.urlPreview
            val context = this.binding.urlPreviewView.context
            binding.urlPreviewView.setOnClickListener {
                context?.startActivity(
                    Intent(Intent.ACTION_VIEW).apply{
                        data = Uri.parse(preview.urlPreview.url)
                    }
                )
            }

        }
    }
    class FilePreviewViewHolder(val binding: ItemFilePreviewBinding) : BaseHolder<Preview.FileWrapper>(binding.root){
        override fun bind(preview: Preview.FileWrapper) {
            binding.file = preview.file
            val context = this.binding.filePropertyView.context
            binding.filePropertyView.setOnClickListener {
                if(preview.file.type?.startsWith("audio") == true){
                    val intent = Intent(binding.root.context, MediaActivity::class.java)
                    intent.putExtra(MediaActivity.EXTRA_FILE, preview.file)
                    context?.startActivity(intent)
                }else{
                    context?.startActivity(
                        Intent().apply{
                            data = Uri.parse(preview.file.path)
                            type = preview.file.type
                        }
                    )
                }

            }

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
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_url_preview,
                        parent,
                        false
                    )
                )
            }
            1 ->{
                return FilePreviewViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_file_preview,
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("0, 1しか想定されていません")
        }
    }
}