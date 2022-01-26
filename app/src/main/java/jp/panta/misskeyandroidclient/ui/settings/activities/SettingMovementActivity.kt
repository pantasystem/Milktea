package jp.panta.misskeyandroidclient.ui.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivitySettingsBinding
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.ui.settings.SettingAdapter
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.BooleanSharedItem
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.Group
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.TextSharedItem

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
        val autoLoadTimeline = BooleanSharedItem(
            key = KeyStore.BooleanKey.AUTO_LOAD_TIMELINE.name,
            default = KeyStore.BooleanKey.AUTO_LOAD_TIMELINE.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.auto_load_timeline
        )

        val hideRemovedNote = BooleanSharedItem(
            key = KeyStore.BooleanKey.HIDE_REMOVED_NOTE.name,
            default = KeyStore.BooleanKey.HIDE_REMOVED_NOTE.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.hide_removed_note
        )
        val timelineGroup = Group(
            titleStringRes = R.string.timeline,
            context = this,
            items = listOf(
                includeLocalRenotes,
                includeRenotedMeyNotes,
                includeMyRenotes,
                autoLoadTimeline,
                hideRemovedNote
            )
        )

        val noteTextLimitGroup = Group(
            titleStringRes = R.string.auto_note_folding,
            context = this,
            items = listOf(
                TextSharedItem(
                    KeyStore.AutoTextFoldingCount.LENGTH.name,
                    R.string.text_length_limit,
                    type = TextSharedItem.InputType.NUMBER,
                    context = this,
                    default = KeyStore.AutoTextFoldingCount.LENGTH.default.toString()
                ),
                TextSharedItem(
                    KeyStore.AutoTextFoldingCount.RETURNS.name,
                    R.string.returns_limit,
                    type = TextSharedItem.InputType.NUMBER,
                    context = this,
                    default = KeyStore.AutoTextFoldingCount.RETURNS.default.toString()
                )
            )
        )

        val updateTimelineInBackground = BooleanSharedItem(
            key = KeyStore.BooleanKey.UPDATE_TIMELINE_IN_BACKGROUND.name,
            default = KeyStore.BooleanKey.UPDATE_TIMELINE_IN_BACKGROUND.default,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.update_timeline_in_background
        )





        val syncGroup = Group(
            titleStringRes = R.string.sync,
            items = listOf(updateTimelineInBackground),
            context = this
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

        adapter.submitList(listOf(timelineGroup, syncGroup, noteTextLimitGroup, postGroup))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
