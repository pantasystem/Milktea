package net.pantasystem.milktea.antenna

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.antenna.databinding.FragmentAntennaListBinding
import net.pantasystem.milktea.antenna.viewmodel.AntennaListViewModel
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AntennaListFragment : Fragment(R.layout.fragment_antenna_list){

    val binding: FragmentAntennaListBinding by dataBinding()
    private val antennaViewModel: AntennaListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(view.context)


        val adapter = AntennaListAdapter(antennaViewModel, viewLifecycleOwner)
        binding.antennaListView.adapter = adapter
        binding.antennaListView.layoutManager = layoutManager

        antennaViewModel.uiState.onEach { uiState ->
            val antennas = when(val content = uiState.antennas.content) {
                is StateContent.Exist -> content.rawContent
                is StateContent.NotExist -> emptyList()
            }
            adapter.submitList(antennas)

            val isLoading = uiState.antennas is ResultState.Loading
            binding.antennaListSwipeRefresh.isRefreshing = isLoading
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle).launchIn(viewLifecycleOwner.lifecycleScope)

        binding.antennaListSwipeRefresh.setOnRefreshListener {
            antennaViewModel.loadInit()
        }
    }
}