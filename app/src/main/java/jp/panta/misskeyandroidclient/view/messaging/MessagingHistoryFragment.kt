package jp.panta.misskeyandroidclient.view.messaging

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MessageActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.messaging.HistoryViewData
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageHistoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageHistoryViewModelFactory
import kotlinx.android.synthetic.main.fragment_messaging_history.*

class MessagingHistoryFragment : Fragment(R.layout.fragment_messaging_history){

    private var mLinearLayoutManager: LinearLayoutManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        history_list_view.layoutManager = layoutManager
        mLinearLayoutManager = layoutManager

        val miApplication = context?.applicationContext as MiApplication
        miApplication.currentAccount.observe(viewLifecycleOwner, Observer {ar ->

            val historyViewModel = ViewModelProvider(this, MessageHistoryViewModelFactory(ar, miApplication))["$ar", MessageHistoryViewModel::class.java]

            val adapter = HistoryListAdapter(diffUtilItemCallback, historyViewModel, viewLifecycleOwner)
            history_list_view.adapter = adapter


            historyViewModel.historyGroupAndUserLiveData.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
            })

            historyViewModel.isRefreshing.observe(viewLifecycleOwner, Observer {
                refresh.isRefreshing = it
            })

            refresh.setOnRefreshListener {
                historyViewModel.loadGroupAndUser()
            }

            historyViewModel.messageHistorySelected.observe(viewLifecycleOwner, Observer { hvd ->
                Handler(Looper.getMainLooper()).post{
                    val intent = Intent(activity, MessageActivity::class.java)
                    intent.putExtra(MessageActivity.EXTRA_MESSAGING_ID, hvd.messagingId)
                    startActivity(intent)
                }
            })

            historyViewModel.loadGroupAndUser()


        })

    }


    val diffUtilItemCallback = object : DiffUtil.ItemCallback<HistoryViewData>(){
        override fun areContentsTheSame(
            oldItem: HistoryViewData,
            newItem: HistoryViewData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: HistoryViewData, newItem: HistoryViewData): Boolean {
            return oldItem.id == newItem.id
        }
    }


}