package article

object ServiceLocator {
    val articleRepository : ArticleRepository = InMemoryArticleRepository()
}