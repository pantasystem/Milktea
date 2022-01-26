package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.databinding.ActivityMessagingListBinding
import jp.panta.misskeyandroidclient.ui.messaging.MessagingHistoryFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class MessagingListActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMessagingListBinding

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_messaging_list)
        setSupportActionBar(mBinding.messagingListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.messagingListBase, MessagingHistoryFragment())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
