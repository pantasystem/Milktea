package jp.panta.misskeyandroidclient.viewmodel.messaging

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.view.messaging.MessageListAdapter
import kotlinx.android.synthetic.main.fragment_message.*
import java.lang.IllegalArgumentException

class MessageFragment : Fragment(R.layout.fragment_message){

    companion object{
        private const val ARGS_MESSAGE_HISTORY = "MessageFragment.args_message_history"
        fun newInstance(messageHistory: Message): MessageFragment{
            val bundle = Bundle()
            bundle.putSerializable(ARGS_MESSAGE_HISTORY, messageHistory)
            return MessageFragment().apply{
                arguments = bundle
            }
        }
    }

    private var mMessageViewModel: MessageViewModel? = null
    private var mLayoutManager: LinearLayoutManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageHistory = arguments?.getSerializable(ARGS_MESSAGE_HISTORY) as Message?

        if(messageHistory == null){
            Log.e("MessageFragment", "messageHistoryがNullです")
            throw IllegalArgumentException("messageHistory must not null")

        }

        val miApplication = context?.applicationContext as MiApplication

        val lm = LinearLayoutManager(context)
        messages_view.layoutManager = lm
        mLayoutManager = lm

        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer{ci ->
            val messageViewModel = ViewModelProvider(this, MessageViewModelFactory(ci, miApplication, messageHistory))[MessageViewModel::class.java]
            mMessageViewModel = messageViewModel

            val adapter = MessageListAdapter(diffUtilItemCallback)
            messages_view.adapter = adapter

            messageViewModel.messagesLiveData.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
            })

            messageViewModel.loadInit()


        })
        messages_view.addOnScrollListener(scrollListener)

    }

    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<MessageViewData>(){
        override fun areContentsTheSame(
            oldItem: MessageViewData,
            newItem: MessageViewData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: MessageViewData, newItem: MessageViewData): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val first =mLayoutManager?.findFirstVisibleItemPosition()
            Log.d("Scrolled", "first :$first")

            if( first == 0 ){
                //esenter?.getOldMessage()
                mMessageViewModel?.loadOld()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        }
    }
}