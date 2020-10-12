package com.bobbyprabowo.kontribute

import com.IssuesQuery
import com.apollographql.apollo.api.Response
import com.bobbyprabowo.kontribute.model.IssueWeight

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
                issue.title.contains("[SP=1]", true) -> {
                    1
                }
                issue.title.contains("[SP=2]", true) -> {
                    2
                }
                issue.title.contains("[SP=3]", true) -> {
                    3
                }
                issue.title.contains("[SP=5]", true) -> {
                    5
                }
                issue.title.contains("[SP=8]", true) -> {
                    8
                }
                issue.title.contains("[SP=13]", true) -> {
                    13
                }
                else -> {
                    0
                }
            }
        }
    }
}