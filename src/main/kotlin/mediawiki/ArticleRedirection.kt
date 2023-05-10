import kotlinx.serialization.Serializable

@Serializable
data class ArticleRedirection(
    val from: String? = null,
    val to: String? = null,
)