package com.bobbyprabowo.kontribute.filter

class UrlForDisplayFilter : UrlFilter {

    override fun execute(urlList: List<String>): List<String> {
        return urlList.distinct().filter {url ->
            url.contains("https://github.com/quipper/", true)
        }
    }

}