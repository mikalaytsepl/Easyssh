package com.example.easyssh.ssh

import android.util.Base64
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.util.OpenSSHPrivateKeyUtil
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import java.io.StringWriter
import java.security.SecureRandom

/**
 * Generowanie kluczy Ed25519 przez Bouncy Castle, z pominięciem
 JCE/EdDSA Androida — które na części urządzeń jest niekompletne i rzuca
 UnsupportedOperationException przy generowaniu/eksporcie klucza.
 */
object Ed25519KeyGen {

    fun generate(comment: String): Pair<String, String> {
        val generator = Ed25519KeyPairGenerator().apply {
            init(Ed25519KeyGenerationParameters(SecureRandom()))
        }
        val pair = generator.generateKeyPair()
        val priv = pair.private as Ed25519PrivateKeyParameters
        val pub = pair.public as Ed25519PublicKeyParameters

        // Klucz publiczny
        val pubBlob = OpenSSHPublicKeyUtil.encodePublicKey(pub)
        val publicLine = "ssh-ed25519 " + Base64.encodeToString(pubBlob, Base64.NO_WRAP) + " " + comment

        // Klucz prywatny
        val privBlob = OpenSSHPrivateKeyUtil.encodePrivateKey(priv)
        val writer = StringWriter()
        PemWriter(writer).use { it.writeObject(PemObject("OPENSSH PRIVATE KEY", privBlob)) }
        val privatePem = writer.toString()

        return privatePem to publicLine
    }
}
