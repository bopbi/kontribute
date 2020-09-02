import com.ContributionQuery
import com.IssuesQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx3.rxQuery
import com.charleskorn.kaml.Yaml
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.internal.io.FileSystem
import okio.buffer
import java.io.File
import java.nio.file.Paths


fun main() {

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

    val FILE_NAME = "$currentDir${File.separator}result_${settings.userToCheck}.xlsx"
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
        .flatMap { issueMap ->
            Observable
                .concat(pullRequestQueries)
                .map { result ->
                    CellBuilder.buildSheetsData(issueMap, result)
                }
                .toList()
                .toObservable()
        }
        .firstOrError()
        .map { sheets ->
            CellWriter.writeCells(FILE_NAME, settings, sheets)
        }
        .subscribe { _, error ->
            if (error != null) {
                println(error)
            }
        }
}