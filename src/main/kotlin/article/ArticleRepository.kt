package article

interface ArticleRepository{
    fun list() : List<Article>
    fun mutableList() : MutableList<Article>
    fun insert(article: Article)
    fun clearAll(confirm: Boolean)
    fun searchArticle(languageCode: String, title: String): String
}