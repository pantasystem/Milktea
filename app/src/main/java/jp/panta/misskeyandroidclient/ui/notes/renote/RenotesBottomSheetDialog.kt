package jp.panta.misskeyandroidclient.ui.notes.renote

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.ui.notes.RenoteUsersScreen
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.renote.RenotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

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

    private lateinit var viewModel: RenotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val noteId = arguments?.let {
            val aId = it.getLong(EXTRA_ACCOUNT_ID)
            val nId = it.getString(EXTRA_NOTE_ID)!!
            Note.Id(aId, nId)
        }!!
        val miCore = requireContext().applicationContext as MiCore
        viewModel = ViewModelProvider(this, RenotesViewModel.Factory(noteId, miCore))[RenotesViewModel::class.java]
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val miCore = requireContext().applicationContext as MiCore

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
                        noteCaptureAPIAdapter = miCore.getNoteCaptureAdapter()
                    )
                }
            }

        }
    }
}