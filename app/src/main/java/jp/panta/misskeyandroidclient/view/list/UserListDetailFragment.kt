package jp.panta.misskeyandroidclient.view.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.list.UserListDetailViewModel
import kotlinx.android.synthetic.main.fragment_list_user_list.*

class UserListDetailFragment : Fragment(R.layout.fragment_list_user_list){
    companion object{

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[UserListDetailViewModel::class.java]
        listUserList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ListUserListAdapter(viewModel, viewLifecycleOwner)
        listUserList.adapter = adapter
        viewModel.listUsers.observe(viewLifecycleOwner, Observer{ list ->
            adapter.submitList(list)
        })
    }
}