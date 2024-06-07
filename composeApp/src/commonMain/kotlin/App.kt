import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import screaper.ScreaperRequest
import screaper.ScreaperResult


@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var clicked by remember { mutableStateOf(false) }
        var showContent by remember { mutableStateOf(false) }
        var screaperResult: ScreaperResult? by remember { mutableStateOf(null) }
        var urls by remember { mutableStateOf("http://0.0.0.0:8080/emulator/(i)") }
        var multiplier by remember { mutableStateOf("1") }
        var regexps by remember {
            mutableStateOf(
                """
                    price = ${Regex("<h1>.*?:\\s*(.*?)</h1>")}
                    label = ${Regex("<p>.*?:\\s*(.*?)</p>")}
                """.trimIndent()
            )
        }

        val client by remember {
            mutableStateOf(HttpClient {
                expectSuccess = true
                install(ContentNegotiation) {
                    json()
                }
            })
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = urls,
                onValueChange = { urls = it },
                label = { Text("Urls") }
            )

            TextField(
                value = multiplier,
                onValueChange = { multiplier = it },
                label = { Text("Multiplier") }
            )

            TextField(
                value = regexps,
                onValueChange = { regexps = it },
                label = { Text("Regexps") }
            )

            Button(onClick = {
                showContent = !showContent
                clicked = true
                screaperResult = null
                scope.launch {
                    val response: HttpResponse = client.post("http://0.0.0.0:8080/screaper/calculate/${multiplier}") {
                        setBody(
                            ScreaperRequest(
                                urls = urls.split(",").map(String::trim),
                                regexPatterns = regexps.lines()
                                    .associate {
                                        it
                                            .split("=", limit = 2)
                                            .let { it.first() to it.last() }
                                    }
                            )
                        )
                        contentType(ContentType.Application.Json)
                    }
                    screaperResult = response.body()
                }
            }) {
                Text("Load")
            }
            val modifier = Modifier.align(Alignment.Start)

            Card(
                elevation = 4.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    screaperResult.let { screaperResult ->
                        if (screaperResult == null) {
                            if (clicked) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .width(64.dp)
                                    )
                                }
                            }
                        } else {
                            Column(modifier) {
                                Text("Overall start time: ${screaperResult.overallStartTime}, Overall duration: ${screaperResult.overallDuration}")
                                Text("Total: ${screaperResult.entries.count()} Errors: ${screaperResult.entries.count { it.error != null }}")
                                Text("Max delay: ${screaperResult.entries.maxOf { it.startTime - screaperResult.overallStartTime }}")
                                Text("Max duration: ${screaperResult.entries.maxOf { it.duration }}")
                            }
                            Column {
                                screaperResult.entries.forEach { entry ->
                                    Card(
                                        elevation = 4.dp
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Row(modifier) {
                                                Text("Url: ${entry.url.s}")
                                            }
                                            Row(modifier) {
                                                Text("Delay time: ${entry.startTime - screaperResult.overallStartTime}, Duration: ${entry.duration}")
                                            }
                                            if (entry.error != null) {
                                                Row(modifier) {
                                                    Text("Error: ${entry.error}")
                                                }
                                            } else {
                                                entry.results.forEach { (name, value) ->
                                                    Row(modifier) {
                                                        Text("Name: ${name.s}, Value: ${value.s}")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}