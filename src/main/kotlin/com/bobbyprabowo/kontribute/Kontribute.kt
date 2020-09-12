package com.bobbyprabowo.kontribute

import com.ContributionQuery
import com.IssuesQuery
import com.ReviewQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx3.rxQuery
import com.charleskorn.kaml.Yaml
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.internal.io.FileSystem
import okio.buffer
import java.io.File
import java.nio.file.Paths

class Kontribute {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val runReviewer = args.any { paramString ->
                paramString == "r"
            }

            val runContribute = args.any { paramString ->
                paramString == "k"
            }

            val noParam = !runContribute && !runReviewer

            val kontribute = Kontribute()
            if (noParam) {
                kontribute.execute()
            } else {
                if (runReviewer) { // run only r
                    kontribute.getReviewContribution()
                }
                if (runContribute) { // run only k
                    kontribute.execute()
                }
            }
        }
    }

    fun execute() {
        val currentDir = Paths.get("").toAbsolutePath()
        val settingsFile = File("$currentDir${File.separator}settings.yaml")

        val source = FileSystem.SYSTEM.source(settingsFile).buffer()
        val settingsString = source.readUtf8()

        val settings = Yaml.default.decodeFromString(Settings.serializer(), settingsString)

        val apolloClient = ApolloClient.builder()
            .serverUrl("https://api.github.com/graphql")
            .okHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor(AuthorizationInterceptor(settings.githubAccessToken))
                    .build()
            )
            .build()

        val repoName = settings.repoToCheck.split("/").last()
        val FILE_NAME = "$currentDir${File.separator}result_${repoName}_${settings.userToCheck}.xlsx"
        val pullRequestQueries = settings.sprintList.map { sprint ->
            val dateRange = sprint.duration
            "repo:${settings.repoToCheck} is:pr is:merged merged:$dateRange author:${settings.userToCheck}"
        }.map { query ->
            Observable.defer {
                apolloClient.rxQuery(ContributionQuery(query = query))
            }
        }

        val issueQueries = settings.sprintList.map { sprint ->
            val dateRange = sprint.duration
            "repo:${settings.repoToCheck} is:issue created:$dateRange"
        }.map { query ->
            Observable.defer {
                apolloClient.rxQuery(IssuesQuery(query = query))
            }
        }

        Observable
            .concat(issueQueries)
            .map {result ->
                IssueMapper.generateMap(result)
            }
            .toList()
            .map { issueMaps ->
                val combineMaps = mutableMapOf<String, IssueWeight>()
                issueMaps.forEach { issueMap ->
                    combineMaps.putAll(issueMap)
                }
                combineMaps
            }
            .flatMap { issueMap ->
                Observable
                    .concat(pullRequestQueries)
                    .map { result ->
                        CellBuilder.buildContributionData(issueMap, result)
                    }
                    .toList()
            }
            .map { sheets ->
                CellWriter.writeContributionCells(FILE_NAME, settings, sheets)
            }
            .subscribe { _, error ->
                if (error != null) {
                    println(error)
                }
            }
    }

    fun getReviewContribution() {
        val currentDir = Paths.get("").toAbsolutePath()
        val settingsFile = File("$currentDir${File.separator}settings.yaml")

        val source = FileSystem.SYSTEM.source(settingsFile).buffer()
        val settingsString = source.readUtf8()

        val settings = Yaml.default.decodeFromString(Settings.serializer(), settingsString)

        val apolloClient = ApolloClient.builder()
            .serverUrl("https://api.github.com/graphql")
            .okHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor(AuthorizationInterceptor(settings.githubAccessToken))
                    .build()
            )
            .build()

        val repoName = settings.repoToCheck.split("/").last()
        val FILE_NAME = "$currentDir${File.separator}review_${repoName}_${settings.userToCheck}.xlsx"
        val pullRequestQueries = settings.sprintList.map { sprint ->
            val dateRange = sprint.duration
            "repo:${settings.repoToCheck} is:pr is:closed merged:$dateRange reviewed-by:${settings.userToCheck} author:${settings.userToCheck}"
        }.map { query ->
            Observable.defer {
                apolloClient.rxQuery(ReviewQuery(query = query))
            }
        }

        Observable
            .concat(pullRequestQueries)
            .map { result ->
                CellBuilder.buildReviewData(result)
            }
            .toList()
            .map { sheets ->
                CellWriter.writeReviewCells(FILE_NAME, settings, sheets)
            }
            .subscribe { _, error ->
                if (error != null) {
                    println(error)
                }
            }
    }
}