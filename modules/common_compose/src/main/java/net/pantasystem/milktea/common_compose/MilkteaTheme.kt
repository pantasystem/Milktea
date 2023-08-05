package net.pantasystem.milktea.common_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter.MdcTheme
import net.pantasystem.milktea.model.setting.AvatarIconShapeType
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository

@Composable
fun MilkteaStyleConfigApplyAndTheme(
    configRepository: LocalConfigRepository,
    content: @Composable () -> Unit,
) {
    val config by configRepository.observe().collectAsState(initial = DefaultConfig.config)
    MdcTheme {
        CompositionLocalProvider(
            LocalAvatarIconShape provides when (config.avatarIconShapeType) {
                AvatarIconShapeType.Circle -> Shape.Circle
                AvatarIconShapeType.Square -> Shape.RoundedCorner
            },
        ) {
            content()
        }
    }
}