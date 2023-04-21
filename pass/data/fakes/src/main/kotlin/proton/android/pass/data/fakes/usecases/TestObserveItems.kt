package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.common.api.None
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.extensions.serializeToProto
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveItems @Inject constructor() : ObserveItems {

    private val flow = testFlow<List<Item>>()

    fun emitValue(value: List<Item>) {
        flow.tryEmit(value)
    }

    fun emitDefault() {
        flow.tryEmit(defaultValues.asList())
    }

    override fun invoke(
        userId: UserId?,
        selection: ShareSelection,
        itemState: ItemState,
        filter: ItemTypeFilter
    ): Flow<List<Item>> = flow

    data class DefaultValues(
        val login: Item,
        val alias: Item,
        val note: Item
    ) {
        fun asList(): List<Item> = listOf(login, alias, note)
    }

    companion object {

        val defaultValues = DefaultValues(
            createLogin(itemId = ItemId("login")),
            createAlias(itemId = ItemId("alias")),
            createNote(itemId = ItemId("note"))
        )

        fun createItem(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            aliasEmail: String? = null,
            itemContents: ItemContents
        ): Item {
            val now = Clock.System.now()
            val asProto = itemContents.serializeToProto("123")
            return TestEncryptionContextProvider().withEncryptionContext {
                Item(
                    id = itemId,
                    itemUuid = "",
                    revision = 1,
                    shareId = shareId,
                    itemType = ItemType.Companion.fromParsed(this, asProto, aliasEmail),
                    title = encrypt(itemContents.title),
                    note = encrypt(itemContents.note),
                    content = encrypt(asProto.toByteArray()),
                    packageInfoSet = emptySet(),
                    state = 0,
                    modificationTime = now,
                    createTime = now,
                    lastAutofillTime = None
                )
            }
        }

        fun createLogin(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "login-item",
            username: String = "username",
            note: String = "note"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.Login(
                title = title,
                note = note,
                username = username,
                password = "",
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = "",
                extraTotpSet = emptySet()
            )
        )

        fun createAlias(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "alias-item",
            alias: String = "some.alias@domain.test",
            note: String = "note"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            aliasEmail = alias,
            itemContents = ItemContents.Alias(
                title = title,
                note = note,
            )
        )

        fun createNote(
            shareId: ShareId = ShareId("share-123"),
            itemId: ItemId = ItemId("item-123"),
            title: String = "note-item",
            note: String = "note"
        ) = createItem(
            shareId = shareId,
            itemId = itemId,
            itemContents = ItemContents.Note(
                title = title,
                note = note,
            )
        )
    }

}

// Copy of the snippet from pass/data/impl/src/main/kotlin/proton/android/pass/data/impl/extensions/ItemMapper.kt
// We should move it to a common place
@Suppress("TooGenericExceptionThrown")
fun ItemType.Companion.fromParsed(
    context: EncryptionContext,
    parsed: ItemV1.Item,
    aliasEmail: String? = null
): ItemType {
    return when (parsed.content.contentCase) {
        ItemV1.Content.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.content.login.username,
            password = context.encrypt(parsed.content.login.password),
            websites = parsed.content.login.urlsList,
            packageInfoSet = parsed.platformSpecific.android.allowedAppsList.map {
                PackageInfo(PackageName(it.packageName), AppName(it.appName))
            }.toSet(),
            primaryTotp = context.encrypt(parsed.content.login.totpUri)
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> {
            requireNotNull(aliasEmail)
            ItemType.Alias(aliasEmail = aliasEmail)
        }
        else -> throw Exception("Unknown ItemType")
    }
}