package jp.panta.misskeyandroidclient.view.notes.reaction.choices

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import kotlinx.android.synthetic.main.dialog_reaction_input.view.*

class ReactionInputDialog : BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_reaction_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApplication = view.context.applicationContext as MiApplication
        val emojis = miApplication.nowInstanceMeta?.emojis?.map{
            ":${it.name}:"
        }?: return
        val activity = activity?: return
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

    }


}