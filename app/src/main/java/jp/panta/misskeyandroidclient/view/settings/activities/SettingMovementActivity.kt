package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.view.settings.SettingAdapter
import jp.panta.misskeyandroidclient.viewmodel.setting.BooleanSharedItem
import jp.panta.misskeyandroidclient.viewmodel.setting.Group
import kotlinx.android.synthetic.main.activity_settings.*

class SettingMovementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_settings)

        setSupportActionBar(settingToolbar)
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

        val adapter = SettingAdapter(this)
        setting_list.adapter = adapter
        setting_list.layoutManager = LinearLayoutManager(this)

        adapter.submitList(listOf(timelineGroup, syncGroup))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
