package jp.panta.misskeyandroidclient.ui.antenna

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentAntennaListBinding
import jp.panta.misskeyandroidclient.ui.antenna.viewmodel.AntennaListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AntennaListFragment : Fragment(R.layout.fragment_antenna_list){

    val binding: FragmentAntennaListBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApplication = view.context.applicationContext as MiApplication

        val layoutManager = LinearLayoutManager(view.context)

        val antennaViewModel = ViewModelProvider(requireActivity() , AntennaListViewModel.Factory(miApplication))[AntennaListViewModel::class.java]

        val adapter = AntennaListAdapter(antennaViewModel, viewLifecycleOwner)
        binding.antennaListView.adapter = adapter
        binding.antennaListView.layoutManager = layoutManager

        antennaViewModel.antennas.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.antennaListSwipeRefresh.setOnRefreshListener {
            antennaViewModel.loadInit()
        }

        antennaViewModel.isLoading.observe(viewLifecycleOwner, {
            binding.antennaListSwipeRefresh.isRefreshing = it
        })


    }
}