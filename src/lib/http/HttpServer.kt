package lib.http

import logger as log
import java.net.*

class HttpServer(val host: String = "0.0.0.0", val port: Int = 80) {
	val baseDir = "."

	fun service() {
		log.log("HttpServer监听于${ host} : ${ port }", "HttpServer")
		val sock = ServerSocket(port)

		while (true) {
			val conn = sock.accept()
			log.log("${ conn.getInetAddress() }", "ACCEPT")
		}
	}
}
