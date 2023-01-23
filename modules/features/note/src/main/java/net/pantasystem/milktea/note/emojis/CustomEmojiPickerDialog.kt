package net.pantasystem.milktea.note.emojis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.emojis.viewmodel.EmojiSelection
import net.pantasystem.milktea.note.emojis.viewmodel.EmojiSelectionViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CustomEmojiPickerDialog : BottomSheetDialogFragment(), EmojiPickerFragment.OnEmojiSelectedListener{

    private var mSelectionViewModel: EmojiSelectionViewModel? = null

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var metaRepository: MetaRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_custom_emoji_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().also {
                it.add(R.id.fragmentBaseContainer, EmojiPickerFragment())
            }.commit()
        }
    }


    override fun onSelect(emoji: String) {
        val parentFr = parentFragment
        val activity = requireActivity()
        if(activity is EmojiSelection){
            activity.onSelect(emoji)
        } else if (parentFr is EmojiSelection) {
            parentFr.onSelect(emoji)

        }else{
            mSelectionViewModel?.onSelect(emoji)
        }
        dismiss()
    }

}