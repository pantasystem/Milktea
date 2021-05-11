package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.databinding.ActivityMessagingListBinding
import jp.panta.misskeyandroidclient.view.messaging.MessagingHistoryFragment

class MessagingListActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMessagingListBinding

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
