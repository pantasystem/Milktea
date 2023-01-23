package net.pantasystem.milktea.note.draft

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_navigation.MediaNavigation
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.from
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.draft.viewmodel.DraftNotesViewModel
import javax.inject.Inject

/**
 * NOTE: 直接的なコードによる参照はないが、activity_draft_notesから参照されているので削除しないこと。
 */
@AndroidEntryPoint
class DraftNotesFragment : Fragment() {

    val viewModel: DraftNotesViewModel by viewModels()

    @Inject
    lateinit var mediaNavigation: MediaNavigation


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    DraftNotesScreen(
                        viewModel = viewModel,
                        onNavigateUp = {
                                       requireActivity().finish()
                        },
                        onEdit = {
                            val intent = NoteEditorActivity.newBundle(
                                requireContext(),
                                draftNoteId = it.draftNoteId
                            )
                            requireActivity().startActivityFromFragment(this@DraftNotesFragment, intent, 300)
                        },
                        onShowFile = {
                            val intent = mediaNavigation.newIntent(
                                MediaNavigationArgs.AFile(
                                    when(it) {
                                        is DraftNoteFile.Local -> FilePreviewSource.Local(AppFile.from(it) as AppFile.Local)
                                        is DraftNoteFile.Remote -> FilePreviewSource.Remote(AppFile.Remote(it.fileProperty.id), it.fileProperty)
                                    }
                                )
                            )
                            startActivity(intent)
                        }
                    )
                }
            }
        }.rootView
    }


}