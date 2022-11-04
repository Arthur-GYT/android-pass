package me.proton.pass.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonTextFieldPlaceHolder

@ExperimentalComposeUiApi
@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStopSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    ProtonTopAppBar(
        modifier = modifier,
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { ProtonTextFieldPlaceHolder(placeholder) },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp
                ),
                singleLine = true,
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        navigationIcon = { ArrowBackIcon { onStopSearch() } },
        elevation = 4.dp
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Preview
@ExperimentalComposeUiApi
@Composable
fun SearchHomeTopBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    ProtonTheme(isDark = isDarkMode) {
        Surface {
            SearchTopBar(
                placeholder = R.string.action_search,
                searchQuery = "some search",
                onSearchQueryChange = {},
                onStopSearch = {}
            )
        }
    }
}

