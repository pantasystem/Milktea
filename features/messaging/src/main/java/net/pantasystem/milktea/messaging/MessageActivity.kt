package net.pantasystem.milktea.messaging

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.SetTheme
import net.pantasystem.milktea.messaging.databinding.ActivityMessageBinding
import net.pantasystem.milktea.model.messaging.MessagingId
import javax.inject.Inject

@AndroidEntryPoint
class MessageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MESSAGING_ID =
            "net.pantasystem.milktea.messaging.MessageActivity.EXTRA_MESSAGING_ID"
    }

    private lateinit var mBinding: ActivityMessageBinding

    @Inject
    lateinit var setTheme: SetTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.setTheme()
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
