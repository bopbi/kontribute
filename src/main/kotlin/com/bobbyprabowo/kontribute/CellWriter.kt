package com.bobbyprabowo.kontribute

import com.bobbyprabowo.kontribute.model.Contribution
import com.bobbyprabowo.kontribute.model.Settings
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class CellWriter {

    companion object {
        fun writeReviewCells(fileName: String, settings: Settings, sheets: List<List<List<Any>>>) {
            val workbook = XSSFWorkbook()
            val cellStyle = workbook.createCellStyle()
            sheets.forEachIndexed { index, data ->
                println("Writing excel for ${settings.sprintList[index].title}")
                val sheet =
                    workbook.createSheet("${settings.sprintList[index].title} (${settings.sprintList[index].duration})")
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
                    }
                }

            }

            try {
                val outputStream = FileOutputStream(fileName)
                workbook.write(outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            println("Done")
        }

        private fun formattingContributionList(contributionList: List<List<Contribution>>) : List<List<List<Any>>> {
            val sheets = mutableListOf<List<List<Any>>>()
            contributionList.forEach { sprintContribution ->
                val rows = mutableListOf<List<Any>>()
                rows.add(
                    listOf("")
                )
                sprintContribution.forEachIndexed { rowNum, contribution ->

                    // write contribution list
                    rows.add(
                        listOf(rowNum + 1, "Title", contribution.title)
                    )
                    rows.add(
                        listOf("", "Body", contribution.body)
                    )
                    contribution.commitMessages.forEach { commitMessage ->
                        rows.add(
                            listOf("", "Commit", commitMessage)
                        )
                    }
                    rows.add(
                        listOf("", "Url", contribution.urlIssues)
                    )
                    rows.add(
                        listOf("", "Weight", contribution.weight)
                    )
                }
                rows.add(
                    listOf("")
                )

                rows.add(
                    listOf("","Url", "Title", "Weight")
                )

                sprintContribution.forEach { contribution ->
                    // write contribution list
                    rows.add(
                        listOf("", contribution.urlIssues, contribution.title, contribution.weight)
                    )
                }

                rows.add(
                    listOf("")
                )

                rows.add(
                    listOf("", "Actor")
                )

                val actors = sprintContribution.flatMap { contribution ->
                    contribution.actors
                }
                actors.forEach { username ->
                    rows.add(
                        listOf("", username)
                    )
                }

                sheets.add(rows)
            }
            return sheets
        }

        private fun formattingPairContributionList(contributionList: List<Contribution>) : List<List<List<Any>>> {
            val sheets = mutableListOf<List<List<Any>>>()
            val rows = mutableListOf<List<Any>>()
                rows.add(
                    listOf("")
                )
                contributionList.forEachIndexed { rowNum, contribution ->

                    // write contribution list
                    rows.add(
                        listOf(rowNum + 1, "Title", contribution.title)
                    )
                    rows.add(
                        listOf("", "Body", contribution.body)
                    )
                    contribution.commitMessages.forEach { commitMessage ->
                        rows.add(
                            listOf("", "Commit", commitMessage)
                        )
                    }
                    rows.add(
                        listOf("", "Url", contribution.urlIssues)
                    )
                    rows.add(
                        listOf("", "Weight", contribution.weight)
                    )
                }
                rows.add(
                    listOf("")
                )

                rows.add(
                    listOf("","Url", "Title", "Weight")
                )



            sheets.add(rows)
            return sheets
        }

        fun writeContributionCells(fileName: String, settings: Settings, contributionList: List<List<Contribution>>) {

            val sheets = formattingContributionList(contributionList)

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
                        if (colData == "Weight") {
                            sheet.addMergedRegion(CellRangeAddress(startMergeRow, rowNum, 0, 0))
                        }
                    }
                }

                sheet.autoSizeColumn(1)
                sheet.autoSizeColumn(2)
            }

            try {
                val outputStream = FileOutputStream(fileName)
                workbook.write(outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            println("Done")
        }

        fun writePairedContributionSheet(fileName: String, settings: Settings, contributionList: List<Contribution>) {

            val sheets = formattingPairContributionList(contributionList)

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
                        if (colData == "Weight") {
                            sheet.addMergedRegion(CellRangeAddress(startMergeRow, rowNum, 0, 0))
                        }
                    }
                }

                sheet.autoSizeColumn(1)
                sheet.autoSizeColumn(2)
            }

            try {
                val outputStream = FileOutputStream(fileName)
                workbook.write(outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            println("Done")
        }
    }

}