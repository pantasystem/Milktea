package net.pantasystem.milktea.messaging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_navigation.DriveNavigation
import net.pantasystem.milktea.common_navigation.DriveNavigationArgs
import net.pantasystem.milktea.common_navigation.EXTRA_SELECTED_FILE_PROPERTY_IDS
import net.pantasystem.milktea.messaging.viewmodel.MessageEditorViewModel
import net.pantasystem.milktea.messaging.viewmodel.MessageViewModel
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.drive.FileProperty
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

    private val messageActionViewModel by viewModels<MessageEditorViewModel>()

    private val messagingId: MessagingId by lazy {
        arguments?.getSerializable(EXTRA_MESSAGING_ID) as MessagingId
    }


    @Inject
    lateinit var accountStore: AccountStore


    @Inject
    lateinit var driveNavigation: DriveNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messageActionViewModel.setMessagingId(messagingId)
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
        val intent = driveNavigation.newIntent(DriveNavigationArgs(selectableFileMaxSize = 1))
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityForPickFileResult.launch(intent)
    }

    private val openDriveActivityForPickFileResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val ids =
            (result.data?.getSerializableExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS) as? List<*>)?.map {
                it as FileProperty.Id
            }
        ids?.firstOrNull()?.let {
            messageActionViewModel.setFilePropertyFromId(it)
        }
    }
}