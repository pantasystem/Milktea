package jp.panta.misskeyandroidclient.view.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemListAddUserBinding
import jp.panta.misskeyandroidclient.databinding.ItemListBinding
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.viewmodel.list.ListListViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListPullPushUserViewModel

class ListListAdapter(
    private val listListViewModel: ListListViewModel,
    val lifecycleOwner: LifecycleOwner,
    var onTryToEditCallback: OnTryToEditCallback? = null
) : ListAdapter<UserList, ListListAdapter.BaseVH>(
    ItemCallback()
){

    abstract class BaseVH(view: View) : RecyclerView.ViewHolder(view){
        abstract fun bind(userList: UserList)
    }

    inner class VH(val binding: ItemListBinding) : BaseVH(binding.root){
        override fun bind(userList: UserList) {
            binding.list = userList
            binding.lifecycleOwner = lifecycleOwner
            binding.tryToEditCallback = onTryToEditCallback
            binding.listListViewModel = listListViewModel
            binding.executePendingBindings()
        }
    }

    inner class AddUserVH(val binding: ItemListAddUserBinding) : BaseVH(binding.root){
        override fun bind(userList: UserList) {
            binding.userList = userList
            binding.addUserId = addUserId
            binding.pullPushViewModel = pullPushUserViewModel
            binding.executePendingBindings()
        }
    }

    class ItemCallback : DiffUtil.ItemCallback<UserList>(){
        override fun areContentsTheSame(oldItem: UserList, newItem: UserList): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: UserList, newItem: UserList): Boolean {
            return oldItem.id == newItem.id
        }
    }

    interface OnTryToEditCallback{

        fun onEdit(userList: UserList?)
    }

    private var addUserId: String? = null
    private var pullPushUserViewModel: UserListPullPushUserViewModel? = null

    constructor(
        listListViewModel: ListListViewModel,
        lifecycleOwner: LifecycleOwner,
        onTryToEditCallback: OnTryToEditCallback?,
        addUserId: String,
        pullPushUserViewModel: UserListPullPushUserViewModel)
    :this(listListViewModel, lifecycleOwner, onTryToEditCallback){
        this.addUserId = addUserId
        this.pullPushUserViewModel = pullPushUserViewModel
    }

    override fun getItemViewType(position: Int): Int {
        return if(addUserId == null || pullPushUserViewModel == null){
            // ユーザーをリストに加える pull or push
            1
        }else{
            // ユーザーリストを一覧表示する
            2
        }
    }

    override fun onBindViewHolder(holder: BaseVH, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            1 ->{
                val binding = DataBindingUtil.inflate<ItemListAddUserBinding>(inflater, R.layout.item_list_add_user, parent, false)
                AddUserVH(binding)
            }
            2 ->{
                val binding = DataBindingUtil.inflate<ItemListBinding>(inflater, R.layout.item_list, parent, false)
                VH(binding)
            }
            else -> throw IllegalStateException("ViewTypeは1 2しか許可されていません。")
        }
    }
}