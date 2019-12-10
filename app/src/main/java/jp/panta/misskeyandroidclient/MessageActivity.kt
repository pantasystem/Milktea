package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityMessageBinding
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageFragment
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_MESSAGE_HISTORY = "jp.panta.misskeyandroidclient.MessageActivity.extra_message_history"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityMessageBinding>(this, R.layout.activity_message)
        setSupportActionBar(messageToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val messageHistory = intent?.getSerializableExtra(EXTRA_MESSAGE_HISTORY) as Message?
        val connectionInstance = (applicationContext as MiApplication).currentConnectionInstanceLiveData.value

        if(messageHistory == null){
            Log.e("MessageActivity", "EXTRA_MESSAGE_HISTORY must not null")
            finish()
            return
        }

        if(connectionInstance == null){
            Log.d("MessageActivity", "connectionInstance not found")
            finish()
            return
        }

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            val fragment = MessageFragment.newInstance(messageHistory)
            ft.add(R.id.content_main, fragment)
            ft.commit()
        }
        setTitle(messageHistory)

        val factory = MessageEditorViewModel.Factory(connectionInstance, application as MiApplication, messageHistory)
        val messageEditorViewModel = ViewModelProvider(this, factory)[MessageEditorViewModel::class.java]
        binding.editorViewModel = messageEditorViewModel
    }

    private fun setTitle(message: Message){
        val ci = (applicationContext as MiApplication).currentConnectionInstanceLiveData.value ?: return
        supportActionBar?.title = if(message.isGroup()){
            message.group?.name
        }else{
            message.opponentUser(ci)?.getDisplayUserName()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
