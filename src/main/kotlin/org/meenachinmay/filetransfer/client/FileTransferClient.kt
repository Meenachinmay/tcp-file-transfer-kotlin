package org.meenachinmay.filetransfer.client

import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.meenachinmay.filetransfer.CompleteFileRequest
import org.meenachinmay.filetransfer.FileChunk
import org.meenachinmay.filetransfer.FileTransferGrpcKt
import java.io.File
import java.util.concurrent.TimeUnit

class FileTransferClient(private val channel: ManagedChannel) {
    private val stub: FileTransferGrpcKt.FileTransferCoroutineStub = FileTransferGrpcKt.FileTransferCoroutineStub(channel)

    suspend fun uploadFile(filePath: String) {
        val file = File(filePath)
        val totalSize = file.length()
        println("Started sending file: ${file.name} (${totalSize / 1024} KB)")

        val fileChunks: Flow<FileChunk> = flow {
            var sentSize: Long = 0
            file.inputStream().use { input ->
                val buffer = ByteArray(1024 * 1024) // 1MB chunks
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val chunk = FileChunk.newBuilder()
                        .setFileName(file.name)
                        .setContent(com.google.protobuf.ByteString.copyFrom(buffer, 0, bytesRead))
                        .setTotalSize(totalSize)
                        .build()
                    emit(chunk)
                    sentSize += bytesRead
                    val progress = (sentSize.toDouble() / totalSize * 100).toInt()
                    println("Sending ${file.name}: $progress% complete")
                }
            }
        }

        val response = stub.uploadFile(fileChunks)
        println("File upload ${if (response.success) "in progress" else "failed"}: ${response.message}")

        // Send completion signal
        val completeResponse = stub.completeFileTransfer(CompleteFileRequest.newBuilder().setFileName(file.name).build())
        println("File transfer completion signal ${if (completeResponse.success) "sent successfully" else "failed"}: ${completeResponse.message}")
    }

    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}