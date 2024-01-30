package utils

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.util.*

object Downloader {
    fun download(fileURL: String, savePath: String, callback: (DownloadStatus)->Unit){
        callback(DownloadStatus("新的任务 from $fileURL, to $savePath", fileURL, savePath))
        try {
            val conn = URI(fileURL).toURL().openConnection()
            val inputStream = conn.getInputStream()
            val tarDir = File(File(savePath).parent)
            if(!tarDir.exists()){
                if(!tarDir.mkdirs()){
                    callback(DownloadStatus("创建目标文件夹失败 $tarDir", fileURL, savePath))
                    return
                }
            }
            callback(DownloadStatus("开始下载 $fileURL", fileURL, savePath))
            val tmpPath = "$savePath.tmp"
            val tarFile = File(tmpPath)
            tarFile.createNewFile()
            val buff = inputStream.readAllBytes()
            val fos = FileOutputStream(tarFile)
            fos.write(buff)
            fos.close()
            inputStream.close()
            callback(DownloadStatus("下载完成 $savePath", fileURL, savePath))
            File(tmpPath).renameTo(File(savePath))
        } catch (e: Exception) {
            println("catch a exception $e")
            callback(DownloadStatus("下载失败： $e", fileURL, savePath))
        }
        println("download finish $fileURL, to $savePath")
    }
    fun fileContent(fileUrl: String): DownloadStatus?{
        try {
            val conn = URI(fileUrl).toURL().openConnection()
            if(conn.contentType.startsWith("text/plain;")) { //text/plain; charset=utf-8
                val contentStream = conn.content as InputStream
                val content = contentStream.readAllBytes().decodeToString()
                return DownloadStatus(content)
            }
        }catch (e: Exception){
            println("exception $e")
        }
        return null
    }
    class DownloadStatus(
        val message: String,
        val fromUrl: String="",
        val toPath: String="",
        val error: String="",
        val date: String= Date().toString(),
        val attachedList: MutableList<DownloadStatus> = mutableListOf()
    )
}