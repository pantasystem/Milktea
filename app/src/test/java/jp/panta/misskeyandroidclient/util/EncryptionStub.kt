package jp.panta.misskeyandroidclient.util

import net.pantasystem.milktea.common.Encryption

class EncryptionStub : Encryption {
    override fun decrypt(alias: String, encryptedText: String): String {
        return encryptedText
    }

    override fun encrypt(alias: String, plainText: String): String {
        return plainText
    }
}