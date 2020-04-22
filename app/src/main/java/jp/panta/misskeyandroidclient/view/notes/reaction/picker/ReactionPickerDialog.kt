package jp.panta.misskeyandroidclient.view.notes.reaction.picker

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory

class ReactionPickerDialog : AppCompatDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_reaction_picker, null)
        dialog.setContentView(view)

        val miApplication = view.context.applicationContext as MiApplication
        val ar = miApplication.currentAccount.value

        val notesViewModel = ViewModelProvider(activity!!, NotesViewModelFactory(ar!!, miApplication))[NotesViewModel::class.java]
        val defaultEmojiReactionsMap = ReactionResourceMap.reactionMap
        return dialog
    }
}