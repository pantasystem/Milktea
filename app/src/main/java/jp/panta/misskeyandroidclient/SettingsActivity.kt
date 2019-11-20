package jp.panta.misskeyandroidclient

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.view.settings.SettingAdapter
import jp.panta.misskeyandroidclient.viewmodel.setting.BooleanSharedItem
import jp.panta.misskeyandroidclient.viewmodel.setting.Group
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val keys = KeyStore.BooleanKey.values().toList()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        /*keys.forEach{

        }*/
        val includeLocalRenotes = BooleanSharedItem(
            key = KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.name,
            default = true,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.include_local_renotes

        )

        val includeRenotedMeyNotes = BooleanSharedItem(
            key = KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.name,
            default = true,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.include_renoted_my_notes
        )

        val includeMyRenotes = BooleanSharedItem(
            key = KeyStore.BooleanKey.INCLUDE_MY_RENOTES.name,
            default = true,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.include_my_renotes
        )
        val autoLoadTimeline = BooleanSharedItem(
            key = KeyStore.BooleanKey.AUTO_LOAD_TIMELINE.name,
            default = true,
            choiceType = BooleanSharedItem.ChoiceType.SWITCH,
            context = this,
            titleStringRes = R.string.auto_load_timeline
        )
        val timelineGroup = Group(
            titleStringRes = R.string.timeline,
            context = this,
            items = listOf(includeLocalRenotes, includeRenotedMeyNotes, includeMyRenotes, autoLoadTimeline)
        )



        val adapter = SettingAdapter(this)
        setting_list.adapter = adapter
        setting_list.layoutManager = LinearLayoutManager(this)

        adapter.submitList(listOf(timelineGroup))



    }
}
