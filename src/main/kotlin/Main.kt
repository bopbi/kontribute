import com.ContributionQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx3.rxQuery
import com.charleskorn.kaml.Yaml
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.internal.io.FileSystem
import okio.buffer
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
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
    val queryList = settings.sprintList.map { sprint ->
        val dateRange = sprint.duration
        "repo:${settings.repoToCheck} is:pr is:merged merged:$dateRange author:${settings.userToCheck}"
    }.map { query ->
        Observable.defer {
            apolloClient.rxQuery(ContributionQuery(query = query))
        }
    }

    Observable.concat(
        queryList
    ).map { result ->
        val contributionQuery = result.operation as ContributionQuery
        println("Process Response for ${contributionQuery.query}")
        val rows = mutableListOf<List<Any>>()
        rows.add(listOf(""))
        result.data?.search?.nodes?.forEachIndexed { number, node ->
            node?.asPullRequest?.let { pullRequest ->
                rows.add(listOf(number + 1, "Title", pullRequest.title))
                rows.add(listOf("", "Body", pullRequest.body))
                val actorList = mutableListOf<String>()
                val urlList = mutableListOf<String>()
                val bodyParser = UrlDetector(pullRequest.bodyHTML as String, UrlDetectorOptions.HTML)
                val bodyFound = bodyParser.detect()
                urlList.addAll(bodyFound.map { url ->
                    url.fullUrl
                }.filter { url ->
                    !url.contains("https://user-images.githubusercontent.com/", ignoreCase = true)
                })
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
                val urlForDisplay = urlList.distinct().filter { url ->
                    !url.contains("@quipper.com", true)
                }
                rows.add(listOf("", "url", urlForDisplay.toString()))
                actorList.addAll(urlList.filter { url ->
                    url.contains("@quipper.com", true)
                }.map { url ->
                    url.replace("@quipper.com", "", true)
                        .replace("http://href=mailto:", "", true)
                        .replace("http://", "", true)
                        .replace("/", "", true)
                })
                rows.add(listOf("", "Actor", actorList.distinct().toString()))
            }
        }
        rows
    }
        .toList()
        .map { sheets ->
            val workbook = XSSFWorkbook()
            val cellStyle = workbook.createCellStyle()
            sheets.forEachIndexed { index, data ->
                println("Writing excel for ${settings.sprintList[index].title}")
                val sheet =
                    workbook.createSheet("${settings.sprintList[index].title} (${settings.sprintList[index].duration})")
                var startMergeRow = 0
                data.forEachIndexed { rowNum, rowData ->
                    val row = sheet.createRow(rowNum)
                    rowData.forEachIndexed { colNum, colData ->
                        val cell: Cell = row.createCell(colNum)
                        if (colData is String) {
                            cellStyle.wrapText = true
                            cell.cellStyle = cellStyle
                            cell.setCellValue(colData)
                        } else if (colData is Number) {
                            cell.setCellValue(colData.toDouble())
                        }

                        if (colData == "Title") {
                            startMergeRow = rowNum
                        }
                        if (colData == "Actor") {
                            sheet.addMergedRegion(CellRangeAddress(startMergeRow, rowNum, 0, 0))
                        }
                    }
                }

                sheet.autoSizeColumn(1)
                sheet.autoSizeColumn(2)
            }

            try {
                val outputStream = FileOutputStream(FILE_NAME)
                workbook.write(outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            println("Done")
        }
        .subscribe()
}