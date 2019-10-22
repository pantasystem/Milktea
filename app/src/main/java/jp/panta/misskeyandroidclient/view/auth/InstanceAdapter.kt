package jp.panta.misskeyandroidclient.view.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemInstanceBinding
import jp.panta.misskeyandroidclient.model.auth.Instance
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel

class InstanceAdapter(diffUtil: DiffUtil.ItemCallback<Instance>, val authViewModel: AuthViewModel) : ListAdapter<Instance, InstanceAdapter.InstanceViewHolder>(diffUtil){
    class InstanceViewHolder(val binding: ItemInstanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: InstanceViewHolder, position: Int) {
        holder.binding.instance = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstanceViewHolder {
        val binding = DataBindingUtil.inflate<ItemInstanceBinding>(LayoutInflater.from(parent.context), R.layout.item_instance, parent, false)
        binding.authViewModel = authViewModel
        return InstanceViewHolder(binding)
    }
}