package net.pantasystem.milktea.user.qrshare

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.glxn.qrgen.android.QRCode
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@AndroidEntryPoint
class QRShareDialog : AppCompatDialogFragment() {

    @Inject
    internal lateinit var configRepository: LocalConfigRepository
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).also { builder ->
            builder.setView(
                ComposeView(requireContext()).apply {
                    setContent {
                        MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                            Surface {
                                var qrBitmap by remember {
                                    mutableStateOf<ImageBitmap?>(null)
                                }
                                DisposableEffect(Unit) {
                                    qrBitmap = QRCode.from(
                                        "https://calc.panta.systems/@Panta",
                                    ).withSize(512, 512).bitmap().asImageBitmap()
                                    onDispose {
                                        qrBitmap = null
                                    }
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
                                        DisposableEffect(Unit) {

                                            qrBitmap = QRCode.from(
                                                "https://calc.panta.systems/@Panta",
                                            ).withSize(
                                                sizePx.toInt(),
                                                sizePx.toInt(),
                                            ).bitmap().asImageBitmap()
                                            onDispose {
                                                qrBitmap = null
                                            }
                                        }
                                        when (qrBitmap) {
                                            null -> Unit
                                            else -> Image(
                                                qrBitmap!!,
                                                contentDescription = null,
                                                Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.heightIn(16.dp))
                                    Text("@Panta@calc.panta.systems", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.heightIn(16.dp))
                                    Row(
                                        Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Icon(Icons.Default.Link, contentDescription = null)
                                            Text("コピー")
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null)
                                            Text("共有")
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null)
                                            Text("保存")
                                        }
                                    }


                                }
                            }
                        }
                    }
                }
            )
        }.create()
    }
}