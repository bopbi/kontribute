package com.bobbyprabowo.kontribute

import com.ContributionQuery
import com.ReviewQuery
import com.apollographql.apollo.api.Response
import com.bobbyprabowo.kontribute.filter.ActorFilter
import com.bobbyprabowo.kontribute.filter.UrlFilter
import com.bobbyprabowo.kontribute.filter.UrlForDisplayFilter
import com.bobbyprabowo.kontribute.model.Contribution
import com.bobbyprabowo.kontribute.model.IssueWeight
import com.linkedin.urls.Url
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions

class CellBuilder {

    companion object {

        fun buildReviewData(result: Response<ReviewQuery.Data>): List<List<Any>> {
            val reviewQuery = result.operation as ReviewQuery
            println("Process Response for ${reviewQuery.query}")
            val rows = mutableListOf<List<Any>>()
            rows.add(listOf("no", "url", "title"))
            result.data?.search?.nodes?.forEachIndexed { number, node ->
                node?.asPullRequest?.let { pullRequest ->
                    rows.add(listOf(number + 1, pullRequest.url, pullRequest.title))
                }
            }
            return rows
        }

        fun buildContributionData(issueMap: Map<String, IssueWeight>, result: Response<ContributionQuery.Data>): List<Contribution> {
            val actorFilter: UrlFilter = ActorFilter()
            val urlForDisplayFilter : UrlFilter = UrlForDisplayFilter()

            val contributionQuery = result.operation as ContributionQuery
            println("Process Response for ${contributionQuery.query}")
            val sprintContribution = mutableListOf<Contribution>()
            result.data?.search?.nodes?.forEach { node ->
                node?.asPullRequest?.let { pullRequest ->

                    val actorList = mutableListOf<String>()
                    val bodyParser = UrlDetector(pullRequest.bodyHTML as String, UrlDetectorOptions.HTML)
                    val bodyFound = bodyParser.detect()
                    val urlList = filterUrl(bodyFound).toMutableList()
                    val commitMessages = mutableListOf<String>()
                    pullRequest.commits.nodes?.let { commits ->
                        commits.forEach { node ->
                            node?.commit?.let { commit ->
                                commitMessages.add(commit.message)
                                commit.committer?.name?.let { actor ->
                                    actorList.add(actor)
                                }
                                val commitParser =
                                    UrlDetector(commit.messageBodyHTML as String, UrlDetectorOptions.HTML)
                                val commitFound = commitParser.detect()
                                urlList.addAll(commitFound.map { url ->
                                    url.fullUrl
                                        .replace(oldValue = "lt;", newValue = "", ignoreCase = true)
                                }.filter { url ->
                                    !url.contains(
                                        "https://user-images.githubusercontent.com/",
                                        ignoreCase = true
                                    )
                                })
                            }
                        }
                    }


                    val urlForDisplay = urlForDisplayFilter.execute(urlList)
                    actorList.addAll(actorFilter.execute(urlList))
                    val actorsForDisplay = actorList.distinct()
                    val issueWeight = getWeight(issueMap, urlForDisplay)

                    val contribution = Contribution(
                        title = pullRequest.title,
                        body = pullRequest.body,
                        commitMessages = commitMessages,
                        actors = actorsForDisplay,
                        urlIssues = urlForDisplay,
                        weight = issueWeight
                    )
                    sprintContribution.add(contribution)
                }
            }
            return sprintContribution
        }

        private fun filterUrl(urlList: List<Url>): List<String> {
            return urlList.map { url ->
                url.fullUrl
            }.filter { url ->
                !url.contains("https://user-images.githubusercontent.com/", ignoreCase = true)
            }
        }

        private fun getIssueTitle(issueMap: Map<String, IssueWeight>, url: String): String {
            return issueMap[url]?.title ?: "N/A on the Mapper"
        }

        private fun getWeight(issueMap: Map<String, IssueWeight>, urlList: List<String>): Int {
            return urlList.sumBy {url ->
                issueMap[url]?.weight ?: 0
            }
        }

    }

}