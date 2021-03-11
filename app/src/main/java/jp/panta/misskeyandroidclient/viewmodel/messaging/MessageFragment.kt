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
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.view.messaging.MessageListAdapter
import kotlinx.android.synthetic.main.fragment_message.*
import java.lang.IllegalArgumentException

class MessageFragment : Fragment(R.layout.fragment_message){

    companion object{
        private const val ARGS_MESSAGE_HISTORY = "MessageFragment.args_message_history"
        fun newInstance(messageHistory: MessageDTO): MessageFragment{
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

        val messageHistory = arguments?.getSerializable(ARGS_MESSAGE_HISTORY) as MessageDTO?

        if(messageHistory == null){
            Log.e("MessageFragment", "messageHistoryがNullです")
            throw IllegalArgumentException("messageHistory must not null")

        }

        val miApplication = context?.applicationContext as MiApplication

        val lm = LinearLayoutManager(context)
        //lm.stackFromEnd = true
        messages_view.layoutManager = lm
        mLayoutManager = lm

        miApplication.getCurrentAccount().observe(viewLifecycleOwner, Observer{ accountRelation ->
            val messageViewModel = ViewModelProvider(this, MessageViewModelFactory(accountRelation, miApplication, messageHistory))[MessageViewModel::class.java]
            mMessageViewModel = messageViewModel

            val adapter = MessageListAdapter(diffUtilItemCallback, viewLifecycleOwner)
            messages_view.adapter = adapter

            var messageState: MessageViewModel.State? = null
            messageViewModel.messagesLiveData.observe(viewLifecycleOwner, Observer {
                messageState = it

                adapter.submitList(it.messages)

            })

            messageViewModel.loadInit()

            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    Log.d(tag, "size:${(messageState?.messages?.size)}, siStateNull:${messageState == null}")

                    when(messageState?.type){
                        MessageViewModel.State.Type.RECEIVED -> {
                            //val firstVisiblePosition = lm.findFirstVisibleItemPosition()
                            val lastVisiblePosition = lm.findLastVisibleItemPosition()
                            Log.d(tag, "lastVisiblePosition:$lastVisiblePosition, itemCount:$itemCount, positionStart:$positionStart")
                            if((lastVisiblePosition + itemCount) == positionStart){
                                lm.scrollToPosition(positionStart)
                            }
                        }
                        MessageViewModel.State.Type.LOAD_INIT ->{
                            lm.scrollToPosition((messageState?.messages?.size?: 1) - 1)
                        }
                        else -> {}
                    }
                }

            })

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
            //Log.d("Scrolled", "first :$first")

            if( first == 0 ){
                //esenter?.getOldMessage()
                mMessageViewModel?.loadOld()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        }
    }
}