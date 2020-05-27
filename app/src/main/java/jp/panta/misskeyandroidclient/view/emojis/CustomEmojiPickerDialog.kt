package jp.panta.misskeyandroidclient.view.emojis

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.emojis.EmojiSelection
import jp.panta.misskeyandroidclient.viewmodel.emojis.Emojis
import kotlinx.android.synthetic.main.dialog_custom_emoji_picker.view.*

class CustomEmojiPickerDialog : AppCompatDialogFragment(){

    private var mEmojisAdapter: EmojiListAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)

        val activity = requireActivity()
        if(activity is EmojiSelection){
            val adapter = EmojiListAdapter(activity, requireActivity())
            mEmojisAdapter = adapter
        }

        val emojis = (dialog.context.applicationContext as MiApplication).getCurrentInstanceMeta()?.emojis

        View.inflate(dialog.context, R.layout.dialog_custom_emoji_picker, null)?.let{ view ->
            dialog.setContentView(view)

            view.inputEmoji.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) = Unit
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = (s?: "").toString()
                    if(text.isBlank()){
                        emojis?.let{
                            mEmojisAdapter?.submitList(Emojis.categoryBy(it))
                        }
                    }else{
                        emojis?.filter{
                            it.name.contains(text)
                        }?.let{
                            mEmojisAdapter?.submitList(it.map{ emoji ->
                                Emojis.CustomEmoji(emoji)
                            })
                        }
                    }
                }
            })


        }
        return dialog
    }
}