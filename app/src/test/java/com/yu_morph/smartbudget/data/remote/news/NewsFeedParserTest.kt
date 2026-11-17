package com.yu_morph.smartbudget.data.remote.news

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsFeedParserTest {
    @Test
    fun `RSS 기사와 첫 번째 썸네일을 파싱한다`() {
        val articles =
            NewsFeedParser.parse(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/">
                    <channel>
                        <item>
                            <title><![CDATA[첫 번째 &quot;경제&quot; 기사]]></title>
                            <link>https://example.com/first</link>
                            <description><![CDATA[<b>주요</b> 기사 설명입니다.]]></description>
                            <media:content url="https://example.com/first.jpg" type="image/jpeg" />
                            <media:content url="https://example.com/second.jpg" type="image/jpeg" />
                        </item>
                        <item>
                            <title>두 번째 기사</title>
                            <link>https://example.com/second</link>
                            <description>간단한 설명</description>
                        </item>
                    </channel>
                </rss>
                """.trimIndent(),
            )

        assertEquals(2, articles.size)
        assertEquals("첫 번째 \"경제\" 기사", articles[0].title)
        assertEquals("주요 기사 설명입니다.", articles[0].description)
        assertEquals("https://example.com/first.jpg", articles[0].thumbnailUrl)
        assertNull(articles[1].thumbnailUrl)
    }

    @Test
    fun `필수 제목이나 링크가 없는 항목은 제외한다`() {
        val articles =
            NewsFeedParser.parse(
                """
                <rss version="2.0">
                    <channel>
                        <item><title>링크 없음</title></item>
                        <item><link>https://example.com/no-title</link></item>
                        <item><title>정상 기사</title><link>https://example.com/valid</link></item>
                    </channel>
                </rss>
                """.trimIndent(),
            )

        assertEquals(listOf("정상 기사"), articles.map(NewsArticle::title))
    }
}
