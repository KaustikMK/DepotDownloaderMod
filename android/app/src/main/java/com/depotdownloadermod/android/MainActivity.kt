package com.depotdownloadermod.android

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.system.Os
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DepotDownloaderApp() }
    }
}

private val DepotColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    secondary = Color(0xFF86EFAC),
    background = Color(0xFF0B1220),
    surface = Color(0xFF121C2E),
    surfaceVariant = Color(0xFF1C2A40),
    onPrimary = Color(0xFF082F49),
)

@Composable
private fun DepotDownloaderApp() {
    MaterialTheme(colorScheme = DepotColors) {
        Surface(Modifier.fillMaxSize()) { DownloadScreen() }
    }
}

@Composable
private fun DownloadScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var appId by remember { mutableStateOf("") }
    var manifestFile by remember { mutableStateOf<File?>(null) }
    var keysFile by remember { mutableStateOf<File?>(null) }
    var log by remember { mutableStateOf(listOf("Ready. Select your manifest and depot keys.")) }
    var isRunning by remember { mutableStateOf(false) }

    fun importFile(uri: Uri, label: String, setFile: (File) -> Unit) {
        val target = File(context.filesDir, "imports/${System.currentTimeMillis()}-${label}")
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use(input::copyTo) }
        setFile(target)
        log = log + "OK   Imported $label"
    }
    val manifestPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importFile(it, "manifest.manifest", { manifestFile = it }) }
    }
    val keysPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importFile(it, "depotkeys.txt", { keysFile = it }) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0B1220)),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("DEPOT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            Text("Downloader Mod", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Android arm64 client", color = Color(0xFF94A3B8))
        }
        item {
            StatusCard("01", "Manifest file", manifestFile?.name ?: "Choose a dumped .manifest file", Icons.Default.FolderOpen) {
                manifestPicker.launch(arrayOf("application/octet-stream", "text/plain"))
            }
        }
        item {
            StatusCard("02", "Depot keys", keysFile?.name ?: "Choose a depot keys file", Icons.Default.Key) {
                keysPicker.launch(arrayOf("text/plain", "application/octet-stream"))
            }
        }
        item {
            OutlinedTextField(
                value = appId,
                onValueChange = { appId = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Steam App ID") },
                singleLine = true,
                supportingText = { Text("Example: 730 for Counter-Strike 2") },
            )
        }
        item {
            Button(
                enabled = !isRunning && appId.isNotBlank() && manifestFile != null && keysFile != null,
                onClick = {
                    isRunning = true
                    log = log + "INFO Starting download for App ID $appId"
                    scope.launch {
                        val output = runDownloader(context, appId, manifestFile!!, keysFile!!)
                        log = log + output
                        isRunning = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (isRunning) CircularProgressIndicator(Modifier.width(22.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(if (isRunning) "DOWNLOADING…" else "START DOWNLOAD", fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111C2D)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("ACTIVITY", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    log.takeLast(100).forEach { line ->
                        Text(line, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(number: String, title: String, detail: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("$number  $title", fontWeight = FontWeight.Bold)
                Text(detail, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private suspend fun runDownloader(context: Context, appId: String, manifest: File, keys: File): List<String> = withContext(Dispatchers.IO) {
    val stamp = DateTimeFormatter.ofPattern("HH:mm:ss")
    fun line(message: String) = "[${LocalTime.now().format(stamp)}] $message"
    val executable = File(context.filesDir, "bin/depotdownloader")
    try {
        executable.parentFile?.mkdirs()
        if (!executable.exists()) {
            context.assets.open("depotdownloader").use { input -> executable.outputStream().use(input::copyTo) }
            Os.chmod(executable.absolutePath, 0b111000000)
        }
        val outputDir = File(context.getExternalFilesDir(null), "downloads/$appId").apply { mkdirs() }
        val process = ProcessBuilder(listOf(executable.absolutePath, "-app", appId, "-manifestfile", manifest.absolutePath, "-depotkeys", keys.absolutePath, "-dir", outputDir.absolutePath))
            .redirectErrorStream(true).start()
        val lines = process.inputStream.bufferedReader().useLines { sequence -> sequence.map { line(it) }.toList() }
        lines + line("Process finished with exit code ${process.waitFor()}.")
    } catch (exception: Exception) {
        listOf(line("ERROR ${exception.message ?: "Could not launch bundled arm64 binary."}"))
    }
}
