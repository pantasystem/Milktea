package net.pantasystem.milktea.note

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.note.databinding.ActivityDraftNotesBinding
import javax.inject.Inject

@AndroidEntryPoint
class DraftNotesActivity : AppCompatActivity() {

    @Inject
    lateinit var applyTheme: ApplyTheme

    private lateinit var mBinding: ActivityDraftNotesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_draft_notes)
        mBinding.lifecycleOwner = this

    }


}
