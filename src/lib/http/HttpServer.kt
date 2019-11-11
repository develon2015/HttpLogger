package lib.http

import logger as log
import java.lang.Thread.currentThread as cth
import java.net.*
import java.io.*
import java.nio.charset.*

class HttpServer(val host: String = "0.0.0.0", val port: Int = 80, val baseDir: String = ".") {
	fun service() {
		log.d("HttpServer监听于${ host} : ${ port }", "HttpServer")
		val sock = ServerSocket(port)

		while (true) {
			val conn = sock.accept()

			Thread {
				val name = cth().name

				log.d("${ (conn.remoteSocketAddress as InetSocketAddress).let{ "${ it.address.hostAddress }:${ it.port }" } }", "$name: 新的客户端连接")

				val ins = conn.getInputStream()
				val ous = conn.getOutputStream()
				val reader = BufferedReader(InputStreamReader(ins, Charset.forName("UTF-8")) )

				fun handleConn() {
					// HTTP protocol?
					val line = reader.readLine()
					if (line == null) {
						log.d("$name: 客户端断开连接")
						break
					}
					log.d(line)
				}

				while (true) {
				}

				conn.close()

			}.start()
		}
	}
}
