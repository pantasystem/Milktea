package net.pantasystem.milktea.note.renote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.view.RenoteUsersScreen
import javax.inject.Inject

@AndroidEntryPoint
class RenotesBottomSheetDialog : BottomSheetDialogFragment(){

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    companion object {
        const val FRAGMENT_TAG = "RenotesBottomSheetDialog"
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



    private val viewModel by viewModels<RenotesViewModel> {
        val noteId = arguments?.let {
            val aId = it.getLong(EXTRA_ACCOUNT_ID)
            val nId = it.getString(EXTRA_NOTE_ID)!!
            Note.Id(aId, nId)
        }!!
        RenotesViewModel.provideViewModel(renotesViewModelAssistedFactory, noteId)
    }


    private val bottomSheetDialogBehavior: BottomSheetBehavior<FrameLayout>?
        get() = (dialog as? BottomSheetDialog)?.behavior

    @Inject
    lateinit var noteCaptureAPIAdapter: NoteCaptureAPIAdapter

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    RenoteUsersScreen(
                        renotesViewModel = viewModel,
                        onSelected = { nr ->
                            dismiss()
                            val intent = userDetailNavigation.newIntent(UserDetailNavigationArgs.UserId(
                                nr.user.id
                            ))
                            startActivity(intent)
                        },
                        onScrollState = { state ->
                            bottomSheetDialogBehavior?.isDraggable = state
                        }
                    )
                }
            }

        }
    }
}