package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.form.SimpleNoteSection
import proton.android.pass.composecomponents.impl.item.LinkedAppsListSection
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.AddTotp
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.AliasOptions
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.GeneratePassword
import proton.android.pass.featureitemcreate.impl.login.LoginStickyFormOptionsContentType.None
import proton.android.pass.featureitemcreate.impl.login.customfields.CustomFieldsContent

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("UnusedPrivateMember")
@Composable
internal fun LoginItemForm(
    modifier: Modifier = Modifier,
    isEditAllowed: Boolean,
    loginItem: LoginItem,
    totpUiState: TotpUiState,
    customFieldsState: CustomFieldsState,
    showCreateAliasButton: Boolean,
    primaryEmail: String?,
    isUpdate: Boolean,
    isTotpError: Boolean,
    focusLastWebsite: Boolean,
    canUpdateUsername: Boolean,
    websitesWithErrors: ImmutableList<Int>,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTotpChange: (String) -> Unit,
    onWebsiteSectionEvent: (WebsiteSectionEvent) -> Unit,
    onNoteChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    onAliasOptionsClick: () -> Unit,
    onPasteTotpClick: () -> Unit,
    onLinkedAppDelete: (PackageInfoUi) -> Unit,
    onNavigate: (BaseLoginNavigation) -> Unit,
    titleSection: @Composable ColumnScope.() -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = modifier) {
        var currentStickyFormOption by remember { mutableStateOf(None) }
        val isCurrentStickyVisible by remember(currentStickyFormOption) {
            mutableStateOf(currentStickyFormOption != None)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            titleSection()
            MainLoginSection(
                loginItem = loginItem,
                canUpdateUsername = canUpdateUsername,
                totpUiState = totpUiState,
                isEditAllowed = isEditAllowed,
                isTotpError = isTotpError,
                onUsernameChange = onUsernameChange,
                onUsernameFocus = { isFocused ->
                    currentStickyFormOption = if (isFocused) {
                        AliasOptions
                    } else {
                        None
                    }
                },
                onAliasOptionsClick = onAliasOptionsClick,
                onPasswordChange = onPasswordChange,
                onPasswordFocus = { isFocused ->
                    currentStickyFormOption = if (isFocused) {
                        GeneratePassword
                    } else {
                        None
                    }
                },
                onTotpChanged = onTotpChange,
                onTotpFocus = { isFocused ->
                    currentStickyFormOption = if (isFocused) {
                        AddTotp
                    } else {
                        None
                    }
                },
                onUpgrade = { onNavigate(BaseLoginNavigation.Upgrade) }
            )
            WebsitesSection(
                websites = loginItem.websiteAddresses.toImmutableList(),
                isEditAllowed = isEditAllowed,
                websitesWithErrors = websitesWithErrors,
                focusLastWebsite = focusLastWebsite,
                onWebsiteSectionEvent = onWebsiteSectionEvent
            )
            SimpleNoteSection(
                value = loginItem.note,
                enabled = isEditAllowed,
                onChange = onNoteChange
            )
            if (isUpdate) {
                LinkedAppsListSection(
                    packageInfoUiSet = loginItem.packageInfoSet,
                    isEditable = true,
                    onLinkedAppDelete = onLinkedAppDelete
                )
            }

            CustomFieldsContent(
                state = customFieldsState,
                canEdit = isEditAllowed,
                onNavigate = onNavigate
            )

            if (isCurrentStickyVisible) {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            visible = isCurrentStickyVisible,
            enter = expandVertically()
        ) {
            when (currentStickyFormOption) {
                GeneratePassword ->
                    StickyGeneratePassword(
                        onClick = {
                            onGeneratePasswordClick()
                            keyboardController?.hide()
                        }
                    )

                AliasOptions -> StickyUsernameOptions(
                    showCreateAliasButton = showCreateAliasButton,
                    primaryEmail = primaryEmail,
                    onCreateAliasClick = {
                        onCreateAliasClick()
                        keyboardController?.hide()
                    },
                    onPrefillCurrentEmailClick = {
                        onUsernameChange(it)
                        keyboardController?.hide()
                    }
                )

                AddTotp -> StickyTotpOptions(
                    onPasteCode = {
                        onPasteTotpClick()
                        keyboardController?.hide()
                    },
                    onScanCode = {
                        onNavigate(BaseLoginNavigation.ScanTotp)
                        keyboardController?.hide()
                    }
                )

                None -> {}
            }
        }
    }
}
