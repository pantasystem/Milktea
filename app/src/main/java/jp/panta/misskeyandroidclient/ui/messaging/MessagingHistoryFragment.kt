package jp.panta.misskeyandroidclient.ui.messaging

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MessageActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentMessagingHistoryBinding
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.HistoryViewData
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageHistoryViewModel
import net.pantasystem.milktea.common.StateContent


@AndroidEntryPoint
class MessagingHistoryFragment : Fragment(R.layout.fragment_messaging_history) {

    private var mLinearLayoutManager: LinearLayoutManager? = null

    private val binding: FragmentMessagingHistoryBinding by dataBinding()

    private val historyViewModel: MessageHistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        binding.historyListView.layoutManager = layoutManager
        mLinearLayoutManager = layoutManager

        val adapter =
            HistoryListAdapter(diffUtilItemCallback, historyViewModel, viewLifecycleOwner)
        binding.historyListView.adapter = adapter
        lifecycleScope.launchWhenResumed {
            historyViewModel.loadGroupAndUser()
            historyViewModel.histories.collect { resultState ->
                Log.d("MsgHistoryFragment", "msg:$resultState")
                when(val content = resultState.content) {
                    is StateContent.Exist -> {
                        adapter.submitList(content.rawContent)
                    }
                    is StateContent.NotExist -> {
                        adapter.submitList(emptyList())
                    }
                }

            }
        }

        historyViewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.refresh.isRefreshing = it
        }


        binding.refresh.setOnRefreshListener {
            historyViewModel.loadGroupAndUser()
        }

        historyViewModel.messageHistorySelected.observe(viewLifecycleOwner) { hvd ->
            Handler(Looper.getMainLooper()).post {
                val intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra(MessageActivity.EXTRA_MESSAGING_ID, hvd.messagingId)
                startActivity(intent)
            }
        }


    }


    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<HistoryViewData>() {
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