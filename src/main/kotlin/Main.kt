import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.ConfigTable
import ui.Status
import ui.cleanTmpFiles


@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(Color(0xfffafffa)), verticalArrangement=Arrangement.SpaceBetween){
            ConfigTable()
            Status()
        }
    }
}

fun main() = application {
    Window(onCloseRequest = {
        cleanTmpFiles()
        exitApplication()
    }, title = "Gradle 下载器", icon = painterResource("images/gradle.png")) {
        App()
    }
}

