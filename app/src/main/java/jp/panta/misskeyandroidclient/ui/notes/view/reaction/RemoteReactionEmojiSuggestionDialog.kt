package jp.panta.misskeyandroidclient.ui.notes.view.reaction

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment

class RemoteReactionEmojiSuggestionDialog : AppCompatDialogFragment() {

    companion object {
        fun newInstance(reaction: String): RemoteReactionEmojiSuggestionDialog {
            return RemoteReactionEmojiSuggestionDialog().also { fragment ->
                fragment.arguments = Bundle().also { bundle ->
                    bundle.putString("EXTRA_REACTION", reaction)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)


    }
}