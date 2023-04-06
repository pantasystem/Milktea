package net.pantasystem.milktea.setting.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.compose.SettingListTileLayout
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var applyTheme: ApplyTheme

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContent {
            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = {
                                    finish()
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            },
                            title = {
                                Text(stringResource(id = R.string.setting))
                            }
                        )
                    }
                ) {
                    Column(
                        Modifier
                            .padding(it)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        AccountSettingActivity::class.java,
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.account))
                        }
                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        SettingMovementActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.movement))
                        }
                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        PageSettingActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.nav_setting_tab))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        SettingAppearanceActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.appearance))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        SecuritySettingActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(R.string.security_setting))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        RenoteMuteSettingActivity::class.java,
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.settings_renote_mute_title))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        ReactionSettingActivity::class.java,
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.reaction))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        ClientWordFilterSettingActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.client_word_mute))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        AboutMilkteaActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.settings_about_milktea))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@SettingsActivity,
                                        OssLicensesMenuActivity::class.java
                                    )
                                )
                            }
                        ) {
                            Text(stringResource(id = R.string.license))
                        }
                    }
                }
            }
        }
    }

}
