package me.proton.pass.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.TrashItem
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.common.api.some
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.detail.DetailSnackbarMessages.InitError
import me.proton.pass.presentation.detail.DetailSnackbarMessages.SendToTrashError
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsSentToTrashState
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val trashItem: TrashItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: Option<ShareId> =
        Option.fromNullable(savedStateHandle.get<String>("shareId")?.let { ShareId(it) })
    private val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private val itemModelState: MutableStateFlow<Option<ItemModelUiState>> =
        MutableStateFlow(None)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)

    val uiState: StateFlow<ItemDetailScreenUiState> = combine(
        itemModelState,
        isLoadingState,
        isItemSentToTrashState
    ) { itemModel, isLoading, isItemSentToTrash ->
        ItemDetailScreenUiState(
            itemModel,
            isLoading,
            isItemSentToTrash
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ItemDetailScreenUiState.Loading
        )

    init {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .firstOrNull { userId -> userId != null }
            if (userId != null && shareId is Some && itemId is Some) {
                itemRepository.getById(userId, shareId.value, itemId.value)
                    .onSuccess { item ->
                        itemModelState.update {
                            ItemModelUiState(
                                name = item.title.decrypt(cryptoContext.keyStoreCrypto),
                                item = item
                            ).some()
                        }
                    }
                    .onError {
                        val defaultMessage = "Get by id error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                        snackbarMessageRepository.emitSnackbarMessage(InitError)
                    }
            } else {
                PassLogger.i(TAG, "Empty user/share/item Id")
                snackbarMessageRepository.emitSnackbarMessage(InitError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun sendItemToTrash(item: Item) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            trashItem(userId, item.shareId, item.id)
            isItemSentToTrashState.update { IsSentToTrashState.Sent }
        } else {
            PassLogger.i(TAG, "Empty userId")
            snackbarMessageRepository.emitSnackbarMessage(SendToTrashError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onEmitSnackbarMessage(snackbarMessage: DetailSnackbarMessages) =
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }

    companion object {
        private const val TAG = "ItemDetailViewModel"
    }
}
