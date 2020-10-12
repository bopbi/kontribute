package com.bobbyprabowo.kontribute.filter

class ActorFilter : UrlFilter {

    override fun execute(urlList: List<String>): List<String> {
        return urlList.filter { url ->
            url.contains("@quipper.com", true) ||
                    url.contains("@gamalinda.com", true)
        }.map { url ->
            url.replace("@quipper.com", "", true)
                .replace("@gamalinda.com", "", true)
                .replace("http://href=mailto:", "", true)
                .replace("http://", "", true)
                .replace("/", "", true)
        }
    }
}