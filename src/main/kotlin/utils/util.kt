package utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

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
