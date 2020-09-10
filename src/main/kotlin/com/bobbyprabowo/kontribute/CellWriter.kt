package com.bobbyprabowo.kontribute

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

        fun writeContributionCells(fileName: String, settings: Settings, sheets: List<List<List<Any>>>) {
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