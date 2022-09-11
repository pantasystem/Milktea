package net.pantasystem.milktea.messaging

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.messaging.databinding.ActivityMessagingListBinding
import javax.inject.Inject

@AndroidEntryPoint
class MessagingListActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMessagingListBinding

    @Inject
    lateinit var setTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_messaging_list)
        setSupportActionBar(mBinding.messagingListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.messagingListBase,
            MessagingHistoryFragment()
        )
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
