package jp.panta.misskeyandroidclient.view.messaging

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MessageActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentMessagingHistoryBinding
import jp.panta.misskeyandroidclient.viewmodel.messaging.HistoryViewData
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageHistoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageHistoryViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class MessagingHistoryFragment : Fragment(R.layout.fragment_messaging_history){

    private var mLinearLayoutManager: LinearLayoutManager? = null

    private val binding: FragmentMessagingHistoryBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding.historyListView.layoutManager = layoutManager
        mLinearLayoutManager = layoutManager

        val miApplication = context?.applicationContext as MiApplication
        val historyViewModel = ViewModelProvider(
            this,
            MessageHistoryViewModelFactory(miApplication)
        )[MessageHistoryViewModel::class.java]

        val adapter =
            HistoryListAdapter(diffUtilItemCallback, historyViewModel, viewLifecycleOwner)
        binding.historyListView.adapter = adapter
        lifecycleScope.launchWhenResumed {
            historyViewModel.loadGroupAndUser()
            historyViewModel.histories.collect {
                Log.d("MsgHistoryFragment", "msg:$it")
                adapter.submitList(it)
            }
        }

        historyViewModel.isRefreshing.observe(viewLifecycleOwner, {
            binding.refresh.isRefreshing = it
        })


        binding.refresh.setOnRefreshListener {
            historyViewModel.loadGroupAndUser()
        }

        historyViewModel.messageHistorySelected.observe(viewLifecycleOwner, { hvd ->
            Handler(Looper.getMainLooper()).post {
                val intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra(MessageActivity.EXTRA_MESSAGING_ID, hvd.messagingId)
                startActivity(intent)
            }
        })


    }


    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<HistoryViewData>(){
        override fun areContentsTheSame(
            oldItem: HistoryViewData,
            newItem: HistoryViewData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: HistoryViewData, newItem: HistoryViewData): Boolean {
            return oldItem.messagingId == newItem.messagingId
        }
    }


}