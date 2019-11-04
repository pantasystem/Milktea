package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageFragment

class MessageActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_MESSAGE_HISTORY = "jp.panta.misskeyandroidclient.MessageActivity.extra_message_history"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val messageHistory = intent?.getSerializableExtra(EXTRA_MESSAGE_HISTORY) as Message?

        if(messageHistory == null){
            Log.e("MessageActivity", "EXTRA_MESSAGE_HISTORY must not null")
            finish()
            return
        }

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            val fragment = MessageFragment.newInstance(messageHistory)
            ft.add(R.id.content_main, fragment)
            ft.commit()
        }
    }
}
