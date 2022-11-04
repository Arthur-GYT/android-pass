package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AliasSelector(
    state: AliasItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val value = if (state.selectedSuffix != null) {
        state.selectedSuffix.suffix
    } else {
        ""
    }
    Selector(
        text = value,
        modifier = modifier,
        onClick = onClick
    )
}
