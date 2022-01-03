package jp.panta.misskeyandroidclient.view.notes.reaction.choices

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogReactionInputBinding
import jp.panta.misskeyandroidclient.view.reaction.ReactionAutoCompleteArrayAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class ReactionInputDialog : AppCompatDialogFragment(){


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_reaction_input, null)
        dialog.setContentView(view)
        val binding = DialogReactionInputBinding.bind(view)

        val lp = dialog.window?.attributes
        lp?.gravity = Gravity.BOTTOM

        val miApplication = view.context.applicationContext as MiApplication
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?: return dialog
        val activity = activity?: return dialog
        val notesViewModel = ViewModelProvider(activity)[NotesViewModel::class.java]
        val adapter =
            ReactionAutoCompleteArrayAdapter(
                emojis,
                view.context
            )
        binding.inputReaction.setAdapter(adapter)
        binding.inputReaction.setOnItemClickListener { _, _, position, _ ->
            val reaction = adapter.suggestions[position]
            notesViewModel.postReaction(reaction)
            dismiss()
        }
        binding.inputReaction.setOnEditorActionListener { v, _, event ->
            if(event != null && event.keyCode == KeyEvent.KEYCODE_ENTER){
                if(event.action == KeyEvent.ACTION_UP){
                    notesViewModel.postReaction(v.text.toString())
                    (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(v.windowToken, 0)
                    dismiss()
                }
                return@setOnEditorActionListener true
            }
            false
        }
        return dialog
    }



}