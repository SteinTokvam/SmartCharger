package no.steintokvam.smartcharger.easee.objects

data class AccessToken(
    val accessToken: String,
    val expiresIn: Int,
    val accessClaims: List<String>,
    val tokenType: String,
    val refreshToken: String
)
