import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleError(
    val code: String? = null,
    val info: String? = null,
    @SerialName("*") val details: String? = null,
)