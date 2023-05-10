import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleContent(
    @SerialName("*") val text: String? = null,
)