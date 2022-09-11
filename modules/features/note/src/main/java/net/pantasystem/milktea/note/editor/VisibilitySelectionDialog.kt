package net.pantasystem.milktea.note.editor

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogVisibilitySelectionBinding
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@AndroidEntryPoint
class VisibilitySelectionDialog : AppCompatDialogFragment(){


    private val viewModel: NoteEditorViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val tmpDialog = super.onCreateDialog(savedInstanceState)

        val view = View.inflate(tmpDialog.context, R.layout.dialog_visibility_selection,null)
        val binding = DataBindingUtil.bind<DialogVisibilitySelectionBinding>(view)


        val visibilities = arrayOf(
            getString(R.string.visibility_public),
            getString(R.string.visibility_home),
            getString(R.string.visibility_follower),
            getString(R.string.visibility_specified)
        )

        var nowSelectedVisibility = when(viewModel.visibility.value){
            is Visibility.Public -> 0
            is Visibility.Home -> 1
            is Visibility.Followers -> 2
            is Visibility.Specified -> 3
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
                val type =
                    when(visibilities[which]){
                        getString(R.string.visibility_public) -> "public"
                        getString(R.string.visibility_home) -> "home"
                        getString(R.string.visibility_follower) -> "followers"
                        getString(R.string.visibility_specified) -> "specified"
                        else -> "public"
                    }
                val localOnly = viewModel.isLocalOnly.value
                viewModel.setVisibility(
                    Visibility(
                        type,
                        localOnly
                    )
                )

            }
            .setView(view)

        binding?.isLocalOnlySwitch?.setOnCheckedChangeListener { _, isChecked ->
            val visibility = (viewModel.visibility.value) as? CanLocalOnly
                ?: return@setOnCheckedChangeListener
            viewModel.setVisibility(visibility.changeLocalOnly(isChecked) as Visibility)
        }
        binding?.lifecycleOwner = requireActivity()
        binding?.noteEditorViewModel = viewModel


        return dialog.create()
    }


}