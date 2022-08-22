package net.pantasystem.milktea.note.view.emojis

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogCustomEmojiPickerBinding
import net.pantasystem.milktea.note.view.emojis.viewmodel.EmojiSelection
import net.pantasystem.milktea.note.view.emojis.viewmodel.EmojiSelectionViewModel
import net.pantasystem.milktea.note.view.emojis.viewmodel.Emojis
import javax.inject.Inject

@AndroidEntryPoint
class CustomEmojiPickerDialog : BottomSheetDialogFragment(){

    private var mEmojisAdapter: EmojiListAdapter? = null
    private var mSelectionViewModel: EmojiSelectionViewModel? = null

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var metaRepository: MetaRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)



        val binding = View.inflate(dialog.context, R.layout.dialog_custom_emoji_picker, null).let {
            dialog.setContentView(it)
            DialogCustomEmojiPickerBinding.bind(it)
        }
        binding.let{ view ->

            if(requireActivity() !is EmojiSelection){
                mSelectionViewModel = ViewModelProvider(requireActivity())[EmojiSelectionViewModel::class.java]
            }

            val adapter = EmojiListAdapter(EmojiSelectionListener(), requireActivity())
            view.emojisView.adapter = adapter
            val flexBoxLayoutManager = FlexboxLayoutManager(dialog.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            view.emojisView.layoutManager = flexBoxLayoutManager
            mEmojisAdapter = adapter
            Log.d("PickerDialog", "アダプターをセットアップしました")

            accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
                metaRepository.observe(it.instanceDomain)
            }.map {
                it?.emojis?: emptyList()
            }.onEach {
                mEmojisAdapter?.submitList(Emojis.categoryBy(it))
            }.launchIn(lifecycleScope)


        }
        return dialog
    }

    inner class EmojiSelectionListener : EmojiSelection {

        val activity = requireActivity()

        override fun onSelect(emoji: String) {
            val parentFr = parentFragment
            if(activity is EmojiSelection){
                activity.onSelect(emoji)
            } else if (parentFr is EmojiSelection) {
                parentFr.onSelect(emoji)

            }else{
                mSelectionViewModel?.onSelect(emoji)
            }
            dismiss()
        }

        override fun onSelect(emoji: Emoji) {
            if(activity is EmojiSelection){
                activity.onSelect(emoji)
            }else{
                mSelectionViewModel?.onSelect(emoji)
            }
            dismiss()
        }
    }
}