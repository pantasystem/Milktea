import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android.resource.getString

@Composable
fun getStringFromStringSource(src: StringSource): String {
    return src.getString(LocalContext.current)
}