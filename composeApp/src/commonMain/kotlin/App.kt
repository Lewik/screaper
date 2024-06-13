import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import screaper.extractor.CssSelectorExtractor
import screaper.extractor.Extractor
import screaper.extractor.RegexpExtractor
import screaper.utils.exhaustive


enum class UrlType {
    Emulator, Common,
}


@Composable
@Preview
fun App() {
    MaterialTheme {
        val uriHandler = LocalUriHandler.current
        val scope = rememberCoroutineScope()
        var clicked by remember { mutableStateOf(false) }
        var dialogText: String? by remember { mutableStateOf(null) }
        var showContent by remember { mutableStateOf(false) }
        var urlType by remember { mutableStateOf(UrlType.Emulator) }


        var screaperResult: ScreaperResult? by remember { mutableStateOf(null) }
        var urls by remember { mutableStateOf("http://0.0.0.0:8080/emulator/(i)") }
        var multiplier by remember { mutableStateOf("10") }
        val defaultTasks = remember {
            listOf(
                "price" to CssSelectorExtractor.Task("h1"),
                "label" to CssSelectorExtractor.Task("p"),
            )
        }
        val tasks: SnapshotStateList<Pair<String, Extractor.Task>> = remember {
            mutableStateListOf(
                *defaultTasks.toTypedArray()
            )
        }
        val labelErrors = remember {
            derivedStateOf {
                tasks
                    .groupingBy { it.first }
                    .eachCount()
                    .filter { it.value > 1 }
                    .mapValues { listOf("Duplicated name") }
            }
        }

        val taskErrors = remember {
            derivedStateOf {
                tasks.associate { (label, task) ->
                    val errors = mutableListOf<String>()
                    when (task) {
                        is RegexpExtractor.Task -> {
                            if (task.regexp.isBlank()) {
                                errors.add("No value")
                            }
                            Unit
                        }

                        is CssSelectorExtractor.Task -> {
                            if (task.cssSelector.isBlank()) {
                                errors.add("No value")
                            }
                            Unit
                        }
                    }.exhaustive()

                    label to errors.toList()
                }.filterValues { it.isNotEmpty() }
            }
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
        ) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Url type")
                        Spacer(Modifier.size(8.dp))
                        Button(
                            onClick = { urlType = UrlType.Emulator },
                            modifier = Modifier,
                            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp),
                            colors = when (urlType) {
                                UrlType.Emulator -> ButtonDefaults.buttonColors()
                                UrlType.Common -> ButtonDefaults.outlinedButtonColors()
                            }
                        )
                        {
                            Text("Emulator")
                        }
                        Button(
                            onClick = { urlType = UrlType.Common },
                            modifier = Modifier,
                            shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                            colors = when (urlType) {
                                UrlType.Emulator -> ButtonDefaults.outlinedButtonColors()
                                UrlType.Common -> ButtonDefaults.buttonColors()
                            }
                        ) {
                            Text("Common")
                        }


                        Spacer(Modifier.size(8.dp))
                        when (urlType) {
                            UrlType.Emulator -> {
                                TextField(
                                    value = multiplier,
                                    onValueChange = { multiplier = it },
                                    label = { Text("How many requests to send") }
                                )
                            }

                            UrlType.Common -> {
                                TextField(
                                    value = urls,
                                    onValueChange = { urls = it },
                                    label = { Text("Urls (separate by comma)") }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Card(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text("Regular expressions")
                    tasks.forEachIndexed { index, (name, task) ->
                        Row {
                            TextField(value = name,
                                onValueChange = { tasks[index] = it to task },
                                label = { Text("Name") },
                                isError = name in labelErrors.value,
                                supportingText = {
                                    labelErrors.value.getOrElse(name) { emptyList() }.map { Text(it) }
                                }

                            )

                            TextField(
                                value = when (task) {
                                    is RegexpExtractor.Task -> task.regexp
                                    is CssSelectorExtractor.Task -> task.cssSelector
                                },
                                onValueChange = {
                                    tasks[index] = name to CssSelectorExtractor.Task(it)
                                },
                                label = { Text("Task (css selector)") },
                                isError = name in taskErrors.value,
                                supportingText = {
                                    taskErrors
                                        .value
                                        .getOrElse(name) { emptyList() }
                                        .map { Text(it) }
                                }
                            )

                            IconButton(onClick = { tasks.removeAt(index) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "delete")
                            }
                        }
                    }


                    Row {
                        Button(
                            onClick = { tasks.add("" to CssSelectorExtractor.Task("")) }) {
                            Text("Add")
                        }

                        Spacer(Modifier.size(8.dp))

                        Button(onClick = {
                            tasks.clear()
                            tasks.addAll(defaultTasks)
                        }) {
                            Text("Set default")
                        }
                    }
                }
            }



            Button(
                onClick = {
                    showContent = !showContent
                    clicked = true
                    screaperResult = null
                    scope.launch {
                        try {
                            val response: HttpResponse =
                                client.post("http://localhost:8080/screaper/calculate/${if (urlType == UrlType.Emulator) multiplier else null}") {
                                    setBody(
                                        ScreaperRequest(
                                            urls = urls.split(",").map(String::trim),
                                            tasks = tasks.associate { (label, task) ->
                                                label to task
                                            })
                                    )
                                    contentType(ContentType.Application.Json)
                                }
                            screaperResult = response.body()
                        } catch (throwable: Throwable) {
                            dialogText = "Error $throwable"
                        }
                    }
                },
                enabled = taskErrors.value.isEmpty() && labelErrors.value.isEmpty()
            ) {
                Text("Screap")
            }


            Button(onClick = {
                uriHandler.openUri("http://localhost:8080/screaper/log")
            }) {
                Text("Download all results")
            }

            Button(onClick = {
                scope.launch {
                    client.delete("http://localhost:8080/screaper/log")
                    dialogText = "Results deleted"
                }
            }) {
                Text("Delete all results")
            }

            val modifier = Modifier.align(Alignment.Start)

            Card(
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
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Row(modifier) {
                                                Text("Url: ${entry.url}")
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
                                                        Text("Name: $name, Values: $value")
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

            dialogText?.also {
                Dialog(onDismissRequest = {
                    dialogText = null
                    clicked = false
                    screaperResult = null
                }) {
                    Surface(
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = dialogText ?: "Unknown")
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                dialogText = null
                                clicked = false
                                screaperResult = null
                            }) { Text(text = "Ok") }
                        }
                    }
                }
            }
        }
    }
}