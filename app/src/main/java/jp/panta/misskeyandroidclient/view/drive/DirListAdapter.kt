package jp.panta.misskeyandroidclient.view.drive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemDirBinding
import jp.panta.misskeyandroidclient.viewmodel.drive.Directory
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel

class DirListAdapter(itemCallBack: DiffUtil.ItemCallback<Directory>, private val driveViewModel: DriveViewModel) : ListAdapter<Directory, DirListAdapter.DirViewHolder>(itemCallBack){
    class DirViewHolder(val binding: ItemDirBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirViewHolder {
        val binding = DataBindingUtil.inflate<ItemDirBinding>(LayoutInflater.from(parent.context), R.layout.item_dir, parent, false)
        return DirViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DirViewHolder, position: Int) {
        holder.binding.directory = getItem(position)
        holder.binding.driveViewModel = driveViewModel
        holder.binding.executePendingBindings()
    }
}