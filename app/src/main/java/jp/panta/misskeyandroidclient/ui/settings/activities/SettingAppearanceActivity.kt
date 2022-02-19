package jp.panta.misskeyandroidclient.ui.settings.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.databinding.ActivitySettingAppearanceBinding
import jp.panta.misskeyandroidclient.model.account.AccountStore
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepository
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.ui.settings.SettingAdapter
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.BooleanSharedItem
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.SelectionSharedItem
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingAppearanceActivity : AppCompatActivity() {

    @Inject
    lateinit var mSettingStore: SettingStore

    @Inject
    lateinit var accountStore: AccountStore
    @Inject
    lateinit var driveFileRepository: DriveFileRepository
    private val mBinding: ActivitySettingAppearanceBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_setting_appearance)

        setSupportActionBar(mBinding.appearanceToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        mBinding.settingList.layoutManager = LinearLayoutManager(this)
        mBinding.settingList.adapter = adapter
        adapter.submitList(
            listOf(
                themeSelection,
                BooleanSharedItem(
                    key = KeyStore.BooleanKey.HIDE_BOTTOM_NAVIGATION.name,
                    default = KeyStore.BooleanKey.HIDE_BOTTOM_NAVIGATION.default,
                    choiceType = BooleanSharedItem.ChoiceType.SWITCH,
                    context = this,
                    titleStringRes = R.string.hide_bottom_navigation
                ),
                BooleanSharedItem(
                    key = KeyStore.BooleanKey.IS_SIMPLE_EDITOR_ENABLED.name,
                    default = KeyStore.BooleanKey.IS_SIMPLE_EDITOR_ENABLED.default,
                    choiceType = BooleanSharedItem.ChoiceType.SWITCH,
                    context = this,
                    titleStringRes = R.string.use_simple_editor
                ),
                BooleanSharedItem(
                    key = KeyStore.BooleanKey.IS_USER_NAME_DEFAULT.name,
                    default = KeyStore.BooleanKey.IS_USER_NAME_DEFAULT.default,
                    choiceType = BooleanSharedItem.ChoiceType.SWITCH,
                    context = this,
                    titleStringRes = R.string.user_name_as_default_display_name
                ),
                BooleanSharedItem(
                    key = KeyStore.BooleanKey.IS_POST_BUTTON_TO_BOTTOM.name,
                    default = KeyStore.BooleanKey.IS_POST_BUTTON_TO_BOTTOM.default,
                    choiceType = BooleanSharedItem.ChoiceType.SWITCH,
                    context = this,
                    titleStringRes = R.string.post_button_at_the_bottom
                )
            )
        )

        val miApplication = applicationContext as MiApplication

        mBinding.noteOpacitySeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                miApplication.colorSettingStore.surfaceColorOpaque = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        mBinding.noteOpacitySeekBar.progress = miApplication.colorSettingStore.surfaceColorOpaque
        setBackgroundImagePath(mSettingStore.backgroundImagePath)
        mBinding.attachedBackgroundImageFile.setOnClickListener {
            // show file manager
            showFileManager()
        }

        mBinding.deleteBackgroundImage.setOnClickListener {
            setBackgroundImagePath(null)
        }

    }


    private fun showFileManager() {
        val intent = Intent(this, DriveActivity::class.java)
            .putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, 1)
            .putExtra(DriveActivity.EXTRA_ACCOUNT_ID, accountStore.currentAccount?.accountId)
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityResult.launch(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setBackgroundImagePath(path: String?) {
        mBinding.backgroundImagePath.text = path ?: ""
        Glide.with(this)
            .load(path)
            .into(mBinding.backgroundImagePreview)

        mSettingStore.backgroundImagePath = path

    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val openDriveActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val ids =
                (result?.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as List<*>?)?.mapNotNull {
                    it as? FileProperty.Id
                }
            val fileId = ids?.firstOrNull() ?: return@registerForActivityResult
            lifecycleScope.launch(Dispatchers.IO) {
                val file = runCatching {
                    driveFileRepository.find(fileId)
                }.onFailure {
                    Log.e("SettingAppearanceACT", "画像の取得に失敗", it)
                }.getOrNull()
                    ?: return@launch
                withContext(Dispatchers.Main) {
                    setBackgroundImagePath(file.url)
                }
            }
        }

}
