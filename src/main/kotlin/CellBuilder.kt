import com.ContributionQuery
import com.apollographql.apollo.api.Response
import com.linkedin.urls.Url
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions

class CellBuilder {

    companion object {
        fun buildSheetsData(issueMap: Map<String, IssueWeight>, result: Response<ContributionQuery.Data>): List<List<Any>> {
            val contributionQuery = result.operation as ContributionQuery
            println("Process Response for ${contributionQuery.query}")
            val rows = mutableListOf<List<Any>>()
            rows.add(listOf(""))
            val sprintUrlIssues = mutableListOf<String>()
            val sprintActors = mutableListOf<String>()
            result.data?.search?.nodes?.forEachIndexed { number, node ->
                node?.asPullRequest?.let { pullRequest ->
                    rows.add(listOf(number + 1, "Title", pullRequest.title))
                    rows.add(listOf("", "Body", pullRequest.body))
                    val actorList = mutableListOf<String>()
                    val bodyParser = UrlDetector(pullRequest.bodyHTML as String, UrlDetectorOptions.HTML)
                    val bodyFound = bodyParser.detect()
                    val urlList = filterUrl(bodyFound).toMutableList()
                    pullRequest.commits.nodes?.let { commits ->
                        commits.forEach { node ->
                            node?.commit?.let { commit ->
                                rows.add(
                                    listOf(
                                        "",
                                        "Commit",
                                        commit.message
                                    )
                                )
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
                    val urlForDisplay = filterUrlForDisplay(urlList)
                    rows.add(listOf("", "url", urlForDisplay.toString()))
                    sprintUrlIssues.addAll(urlForDisplay)
                    actorList.addAll(filterActor(urlList))
                    val actorsForDisplay = actorList.distinct()
                    rows.add(listOf("", "Actor", actorsForDisplay.toString()))
                    sprintActors.addAll(actorsForDisplay)
                    val issueWeight = getWeight(issueMap, urlForDisplay)
                    rows.add(listOf("", "Weight", issueWeight))
                }
            }
            rows.add(listOf(""))
            rows.add(listOf("", "Issue Handled"))
            sprintUrlIssues.distinct().forEach {url ->
                rows.add(listOf("", url))
            }
            rows.add(listOf(""))
            rows.add(listOf("", "Actors"))
            sprintActors.distinct().forEach { actor ->
                rows.add(listOf("", actor))
            }
            return rows
        }

        private fun filterUrl(urlList: List<Url>): List<String> {
            return urlList.map { url ->
                url.fullUrl
            }.filter { url ->
                !url.contains("https://user-images.githubusercontent.com/", ignoreCase = true)
            }
        }

        private fun filterUrlForDisplay(urlList: List<String>): List<String> {
            return urlList.distinct().filter { url ->
                !url.contains("@quipper.com", true)
            }
        }

        private fun filterActor(urlList: List<String>): List<String> {
            return urlList.filter { url ->
                url.contains("@quipper.com", true)
            }.map { url ->
                url.replace("@quipper.com", "", true)
                    .replace("http://href=mailto:", "", true)
                    .replace("http://", "", true)
                    .replace("/", "", true)
            }
        }

        private fun getWeight(issueMap: Map<String, IssueWeight>, urlList: List<String>): Int {
            return urlList.sumBy {url ->
                issueMap[url]?.weight ?: 0
            }
        }

    }

}