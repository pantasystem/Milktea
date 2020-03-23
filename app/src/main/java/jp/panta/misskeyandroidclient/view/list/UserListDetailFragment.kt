package jp.panta.misskeyandroidclient.view.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.list.UserListDetailViewModel
import kotlinx.android.synthetic.main.fragment_list_user_list.*

class UserListDetailFragment : Fragment(R.layout.fragment_list_user_list){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?: return

        val miCore = view.context.applicationContext as MiCore
        miCore.currentAccount.observe(viewLifecycleOwner, Observer {
            val viewModel = ViewModelProvider(activity!!)[UserListDetailViewModel::class.java]
            listUserList.layoutManager = LinearLayoutManager(view.context)
            val adapter = ListUserListAdapter(viewModel, viewLifecycleOwner)
            listUserList.adapter = adapter
            viewModel.listUsers.observe(viewLifecycleOwner, Observer{
                adapter.submitList(it)
            })
            viewModel.load()
        })
    }
}