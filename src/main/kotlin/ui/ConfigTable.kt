package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utils.Downloader
import utils.getFolderName
import utils.validGradleHome
import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.concurrent.thread

private val defaultGradleName = "gradle-8.5-bin"
private var gradleDir by mutableStateOf(
    System.getenv("GRADLE_HOME") ?: (System.getenv("USERPROFILE") + "\\.gradle")
)
private var gradleFileName by mutableStateOf(defaultGradleName)
private var gradleSrcUrl by mutableStateOf("https://mirrors.cloud.tencent.com/gradle")
private var gradleFileDirName by mutableStateOf("")
@Composable
fun ConfigTable(modifier: Modifier=Modifier) {
    Column(modifier.padding(10.dp)) {
        Box(Modifier.fillMaxWidth().height(20.dp))
        TableItem("Gradle路径", gradleDir,
            placeholder = "获取不到GRADLE_HOME环境变量时使用默认的gradle路径",
            isError = !validGradleHome(gradleDir),
            onTitleClicked = {
                if (validGradleHome(gradleDir)) {
                    Runtime.getRuntime().exec(arrayOf("explorer", "${gradleDir}\\wrapper"))
                }
            }
        ) {
            gradleDir = it
        }
        TableItem("Gradle下载源", gradleSrcUrl,
            onTitleClicked = {
                val uri = URI(gradleSrcUrl)
                try {
                    val desktop = Desktop.getDesktop()
                    desktop.browse(uri)
                } catch (e: Exception) {
                    appendTip("打开网址失败 $e")
                }
            }
        ) {
            gradleSrcUrl = it
        }
        TableItem("Gradle版本", gradleFileName) {
            gradleFileName = it
        }
        Button({
            for (fName in gradleFileName.split("\n")) {
                val fileName = fName.trim().removeSuffix(".zip")
                if (fileName.isNotEmpty()) {
                    gradleFileDirName = getFolderName("https://services.gradle.org/distributions/${fileName}.zip")
                    val src = "${gradleSrcUrl}/${fileName}.zip"
                    val tar = "${gradleDir}\\wrapper\\dists\\${fileName}\\${gradleFileDirName}"
                    val tarFilePath = "$tar\\$fileName.zip"
                    val gradleFile = File(tarFilePath)
                    if (gradleFile.exists()) {
                        appendTip("目标文件已存在 $tarFilePath")
                        continue
                    }
                    if (File("$tarFilePath.tmp").exists()) {
                        appendTip("检测到目标版本正在下载, 如果检测错误，请删除tmp文件\n $tarFilePath.tmp")
                        continue
                    }
                    thread {
                        Downloader.download(src, tarFilePath) { status ->
                            println("callback status $status")
                            appendTaskStatus(status)
                        }
                    }
                }
            }
        }) {
            Text("下载")
        }
    }
}

@Composable
fun TableItem(key: String, value: String, placeholder:String="", isError:Boolean=false, readonly:Boolean=false, onTitleClicked: (()->Unit)?=null, onValueChanged: (String)->Unit){
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(key, Modifier.weight(1f).clickable{ onTitleClicked?.invoke() })
        OutlinedTextField(value, onValueChanged, Modifier.weight(4f),
            placeholder = {
                Text(placeholder)
            },
            isError=isError,
            readOnly = readonly,
            maxLines = 5
        )
    }
}

