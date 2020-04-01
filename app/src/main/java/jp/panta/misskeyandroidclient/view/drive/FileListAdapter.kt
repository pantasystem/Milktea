package jp.panta.misskeyandroidclient.view.drive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemFileBinding
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel

class FileListAdapter(
    diffUtilItemCallback: DiffUtil.ItemCallback<FileViewData>,
    private val fileViewModel: FileViewModel,
    private val driveViewModel: DriveViewModel,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<FileViewData, FileListAdapter.FileViewHolder>(diffUtilItemCallback){
    class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.fileViewModel = fileViewModel
        holder.binding.fileViewData = item
        holder.binding.driveViewModel = driveViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = DataBindingUtil.inflate<ItemFileBinding>(LayoutInflater.from(parent.context), R.layout.item_file, parent, false)
        return FileViewHolder(binding)
    }
}