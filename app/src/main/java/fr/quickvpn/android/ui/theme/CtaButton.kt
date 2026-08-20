package fr.quickvpn.android.ui.theme

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CtaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = CtaWhite,
            contentColor = Background,
            disabledContainerColor = SurfaceDark,
            disabledContentColor = TextSecondary
        )
    ) {
        content()
    }
}

@Composable
fun CtaOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextLight,
            disabledContentColor = TextSecondary
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            CtaWhite.copy(alpha = 0.4f)
        )
    ) {
        content()
    }
}
