package net.pantasystem.milktea.setting.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_compose.RadioTile
import net.pantasystem.milktea.common_compose.SwitchTile
import net.pantasystem.milktea.common_navigation.DriveNavigation
import net.pantasystem.milktea.common_navigation.DriveNavigationArgs
import net.pantasystem.milktea.common_navigation.EXTRA_SELECTED_FILE_PROPERTY_IDS
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.Theme
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.SettingTitleTile
import javax.inject.Inject

data class ThemeUiState(
    val type: Theme,
    @StringRes val label: Int,
)

@AndroidEntryPoint
class SettingAppearanceActivity : AppCompatActivity() {

    @Inject
    lateinit var mSettingStore: SettingStore

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var driveFileRepository: DriveFileRepository

    @Inject
    lateinit var localConfigRepository: LocalConfigRepository

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var driveNavigation: DriveNavigation

    private val themes: List<ThemeUiState> by lazy {
        listOf(
            ThemeUiState(
                Theme.White,
                R.string.theme_white
            ),
            ThemeUiState(
                Theme.Dark,
                R.string.theme_dark,
            ),
            ThemeUiState(
                Theme.Black,
                R.string.theme_black
            ),
            ThemeUiState(
                Theme.Bread,
                R.string.theme_bread,
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContent {
            val configState by settingStore.configState.collectAsState()

            var currentConfigState by remember {
                mutableStateOf(configState)
            }


            LaunchedEffect(key1 = currentConfigState) {
                localConfigRepository.save(currentConfigState).onFailure {
                    Log.d("SettingAppearance", "save error", it)
                }
            }
            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(id = R.string.appearance))
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            }
                        )
                    }
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier.padding(padding)
                    ) {
                        item {
                            SettingTitleTile(text = stringResource(id = R.string.theme))
                        }
                        items(themes) { theme ->
                            RadioTile(
                                selected = currentConfigState.theme == theme.type,
                                onClick = {
                                    currentConfigState = currentConfigState.copy(theme = theme.type)
                                }
                            ) {
                                Text(stringResource(theme.label))
                            }
                        }

                        item {
                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isClassicUI, onChanged = {
                                currentConfigState = currentConfigState.copy(isClassicUI = it)
                            }) {
                                Text(stringResource(R.string.hide_bottom_navigation))
                            }

                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isSimpleEditorEnabled,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isSimpleEditorEnabled = it)
                                }
                            ) {
                                Text(stringResource(R.string.use_simple_editor))
                            }

                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isUserNameDefault, onChanged = {
                                    currentConfigState = currentConfigState.copy(isUserNameDefault = it)
                                }) {
                                Text(stringResource(id = R.string.user_name_as_default_display_name))
                            }

                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isPostButtonAtTheBottom,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isPostButtonAtTheBottom = it)
                                }
                            ) {
                                Text(stringResource(id = R.string.post_button_at_the_bottom))
                            }
                            
                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isEnableInstanceTicker,
                                onChanged = {
                                    currentConfigState = currentConfigState.copy(isEnableInstanceTicker = it)
                                }
                            ) {
                                Text(stringResource(id = R.string.is_enable_instance_ticker))
                            }

                            SettingTitleTile(text = stringResource(id = R.string.background_image))
                            
                            if (configState.backgroundImagePath != null) {
                                Image(
                                    rememberAsyncImagePainter(configState.backgroundImagePath),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 9)
                                )
                            }

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Button(onClick = { showFileManager() }) {
                                    Icon(Icons.Filled.AddToPhotos, contentDescription = null)
                                    Text(stringResource(id = R.string.pick_image))
                                }
                                IconButton(onClick = {
                                    currentConfigState =
                                        currentConfigState.copy(backgroundImagePath = null)
                                }) {
                                    Icon(Icons.Filled.Remove, contentDescription = null)
                                }
                            }

                            SettingTitleTile(text = stringResource(id = R.string.note_opacity))
                            Slider(
                                value = currentConfigState.surfaceColorOpacity.toFloat() / 0xff,
                                onValueChange = {
                                    currentConfigState =
                                        currentConfigState.copy(surfaceColorOpacity = (it * 255).toInt())
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                        }


                    }
                }
            }
        }


    }

    private fun showFileManager() {
        val intent = driveNavigation.newIntent(DriveNavigationArgs(
            selectableFileMaxSize = 1,
            accountId = accountStore.currentAccountId
        ))
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityResult.launch(intent)
    }

    @Suppress("DEPRECATION")
    private val openDriveActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val ids =
                (result?.data?.getSerializableExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS) as List<*>?)?.mapNotNull {
                    it as? FileProperty.Id
                }
            val fileId = ids?.firstOrNull() ?: return@registerForActivityResult
            lifecycleScope.launch(Dispatchers.IO) {
                val file = runCancellableCatching {
                    driveFileRepository.find(fileId)
                }.onFailure {
                    Log.e("SettingAppearanceACT", "画像の取得に失敗", it)
                }.getOrNull()
                    ?: return@launch
                runCancellableCatching {
                    localConfigRepository.save(
                        localConfigRepository.get().getOrThrow().copy(
                            backgroundImagePath = file.url
                        )
                    )
                }

            }
        }


}
