package org.meenachinmay.filetransfer

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import org.meenachinmay.filetransfer.client.FileTransferClient
import java.io.File
import java.util.*

fun main() {
    val properties = Properties().apply {
        FileTransferClient::class.java.getResourceAsStream("/config.properties").use {
            load(it)
        }
    }

    val host = properties.getProperty("server.host")
    val port = properties.getProperty("server.port").toInt()

    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    val client = FileTransferClient(channel)

    println("File Transfer Client")
    println("--------------------")

    runBlocking {
        while (true) {
            println("\nEnter the path of the file to transfer (or 'quit' to exit):")
            val input = readLine()?.trim() ?: break

            if (input.equals("quit", ignoreCase = true)) {
                println("Exiting application...")
                break
            }

            val file = File(input)
            when {
                !file.exists() -> println("Error: File does not exist.")
                !file.isFile -> println("Error: The path is not a file.")
                !file.canRead() -> println("Error: Cannot read the file. Check permissions.")
                else -> {
                    println("Attempting to transfer file: ${file.name}")
                    try {
                        client.uploadFile(input)
                        println("File transfer complete.")
                    } catch (e: Exception) {
                        println("Error transferring file: ${e.message}")
                    }
                }
            }
        }
    }

    println("Shutting down client...")
    client.shutdown()
}