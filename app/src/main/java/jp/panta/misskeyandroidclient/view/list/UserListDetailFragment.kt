package jp.panta.misskeyandroidclient.view.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentListUserListBinding
import jp.panta.misskeyandroidclient.viewmodel.list.UserListDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class UserListDetailFragment : Fragment(R.layout.fragment_list_user_list){

    private val binding: FragmentListUserListBinding by dataBinding()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[UserListDetailViewModel::class.java]
        binding.listUserList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ListUserListAdapter(viewModel, viewLifecycleOwner)
        binding.listUserList.adapter = adapter
        viewModel.listUsers.observe(viewLifecycleOwner, { list ->
            adapter.submitList(list)
        })
    }
}