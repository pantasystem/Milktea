package net.pantasystem.milktea.messaging

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.MessageNavigation
import net.pantasystem.milktea.common_navigation.MessageNavigationArgs
import net.pantasystem.milktea.messaging.databinding.ActivityMessageBinding
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

class MessageNavigationImpl @Inject constructor(
    val activity: Activity
) : MessageNavigation {
    override fun newIntent(args: MessageNavigationArgs): Intent {
        val intent = Intent(activity, MessageActivity::class.java)
        intent.putExtra(MessageActivity.EXTRA_MESSAGING_ID, args.messagingId)
        return intent
    }
}

@AndroidEntryPoint
class MessageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MESSAGING_ID =
            "net.pantasystem.milktea.messaging.MessageActivity.EXTRA_MESSAGING_ID"
    }

    private lateinit var mBinding: ActivityMessageBinding

    @Inject
    lateinit var setTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_message)
        mBinding.lifecycleOwner = this

        val messagingId = intent?.getSerializableExtra(EXTRA_MESSAGING_ID) as MessagingId?

        if (messagingId == null) {
            Log.e("MessageActivity", "EXTRA_MESSAGE_HISTORY must not null")
            finish()
            return
        }

        if (savedInstanceState == null) {
            val ft = supportFragmentManager.beginTransaction()
            val fragment = MessageFragment.newInstance(messagingId)
            ft.add(R.id.content_main, fragment)
            ft.commit()
        }

    }


}
