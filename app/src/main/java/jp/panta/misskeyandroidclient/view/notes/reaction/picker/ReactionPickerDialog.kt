package jp.panta.misskeyandroidclient.view.notes.reaction.picker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.view.reaction.ReactionAutoCompleteArrayAdapter
import jp.panta.misskeyandroidclient.view.reaction.ReactionChoicesAdapter
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
        val ac = miApplication.getCurrentAccount().value

        val notesViewModel = ViewModelProvider(requireActivity(), NotesViewModelFactory(miApplication))[NotesViewModel::class.java]
        val adapter =
            ReactionChoicesAdapter(
                notesViewModel
            )
        view.reactionsView.adapter = adapter

        notesViewModel.submittedNotesOnReaction.observe(requireActivity(), Observer{
            dismiss()
        })
        
        view.reactionsView.layoutManager = getFlexBoxLayoutManager(view.context)
        //adapter.submitList(ReactionResourceMap.defaultReaction)

        GlobalScope.launch(Dispatchers.IO){
            var reactionSettings = miApplication.reactionUserSettingDao.findByInstanceDomain(
                ac?.instanceDomain!!
            )?.sortedBy {
                it.weight
            }?.map{
                it.reaction
            }?: ReactionResourceMap.defaultReaction
            if(reactionSettings.isEmpty()){
                reactionSettings = ReactionResourceMap.defaultReaction
            }

            Handler(Looper.getMainLooper()).post{
                adapter.submitList(reactionSettings)

            }

        }
        
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?.map{
            ":${it.name}:"
        }?: emptyList()
        
        val autoCompleteAdapter =
            ReactionAutoCompleteArrayAdapter(
                emojis,
                view.context
            )
        view.reactionField.setAdapter(autoCompleteAdapter)
        view.reactionField.setOnItemClickListener { _, _, i, _ ->
            val reaction = autoCompleteAdapter.suggestions[i]
            notesViewModel.postReaction(reaction)
            dismiss()
        }
        view.reactionField.setOnEditorActionListener { v, _, event ->
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