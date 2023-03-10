package proton.android.pass.featurevault.impl

import androidx.annotation.StringRes
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class VaultSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    EditVaultSuccess(R.string.snackbar_edit_vault_success, SnackbarType.SUCCESS),
    EditVaultError(R.string.snackbar_edit_vault_error, SnackbarType.ERROR),
    CreateVaultSuccess(R.string.snackbar_create_vault_success, SnackbarType.SUCCESS),
    CannotCreateMoreVaultsError(R.string.snackbar_cannot_create_more_vaults_error, SnackbarType.ERROR),
    CreateVaultError(R.string.snackbar_create_vault_error, SnackbarType.ERROR),
}
