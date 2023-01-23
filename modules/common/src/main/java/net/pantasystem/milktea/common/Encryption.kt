package net.pantasystem.milktea.common

interface Encryption {
    fun encrypt(alias: String, plainText: String) : String
    fun decrypt(alias: String, encryptedText: String) : String?
}