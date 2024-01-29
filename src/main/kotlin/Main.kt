import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.logging.Logger
import kotlin.concurrent.thread


private var tip by mutableStateOf("")
val defaultGradleName = "gradle-8.5-bin"
var gradleDir by mutableStateOf(
    System.getenv("GRADLE_HOME") ?: (System.getenv("USERPROFILE") + "\\.gradle")
)
var gradleFileName by mutableStateOf(defaultGradleName)
var gradleSrcUrl by mutableStateOf("https://mirrors.cloud.tencent.com/gradle")
var gradleFileDirName by mutableStateOf("")
var downloading by mutableStateOf(false)
var realDownloadUrl by mutableStateOf("")
var realSavePath by mutableStateOf("")
var currentByteRead by mutableStateOf(0)
var shouldCancel by mutableStateOf(false)
var downloadFinished by mutableStateOf(false)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(Modifier.fillMaxSize()){
            Column(Modifier.padding(10.dp)){
                SelectionContainer {
                    val github = "https://github.com/jeadyx/gradle-downloader"
                    val gitee = "https://gitee.com/jeadyx/gradle-downloader"
                    TextButton({
                        val desktop = Desktop.getDesktop()
                        desktop.browse(URI(gitee))
                    }){
                        Text("开源地址: ${gitee}")
                    }
                }
                Box(Modifier.fillMaxWidth().height(20.dp))
                item("Gradle路径", gradleDir,
                    placeholder = "获取不到GRADLE_HOME环境变量时使用默认的gradle路径",
                    isError = validGradleHome(gradleDir)){
                    gradleDir = it
                }
                item("Gradle下载源", gradleSrcUrl){
                    gradleSrcUrl = it
                }
                item("Gradle版本", gradleFileName){
                    gradleFileName = it
                }
                Button({
                    gradleFileDirName = getFolderName("https://services.gradle.org/distributions/${gradleFileName}.zip")

                    val src = "${gradleSrcUrl}/${gradleFileName}.zip"
                    val tar = "${gradleDir}\\wrapper\\dists\\${gradleFileName}\\${gradleFileDirName}"
                    val tarFilePath = "$tar\\$gradleFileName.zip"
                    val gradleFile = File(tarFilePath)
                    if(gradleFile.exists()){
                        tip = "目标文件已存在 \n$tarFilePath"
                        return@Button
                    }
                    thread {
                        download(src, tarFilePath)
                    }
                }, enabled = !downloading){
                    Text("下载")
                }
                AnimatedVisibility(realDownloadUrl.isNotEmpty()){
                    SelectionContainer {
                        Column {
                            Text("实际下载地址: \n    $realDownloadUrl")
                            Text("实际保存路径: \n    $realSavePath")
                        }
                    }
                }
            }

            AnimatedVisibility(tip.isNotEmpty(), Modifier.align(Alignment.BottomCenter)){
                Surface(
                    elevation = 10.dp
                ) {
                    val interactionSource = MutableInteractionSource()
                    val hovered by interactionSource.collectIsHoveredAsState()
                    Box(Modifier.fillMaxWidth().padding(10.dp).clickable(interactionSource, null){}, contentAlignment = Alignment.Center){
                        SelectionContainer {
                            Text(tip)
                        }
                        TextButton(onClick = {
                                tip = ""
                            }, modifier = Modifier.align(Alignment.CenterEnd)){
                            Text("关闭")
                        }
                        LaunchedEffect(hovered){
                            delay(5000)
                            if(!hovered){
                                tip = ""
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(downloading || downloadFinished){
                Box(Modifier.fillMaxSize().background(Color(0x30000000)), contentAlignment = Alignment.BottomCenter){
                    Row(Modifier.padding(10.dp).padding(bottom = 50.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$gradleFileName ${if(downloadFinished) "下载完成" else "下载中"}: ${humanByte(currentByteRead)}",
                            color = if(downloadFinished) Color(0xff308030) else Color(0xffffffff), textAlign = TextAlign.Center)
                        TextButton({
                            if(downloadFinished){
                                downloadFinished = false
                                downloading = false
                            }else{
                                shouldCancel = true
                            }
                        }){
                            Text(if(downloadFinished) "完成" else "取消")
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun humanByte(count:Int): String{
    val kb = remember { 1024 }
    val mb =  remember { 1024*1024 }
    val gb = remember { 1024*1024*1024 }
    return when{
        count <kb -> "${count}byte"
        count in kb until mb -> "${count/kb}kb"
        count in mb until gb -> "${String.format("%.2f", 1f*count/mb)}M"
        count >gb -> "${String.format("%.2f", 1f*count/gb)}G"
        else->"0"
    }
}

fun validGradleHome(gradleDir: String): Boolean{
    return !File("$gradleDir\\wrapper").isDirectory()
}

@Composable
fun item(key: String, value: String, placeholder:String="", isError:Boolean=false, readonly:Boolean=false, onValueChanged: (String)->Unit){
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(key, Modifier.weight(1f))
        OutlinedTextField(value, onValueChanged, Modifier.weight(4f),
            placeholder = {
                          Text(placeholder)
            },
            isError=isError,
            readOnly = readonly
        )
    }
}

fun getFolderName(url: String): String{
    val messageDigest: MessageDigest
    try {
        messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(url.toByteArray())
        val name = BigInteger(1, messageDigest.digest()).toString(36)
        return name
    } catch (e: NoSuchAlgorithmException) {
        return e.localizedMessage
    }
}

fun download(fileURL: String, savePath: String){
    println("download from $fileURL, to $savePath")
    try {
        shouldCancel = false
        downloading = true
        currentByteRead = 0
        downloadFinished = false
        val url: URL = URI(fileURL).toURL()
        val conn: URLConnection = url.openConnection()
        val inputStream: InputStream = conn.getInputStream()
        val tarDir = File(File(savePath).parent)
        if(!tarDir.exists()){
            if(!tarDir.mkdirs()){
                tip = "创建目标文件夹失败 \n$tarDir"
                return
            }
        }
        tip = "开始下载"
        realDownloadUrl = fileURL
        realSavePath = savePath
        val tmpPath = "$savePath.tmp"
        val fos = FileOutputStream(File(tmpPath))
        var i = inputStream.read()
        while (i != -1) {
            currentByteRead++
            fos.write(i)
            i = inputStream.read()
            if(shouldCancel) break
        }
        fos.close()
        inputStream.close()
        if(shouldCancel){
            tip = "取消下载"
            File(tmpPath).delete()
            downloading = false
        }else{
            tip = "下载完成"
            File(tmpPath).renameTo(File(savePath))
            downloadFinished = true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        tip = "下载失败： ${e}"
        downloading = false
    }
    println("download finish $shouldCancel $fileURL, to $savePath")
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Gradle 下载器") {
        App()
    }
}
