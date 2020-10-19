package com.bobbyprabowo.kontribute.model

data class Contribution(
    val issues: List<Issue>,
    val title: String,
    val body: String,
    val commitMessages: List<String>,
    val actors: List<String>,
    val urlIssues: List<String>
)