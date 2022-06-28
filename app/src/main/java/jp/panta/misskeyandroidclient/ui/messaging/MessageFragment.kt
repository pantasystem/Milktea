package jp.panta.misskeyandroidclient.ui.messaging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageActionViewModel
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageViewModel
import net.pantasystem.milktea.drive.DriveActivity
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

@AndroidEntryPoint
class MessageFragment : Fragment() {

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



    private val messageViewModel by viewModels<MessageViewModel>()

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
        val factory = MessageActionViewModel.Factory(
            messagingId,
            requireContext().applicationContext as MiApplication
        )
        messageActionViewModel =
            ViewModelProvider(this, factory)[MessageActionViewModel::class.java]

        messageViewModel.setMessagingId(messagingId)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    MessageScreen(
                        messageViewModel = messageViewModel,
                        messageActionViewModel = messageActionViewModel,
                        onOpenDriveToSelect = {
                            openDriveActivity()
                        },
                        onNavigateUp = {
                            requireActivity().finish()
                        }
                    )
                }
            }
        }.rootView
    }


    private fun openDriveActivity() {
        val intent = Intent(requireActivity(), DriveActivity::class.java)
        intent.putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, 1)
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityForPickFileResult.launch(intent)
    }

    private val openDriveActivityForPickFileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val ids =
            (result.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as? List<*>)?.map {
                it as FileProperty.Id
            }
        ids?.firstOrNull()?.let {
            messageActionViewModel.setFilePropertyFromId(it)
        }
    }
}