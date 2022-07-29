package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentNoteEditorBinding

class NoteEditorFragment : Fragment(R.layout.fragment_note_editor) {

    private val binding: FragmentNoteEditorBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}