package org.meenachinmay.filetransfer

import org.meenachinmay.filetransfer.server.FileTransferServer
import java.util.*

fun main() {
    val properties = Properties().apply {
        FileTransferServer::class.java.getResourceAsStream("/config.properties").use {
            load(it)
        }
    }

    val port = properties.getProperty("server.port").toInt()
    val server = FileTransferServer(port)

    server.start()
    server.blockUntilShutdown()
}