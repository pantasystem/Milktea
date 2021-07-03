package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityMessageBinding
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.view.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.view.text.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.messaging.MessageActionViewModel
import jp.panta.misskeyandroidclient.view.messaging.MessageFragment
import jp.panta.misskeyandroidclient.model.messaging.MessagingId
import jp.panta.misskeyandroidclient.view.TitleSettable

class MessageActivity : AppCompatActivity(), TitleSettable {

    companion object{
        const val EXTRA_MESSAGING_ID = "jp.panta.misskeyandroidclient.MessageActivity.EXTRA_MESSAGING_ID"
    }

    private lateinit var mViewModel: MessageActionViewModel
    private lateinit var mBinding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView<ActivityMessageBinding>(this, R.layout.activity_message)
        mBinding.lifecycleOwner = this
        setSupportActionBar(mBinding.messageToolbar)

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
        mBinding.actionViewModel = messageActionViewModel

        mBinding.openDrive.setOnClickListener {
            openDriveActivity()
        }

        val miCore = application as MiCore
        miCore.getCurrentInstanceMeta()?.emojis?.let{ emojis ->
            mBinding.inputMessage.setTokenizer(CustomEmojiTokenizer())
            mBinding.inputMessage.setAdapter(
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
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityForPickFileResult.launch(intent)
    }

    private val openDriveActivityForPickFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val ids = (result.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as? List<*>)?.map {
            it as FileProperty.Id
        }
        ids?.firstOrNull()?.let {
            mViewModel.setFilePropertyFromId(it)
        }
    }
}
