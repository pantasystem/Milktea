package jp.panta.misskeyandroidclient.ui.notes.view.draft

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.NoteEditorActivity
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft.DraftNotesViewModel
import net.pantasystem.milktea.common_navigation.MediaNavigationKeys
import net.pantasystem.milktea.media.MediaActivity
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.toFile
import javax.inject.Inject

/**
 * NOTE: 直接的なコードによる参照はないが、activity_draft_notesから参照されているので削除しないこと。
 */
@AndroidEntryPoint
class DraftNotesFragment : Fragment() {

    val viewModel: DraftNotesViewModel by viewModels()

    @Inject
    lateinit var filePropertyDataSource: FilePropertyDataSource

    @Inject
    lateinit var driveFileRepository: DriveFileRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    DraftNotesPage(
                        viewModel = viewModel,
                        filePropertyDataSource = filePropertyDataSource,
                        driveFileRepository = driveFileRepository,
                        onAction = {
                            onAction(it)
                        }
                    )
                }
            }
        }.rootView
    }

    private fun onAction(action: DraftNotePageAction) {
        when (action) {
            is DraftNotePageAction.Edit -> {
                val intent = NoteEditorActivity.newBundle(
                    requireContext(),
                    draftNoteId = action.draftNote.draftNoteId
                )
                requireActivity().startActivityFromFragment(this, intent, 300)
            }
            DraftNotePageAction.NavigateUp -> {
                requireActivity().finish()
            }
            is DraftNotePageAction.ShowFile -> {
                val intent = Intent(requireContext(), MediaActivity::class.java)
                intent.putExtra(MediaNavigationKeys.EXTRA_FILE, action.previewActionType.file.toFile())
                startActivity(intent)
            }
        }
    }




}