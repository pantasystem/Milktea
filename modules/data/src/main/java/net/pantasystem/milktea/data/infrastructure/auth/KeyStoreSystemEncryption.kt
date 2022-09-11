@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure.auth

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import net.pantasystem.milktea.common.Encryption
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

class KeyStoreSystemEncryption(private val context: Context) : Encryption {
    companion object{
        const val PROVIDER = "AndroidKeyStore"
        const val ALGORITHM = "RSA"
        val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    }
    override fun encrypt(alias: String, plainText: String): String {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)

        // キーペアがない場合生成
        if (!keyStore.containsAlias(alias)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER)
            keyPairGenerator.initialize(createKeyPairGeneratorSpec(context, alias))
            keyPairGenerator.generateKeyPair()
        }
        val publicKey = keyStore.getCertificate(alias).publicKey
        keyStore.getKey(alias, null)

        // 公開鍵で暗号化
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // SharedPreferencesに保存しやすいようにBase64でString化
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    override fun decrypt(alias: String, encryptedText: String): String? {
        val keyStore = KeyStore.getInstance(PROVIDER)
        keyStore.load(null)
        if (!keyStore.containsAlias(alias)) {
            return null
        }

        val privateKey = keyStore.getKey(alias, null)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val bytes = Base64.decode(encryptedText, Base64.DEFAULT)

        val b = cipher.doFinal(bytes)
        return String(b)
    }

    private fun createKeyPairGeneratorSpec(context: Context, alias: String): KeyPairGeneratorSpec {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 100)

        return KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSubject(X500Principal(String.format("CN=%s", alias)))
            .setSerialNumber(BigInteger.valueOf(114514))
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
    }
}