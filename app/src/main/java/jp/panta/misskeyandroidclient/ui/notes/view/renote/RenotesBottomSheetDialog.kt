package jp.panta.misskeyandroidclient.ui.notes.view.renote

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.ui.notes.view.RenoteUsersScreen
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.renote.RenotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import javax.inject.Inject

@AndroidEntryPoint
class RenotesBottomSheetDialog : BottomSheetDialogFragment(){

    companion object {
        private const val EXTRA_ACCOUNT_ID = "ACCOUNT_ID"
        private const val EXTRA_NOTE_ID = "NOTE_ID"

        fun newInstance(noteId: Note.Id) : RenotesBottomSheetDialog {
            return RenotesBottomSheetDialog().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putLong(EXTRA_ACCOUNT_ID, noteId.accountId)
                    bundle.putString(EXTRA_NOTE_ID, noteId.noteId)
                }
            }
        }
    }

    @Inject
    lateinit var renotesViewModelAssistedFactory: RenotesViewModel.ViewModelAssistedFactory

    private val viewModel by lazy {
        val noteId = arguments?.let {
            val aId = it.getLong(EXTRA_ACCOUNT_ID)
            val nId = it.getString(EXTRA_NOTE_ID)!!
            Note.Id(aId, nId)
        }!!
        renotesViewModelAssistedFactory.create(noteId)
    }


    private val bottomSheetDialogBehavior: BottomSheetBehavior<FrameLayout>?
        get() = (dialog as? BottomSheetDialog)?.behavior

    @Inject
    lateinit var noteCaptureAPIAdapter: NoteCaptureAPIAdapter

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    RenoteUsersScreen(
                        renotesViewModel = viewModel,
                        onSelected = { nr ->
                            dismiss()
                            Intent(requireContext(), UserDetailActivity::class.java)
                            val intent = UserDetailActivity.newInstance(requireContext(), nr.user.id)
                            startActivity(intent)
                        },
                        noteCaptureAPIAdapter = noteCaptureAPIAdapter,
                        onScrollState = { state ->
                            bottomSheetDialogBehavior?.isDraggable = state
                        }
                    )
                }
            }

        }
    }
}