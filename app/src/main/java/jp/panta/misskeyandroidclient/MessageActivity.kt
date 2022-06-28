package jp.panta.misskeyandroidclient

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityMessageBinding
import jp.panta.misskeyandroidclient.ui.TitleSettable
import jp.panta.misskeyandroidclient.ui.messaging.MessageFragment
import net.pantasystem.milktea.model.messaging.MessagingId

@AndroidEntryPoint
class MessageActivity : AppCompatActivity(), TitleSettable {

    companion object{
        const val EXTRA_MESSAGING_ID = "jp.panta.misskeyandroidclient.MessageActivity.EXTRA_MESSAGING_ID"
    }

    private lateinit var mBinding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_message)
        mBinding.lifecycleOwner = this
        setSupportActionBar(mBinding.messageToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val messagingId = intent?.getSerializableExtra(EXTRA_MESSAGING_ID) as MessagingId?


        if(messagingId == null){
            Log.e("MessageActivity", "EXTRA_MESSAGE_HISTORY must not null")
            finish()
            return
        }

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            val fragment = MessageFragment.newInstance(messagingId)
            ft.add(R.id.content_main, fragment)
            ft.commit()
        }





    }

    override fun setTitle(text: String) {
        supportActionBar?.title = text
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }



}
