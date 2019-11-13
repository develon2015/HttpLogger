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

				fun handleConn() : Boolean {

					// first line
					var line: String? = reader.readLine()

					if (line == null) {
						log.d("$name -> 连接被关闭")
						return false
					}

					val regexRequest = """(GET|HEAD) ([^\s]*) HTTP/(\d\.\d)""".toRegex()
					val matchResult = regexRequest.matchEntire(line)

					if (matchResult == null) {
						log.d(line, "$name: Bad request")
						return false
					}

					// 合法的请求
					val (method, uri, version) = matchResult.destructured

					log.d("$method $uri HTTP/$version", "$name HTTP请求")

					// Headers
					while (true) {
						line = reader.readLine()
						if (line == null) {
							log.d("$name: 客户端断开连接")
							return false
						}
						log.d(line)
					}

					log.d("$name 处理完成, 继续等待")
					return true
				}

				while (handleConn()) {
					log.d("await handle...")
				}

				conn.close()

			}.start()
		}
	}
}
