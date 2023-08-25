package net.pantasystem.milktea.user.qrshare

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.profile.viewmodel.UserDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class QRShareDialog : AppCompatDialogFragment() {
    companion object {
        const val FRAGMENT_TAG = "QRShareDialog"
    }

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    @Inject
    lateinit var qrCodeBitmapGenerator: QRCodeBitmapGenerator

    val viewModel by activityViewModels<UserDetailViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).also { builder ->
            builder.setView(
                ComposeView(requireContext()).apply {
                    setContent {
                        MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                            val user by viewModel.userState.collectAsState()
                            val url by viewModel.originProfileUrl.collectAsState()
                            val scope = rememberCoroutineScope()

                            QRShareDialogLayout(
                                user,
                                url,
                                qrCodeBitmapGenerator,
                                onCopyUrlButtonClicked = {
                                    // クリップボードにuserのurlをコピーする
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("url", it))
                                },
                                onShareButtonClicked = { shareTarget ->
                                    // urlとbitmapを共有する
                                    scope.launch {
                                        qrCodeBitmapGenerator.exportToFile(shareTarget, 512).onSuccess { uri ->
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                type = "image/png"
                                            }
                                            requireContext().startActivity(Intent.createChooser(sendIntent, null))
                                        }
                                    }
                                },
                                onSaveButtonClicked = { exportTargetUser ->
                                    // bitmapを保存する
                                    scope.launch {
                                        qrCodeBitmapGenerator.exportToFile(exportTargetUser, 512).onSuccess {
                                            Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                                        }.onFailure {
                                            Toast.makeText(context, R.string.failure, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            )
        }.create()
    }
}

@Composable
fun QRShareDialogLayout(
    user: User?,
    url: String?,
    qrCodeBitmapGenerator: QRCodeBitmapGenerator,
    onCopyUrlButtonClicked: (String) -> Unit = {},
    onShareButtonClicked: (User) -> Unit = {},
    onSaveButtonClicked: (User) -> Unit = {},
) {
    Surface {
        var qrBitmap by remember {
            mutableStateOf<ImageBitmap?>(null)
        }

        Column(
            Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                val height = this.maxHeight
                val width = this.maxWidth

                val density = LocalDensity.current
                val size = remember(height, width) {
                    if (height > width) width else height
                }
                val sizePx = with(density) {
                    size.toPx()
                }
                LaunchedEffect(user?.displayUserName) {

                    if (user != null) {
                        qrBitmap = qrCodeBitmapGenerator.generateBitmap(user, sizePx.toInt()).getOrNull()
                            ?.asImageBitmap()
                    }

                }
                when (val bitmap = qrBitmap) {
                    null -> Unit
                    else -> Image(
                        bitmap,
                        contentDescription = null,
                        Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.heightIn(16.dp))
            Text(user?.displayUserName ?: "", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.heightIn(16.dp))
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    Modifier
                        .clickable {
                            url?.let {
                                onCopyUrlButtonClicked(it)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Text(stringResource(id = R.string.copy))
                }

                Column(
                    Modifier
                        .clickable {
                            user?.let { exportTargetUser ->
                                onShareButtonClicked(exportTargetUser)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text(stringResource(id = R.string.share))
                }

                Column(
                    Modifier
                        .clickable {
                            user?.let(onSaveButtonClicked)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    }
}