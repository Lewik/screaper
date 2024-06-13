package screaper.extractor

import com.fleeksoft.ksoup.Ksoup

actual fun ksoupParse(html: String, cssSelector: String) =
    Ksoup.parse(html).select(cssSelector).mapNotNull { it.text() }