package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.databinding.ActivityDraftNotesBinding

class DraftNotesActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDraftNotesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_draft_notes)
        setContentView(R.layout.activity_draft_notes)

        setSupportActionBar(mBinding.draftNotesToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home ->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
