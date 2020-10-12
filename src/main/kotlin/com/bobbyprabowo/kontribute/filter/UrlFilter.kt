package com.bobbyprabowo.kontribute.filter

interface UrlFilter {

    fun execute(urlList: List<String>): List<String>
}