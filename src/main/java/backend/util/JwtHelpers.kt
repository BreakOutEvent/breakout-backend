package backend.util

import backend.controller.exceptions.UnauthorizedException
import com.auth0.jwt.Algorithm
import com.auth0.jwt.JWTSigner
import com.auth0.jwt.JWTVerifier
import java.security.SignatureException

fun getSignedJwtToken(secret: String, subject: String): String {
    val claim = mapOf("subject" to subject)
    val signer = JWTSigner(secret)
    return signer.sign(claim, JWTSigner.Options().setAlgorithm(Algorithm.HS512))
}

fun verifyJwtClaim(secret: String, token: String, id: String) {

    val verifier = JWTVerifier(secret, "audience")

    try {
        val claims = verifier.verify(token)
        val subject = claims["subject"] as String
        if (!subject.equals(id)) throw UnauthorizedException("Invalid JWT token")
    } catch (e: SignatureException) {
        throw UnauthorizedException(e.message ?: "Invalid JWT token")
    } catch (e: IllegalStateException) {
        throw UnauthorizedException(e.message ?: "Invalid JWT token")
    }
}
