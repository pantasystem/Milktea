package jp.panta.misskeyandroidclient.view.notes.reaction

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.dialog_select_reaction.view.*

class ReactionSelectionDialog : BottomSheetDialogFragment() {

    private var mNoteViewModel: NotesViewModel? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        Log.d("ReactionSelectionDialog", "setupDialog")

        val view = View.inflate(context, R.layout.dialog_select_reaction, null)
        dialog.setContentView(view)

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(BottomSheetBehavior.STATE_EXPANDED == newState){
                    view.bottom_sheet_behavior_icon.setImageResource(R.drawable.ic_expand_more_black_24dp)
                }else{
                    view.bottom_sheet_behavior_icon.setImageResource(R.drawable.ic_expand_less_black_24dp)
                }

            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        val activity = activity
        val miApplication = context?.applicationContext as MiApplication
        val cn  = miApplication.currentConnectionInstanceLiveData.value
        val emojis = miApplication.nowInstanceMeta?.emojis?.map{
            ":${it.name}:"
        }

        if(activity != null && cn != null){
            val notesViewModel = ViewModelProvider(activity, NotesViewModelFactory(cn, miApplication)).get(NotesViewModel::class.java)
            mNoteViewModel = notesViewModel
            val defaultReaction = ReactionResourceMap.reactionDrawableMap.map{
                it.key
            }

            val reactions  = if(emojis == null) defaultReaction else ArrayList<String>(defaultReaction).apply{
                addAll(emojis)
            }
            val adapter = ReactionPreviewAdapter(object : DiffUtil.ItemCallback<String>(){
                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }

                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }
            }, notesViewModel)
            view.reaction_preview_list.adapter = adapter
            view.reaction_preview_list.layoutManager = LinearLayoutManager(context)
            adapter.submitList(reactions)

            notesViewModel.submittedNotesOnReaction.observe(activity, Observer {
                Log.d("ReactionSelectionDialog", "終了が呼び出された")
                dismiss()
            })
            /*notesViewModel.submittedNotesOnReaction.observe(activity, Observer {
                dismiss()
            })*/
            /*notesViewModel.submittedNotesOnReaction.observe(view, Observer {
                dismiss()
            })*/

        }

    }


}