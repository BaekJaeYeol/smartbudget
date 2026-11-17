package com.yu_morph.smartbudget.data.remote.news

import android.content.Context
import com.yu_morph.smartbudget.data.remote.SmartBudgetHttpClientFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import java.io.Closeable
import java.io.IOException
import java.io.StringReader
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.CancellationException
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

private const val BREAKING_NEWS_URL = "https://www.newsis.com/RSS/sokbo.xml"
private const val ECONOMY_NEWS_URL = "https://www.yna.co.kr/rss/economy.xml"
private const val BREAKING_NEWS_LIMIT = 12
private const val ECONOMY_NEWS_LIMIT = 40
private const val HTTP_SUCCESS_MIN = 200
private const val HTTP_SUCCESS_MAX = 299
private const val USER_AGENT = "SmartBudget Android RSS Reader"

data class NewsArticle(val title: String, val link: String, val publishedAt: Instant?, val thumbnailUrl: String?)

class NewsFeedRepository(context: Context, private val client: HttpClient = SmartBudgetHttpClientFactory.create()) :
    Closeable {
    private val cache = context.applicationContext.getSharedPreferences("news_feed_cache", Context.MODE_PRIVATE)

    suspend fun loadBreakingNews(): List<NewsArticle> =
        loadFeed(BREAKING_NEWS_URL, "breaking_news", BREAKING_NEWS_LIMIT)

    suspend fun loadEconomyNews(): List<NewsArticle> = loadFeed(ECONOMY_NEWS_URL, "economy_news", ECONOMY_NEWS_LIMIT)

    override fun close() {
        client.close()
    }

    private suspend fun loadFeed(url: String, cacheKey: String, limit: Int): List<NewsArticle> {
        val xml = loadXml(url, cacheKey)
        return NewsFeedParser.parse(xml).take(limit)
    }

    private suspend fun loadXml(url: String, cacheKey: String): String = try {
        val response = client.get(url) { header(HttpHeaders.UserAgent, USER_AGENT) }
        if (response.status.value !in HTTP_SUCCESS_MIN..HTTP_SUCCESS_MAX) {
            cachedOrThrow(cacheKey, "RSS 요청 실패: HTTP ${response.status.value}")
        } else {
            response.bodyAsText().also { cache.edit().putString(cacheKey, it).apply() }
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        cache.getString(cacheKey, null) ?: throw IOException("RSS를 불러오지 못했습니다.", exception)
    }

    private fun cachedOrThrow(cacheKey: String, message: String): String =
        cache.getString(cacheKey, null) ?: throw IOException(message)
}

internal object NewsFeedParser {
    fun parse(xml: String): List<NewsArticle> {
        require(!xml.contains("<!DOCTYPE", ignoreCase = true)) { "DOCTYPE이 포함된 RSS는 처리할 수 없습니다." }
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
            setFeatureIfSupported("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeatureIfSupported("http://xml.org/sax/features/external-general-entities", false)
            setFeatureIfSupported("http://xml.org/sax/features/external-parameter-entities", false)
        }
        val document = factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
        val items = document.getElementsByTagName("item")
        return buildList {
            for (index in 0 until items.length) {
                (items.item(index) as? Element)?.toNewsArticle()?.let(::add)
            }
        }
    }

    private fun Element.toNewsArticle(): NewsArticle? {
        val title = directChildText("title").cleanText()
        val link = directChildText("link").trim()
        return if (title.isBlank() || link.isBlank()) {
            null
        } else {
            NewsArticle(
                title = title,
                link = link,
                publishedAt = directChildText("pubDate").toPublishedAt(),
                thumbnailUrl = mediaThumbnailUrl(),
            )
        }
    }

    private fun Element.directChildText(name: String): String {
        val children = childNodes
        for (index in 0 until children.length) {
            val child = children.item(index)
            if (child.nodeType == Node.ELEMENT_NODE && (child.localName ?: child.nodeName) == name) {
                return child.textContent.orEmpty()
            }
        }
        return ""
    }

    private fun Element.mediaThumbnailUrl(): String? {
        val children = childNodes
        for (index in 0 until children.length) {
            val child = children.item(index) as? Element ?: continue
            if (child.localName == "content" && child.prefix == "media") {
                return child.getAttribute("url").trim().takeIf(String::isNotBlank)
            }
        }
        return null
    }

    private fun String.cleanText(): String = replace(Regex("<[^>]+>"), " ")
        .replace("&nbsp;", " ")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.toPublishedAt(): Instant? = try {
        ZonedDateTime.parse(trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
    } catch (_: DateTimeParseException) {
        null
    }

    private fun DocumentBuilderFactory.setFeatureIfSupported(feature: String, enabled: Boolean) {
        // Android 버전에 따라 일부 보안 기능을 지원하지 않으므로 원문의 DOCTYPE 차단과 함께 적용한다.
        runCatching { setFeature(feature, enabled) }
    }
}
