package jp.panta.misskeyandroidclient.ui.messaging

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentMessageBinding
import jp.panta.misskeyandroidclient.ui.TitleSettable
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.model.messaging.MessagingId

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MessageFragment : Fragment(R.layout.fragment_message) {

    companion object {
        private const val EXTRA_MESSAGING_ID =
            "jp.panta.misskeyandroidclient.viewmodel.messaging.EXTRA_MESSAGING_ID"

        fun newInstance(messagingId: MessagingId): MessageFragment {
            return MessageFragment().also { fragment ->
                fragment.arguments = Bundle().also {
                    it.putSerializable(EXTRA_MESSAGING_ID, messagingId)
                }
            }
        }
    }

    private var mLayoutManager: LinearLayoutManager? = null

    private val mBinding: FragmentMessageBinding by dataBinding()

    val messageViewModel by viewModels<MessageViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messagingId = arguments?.getSerializable(EXTRA_MESSAGING_ID) as? MessagingId

        require(messagingId != null) {
            "messageHistory must not null"
        }

        val lm = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }

        mBinding.messagesView.layoutManager = lm
        mLayoutManager = lm

        messageViewModel.setMessagingId(messagingId)


        val adapter = MessageListAdapter(diffUtilItemCallback, viewLifecycleOwner)
        mBinding.messagesView.adapter = adapter

        lifecycleScope.launch {
            messageViewModel.messages.collect { state ->
                val list = when (val content = state.content) {
                    is StateContent.Exist -> {
                        content.rawContent
                    }
                    is StateContent.NotExist -> {
                        emptyList()
                    }
                }
                adapter.submitList(list)
            }
        }

        mBinding.messagesView.addOnScrollListener(scrollListener)

        messageViewModel.title.observe(viewLifecycleOwner) { title ->
            if (title != null) {
                runCatching { requireActivity() as? TitleSettable }.getOrNull()?.setTitle(title)
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if (messageViewModel.latestReceivedMessageId != null) {
                    mBinding.messagesView.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })
    }


    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<MessageRelation>() {
        override fun areContentsTheSame(
            oldItem: MessageRelation,
            newItem: MessageRelation
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: MessageRelation, newItem: MessageRelation): Boolean {
            return oldItem.message.id == newItem.message.id
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            val first = mLayoutManager?.findFirstVisibleItemPosition()
            //Log.d("Scrolled", "first :$first")

            if (first == 0) {
                messageViewModel.loadOld()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        }
    }
}