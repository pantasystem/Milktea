package net.pantasystem.milktea.setting.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_resource.R
import net.pantasystem.milktea.setting.compose.SettingListTileLayout
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AboutMilkteaActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        val version = getSelfVersion()
        val lang = Locale.getDefault().language
        setContent {
            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "navigate up")
                                }
                            },
                            title = {
                                Text(stringResource(id = R.string.settings_about_milktea))
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier.padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Image(
                            painter = rememberAsyncImagePainter("https://raw.githubusercontent.com/pantasystem/Milktea/master/app/src/main/ic_launcher-web.png"),
                            contentDescription = "App icon",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(id = R.string.app_name),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(version ?: "")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(id = R.string.milktea_catchphrase))
                        Spacer(Modifier.height(16.dp))

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pantasystem/Milktea")))
                            }
                        ) {
                            Text(stringResource(id = R.string.settings_about_milktea_source_code))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.patreon.com/pantasystem")))
                            }
                        ) {
                            Text(stringResource(R.string.donation))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pantasystem/Milktea/blob/develop/privacy_policy_${
                                    when(lang) {
                                        "zh", "jp", "en" -> lang
                                        else -> "en"
                                    }
                                }.md")))
                            }
                        ) {
                            Text(stringResource(id = R.string.privacy_policy))
                        }

                        SettingListTileLayout(
                            verticalPadding = 12.dp,
                            onClick = {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pantasystem/Milktea/blob/develop/terms_of_service_${
                                    when(lang) {
                                        "zh", "jp", "en" -> lang
                                        else -> "en"
                                    }
                                }.md")))
                            }
                        ) {
                            Text(stringResource(id = R.string.terms_of_service))
                        }
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getSelfVersion(): String? {
        val pm = packageManager
        return try {
            pm.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
    }
}