package jp.panta.misskeyandroidclient.ui.settings.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivitySettingsBinding
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.ui.settings.SettingAdapter
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.BooleanSharedItem
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.Group
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.TextSharedItem
import net.pantasystem.milktea.data.infrastructure.KeyStore
import net.pantasystem.milktea.data.infrastructure.settings.Keys
import net.pantasystem.milktea.data.infrastructure.settings.str
import net.pantasystem.milktea.model.setting.DefaultConfig

class SettingMovementActivity : AppCompatActivity() {

    lateinit var mBinding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        setSupportActionBar(mBinding.settingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val includeLocalRenotes = BooleanSharedItem(
            key = KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.name,
            default = KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.include_local_renotes

        )

        val includeRenotedMeyNotes = BooleanSharedItem(
            key = KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.name,
            default = KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.include_renoted_my_notes
        )

        val includeMyRenotes = BooleanSharedItem(
            key = KeyStore.BooleanKey.INCLUDE_MY_RENOTES.name,
            default = KeyStore.BooleanKey.INCLUDE_MY_RENOTES.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.include_my_renotes
        )


        val timelineGroup = Group(
            titleStringRes = R.string.timeline,
            context = this,
            items = listOf(
                includeLocalRenotes,
                includeRenotedMeyNotes,
                includeMyRenotes,
            )
        )

        val noteTextLimitGroup = Group(
            titleStringRes = R.string.auto_note_folding,
            context = this,
            items = listOf(
                TextSharedItem(
                    Keys.NoteLimitHeight.str(),
                    R.string.height_limit,
                    type = TextSharedItem.InputType.NUMBER,
                    context = this,
                    default = DefaultConfig.config.noteExpandedHeightSize.toString()
                ),
            )
        )




        val learnNoteVisibility = BooleanSharedItem(
            key = KeyStore.BooleanKey.IS_LEARN_NOTE_VISIBILITY.name,
            default = KeyStore.BooleanKey.IS_LEARN_NOTE_VISIBILITY.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.learn_note_visibility
        )

        val postGroup = Group(
            titleStringRes = R.string.post,
            items = listOf(learnNoteVisibility),
            context = this
        )

        val adapter = SettingAdapter(this)
        mBinding.settingList.adapter = adapter
        mBinding.settingList.layoutManager = LinearLayoutManager(this)

        adapter.submitList(listOf(timelineGroup, noteTextLimitGroup, postGroup))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
