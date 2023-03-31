package net.pantasystem.milktea.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.data.infrastructure.auth.Authorization

@Composable
fun AuthApprovedScreen(
    modifier: Modifier = Modifier,
    state: Authorization.Approved,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.auth_welcome), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }

        Row(Modifier.fillMaxWidth()) {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.75f)
            ) {
                Text(
                    stringResource(id = R.string.auth_continue_auth)
                )
            }
        }

    }
}