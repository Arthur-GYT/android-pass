package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.GetItemById
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetItemById @Inject constructor() : GetItemById {

    private var result = testFlow<Result<Item>>()
    private val memory: MutableList<Payload> = mutableListOf()

    fun memory(): List<Payload> = memory

    fun emitValue(value: Result<Item>) {
        result.tryEmit(value)
    }

    override fun invoke(shareId: ShareId, itemId: ItemId): Flow<Item> {
        memory.add(Payload(shareId, itemId))
        return result.map { it.getOrThrow() }
    }

    data class Payload(val shareId: ShareId, val itemId: ItemId)
}
