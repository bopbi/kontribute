import com.IssuesQuery
import com.apollographql.apollo.api.Response

class IssueMapper {

    companion object {

        fun generateMap(result: Response<IssuesQuery.Data>): Map<String, IssueWeight> {
            val issuesQuery = result.operation as IssuesQuery
            println("Process Response for ${issuesQuery.query}")
            val resultMap = mutableMapOf<String, IssueWeight>()
            result.data?.search?.nodes?.forEach { node ->
                node?.asIssue?.let { issue ->
                    resultMap[issue.url as String] = IssueWeight(title = issue.title, weight = getWeight(issue))
                }
            }
            return resultMap
        }

        private fun getWeight(issue: IssuesQuery.AsIssue): Int {
            return when {
                issue.title.contains("[SP=1]") -> {
                    1
                }
                issue.title.contains("[SP=2]") -> {
                    2
                }
                issue.title.contains("[SP=3]") -> {
                    3
                }
                issue.title.contains("[SP=5]") -> {
                    5
                }
                issue.title.contains("[SP=8]") -> {
                    8
                }
                else -> {
                    0
                }
            }
        }
    }
}