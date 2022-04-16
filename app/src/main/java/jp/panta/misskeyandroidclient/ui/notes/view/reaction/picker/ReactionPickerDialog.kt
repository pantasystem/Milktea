package jp.panta.misskeyandroidclient.ui.notes.view.reaction.picker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.*
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogReactionPickerBinding
import net.pantasystem.milktea.data.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.ui.reaction.ReactionAutoCompleteArrayAdapter
import jp.panta.misskeyandroidclient.ui.reaction.ReactionChoicesAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReactionPickerDialog : AppCompatDialogFragment(){
    val notesViewModel by activityViewModels<NotesViewModel>()
    @Inject
    lateinit var reactionUserSettingDao: ReactionUserSettingDao

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_reaction_picker, null)
        dialog.setContentView(view)
        val binding = DialogReactionPickerBinding.bind(view)

        val miApplication = view.context.applicationContext as MiCore
        val ac = miApplication.getAccountStore().currentAccount


        val adapter =
            ReactionChoicesAdapter {
                dismiss()
                notesViewModel.postReaction(it)
            }
        binding.reactionsView.adapter = adapter


        
        binding.reactionsView.layoutManager = getFlexBoxLayoutManager(view.context)
        //adapter.submitList(ReactionResourceMap.defaultReaction)

        lifecycleScope.launch(Dispatchers.IO){
            var reactionSettings = reactionUserSettingDao.findByInstanceDomain(
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

        miApplication.getAccountStore().observeCurrentAccount.filterNotNull().flatMapLatest {
            miApplication.getMetaRepository().observe(it.instanceDomain)
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
                notesViewModel.postReaction(reaction)
                dismiss()
            }
        }.launchIn(lifecycleScope)
        



        binding.reactionField.setOnEditorActionListener { v, _, event ->
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
        binding.reactionField
        return dialog
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