import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import article.Article
import article.ServiceLocator
import mediawiki.lang
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

@Composable
@Preview
fun App() {
    val articleRepository = ServiceLocator.articleRepository
    val options = lang

    var expandedLangDropdown by remember { mutableStateOf(false) }
    var resourceFlagPath by remember { mutableStateOf("img/flags/en.png") }

    var name by remember { mutableStateOf("") }
    var lang by mutableStateOf("en")
    var content by remember { mutableStateOf("") }

    val favouriteList by remember { mutableStateOf(articleRepository.list()) }
    var expandedFavDropdown by remember { mutableStateOf(false) }
    var enabledFavButton by remember { mutableStateOf(false) }
    var confirmDeletion by remember { mutableStateOf(false) }
    var deleteFavText by remember { mutableStateOf("Delete favorites") }

    var enabledWebButton by remember { mutableStateOf(false) }
    var enabledSearchButton by remember { mutableStateOf(false) }

    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarText by remember { mutableStateOf("") }

    fun searchFun(lang: String, name: String, doNotEnableButtons: Boolean = false){
        content = "LOADING..."
        content = articleRepository.searchArticle(lang, name)
        enabledSearchButton = true

        if (doNotEnableButtons) {
            enabledFavButton = false
            enabledWebButton = true
        } else {
            if (content.startsWith("⚠ ERROR")) {
                enabledFavButton = false
                enabledWebButton = false
            } else {
                enabledFavButton = true
                enabledWebButton = true
            }
        }
    }

    fun openInBrowser(url: String){
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        } else {
            snackbarText = "Open in browser function is not supported."
            snackbarVisible = true

            enabledWebButton = false
        }
    }

    snackbarText = "Program created by Joan Illescas in Kotlin.\nBug(s) detected: Favourite list not updating until you restart the program."
    snackbarVisible = true

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource("img/wikipedia.png"),
                        contentDescription = "Wikipedia Logo",
                        modifier = Modifier
                            .clickable { openInBrowser("https://www.wikipedia.org") }
                            .height(50.dp)
                            .width(50.dp)
                    )

                    Spacer(Modifier.width(25.dp))

                    Image(
                        painter = painterResource(resourceFlagPath),
                        contentDescription = "More options",
                        modifier = Modifier
                            .height(50.dp)
                            .width(50.dp)
                    )

                    Spacer(Modifier.width(2.dp))

                    Box {
                        Image(
                            painter = painterResource("img/arrow_down.jpg"),
                            contentDescription = "Select language",
                            modifier = Modifier
                                .clickable { expandedLangDropdown = true }
                                .height(60.dp)
                                .width(60.dp)
                        )

                        DropdownMenu(
                            expanded = expandedLangDropdown,
                            onDismissRequest = { expandedLangDropdown = false },
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(onClick = {
                                    lang = option["code"] ?: "en"
                                    resourceFlagPath = "img/flags/$lang.png"
                                    expandedLangDropdown = false
                                }) {
                                    Text(text = option["name"] ?: "English")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            enabledSearchButton = name.isNotEmpty()
                        },
                        label = { Text("Search for an article") },
                        singleLine = true,
                        modifier = Modifier.width(200.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Button(
                        enabled = enabledSearchButton,
                        onClick = { searchFun(lang, URLEncoder.encode(name, "UTF-8")) }
                    ){
                        Text("Search")
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = {
                            articleRepository.insert( Article(URLEncoder.encode(name, "UTF-8"), lang) )
                            enabledFavButton = false

                            snackbarText = "Article was added to favourites! (You will see it next time you load the app)"
                            snackbarVisible = true
                        },

                        enabled = enabledFavButton
                    ){
                        Text("Add to favourites")
                    }

                    Spacer(Modifier.width(10.dp))

                    Button(
                        onClick = {
                            content = ""
                            enabledFavButton = false
                            enabledWebButton = false
                            name = ""
                        }
                    ){
                        Text("Clear")
                    }
                }

                Spacer(Modifier.width(10.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().height(425.dp).padding(20.dp),
                    readOnly = true,
                )

                Spacer(Modifier.width(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val url = "https://$lang.wikipedia.org/wiki/${URLEncoder.encode(name, "UTF-8")}"
                            openInBrowser(url)
                        },
                        enabled = enabledWebButton
                    ) {
                        Text("Read it on Wikipedia!")
                    }

                    Spacer(Modifier.width(15.dp))

                    Button(
                        onClick = {
                            if (confirmDeletion){
                                articleRepository.clearAll(confirmDeletion)
                                confirmDeletion = false

                                snackbarText = "Data was cleaned up! (You will see it next time you load the app)"
                                snackbarVisible = true

                                deleteFavText = "Delete favorites"
                            } else {
                                deleteFavText = "Delete favorites (CONFIRM)"
                                confirmDeletion = true
                            }
                        },

                        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Red)
                    ) {
                        Text(
                            text = deleteFavText,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Box {
                        Image(
                            painter = painterResource("img/favourite.png"),
                            contentDescription = "Favourites",
                            modifier = Modifier
                                .clickable {
                                    expandedFavDropdown = true
                                }
                                .height(45.dp)
                                .width(45.dp)
                        )

                        DropdownMenu(
                            expanded = expandedFavDropdown,
                            onDismissRequest = { expandedFavDropdown = false },
                        ) {
                            if (favouriteList.isNotEmpty()) {
                                favouriteList.forEach { favourite ->
                                    DropdownMenuItem(onClick = {
                                        lang = favourite.lang
                                        name = favourite.title
                                        resourceFlagPath = "img/flags/$lang.png"
                                        expandedFavDropdown = false
                                        searchFun(favourite.lang, URLEncoder.encode(favourite.title, "UTF-8"), true)
                                    }) {
                                        Text(text = "[${favourite.lang}] ${favourite.title}")
                                    }
                                }
                            } else {
                                snackbarText = "You don't have any favourite article added."
                                snackbarVisible = true
                            }
                        }
                    }
                }
            }
        }

        if (snackbarVisible) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                backgroundColor = Color.DarkGray,
                contentColor = Color.White,
                content = { Text(snackbarText) },
                action = {
                    TextButton(
                        onClick = {
                            snackbarVisible = false
                            snackbarText = ""
                        }
                    ) {
                        Text(
                            text = "Close message",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}
