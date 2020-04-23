package jp.panta.misskeyandroidclient.view.notes.reaction.picker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.view.notes.reaction.choices.ReactionAutoCompleteArrayAdapter
import jp.panta.misskeyandroidclient.view.notes.reaction.choices.ReactionChoicesAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.dialog_reaction_picker.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReactionPickerDialog : AppCompatDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_reaction_picker, null)
        dialog.setContentView(view)

        val miApplication = view.context.applicationContext as MiApplication
        val ar = miApplication.currentAccount.value

        val notesViewModel = ViewModelProvider(activity!!, NotesViewModelFactory(ar!!, miApplication))[NotesViewModel::class.java]
        val adapter = ReactionChoicesAdapter(notesViewModel)
        view.reactionsView.adapter = adapter

        notesViewModel.submittedNotesOnReaction.observe(activity!!, Observer{
            dismiss()
        })
        
        view.reactionsView.layoutManager = getFlexBoxLayoutManager(view.context)
        adapter.submitList(ReactionResourceMap.defaultReaction)

        GlobalScope.launch(Dispatchers.IO){
            var reactionSettings = miApplication.reactionUserSettingDao.findByInstanceDomain(
                ar.getCurrentConnectionInformation()?.instanceBaseUrl!!
            )?.sortedBy {
                it.weight
            }?.map{
                it.reaction
            }?: ReactionResourceMap.defaultReaction
            if(reactionSettings.isEmpty()){
                reactionSettings = ReactionResourceMap.defaultReaction
            }
            adapter.submitList(reactionSettings)

        }
        
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?.map{
            ":${it.name}:"
        }?: emptyList()
        
        val autoCompleteAdapter = ReactionAutoCompleteArrayAdapter(emojis, notesViewModel, view.context)
        view.reactionField.setAdapter(autoCompleteAdapter)
        view.reactionField.setOnItemClickListener { _, _, i, _ ->
            val reaction = autoCompleteAdapter.suggestions[i]
            notesViewModel.postReaction(reaction)
            dismiss()
        }
        view.reactionField.setOnEditorActionListener { textView, i, keyEvent ->
            if(keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER){
                if(keyEvent.action == KeyEvent.ACTION_UP){
                    notesViewModel.postReaction(textView.text.toString())
                    return@setOnEditorActionListener true
                }
            }
            false
        }
        view.reactionField
        return dialog
    }
    
    private fun showReactionUserSettings(){
        
    }
    
    private fun getFlexBoxLayoutManager(context: Context): FlexboxLayoutManager{
        val flexBoxLayoutManager = FlexboxLayoutManager(context)
        flexBoxLayoutManager.flexDirection = FlexDirection.ROW
        flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
        flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        return flexBoxLayoutManager
    }
}