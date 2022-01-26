package jp.panta.misskeyandroidclient.ui.messaging

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentMessageBinding
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.ui.TitleSettable
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewData
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.lang.IllegalArgumentException

@FlowPreview
@ExperimentalCoroutinesApi
class MessageFragment : Fragment(R.layout.fragment_message){

    companion object{
        private const val EXTRA_MESSAGING_ID = "jp.panta.misskeyandroidclient.viewmodel.messaging.EXTRA_MESSAGING_ID"

        fun newInstance(messagingId: MessagingId): MessageFragment {
            return MessageFragment().also { fragment ->
                fragment.arguments = Bundle().also {
                    it.putSerializable(EXTRA_MESSAGING_ID, messagingId)
                }
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private var mMessageViewModel: MessageViewModel? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private val mBinding: FragmentMessageBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messagingId = arguments?.getSerializable(EXTRA_MESSAGING_ID) as? MessagingId

        if(messagingId == null){
            Log.e("MessageFragment", "messageHistoryがNullです")
            throw IllegalArgumentException("messageHistory must not null")
        }

        val miApplication = context?.applicationContext as MiApplication

        val lm = LinearLayoutManager(context)
        //lm.stackFromEnd = true
        mBinding.messagesView.layoutManager = lm
        mLayoutManager = lm

        val messageViewModel = ViewModelProvider(this, MessageViewModelFactory(messagingId, miApplication))[MessageViewModel::class.java]
        mMessageViewModel = messageViewModel

        val adapter = MessageListAdapter(diffUtilItemCallback, viewLifecycleOwner)
        mBinding.messagesView.adapter = adapter

        var messageState: MessageViewModel.State? = null
        messageViewModel.messagesLiveData.observe(viewLifecycleOwner, {
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
        mBinding.messagesView.addOnScrollListener(scrollListener)

        messageViewModel.title.observe(viewLifecycleOwner) { title ->
            if(title != null) {
                runCatching{ requireActivity() as? TitleSettable }.getOrNull()?.setTitle(title)
            }
        }


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

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val first =mLayoutManager?.findFirstVisibleItemPosition()
            //Log.d("Scrolled", "first :$first")

            if( first == 0 ){
                mMessageViewModel?.loadOld()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        }
    }
}