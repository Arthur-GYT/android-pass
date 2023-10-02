/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featurehome.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class HomeSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ObserveItemsError(R.string.error_observing_items, SnackbarType.ERROR),
    RefreshError(R.string.error_refreshing, SnackbarType.ERROR),
    LoginMovedToTrash(R.string.home_snackbar_login_moved_to_trash, SnackbarType.SUCCESS),
    AliasMovedToTrash(R.string.home_snackbar_alias_moved_to_trash, SnackbarType.SUCCESS),
    NoteMovedToTrash(R.string.home_snackbar_note_moved_to_trash, SnackbarType.SUCCESS),
    CreditCardMovedToTrash(R.string.home_snackbar_credit_card_moved_to_trash, SnackbarType.SUCCESS),
    MoveToTrashError(R.string.home_snackbar_move_to_trash_error, SnackbarType.ERROR),
    AliasCopied(R.string.alias_copied_to_clipboard, SnackbarType.NORM, true),
    NoteCopied(R.string.note_copied_to_clipboard, SnackbarType.NORM, true),
    PasswordCopied(R.string.password_copied_to_clipboard, SnackbarType.NORM, true),
    UsernameCopied(R.string.username_copied_to_clipboard, SnackbarType.NORM, true),
    CreditCardNumberCopied(R.string.credit_card_number_copied_to_clipboard, SnackbarType.NORM, true),
    CreditCardCvvCopied(R.string.credit_card_cvv_copied_to_clipboard, SnackbarType.NORM, true),

    ItemTooLongCopied(R.string.item_too_long_copied_to_clipboard, SnackbarType.WARNING, false),

    ClearTrashError(R.string.trash_error_clearing_trash, SnackbarType.ERROR),
    RestoreItemsError(R.string.trash_error_restoring_items, SnackbarType.ERROR),
    DeleteItemError(R.string.trash_error_deleting_item, SnackbarType.ERROR),
}

