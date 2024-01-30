package utils

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun getFolderName(url: String): String{
    val messageDigest: MessageDigest
    try {
        messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(url.toByteArray())
        val name = BigInteger(1, messageDigest.digest()).toString(36)
        return name
    } catch (e: NoSuchAlgorithmException) {
        return e.localizedMessage
    }
}

fun validGradleHome(gradleDir: String): Boolean{
    return File("$gradleDir\\wrapper").isDirectory()
}