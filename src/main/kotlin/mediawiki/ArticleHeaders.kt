package mediawiki

import ArticleContent
import ArticleRedirection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleHeaders(
    val title: String? = null,
    @SerialName("redirects") val redirection: List<ArticleRedirection>? = listOf(),
    @SerialName("pageid") val id: Int? = null,
    @SerialName("text") val content: ArticleContent? = ArticleContent(),
)