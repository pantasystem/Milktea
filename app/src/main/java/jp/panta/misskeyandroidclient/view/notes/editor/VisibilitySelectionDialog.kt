package jp.panta.misskeyandroidclient.view.notes.editor

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogVisibilitySelectionBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask
import java.util.*

class VisibilitySelectionDialog : AppCompatDialogFragment(){



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tmpDialog = super.onCreateDialog(savedInstanceState)

        val view = View.inflate(tmpDialog.context, R.layout.dialog_visibility_selection,null)
        val binding = DataBindingUtil.bind<DialogVisibilitySelectionBinding>(view)
        val viewModel = ViewModelProvider(requireActivity())[NoteEditorViewModel::class.java]


        val visibilities = arrayOf(
            getString(R.string.visibility_public),
            getString(R.string.visibility_home),
            getString(R.string.visibility_follower),
            getString(R.string.visibility_specified)
        )

        var nowSelectedVisibility = when(viewModel.visibility.value){
            PostNoteTask.Visibility.PUBLIC -> 0
            PostNoteTask.Visibility.HOME -> 1
            PostNoteTask.Visibility.FOLLOWERS -> 2
            PostNoteTask.Visibility.SPECIFIED -> 3
            else -> 0
        }
        if(nowSelectedVisibility !in visibilities.indices){
            nowSelectedVisibility = 0
        }
        val dialog = MaterialAlertDialogBuilder(tmpDialog.context)
            .setNegativeButton(android.R.string.cancel){ _, _ ->
                dismiss()
            }
            .setPositiveButton(android.R.string.ok){ _, _ ->
                dismiss()
            }
            .setSingleChoiceItems(
                visibilities, nowSelectedVisibility
            ) { _, which ->

                when(visibilities[which]){
                    getString(R.string.visibility_public) -> viewModel.setVisibility(PostNoteTask.Visibility.PUBLIC)
                    getString(R.string.visibility_home) -> viewModel.setVisibility(PostNoteTask.Visibility.HOME)
                    getString(R.string.visibility_follower) -> viewModel.setVisibility(PostNoteTask.Visibility.FOLLOWERS)
                    getString(R.string.visibility_specified) -> viewModel.setVisibility(PostNoteTask.Visibility.SPECIFIED)
                    else -> viewModel.setVisibility(PostNoteTask.Visibility.PUBLIC)
                }

            }
            .setView(view)

        binding?.lifecycleOwner = requireActivity()
        binding?.noteEditorViewModel = viewModel


        return dialog.create()
    }


}