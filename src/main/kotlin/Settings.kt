@kotlinx.serialization.Serializable
data class Settings(
    val githubAccessToken: String,
    val userToCheck: String,
    val repoToCheck: String,
    val sprintList: List<Sprint>
)

@kotlinx.serialization.Serializable
data class Sprint(val title: String, val duration: String)