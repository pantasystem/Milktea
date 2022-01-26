package jp.panta.misskeyandroidclient.ui.settings.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivitySettingAppearanceBinding
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.ui.settings.SettingAdapter
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.BooleanSharedItem
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.SelectionSharedItem

class SettingAppearanceActivity : AppCompatActivity() {

    private  lateinit var mSettingStore: SettingStore
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
        mSettingStore = SettingStore(getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE))
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

        mBinding.noteOpacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
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

        mBinding.deleteBackgroundImage.setOnClickListener{
            setBackgroundImagePath(null)
        }

    }



    private fun showFileManager(){
        if(checkPermission()){
            requestSelectFileResult.launch(arrayOf("*/*"))
        }else{
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean{
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        if(! checkPermission()){
            requestReadExternalStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }



    private val requestSelectFileResult = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let{
            setBackgroundImagePath(uri.toString())
        }
    }

    private val requestReadExternalStorageLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it){
            showFileManager()
        }else{
            Toast.makeText(this, "ストレージへのアクセスを許可しないとファイルを読み込めないぽよ", Toast.LENGTH_LONG).show()
        }
    }

    private fun setBackgroundImagePath(path: String?){
        mBinding.backgroundImagePath.text = path?: ""
        Glide.with(this)
            .load(path)
            .into(mBinding.backgroundImagePreview)

        mSettingStore.backgroundImagePath = path

    }
}
