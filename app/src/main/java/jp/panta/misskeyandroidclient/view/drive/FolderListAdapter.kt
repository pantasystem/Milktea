package jp.panta.misskeyandroidclient.view.drive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemFolderBinding
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewModel


class FolderListAdapter(
    diffUtilItemCallback: DiffUtil.ItemCallback<FolderViewData>,
    private val driveViewModel: DriveViewModel,
    private val folderViewModel: FolderViewModel
) : ListAdapter<FolderViewData, FolderListAdapter.FolderViewHolder>(diffUtilItemCallback){

    class FolderViewHolder(val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = DataBindingUtil.inflate<ItemFolderBinding>(LayoutInflater.from(parent.context), R.layout.item_folder, parent, false)
        return FolderViewHolder(binding)
    }
    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {

        holder.binding.folder = getItem(position)
        holder.binding.driveViewModel =  driveViewModel
        holder.binding.folderViewModel = folderViewModel
        holder.binding.executePendingBindings()
    }
}