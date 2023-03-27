package proton.android.pass.telemetry.api

import proton.pass.domain.ItemType

enum class EventItemType(val itemTypeName: String) {
    Login("login"),
    Note("note"),
    Alias("alias"),
    Password("password");

    companion object {
        fun from(itemType: ItemType): EventItemType = when (itemType) {
            ItemType.Password -> Password
            is ItemType.Alias -> Alias
            is ItemType.Note -> Note
            is ItemType.Login -> Login
        }
    }
}

@Suppress("UnnecessaryAbstractClass")
abstract class TelemetryEvent(val eventName: String) {
    open fun dimensions(): Map<String, String> = emptyMap()
}