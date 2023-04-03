package net.pantasystem.milktea.note.reaction.picker

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.flexbox.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android_ui.reaction.ReactionAutoCompleteArrayAdapter
import net.pantasystem.milktea.common_android_ui.reaction.ReactionChoicesAdapter
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogReactionPickerBinding
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ReactionPickerDialog : AppCompatDialogFragment(){

    companion object {
        fun newInstance(noteId: Note.Id): ReactionPickerDialog {
            return ReactionPickerDialog().apply {
                arguments = Bundle().apply {
                    putString("NOTE_ID", noteId.noteId)
                    putLong("ACCOUNT_ID", noteId.accountId)
                }
            }
        }
    }

    val notesViewModel by activityViewModels<NotesViewModel>()


    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var metaRepository: MetaRepository

    private val reactionPickerDialogViewModel by activityViewModels<ReactionPickerDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_reaction_picker, container, false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogReactionPickerBinding.bind(view)
        val noteId = Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!,
        )

        val adapter =
            ReactionChoicesAdapter {
                dismiss()
                notesViewModel.toggleReaction(noteId, it)
            }
        binding.reactionsView.adapter = adapter



        binding.reactionsView.layoutManager = getFlexBoxLayoutManager(view.context)

        reactionPickerDialogViewModel.setCurrentAccountById(noteId.accountId)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                reactionPickerDialogViewModel.userConfigReactions.collect { reactions ->
                    adapter.submitList(reactions)
                }
            }

        }

        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            metaRepository.observe(it.normalizedInstanceUri)
        }.mapNotNull {
            it?.emojis
        }.onEach { emojis ->
            val autoCompleteAdapter =
                ReactionAutoCompleteArrayAdapter(
                    emojis,
                    view.context
                )
            binding.reactionField.setAdapter(autoCompleteAdapter)
            binding.reactionField.setOnItemClickListener { _, _, i, _ ->
                val reaction = autoCompleteAdapter.suggestions[i]
                notesViewModel.toggleReaction(noteId, reaction)
                dismiss()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)




        binding.reactionField.setOnEditorActionListener { v, _, event ->
            if(event != null && event.keyCode == KeyEvent.KEYCODE_ENTER){
                if(event.action == KeyEvent.ACTION_UP){
                    notesViewModel.toggleReaction(noteId, v.text.toString())
                    (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(v.windowToken, 0)
                    dismiss()
                }
                return@setOnEditorActionListener true
            }
            false
        }
        binding.reactionField
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