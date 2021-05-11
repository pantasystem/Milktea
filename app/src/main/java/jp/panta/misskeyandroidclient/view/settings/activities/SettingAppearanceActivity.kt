package jp.panta.misskeyandroidclient.view.settings.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.databinding.ActivitySettingAppearanceBinding
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.settings.SettingAdapter
import jp.panta.misskeyandroidclient.viewmodel.setting.BooleanSharedItem
import jp.panta.misskeyandroidclient.viewmodel.setting.SelectionSharedItem

class SettingAppearanceActivity : AppCompatActivity() {

    companion object{
        const val SELECT_LOCAL_FILE_REQUEST_CODE = 514
        const val READ_STORAGE_PERMISSION_REQUEST_CODE = 1919
    }

    private  lateinit var mSettingStore: SettingStore
    private lateinit var mBinding: ActivitySettingAppearanceBinding

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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, SELECT_LOCAL_FILE_REQUEST_CODE)
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
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            SELECT_LOCAL_FILE_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    Log.d("NoteEditorActivity", "選択した")

                    val uri = data?.data
                    uri?.let{
                        setBackgroundImagePath(uri.toString())
                    }

                }
            }
            READ_STORAGE_PERMISSION_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    showFileManager()
                }else{
                    Toast.makeText(this, "ストレージへのアクセスを許可しないとファイルを読み込めないぽよ", Toast.LENGTH_LONG).show()
                }
            }

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
