package com.hoabh.chanko.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.regex.Pattern

/**
 * Architecture Guard Test:
 * Ngăn tái xuất hiện Color(0x...) trong toàn bộ presentation/ và ui/ (ngoại trừ Color.kt).
 */
class ColorArchitectureTest {

    private val hexColorPattern = Pattern.compile("""\bColor\s*\(\s*0[xX]""")

    @Test
    fun no_direct_hex_color_in_presentation_and_ui_outside_color_kt() {
        val rootDir = if (File("src/main/java/com/hoabh/chanko").exists()) {
            File("src/main/java/com/hoabh/chanko")
        } else {
            File("app/src/main/java/com/hoabh/chanko")
        }

        val presentationDir = File(rootDir, "presentation")
        val uiDir = File(rootDir, "ui")

        // Fail-fast checks: Báo lỗi ngay nếu không tìm thấy thư mục nguồn
        assertTrue(
            "Presentation directory must exist: ${presentationDir.absolutePath}",
            presentationDir.exists()
        )
        assertTrue(
            "UI directory must exist: ${uiDir.absolutePath}",
            uiDir.exists()
        )

        val scannedFiles = mutableListOf<File>()
        val violations = mutableListOf<String>()

        listOf(presentationDir, uiDir).forEach { targetDir ->
            targetDir.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    scannedFiles.add(file)
                    if (file.name == "Color.kt") return@forEach

                    val cleanedCode = stripCommentsAndStrings(file.readText())
                    val matcher = hexColorPattern.matcher(cleanedCode)
                    if (matcher.find()) {
                        violations.add("${file.relativeTo(rootDir).path}: ${matcher.group()}")
                    }
                }
        }

        // Bắt buộc quét được file thực tế, tránh false-green
        assertTrue(
            "Must scan at least 20 Kotlin files in presentation and ui, found ${scannedFiles.size}",
            scannedFiles.size >= 20
        )
        assertEquals(
            "Found Color(0x...) violations outside Color.kt. Please use CandyColors tokens instead:\n" +
                violations.joinToString("\n"),
            emptyList<String>(),
            violations
        )
    }

    /**
     * Bóc tách/bỏ qua comment (//, /* ... */) và chuỗi string literal ("""...""", "...").
     */
    private fun stripCommentsAndStrings(source: String): String {
        val sb = StringBuilder()
        var i = 0
        val n = source.length
        while (i < n) {
            if (i + 1 < n && source[i] == '/' && source[i + 1] == '/') {
                // Line comment
                i += 2
                while (i < n && source[i] != '\n') i++
            } else if (i + 1 < n && source[i] == '/' && source[i + 1] == '*') {
                // Block comment
                i += 2
                while (i + 1 < n && !(source[i] == '*' && source[i + 1] == '/')) i++
                i += 2
            } else if (i + 2 < n && source[i] == '"' && source[i + 1] == '"' && source[i + 2] == '"') {
                // Triple-quoted string
                i += 3
                while (i + 2 < n && !(source[i] == '"' && source[i + 1] == '"' && source[i + 2] == '"')) i++
                i += 3
            } else if (source[i] == '"') {
                // Regular string
                i++
                while (i < n && source[i] != '"') {
                    if (source[i] == '\\' && i + 1 < n) i++ // skip escaped char
                    i++
                }
                i++
            } else {
                sb.append(source[i])
                i++
            }
        }
        return sb.toString()
    }
}
