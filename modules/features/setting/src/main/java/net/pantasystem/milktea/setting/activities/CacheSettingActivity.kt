package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.compose.SettingTitleTile
import net.pantasystem.milktea.setting.viewmodel.CacheSettingViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CacheSettingActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    private val viewModel by viewModels<CacheSettingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            },
                            title = {
                                Text(stringResource(id = R.string.settings_cache_config))
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        Modifier
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SettingTitleTile(stringResource(id = R.string.settings_note_cache))
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("Size: ${uiState.noteCacheSize}")
                            TextButton(onClick = viewModel::onClearNoteCache) {
                                Text(stringResource(id = R.string.remove))
                            }
                        }

                        SettingTitleTile(stringResource(id = R.string.settings_custom_emoji_cache))
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("Size: ${uiState.imageCacheSize}")
                            TextButton(onClick = viewModel::onClearCustomEmojiCache) {
                                Text(stringResource(id = R.string.remove))
                            }
                        }

                        SettingTitleTile(stringResource(id = R.string.settings_user_cache))
                        Column(Modifier.padding(horizontal = 16.dp)) {
                            Text("Size: ${uiState.userCacheSize}")
                            TextButton(onClick = viewModel::onClearUserCache) {
                                Text(stringResource(id = R.string.remove))
                            }
                        }
                    }
                }
            }
        }
    }
}