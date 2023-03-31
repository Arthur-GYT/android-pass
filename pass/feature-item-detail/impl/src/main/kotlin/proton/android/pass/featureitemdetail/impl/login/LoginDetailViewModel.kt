package proton.android.pass.featureitemdetail.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.PasswordCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.TotpCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.UsernameCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.WebsiteCopiedToClipboard
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val observeTotpFromUri: ObserveTotpFromUri,
    private val trashItem: TrashItem,
    getItemById: GetItemById,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))
    private val itemId: ItemId =
        ItemId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ItemId.key)))

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val passwordState: MutableStateFlow<PasswordState> =
        MutableStateFlow(getInitialPasswordState())
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)

    private val observeTotpOptionFlow = getItemById(shareId, itemId)
        .flatMapLatest { itemLoadingResult ->
            val item = itemLoadingResult.getOrNull()
            item ?: return@flatMapLatest flowOf(None)
            val itemContents = item.itemType as ItemType.Login
            val decrypted = encryptionContextProvider.withEncryptionContext {
                decrypt(itemContents.primaryTotp)
            }
            observeTotpFromUri(decrypted)
                .map { flow -> flow.map { it.toOption() } }
                .getOrDefault(flowOf(None))
        }
        .distinctUntilChanged()

    val uiState: StateFlow<LoginDetailUiState> = combine(
        getItemById(shareId, itemId),
        passwordState,
        observeTotpOptionFlow,
        isLoadingState,
        isItemSentToTrashState
    ) { itemLoadingResult, password, totpOption, isLoading, isItemSentToTrash ->
        when (itemLoadingResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(InitError)
                LoginDetailUiState.Error
            }
            LoadingResult.Loading -> LoginDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val itemContents = itemLoadingResult.data.itemType as ItemType.Login
                encryptionContextProvider.withEncryptionContext {
                    LoginDetailUiState.Success(
                        shareId = itemLoadingResult.data.shareId,
                        itemId = itemLoadingResult.data.id,
                        itemType = itemLoadingResult.data.itemType,
                        title = decrypt(itemLoadingResult.data.title),
                        username = itemContents.username,
                        password = password,
                        websites = itemContents.websites.toImmutableList(),
                        packageInfoSet = itemContents.packageInfoSet.map(::PackageInfoUi)
                            .toImmutableSet(),
                        note = decrypt(itemLoadingResult.data.note),
                        totpUiState = totpOption.map {
                            TotpUiState(it.code, it.remainingSeconds, it.totalSeconds)
                        }.value(),
                        state = itemLoadingResult.data.state,
                        isLoading = isLoading.value(),
                        isItemSentToTrash = isItemSentToTrash.value()
                    )
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginDetailUiState.NotInitialised
        )

    fun copyPasswordToClipboard() = viewModelScope.launch(coroutineExceptionHandler) {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemType as? ItemType.Login ?: return@launch
        val text = when (val password = passwordState.value) {
            is PasswordState.Revealed -> password.clearText
            is PasswordState.Concealed -> encryptionContextProvider.withEncryptionContext {
                decrypt(itemType.password)
            }
        }
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(text = text, isSecure = true)
        }
        snackbarDispatcher(PasswordCopiedToClipboard)
    }

    fun copyUsernameToClipboard() = viewModelScope.launch {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemType as? ItemType.Login ?: return@launch
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(itemType.username)
        }
        snackbarDispatcher(UsernameCopiedToClipboard)
    }

    fun copyWebsiteToClipboard(website: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(website)
        }
        snackbarDispatcher(WebsiteCopiedToClipboard)
    }

    fun copyTotpCodeToClipboard(code: String) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            clipboardManager.copyToClipboard(code)
        }
        snackbarDispatcher(TotpCopiedToClipboard)
    }

    fun togglePassword() = viewModelScope.launch(coroutineExceptionHandler) {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemType as? ItemType.Login ?: return@launch

        when (passwordState.value) {
            is PasswordState.Concealed ->
                encryptionContextProvider.withEncryptionContext {
                    passwordState.value = PasswordState.Revealed(
                        encrypted = itemType.password,
                        clearText = decrypt(itemType.password)
                    )
                }
            is PasswordState.Revealed ->
                passwordState.value = PasswordState.Concealed(itemType.password)
        }
    }

    fun onDelete(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        trashItem(shareId = shareId, itemId = itemId)
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onError {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun getInitialPasswordState(): PasswordState =
        encryptionContextProvider.withEncryptionContext {
            PasswordState.Concealed(encrypt(""))
        }

    companion object {
        private const val TAG = "LoginDetailViewModel"
    }
}
