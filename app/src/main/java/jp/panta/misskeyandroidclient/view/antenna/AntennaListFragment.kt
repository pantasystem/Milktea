package jp.panta.misskeyandroidclient.view.antenna

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.AntennaEditorActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaListViewModel
import kotlinx.android.synthetic.main.fragment_antenna_list.*

class AntennaListFragment : Fragment(R.layout.fragment_antenna_list){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApplication = view.context.applicationContext as MiApplication

        val layoutManager = LinearLayoutManager(view.context)

        val antennaViewModel = ViewModelProvider(requireActivity() , AntennaListViewModel.Factory(miApplication))[AntennaListViewModel::class.java]

        val adapter = AntennaListAdapter(antennaViewModel, viewLifecycleOwner)
        antennaListView.adapter = adapter
        antennaListView.layoutManager = layoutManager

        antennaViewModel.antennas.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        antennaListSwipeRefresh.setOnRefreshListener {
            antennaViewModel.loadInit()
        }

        antennaViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            antennaListSwipeRefresh.isRefreshing = it
        })


    }
}