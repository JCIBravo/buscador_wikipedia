package article

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mediawiki.MediaWikiAPI
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.sql.Connection
import java.sql.DriverManager

class InMemoryArticleRepository : ArticleRepository{
    private val articles = mutableListOf<Article>()
    private val url: String = "jdbc:sqlite:D:\\KotlinProjects\\Projecte_UF4\\db\\fav_articles.db"
    private val conn: Connection = DriverManager.getConnection(url)

    override fun list(): List<Article> {
        val statement = conn.prepareStatement("SELECT * FROM savedArticles;")
        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            articles.add(
                Article(
                    URLDecoder.decode(resultSet.getString("title"), "UTF-8"),
                    resultSet.getString("lang")
                )
            )
        }

        statement.close()
        return articles.toList()
    }

    override fun mutableList(): MutableList<Article> {
        return articles.toList().toMutableList()
    }

    override fun insert(article: Article) {
        val statement = conn.prepareStatement("INSERT INTO savedArticles VALUES (?, ?);")

        statement.setString(1, article.title)
        statement.setString(2, article.lang)
        statement.executeUpdate()
        statement.close()
    }


    override fun clearAll(confirm: Boolean) {
        if (confirm) {
            val statement = conn.prepareStatement("DELETE FROM savedArticles;")
            statement.executeUpdate()
            statement.close()

            println("Data was cleaned up!")
        } else {
            println("Execute again to confirm")
        }
    }

    override fun searchArticle(languageCode: String, title: String): String {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json( Json { ignoreUnknownKeys = true } )
            }
        }

        lateinit var result: String
        runBlocking {
            val apiURL = "https://$languageCode.wikipedia.org/w/api.php?action=parse&page=$title&prop=text&format=json&redirects="

            println("TRYING TO REQUEST @ $apiURL!!")
            val resultJSON: MediaWikiAPI = client.get(apiURL).body()

            result =
                if (resultJSON.error?.code == null) {
                    if (resultJSON.parse != null) {
                        val unparsed = resultJSON.parse.content!!.text!!
                        val doc = Jsoup.parse(unparsed)
                        val text = doc.text()

                        if (resultJSON.parse.redirection != null && resultJSON.parse.redirection.isNotEmpty()){
                            "ⓘ REDIRECTED FROM ${resultJSON.parse.redirection[0].from} TO ${resultJSON.parse.redirection[0].to} ⓘ\n\n$text"
                        } else {
                            text
                        }
                    } else {
                        //Es técnicamente imposible que el servidor no devuelva ni "parse" ni "error". O devuelve uno o el otro!
                        throw Exception("500 Internal Server Error")
                    }
                } else {
                    "⚠ ERROR (${ resultJSON.error.code }) ⚠: ${ resultJSON.error.info }\n${ resultJSON.error.details }"
                }
        }

        return result
    }
}