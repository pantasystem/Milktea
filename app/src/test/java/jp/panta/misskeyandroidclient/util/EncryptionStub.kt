package jp.panta.misskeyandroidclient.util

import jp.panta.misskeyandroidclient.model.Encryption

class EncryptionStub : Encryption{
    override fun decrypt(alias: String, encryptedText: String): String {
        return encryptedText
    }

    override fun encrypt(alias: String, plainText: String): String {
        return plainText
    }
}