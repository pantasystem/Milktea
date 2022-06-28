package jp.panta.misskeyandroidclient.ui.messaging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentMessageBinding
import jp.panta.misskeyandroidclient.ui.TitleSettable
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageActionViewModel
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel
import jp.panta.misskeyandroidclient.ui.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.ui.text.CustomEmojiTokenizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.drive.DriveActivity
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

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

    private val messagingId: MessagingId by lazy {
        arguments?.getSerializable(EXTRA_MESSAGING_ID) as MessagingId
    }

    lateinit var messageActionViewModel: MessageActionViewModel

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var metaRepository: MetaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = MessageActionViewModel.Factory(messagingId, requireContext().applicationContext as MiApplication)
        messageActionViewModel = ViewModelProvider(this, factory)[MessageActionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        if (!BuildConfig.DEBUG) {
            return super.onCreateView(inflater, container, savedInstanceState)
//        }

//        return ComposeView(requireContext()).apply {
//            setContent {
//                MdcTheme {
//
//                }
//            }
//        }.rootView
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        if (BuildConfig.DEBUG) {
//            return
//        }

        val lm = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }

        mBinding.actionViewModel = messageActionViewModel
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

        mBinding.openDrive.setOnClickListener {
            openDriveActivity()
        }

        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            metaRepository.observe(it.instanceDomain)
        }.distinctUntilChanged().filterNotNull().onEach {
            mBinding.inputMessage.setTokenizer(CustomEmojiTokenizer())
            mBinding.inputMessage.setAdapter(
                CustomEmojiCompleteAdapter(
                    it.emojis ?: emptyList(),
                    requireContext()
                )
            )
        }.launchIn(lifecycleScope)

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

    private fun openDriveActivity(){
        val intent = Intent(requireActivity(), DriveActivity::class.java)
        intent.putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, 1)
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityForPickFileResult.launch(intent)
    }

    private val openDriveActivityForPickFileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        val ids = (result.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as? List<*>)?.map {
            it as FileProperty.Id
        }
        ids?.firstOrNull()?.let {
            messageActionViewModel.setFilePropertyFromId(it)
        }
    }
}