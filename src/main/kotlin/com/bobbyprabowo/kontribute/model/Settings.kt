package com.bobbyprabowo.kontribute.model

@kotlinx.serialization.Serializable
data class Settings(
    val githubAccessToken: String,
    val userList: List<User>,
    val repoList: List<Repository>,
    val sprintList: List<Sprint>
)

