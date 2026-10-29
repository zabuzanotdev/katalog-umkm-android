package com.zaacoo.katalog.data

import okhttp3.Interceptor
import okhttp3.Response
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * InfinityFree (dan beberapa shared hosting lain) memasang tantangan cookie
 * berbasis JavaScript sebelum halaman PHP boleh diakses. Aplikasi native tidak
 * punya mesin JS, jadi tantangan yang sama diselesaikan di sini:
 *
 *   cookie __test = AES-128-CBC-decrypt(c, key=a, iv=b)  -> hex
 *
 * Cookie disimpan supaya request berikutnya tidak perlu mengulang tantangan.
 */
class ChallengeInterceptor(private val prefs: Prefs) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val first = original.newBuilder().apply {
            prefs.challengeCookie?.let { header("Cookie", "__test=$it") }
        }.build()

        val response = chain.proceed(first)

        if (!isChallenge(response)) {
            return response
        }

        val challengeHtml = response.peekBody(MAX_CHALLENGE_BYTES).string()
        response.close()

        val cookie = ChallengeSolver.solve(challengeHtml) ?: return chain.proceed(first)
        prefs.challengeCookie = cookie

        val retry = original.newBuilder()
            .header("Cookie", "__test=$cookie")
            .build()

        return chain.proceed(retry)
    }

    private fun isChallenge(response: Response): Boolean {
        if (!response.isSuccessful) return false
        val contentType = response.header("Content-Type").orEmpty()
        if (!contentType.contains("text/html", ignoreCase = true)) return false
        val body = response.peekBody(4096).string()
        return body.contains("aes.js") && body.contains("slowAES")
    }

    private companion object {
        const val MAX_CHALLENGE_BYTES = 64L * 1024L
    }
}

object ChallengeSolver {

    private val keyRegex = Regex("""a=toNumbers\("([0-9a-fA-F]+)"\)""")
    private val ivRegex = Regex("""b=toNumbers\("([0-9a-fA-F]+)"\)""")
    private val cipherRegex = Regex("""c=toNumbers\("([0-9a-fA-F]+)"\)""")

    fun solve(html: String): String? {
        val keyHex = keyRegex.find(html)?.groupValues?.getOrNull(1) ?: return null
        val ivHex = ivRegex.find(html)?.groupValues?.getOrNull(1) ?: return null
        val cipherHex = cipherRegex.find(html)?.groupValues?.getOrNull(1) ?: return null

        return runCatching {
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(hexToBytes(keyHex), "AES"),
                IvParameterSpec(hexToBytes(ivHex)),
            )
            val plain = cipher.doFinal(hexToBytes(cipherHex))
            plain.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }
        }.getOrNull()
    }

    private fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex tidak valid" }
        return ByteArray(hex.length / 2) { index ->
            hex.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
