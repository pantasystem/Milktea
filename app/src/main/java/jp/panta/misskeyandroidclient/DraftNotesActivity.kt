package jp.panta.misskeyandroidclient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityDraftNotesBinding

@AndroidEntryPoint
class DraftNotesActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDraftNotesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_draft_notes)
        mBinding.lifecycleOwner = this

    }


}
