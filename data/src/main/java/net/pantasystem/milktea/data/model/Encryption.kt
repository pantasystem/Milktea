package net.pantasystem.milktea.data.model

interface Encryption {
    fun encrypt(alias: String, plainText: String) : String
    fun decrypt(alias: String, encryptedText: String) : String?
}