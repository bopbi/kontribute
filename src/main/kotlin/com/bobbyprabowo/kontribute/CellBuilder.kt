package com.bobbyprabowo.kontribute

import com.ContributionQuery
import com.ReviewQuery
import com.apollographql.apollo.api.Response
import com.bobbyprabowo.kontribute.filter.ActorFilter
import com.bobbyprabowo.kontribute.filter.UrlFilter
import com.bobbyprabowo.kontribute.filter.UrlForDisplayFilter
import com.bobbyprabowo.kontribute.model.Contribution
import com.bobbyprabowo.kontribute.model.Issue
import com.bobbyprabowo.kontribute.model.IssueWeight
import com.bobbyprabowo.kontribute.model.Review
import com.linkedin.urls.Url
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions

class CellBuilder {

    companion object {

        fun buildReviewData(result: Response<ReviewQuery.Data>): List<Review> {
            val reviewQuery = result.operation as ReviewQuery
            println("Process Response for ${reviewQuery.query}")
            val reviewList = mutableListOf<Review>()
            result.data?.search?.nodes?.forEach { node ->
                node?.asPullRequest?.let { pullRequest ->
                    reviewList.add(Review(pullRequest.url as String, pullRequest.title))
                }
            }
            return reviewList
        }

        fun buildContributionData(repoName: String, issueMap: Map<String, IssueWeight>, result: Response<ContributionQuery.Data>): List<Contribution> {
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
                    val urlList = filterUrl(repoName, bodyFound).toMutableList()
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
                                val urls = commitFound.filter { url ->
                                    url.fullUrl.contains("https://github.com/quipper/${repoName}/issues/", true)
                                }.map { url ->
                                    url.fullUrl
                                        .replace(oldValue = "lt;", newValue = "", ignoreCase = true)
                                }.distinct()
                                urlList.addAll(urls)
                            }
                        }
                    }


                    val urlForDisplay = urlForDisplayFilter.execute(urlList)
                    actorList.addAll(actorFilter.execute(urlList))
                    val actorsForDisplay = actorList.distinct()
                    val issues = urlForDisplay.map {url ->
                        getIssue(issueMap, url)
                    }
                    val contribution = Contribution(
                        issues = issues,
                        title = pullRequest.title,
                        body = pullRequest.body,
                        commitMessages = commitMessages,
                        actors = actorsForDisplay,
                        urlIssues = urlForDisplay
                    )
                    sprintContribution.add(contribution)
                }
            }
            return sprintContribution
        }

        private fun filterUrl(repoName: String, urlList: List<Url>): List<String> {
            return urlList.map { url ->
                url.fullUrl
            }.filter { url ->
                url.contains("https://github.com/quipper/${repoName}/issues/", ignoreCase = true)
            }
        }

        private fun getIssue(issueMap: Map<String, IssueWeight>, url: String): Issue {
            val issueWeight = issueMap[url]
            return if (issueWeight == null) {
                Issue(url = url, title = "N/A on the Mapper", weight = 0)
            } else {
                Issue(url = url, title = issueWeight.title, weight = issueWeight.weight)
            }
        }

    }

}