package net.pantasystem.milktea.userlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.userlist.databinding.FragmentListUserListBinding
import net.pantasystem.milktea.userlist.viewmodel.UserListDetailViewModel

@FlowPreview
@ExperimentalCoroutinesApi
class UserListDetailFragment : Fragment(R.layout.fragment_list_user_list) {

    private val binding: FragmentListUserListBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[UserListDetailViewModel::class.java]
        binding.listUserList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ListUserListAdapter(viewModel, viewLifecycleOwner)
        binding.listUserList.adapter = adapter
        viewModel.listUsers.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }
}