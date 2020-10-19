package com.bobbyprabowo.kontribute

import com.ContributionQuery
import com.IssuesQuery
import com.ReviewQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx3.rxQuery
import com.bobbyprabowo.kontribute.domain.IssueMapper
import com.bobbyprabowo.kontribute.domain.PairFinder
import com.bobbyprabowo.kontribute.model.IssueWeight
import com.bobbyprabowo.kontribute.model.Settings
import com.charleskorn.kaml.Yaml
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.internal.io.FileSystem
import okio.buffer
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

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

            if (noParam) {
                kontribute.getResultContribution(currentDir, settings, apolloClient)
            } else {
                if (runReviewer) { // run only r
                    kontribute.getReviewContribution(currentDir, settings, apolloClient)
                }
                if (runContribute) { // run only k
                    kontribute.getResultContribution(currentDir, settings, apolloClient)
                }
            }
        }
    }

    fun getResultContribution(currentDir: Path, settings: Settings, apolloClient: ApolloClient) {

        Observable.fromIterable(settings.repoList)
            .flatMap { repository ->
                val issueQueries = settings.sprintList.map { sprint ->
                    val dateRange = sprint.duration
                    "repo:${repository.name} is:issue created:$dateRange"
                }.map { query ->
                    Observable.defer {
                        apolloClient.rxQuery(IssuesQuery(query = query))
                    }
                }

                Observable.concat(issueQueries)
                    .map { result ->
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
                    .toObservable()
                    .flatMap { issueMap ->

                        Observable
                            .fromIterable(settings.userList)
                            .flatMap { user ->
                                val repoName = repository.name.split("/").last()
                                val fileName = "$currentDir${File.separator}result_${repoName}_${user.username}.xlsx"
                                val pullRequestQueries = settings.sprintList.map { sprint ->
                                    val dateRange = sprint.duration
                                    "repo:${repository.name} is:pr is:merged merged:$dateRange author:${user.username}"
                                }.map { query ->
                                    Observable.defer {
                                        apolloClient.rxQuery(ContributionQuery(query = query))
                                    }
                                }

                                Observable
                                    .concat(pullRequestQueries)
                                    .map { result ->
                                        CellBuilder.buildContributionData(repoName, issueMap, result)
                                    }
                                    .toList()
                                    .doOnSuccess { contributions ->
                                        CellWriter.writeContributionCells(fileName, settings, contributions)
                                    }
                                    .toObservable()
                            }
                    }
            }
            .toList()
            .doOnSuccess { wholeContribution ->
                val pairedContribution = PairFinder.execute(wholeContribution)
                val fileName = "$currentDir${File.separator}pair_session.xlsx"
                CellWriter.writePairedContributionSheet(fileName, settings, pairedContribution)
            }
            .subscribe({
                println("all is complete")
            }, {error ->
                println("oops error" + error.message)
            })
    }

    fun getReviewContribution(currentDir: Path, settings: Settings, apolloClient: ApolloClient) {


        Observable
            .fromIterable(settings.userList)
            .flatMap { user ->
                Observable
                    .fromIterable(settings.repoList)
                    .flatMapSingle { repository ->
                        val repoName = repository.name.split("/").last()
                        val fileName = "$currentDir${File.separator}review_${repoName}_${user.username}.xlsx"
                        val pullRequestQueries = settings
                            .sprintList
                            .map { sprint ->
                                val dateRange = sprint.duration
                                val query =
                                    "repo:${repository.name} is:pr is:closed merged:$dateRange reviewed-by:${user.username} -author:${user.username}"
                                Observable.defer {
                                    apolloClient.rxQuery(ReviewQuery(query = query))
                                }.delay(5, TimeUnit.SECONDS)
                            }

                        Observable.concat(pullRequestQueries)
                            .map { result ->
                                CellBuilder.buildReviewData(result)
                            }
                            .toList()
                            .map { sheets ->
                                CellWriter.writeReviewCells(fileName, settings, sheets)
                            }
                    }
            }
            .subscribe()
    }
}