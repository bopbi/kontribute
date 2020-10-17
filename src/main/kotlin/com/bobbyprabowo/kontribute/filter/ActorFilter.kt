package com.bobbyprabowo.kontribute.filter

class ActorFilter : UrlFilter {

    override fun execute(urlList: List<String>): List<String> {
        return urlList
            .filter { url ->
                url.contains("@")
            }
            .map {url ->
                url.split("@").first()
            }
    }
}