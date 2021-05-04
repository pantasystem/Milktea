package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityMessageBinding
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.view.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.view.text.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageActionViewModel
import jp.panta.misskeyandroidclient.view.messaging.MessageFragment
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.view.TitleSettable
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity(), TitleSettable {

    companion object{
        const val EXTRA_MESSAGING_ID = "jp.panta.misskeyandroidclient.MessageActivity.EXTRA_MESSAGING_ID"

        const val SELECT_DRIVE_FILE_REQUEST_CODE = 114

    }

    private lateinit var mViewModel: MessageActionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityMessageBinding>(this, R.layout.activity_message)
        binding.lifecycleOwner = this
        setSupportActionBar(messageToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val messagingId = intent?.getSerializableExtra(EXTRA_MESSAGING_ID) as MessagingId?
        val account = (applicationContext as MiApplication).getCurrentAccount().value


        if(messagingId == null){
            Log.e("MessageActivity", "EXTRA_MESSAGE_HISTORY must not null")
            finish()
            return
        }

        if(account == null){
            Log.d("MessageActivity", "ac not found")
            finish()
            return
        }

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            val fragment = MessageFragment.newInstance(messagingId)
            ft.add(R.id.content_main, fragment)
            ft.commit()
        }

        val factory = MessageActionViewModel.Factory(messagingId, application as MiApplication)
        val messageActionViewModel = ViewModelProvider(this, factory)[MessageActionViewModel::class.java]
        mViewModel = messageActionViewModel
        binding.actionViewModel = messageActionViewModel

        binding.openDrive.setOnClickListener {
            openDriveActivity()
        }

        val miCore = application as MiCore
        miCore.getCurrentInstanceMeta()?.emojis?.let{ emojis ->
            binding.inputMessage.setTokenizer(CustomEmojiTokenizer())
            binding.inputMessage.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    this
                )
            )
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

    private fun openDriveActivity(){
        val intent = Intent(this, DriveActivity::class.java)
        intent.putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, 1)
        startActivityForResult(intent, SELECT_DRIVE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            SELECT_DRIVE_FILE_REQUEST_CODE ->{
                if(resultCode == Activity.RESULT_OK){
                    mViewModel.file.value = (data?.getSerializableExtra(DriveActivity.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE) as List<*>).map{
                        it as FilePropertyDTO
                    }.firstOrNull()
                }
            }
        }
    }
}
