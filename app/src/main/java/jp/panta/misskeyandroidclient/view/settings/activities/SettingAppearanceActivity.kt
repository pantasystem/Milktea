package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.settings.SettingAdapter
import jp.panta.misskeyandroidclient.viewmodel.setting.Group
import jp.panta.misskeyandroidclient.viewmodel.setting.SelectionSharedItem
import kotlinx.android.synthetic.main.activity_setting_appearance.*

class SettingAppearanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_appearance)

        val themeChoices = listOf(
            SelectionSharedItem.Choice(
                R.string.theme_white,
                KeyStore.IntKey.THEME_WHITE.default,
                this
            ),
            SelectionSharedItem.Choice(
                R.string.theme_dark,
                KeyStore.IntKey.THEME_DARK.default,
                this
            ),
            SelectionSharedItem.Choice(
                R.string.theme_black,
                KeyStore.IntKey.THEME_BLACK.default,
                this
            ),
            SelectionSharedItem.Choice(
                R.string.theme_bread,
                KeyStore.IntKey.THEME_BREAD.default,
                this
            )
        )
        val themeSelection = SelectionSharedItem(
            KeyStore.IntKey.THEME.name,
            R.string.theme,
            KeyStore.IntKey.THEME.default,
            themeChoices,
            this
        )
        //val group = Group(null, listOf(themeSelection), this)
        val adapter = SettingAdapter(this)
        setting_list.layoutManager = LinearLayoutManager(this)
        setting_list.adapter = adapter
        adapter.submitList(listOf(themeSelection))

    }
}
