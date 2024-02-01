package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import utils.Downloader
import java.io.File

val stateList = mutableStateListOf<Downloader.DownloadStatus>()
@Composable
fun Status(modifier: Modifier=Modifier){
    Column(modifier.background(Color(0xfff0f0ff))) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("操作状态", color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Divider()
        }
        LazyColumn(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stateList) { state ->
                Surface(
                    elevation = 2.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            if(state.fromUrl.isNotEmpty()) Text(state.fromUrl, color = Color(0xff30a030))
                            for(st in state.attachedList.reversed()){
                                StateItem(st)
                                Divider(Modifier.padding(vertical = 10.dp), thickness = 2.dp)
                            }
                            StateItem(state)
                        }
                        TextButton(onClick = {
                            stateList.remove(state)
                        }
                        ) {
                            Text("移除")
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(true){
        val fileContent = Downloader.fileContent("https://gitee.com/jeady5/common-tools/raw/master/gradleDownloaderWelcome")
        fileContent?.let{
            val tips = it.message.split("\n").map {
                Downloader.DownloadStatus(it)
            }
            stateList.addAll(tips)
        }
    }
}

@Composable
fun StateItem(state: Downloader.DownloadStatus) {
    SelectionContainer {
        Text(buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(state.date)
            }
            if (state.message.isNotEmpty()) {
                appendLine()
                append(state.message)
            }
            if (state.error.isNotEmpty()) {
                withStyle(SpanStyle(color = Color(0xffa05050))) {
                    appendLine()
                    append(state.error)
                }
            }
        })
    }
}

fun appendTaskStatus(status: Downloader.DownloadStatus){
    var stateWillAdd: Downloader.DownloadStatus = status
    val findExist = stateList.any {state->
        if (state.fromUrl == status.fromUrl || state.toPath == status.toPath) {
            stateWillAdd = state
            stateList.remove(state)
            return@any true
        }
        false
    }
    if(findExist) {
        stateWillAdd.apply {
            attachedList.add(status)
        }
    }
    stateList.add(0, stateWillAdd)
}

fun appendTip(tip: String){
    stateList.add(0, Downloader.DownloadStatus(tip))
}

fun cleanTmpFiles() {
    for(state in stateList){
        val tmpFile = File("${state.toPath}.tmp")
        if(tmpFile.isFile){
            tmpFile.delete()
        }
    }
}