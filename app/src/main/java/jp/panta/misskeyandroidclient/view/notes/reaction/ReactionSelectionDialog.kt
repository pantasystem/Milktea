package jp.panta.misskeyandroidclient.view.notes.reaction

import android.app.Dialog
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.dialog_select_reaction.view.*

class ReactionSelectionDialog : BottomSheetDialogFragment() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.dialog_select_reaction, null)
        dialog.setContentView(view)

        val activity = activity
        val miApplication = context?.applicationContext as MiApplication
        val cn  = miApplication.currentConnectionInstanceLiveData.value


        if(activity != null && cn != null){
            val notesViewModel = ViewModelProvider(activity, NotesViewModelFactory(cn, miApplication)).get(NotesViewModel::class.java)
            val defaultReaction = ReactionResourceMap.reactionDrawableMap
            val adapter = ReactionPreviewAdapter(object : DiffUtil.ItemCallback<Pair<String, Int>>(){
                override fun areContentsTheSame(
                    oldItem: Pair<String, Int>,
                    newItem: Pair<String, Int>
                ): Boolean {
                    return oldItem == newItem
                }

                override fun areItemsTheSame(
                    oldItem: Pair<String, Int>,
                    newItem: Pair<String, Int>
                ): Boolean {
                    return oldItem == newItem
                }
            }, notesViewModel)
            view.reaction_preview_list.adapter = adapter
            view.reaction_preview_list.layoutManager = LinearLayoutManager(context)
            adapter.submitList(defaultReaction.toList())

            /*notesViewModel.submittedNotesOnReaction.observe(activity, Observer {
                dismiss()
            })*/
            /*notesViewModel.submittedNotesOnReaction.observe(view, Observer {
                dismiss()
            })*/

        }

    }
}