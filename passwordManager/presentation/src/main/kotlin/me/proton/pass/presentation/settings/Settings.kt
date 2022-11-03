package me.proton.pass.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.uievents.IsButtonEnabled

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onFingerPrintLockChange: (IsButtonEnabled) -> Unit
) {
    Column(modifier = modifier) {
        AutofillSection(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        Divider(modifier = Modifier.fillMaxWidth())
        AuthenticationSection(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            isToggleChecked = state.isFingerPrintEnabled,
            onToggleChange = onFingerPrintLockChange
        )
        Divider(modifier = Modifier.fillMaxWidth())
        AppearanceSection(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
    }
}

@Preview
@Composable
fun SettingsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            Settings(
                state = SettingsUiState(isFingerPrintEnabled = IsButtonEnabled.Enabled),
                onFingerPrintLockChange = {}
            )
        }
    }
}
