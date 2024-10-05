package org.meenachinmay.filetransfer.server

import io.grpc.ServerBuilder
import kotlinx.coroutines.flow.Flow
import org.meenachinmay.filetransfer.*
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileTransferServer(private val port: Int) {
    private val server = ServerBuilder
        .forPort(port)
        .addService(FileTransferService())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("*** shutting down gRPC server since JVM is shutting down")
            this@FileTransferServer.stop()
            println("*** server shut down")
        })
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class FileTransferService : FileTransferGrpcKt.FileTransferCoroutineImplBase() {
        override suspend fun uploadFile(requests: Flow<FileChunk>): FileUploadResponse {
            var fileName: String? = null
            var outputStream: FileOutputStream? = null
            var totalSize: Long = 0
            var receivedSize: Long = 0

            try {
                requests.collect { chunk ->
                    if (fileName == null) {
                        fileName = chunk.fileName.takeIf { it.isNotBlank() } ?: "received_file_${UUID.randomUUID()}"
                        totalSize = chunk.totalSize
                        println("Started receiving file: $fileName (${totalSize / 1024} KB)")
                        val file = File(fileName)
                        outputStream = FileOutputStream(file)
                    }
                    outputStream?.write(chunk.content.toByteArray())
                    receivedSize += chunk.content.size()
                    val progress = (receivedSize.toDouble() / totalSize * 100).toInt()
                    println("Receiving $fileName: $progress% complete")
                }

                return FileUploadResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("File upload in progress")
                    .build()
            } catch (e: Exception) {
                return FileUploadResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error uploading file: ${e.message}")
                    .build()
            } finally {
                outputStream?.close()
            }
        }

        override suspend fun completeFileTransfer(request: CompleteFileRequest): CompleteFileResponse {
            println("File transfer completed: ${request.fileName}")
            return CompleteFileResponse.newBuilder()
                .setSuccess(true)
                .setMessage("File transfer completed successfully")
                .build()
        }
    }
}