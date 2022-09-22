package me.proton.android.pass.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ConfirmItemDeletionDialog
import me.proton.android.pass.ui.shared.ConfirmSignOutDialog
import me.proton.android.pass.ui.shared.ItemAction
import me.proton.android.pass.ui.shared.ItemsList
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavDrawerNavigation
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawer
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState

internal typealias OnItemClick = (ShareId, ItemId) -> Unit

interface HomeScreenNavigation {
    val toCreateLogin: (ShareId) -> Unit
    val toEditLogin: (ShareId, ItemId) -> Unit

    val toCreateNote: (ShareId) -> Unit
    val toEditNote: (ShareId, ItemId) -> Unit

    val toCreateAlias: (ShareId) -> Unit
    val toEditAlias: (ShareId, ItemId) -> Unit

    val toItemDetail: (ShareId, ItemId) -> Unit

    val toCreatePassword: (ShareId) -> Unit
}

@ExperimentalMaterialApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navDrawerNavigation: NavDrawerNavigation,
    navigation: HomeScreenNavigation,
    viewModel: HomeViewModel = hiltViewModel()
) {
    RequestAutofillIfSupported()
    val uiState by rememberFlowWithLifecycle(flow = viewModel.viewState)
        .collectAsState(initial = HomeUiState.Loading)
    val navDrawerState by rememberFlowWithLifecycle(flow = viewModel.navDrawerState)
        .collectAsState(initial = viewModel.initialNavDrawerState)
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val selectedShare = if (uiState is HomeUiState.Content) {
        (uiState as HomeUiState.Content).selectedShare
    } else {
        null
    }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            BottomSheetContents(
                state = bottomSheetState,
                navigation = navigation,
                shareId = selectedShare
            )
        }
    ) {
        HomeScreenContents(
            uiState = uiState,
            bottomSheetState = bottomSheetState,
            navDrawerNavigation = navDrawerNavigation,
            navDrawerState = navDrawerState,
            navigation = navigation,
            sendItemToTrash = { viewModel.sendItemToTrash(it) },
            modifier = modifier
        )
    }
}

@Suppress("LongParameterList")
@ExperimentalMaterialApi
@Composable
private fun HomeScreenContents(
    uiState: HomeUiState,
    bottomSheetState: ModalBottomSheetState,
    navDrawerNavigation: NavDrawerNavigation,
    navDrawerState: NavigationDrawerViewState,
    navigation: HomeScreenNavigation,
    sendItemToTrash: (ItemUiModel) -> Unit,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var confirmSignOutDialogState by remember { mutableStateOf<Boolean?>(null) }
    val homeScaffoldState = rememberHomeScaffoldState()
    val isDrawerOpen = with(homeScaffoldState.scaffoldState.drawerState) {
        isOpen && !isAnimationRunning || isClosed && isAnimationRunning
    }
    LaunchedEffect(isDrawerOpen) {
        navDrawerNavigation.onDrawerStateChanged(isDrawerOpen)
    }
    val drawerGesturesEnabled by homeScaffoldState.drawerGesturesEnabled

    Scaffold(
        modifier = modifier,
        scaffoldState = homeScaffoldState.scaffoldState,
        drawerContent = {
            NavigationDrawer(
                drawerState = homeScaffoldState.scaffoldState.drawerState,
                viewState = navDrawerState,
                navigation = navDrawerNavigation,
                onSignOutClick = { confirmSignOutDialogState = true },
                modifier = Modifier
                    .statusBarsPadding()
                    .navigationBarsPadding()
            )
        },
        drawerGesturesEnabled = drawerGesturesEnabled,
        topBar = {
            HomeTopBar(
                homeScaffoldState = homeScaffoldState,
                bottomSheetState = bottomSheetState,
                coroutineScope = coroutineScope
            )
        }
    ) { contentPadding ->
        Box {
            when (uiState) {
                is HomeUiState.Loading -> LoadingDialog()
                is HomeUiState.Content -> {
                    var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }
                    Home(
                        items = uiState.items,
                        modifier = Modifier.padding(contentPadding),
                        onItemClick = { shareId, itemId -> navigation.toItemDetail(shareId, itemId) },
                        navigation = navigation,
                        onDeleteItemClicked = { itemToDelete = it }
                    )
                    ConfirmSignOutDialog(
                        state = confirmSignOutDialogState,
                        onDismiss = { confirmSignOutDialogState = null },
                        onConfirm = { navDrawerNavigation.onRemove(null) }
                    )
                    ConfirmItemDeletionDialog(
                        state = itemToDelete,
                        onDismiss = { itemToDelete = null },
                        title = R.string.alert_confirm_item_send_to_trash_title,
                        message = R.string.alert_confirm_item_send_to_trash_message,
                        onConfirm = sendItemToTrash
                    )
                }
                is HomeUiState.Error -> Text("Something went boom: ${uiState.message}")
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun HomeTopBar(
    homeScaffoldState: HomeScaffoldState,
    bottomSheetState: ModalBottomSheetState,
    coroutineScope: CoroutineScope
) {
    val (isSearchMode, setIsSearchMode) = remember { mutableStateOf(false) }

    ProtonTopAppBar(
        title = {
            if (isSearchMode) {
                HomeSearchBar()
            } else {
                TopBarTitleView(title = stringResource(id = R.string.title_items))
            }
        },
        navigationIcon = {
            Icon(
                Icons.Default.Menu,
                modifier = Modifier.clickable(onClick = {
                    val drawerState = homeScaffoldState.scaffoldState.drawerState
                    coroutineScope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() }
                }),
                contentDescription = null
            )
        },
        actions = {
            IconButton(onClick = {
                setIsSearchMode(true)
            }) {
                Icon(
                    painterResource(R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    if (bottomSheetState.isVisible) {
                        bottomSheetState.hide()
                    } else {
                        bottomSheetState.show()
                    }
                }
            }) {
                Icon(
                    painterResource(R.drawable.ic_proton_plus),
                    contentDescription = stringResource(R.string.action_create),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}

@Composable
private fun HomeSearchBar() {
    Text(text = "TODO: Search :)")
}

@Composable
private fun Home(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onItemClick: OnItemClick,
    navigation: HomeScreenNavigation,
    onDeleteItemClicked: (ItemUiModel) -> Unit
) {
    if (items.isNotEmpty()) {
        ItemsList(
            items = items,
            modifier = modifier,
            onItemClick = onItemClick,
            itemActions = listOf(
                ItemAction(
                    onSelect = { goToEdit(navigation, it) },
                    title = R.string.action_edit_placeholder,
                    icon = R.drawable.ic_proton_eraser,
                    textColor = ProtonTheme.colors.textNorm
                ),
                ItemAction(
                    onSelect = { onDeleteItemClicked(it) },
                    title = R.string.action_move_to_trash,
                    icon = R.drawable.ic_proton_trash,
                    textColor = ProtonTheme.colors.notificationError
                )
            )
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.message_no_saved_credentials),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

internal fun goToEdit(
    navigation: HomeScreenNavigation,
    item: ItemUiModel
) {
    when (item.itemType) {
        is ItemType.Login -> navigation.toEditLogin(item.shareId, item.id)
        is ItemType.Note -> navigation.toEditNote(item.shareId, item.id)
        is ItemType.Alias -> navigation.toEditAlias(item.shareId, item.id)
    }
}

@Stable
@ExperimentalMaterialApi
data class HomeScaffoldState(
    val scaffoldState: ScaffoldState,
    val drawerGesturesEnabled: MutableState<Boolean>
)

@Composable
@ExperimentalMaterialApi
fun rememberHomeScaffoldState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    drawerGesturesEnabled: MutableState<Boolean> = mutableStateOf(true)
): HomeScaffoldState = remember {
    HomeScaffoldState(
        scaffoldState,
        drawerGesturesEnabled
    )
}

@Composable
private fun RequestAutofillIfSupported() {
//    if (Build.VERSION.SDK_INT >= 9000) {
//        val context = LocalContext.current
//        RequestAutofillAccessIfNeeded(context = context)
//    }
}

/*
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RequestAutofillAccessIfNeeded(context: Context) {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    if (!autofillManager.hasEnabledAutofillServices()) {
        LaunchedEffect(true) {
            val intent = Intent(ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
}
*/
