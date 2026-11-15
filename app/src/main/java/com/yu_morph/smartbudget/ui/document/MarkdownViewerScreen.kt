package com.yu_morph.smartbudget.ui.document

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yu_morph.smartbudget.R
import com.yu_morph.smartbudget.ui.main.mainScreenBackgroundBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val LightTableHeaderBackground = Color(0xFFE5E5E5)
private val DarkTableHeaderBackground = Color(0xFF363636)

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock

    data class Paragraph(val text: String) : MarkdownBlock

    data class Bullet(val text: String) : MarkdownBlock

    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock

    data object Divider : MarkdownBlock
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MarkdownViewerScreen(document: MarkdownDocument, onBack: () -> Unit) {
    val context = LocalContext.current
    var content by remember(document) { mutableStateOf<String?>(null) }
    var loadFailed by remember(document) { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    LaunchedEffect(document) {
        runCatching {
            withContext(Dispatchers.IO) {
                context.assets.open(document.assetName).bufferedReader().use { it.readText() }
            }
        }.onSuccess { content = it }
            .onFailure { loadFailed = true }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(document.titleResource),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(mainScreenBackgroundBrush()),
        ) {
            when {
                loadFailed -> Text(
                    text = stringResource(R.string.markdown_document_load_error),
                    modifier = Modifier.padding(24.dp),
                )

                content != null -> MarkdownContent(markdown = content.orEmpty())
            }
        }
    }
}

@Composable
private fun MarkdownContent(markdown: String, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        parseMarkdown(markdown).forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> MarkdownHeading(block)

                is MarkdownBlock.Paragraph -> Text(
                    text = cleanInlineMarkdown(block.text),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                )

                is MarkdownBlock.Bullet -> Row(modifier = Modifier.fillMaxWidth()) {
                    Text("•", modifier = Modifier.padding(end = 10.dp), fontWeight = FontWeight.Bold)
                    Text(cleanInlineMarkdown(block.text), fontSize = 15.sp, lineHeight = 23.sp)
                }

                is MarkdownBlock.Table -> MarkdownTable(block)

                MarkdownBlock.Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun MarkdownTable(table: MarkdownBlock.Table) {
    Column(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        MarkdownTableRow(cells = table.headers, isHeader = true)
        table.rows.forEach { row ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MarkdownTableRow(cells = row, isHeader = false)
        }
    }
}

@Composable
private fun MarkdownTableRow(cells: List<String>, isHeader: Boolean) {
    val widths = listOf(240.dp, 90.dp, 160.dp, 220.dp)
    val headerBackground =
        if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
            DarkTableHeaderBackground
        } else {
            LightTableHeaderBackground
        }
    Row(
        modifier =
            Modifier.background(
                if (isHeader) headerBackground else Color.Transparent,
            ),
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cleanInlineMarkdown(cell),
                modifier = Modifier.width(widths.getOrElse(index) { 180.dp }).padding(12.dp),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun MarkdownHeading(block: MarkdownBlock.Heading) {
    val fontSize = when (block.level) {
        1 -> 26.sp
        2 -> 21.sp
        else -> 17.sp
    }
    Text(
        text = cleanInlineMarkdown(block.text),
        modifier = Modifier.padding(top = if (block.level == 1) 0.dp else 12.dp),
        fontSize = fontSize,
        lineHeight = fontSize * 1.3,
        fontWeight = FontWeight.Bold,
        fontStyle = if (block.level > 3) FontStyle.Italic else FontStyle.Normal,
    )
}

@Suppress("CyclomaticComplexMethod")
private fun parseMarkdown(markdown: String): List<MarkdownBlock> = buildList {
    val paragraph = mutableListOf<String>()
    val lines = markdown.lines()
    var index = 0

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            add(MarkdownBlock.Paragraph(paragraph.joinToString(" ")))
            paragraph.clear()
        }
    }

    while (index < lines.size) {
        val line = lines[index].trim()
        when {
            line.isEmpty() -> flushParagraph()

            line.startsWith("|") && index + 1 < lines.size && isTableSeparator(lines[index + 1]) -> {
                flushParagraph()
                val headers = parseTableCells(line)
                val rows = mutableListOf<List<String>>()
                index += 2
                while (index < lines.size && lines[index].trim().startsWith("|")) {
                    rows += parseTableCells(lines[index])
                    index++
                }
                add(MarkdownBlock.Table(headers, rows))
                continue
            }

            line == "---" || line == "***" -> {
                flushParagraph()
                add(MarkdownBlock.Divider)
            }

            line.startsWith("#") -> {
                flushParagraph()
                val level = line.takeWhile { it == '#' }.length.coerceAtMost(6)
                add(MarkdownBlock.Heading(level, line.drop(level).trim()))
            }

            line.startsWith("- ") || line.startsWith("* ") -> {
                flushParagraph()
                add(MarkdownBlock.Bullet(line.drop(2).trim()))
            }

            else -> paragraph += line
        }
        index++
    }
    flushParagraph()
}

private fun isTableSeparator(line: String): Boolean {
    val cells = parseTableCells(line)
    return cells.isNotEmpty() && cells.all { it.replace(":", "").matches(Regex("-{3,}")) }
}

private fun parseTableCells(line: String): List<String> =
    line.trim().removePrefix("|").removeSuffix("|").split('|').map(String::trim)

private fun cleanInlineMarkdown(text: String): String = text
    .replace(Regex("\\[([^]]+)]\\(([^)]+)\\)"), "$1 ($2)")
    .replace("**", "")
    .replace("__", "")
    .replace("`", "")
