package jp.panta.misskeyandroidclient.view.notes.reaction.choices

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import kotlinx.android.synthetic.main.dialog_reaction_input.view.*

class ReactionInputDialog : AppCompatDialogFragment(){


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_reaction_input, null)
        dialog.setContentView(view)

        val lp = dialog.window?.attributes
        lp?.gravity = Gravity.BOTTOM

        val miApplication = view.context.applicationContext as MiApplication
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?.map{
            ":${it.name}:"
        }?: return dialog
        val activity = activity?: return dialog
        val notesViewModel = ViewModelProvider(activity)[NotesViewModel::class.java]
        val adapter = ReactionAutoCompleteArrayAdapter(emojis, notesViewModel, view.context)
        view.input_reaction.setAdapter(adapter)
        view.input_reaction.setOnItemClickListener { _, _, position, _ ->
            val reaction = adapter.suggestions[position]
            notesViewModel.postReaction(reaction)
            dismiss()
        }
        view.input_reaction.setOnEditorActionListener { v, _, event ->
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