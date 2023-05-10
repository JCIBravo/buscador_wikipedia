package mediawiki

import ArticleError
import kotlinx.serialization.Serializable

@Serializable
data class MediaWikiAPI (
    val parse: ArticleHeaders? = ArticleHeaders(),
    val error: ArticleError? = ArticleError()
)