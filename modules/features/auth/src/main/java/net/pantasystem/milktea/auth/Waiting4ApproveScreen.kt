package net.pantasystem.milktea.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.glxn.qrgen.android.QRCode
import net.pantasystem.milktea.data.infrastructure.auth.Authorization

@Composable
fun Waiting4ApproveScreen(
    modifier: Modifier = Modifier,
    state: Authorization.Waiting4UserAuthorization,
    onApprovedButtonClicked: () -> Unit,
    onCopyAuthUrlButtonClicked: () -> Unit,
) {
    var qrCode: ImageBitmap? by remember {
        mutableStateOf(null)
    }
    DisposableEffect(key1 = state) {
        qrCode = QRCode.from(state.generateAuthUrl()).bitmap().asImageBitmap()
        onDispose {
            qrCode = null
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(stringResource(id = R.string.auth_waiting_4_u_to_approve), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = state.generateAuthUrl(),
                    onValueChange = {},
                )
                IconButton(onClick = onCopyAuthUrlButtonClicked) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy url")
                }
            }
        }

        when(val image = qrCode) {
            null -> Unit
            else -> {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(id = R.string.auth_qr_code_message))
                    Image(image, contentDescription = null, modifier = Modifier.size(128.dp))
                }
            }
        }



        Button(
            onClick = onApprovedButtonClicked,
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(id = R.string.auth_i_have_approved))
        }
    }
}