package me.proton.pass.data.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.dataPacket
import me.proton.core.crypto.common.pgp.keyPacket
import me.proton.core.key.domain.decryptSessionKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.data.extensions.serializeToProto
import me.proton.pass.data.requests.CreateItemRequest
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.domain.key.publicKey
import me.proton.pass.domain.key.usePrivateKey
import javax.inject.Inject

class CreateItem @Inject constructor(
    private val cryptoContext: CryptoContext
) : BaseCryptoOperation(cryptoContext) {

    fun create(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        userAddress: UserAddress,
        itemContents: ItemContents,
        packageName: PackageName? = null
    ): CreateItemRequest {
        val serializedItem = itemContents.serializeToProto(packageName).toByteArray()
        val vaultKeyPublicKey = vaultKey.publicKey(cryptoContext)

        val (encryptedContents, vaultKeyPacket) = encryptContent(serializedItem, vaultKeyPublicKey)
        val sessionKey = vaultKey.usePrivateKey(cryptoContext) {
            decryptSessionKey(vaultKeyPacket)
        }

        val userSignature = userAddress.useKeys(cryptoContext) { signData(serializedItem) }
        val (vaultKeyPacketSignature, itemKeySignature) = itemKey.usePrivateKey(cryptoContext) {
            Pair(signData(vaultKeyPacket), signData(serializedItem))
        }

        val encryptedUserSignature =
            cryptoContext.pgpCrypto.encryptData(unarmor(userSignature), sessionKey)
        val encryptedItemSignature =
            cryptoContext.pgpCrypto.encryptData(unarmor(itemKeySignature), sessionKey)

        return CreateItemRequest(
            rotationId = vaultKey.rotationId,
            labels = emptyList(),
            vaultKeyPacket = b64(vaultKeyPacket),
            vaultKeyPacketSignature = b64(unarmor(vaultKeyPacketSignature)),
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            userSignature = b64(encryptedUserSignature),
            itemKeySignature = b64(encryptedItemSignature),
            content = b64(encryptedContents)
        )
    }

    private fun encryptContent(
        serializedItem: ByteArray,
        vaultPublicKey: PublicKey
    ): Pair<Unarmored, Unarmored> {
        val encrypted = cryptoContext.pgpCrypto.encryptData(serializedItem, vaultPublicKey.key)
        val packets = cryptoContext.pgpCrypto.getEncryptedPackets(encrypted)
        return Pair(packets.dataPacket(), packets.keyPacket())
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}
