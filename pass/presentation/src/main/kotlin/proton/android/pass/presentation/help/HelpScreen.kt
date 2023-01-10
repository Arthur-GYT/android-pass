package proton.android.pass.presentation.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.pass.presentation.R

@ExperimentalMaterialApi
@Composable
fun HelpScreen(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onDrawerIconClick: () -> Unit
) {
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            ProtonTopAppBar(
                title = {
                    TopBarTitleView(
                        title = stringResource(id = R.string.title_help)
                    )
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.Menu,
                        modifier = Modifier.clickable { onDrawerIconClick() },
                        contentDescription = null
                    )
                }
            )
        }
    ) { contentPadding ->
        Box(modifier = modifier.padding(contentPadding)) {
            Text(text = "Future Help screen")
        }
    }
}
