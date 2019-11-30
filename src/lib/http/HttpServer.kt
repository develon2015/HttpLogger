package lib.http

import logger as log
import java.lang.Thread.currentThread as cth
import java.net.*
import java.util.*
import java.io.*
import java.nio.charset.*

class HttpServer(private val host: String = "0.0.0.0", private val port: Int = 80, private val baseDir: String = ".") {

	data class HttpRequest(val method: String, var uri: String, val version: String, val headers: List<Header>)

	data class Header(val name: String, val value: String)

	private val mime = mapOf(
		"html" to "text/html",
		"js" to "text/javascript",
		"css" to "text/css",
		"xml" to "application/xml",
		"json" to "application/json",

		"zip" to "application/zip",
		"tar" to "application/x-tar",
		"rar" to "application/x-rar-compressed",
		"pdf" to "application/pdf",

		"webm" to "video/webm",
		"mp4" to "video/mp4",

		"mp3" to "audio/mp3",

		"jpg" to "image/jpeg",
		"jpeg" to "image/jpg",
		"png" to "image/png",
		"ico" to "image/ico",

		"default" to "application/octet-stream"
	)

    private val extra = """
		|Server: FileServer
		|Access-Control-Allow-Origin: *
	""".trimIndent()
	/**
	 * API: /ls?{ dir: /?path/to/dir }
	 */
	private fun ls(ous: OutputStream, request: HttpRequest) {
		val uri = request.uri.let {
			it.substring(4 until it.length)
		}

		val dir = File(baseDir, uri)//.normalize()
		log.d("ls: ${dir.absolutePath}")
		if (!dir.isDirectory) {
			// advice redirect
			ous.write("""
				|HTTP/2.0 404 NOT DIRECTORY
				|Content-Length: 0
				$extra
				|
				|
				|
				""".trimMargin().toByteArray()
			)
			ous.flush()
			return
		}

		val type = mime["json"] ?: error("Unknown MIME Type")
		var dirs = ""
		var files = ""

		dir.listFiles() ?.let {
			it.forEach { f ->
				if (f.isDirectory)
					dirs += """${if (dirs == "") "" else ", "}"${URLEncoder.encode(f.name, "UTF-8")}""""
				else
					files += """${if (files == "") "" else ", "}"${URLEncoder.encode(f.name, "UTF-8")}""""
			}
		}

		val json = """
			|{
			|	"pwd": "${URLEncoder.encode(uri, "UTF-8")}",
			|	"dirs": [${dirs}],
			|	"files": [${files}]
			|}
		""".trimMargin()

		val jsonBytes =  json.toByteArray()
		val length = jsonBytes.size

		ous.write("""
			|HTTP/2.0 200 OK
			|Content-Type: $type
			|Content-Length: $length
			$extra
			|
			|
			""".trimMargin().toByteArray()
		)
		// send json data
		ous.write(jsonBytes)
		ous.flush()
	}

	private fun response(ous: OutputStream, request: HttpRequest) {
        // API holder
		run {
			request.uri.let {
				if (it.length > 3 && it.substring(0..3) == "/ls?")
					return ls(ous, request)
			}
		}

		var type = mime["default"] ?: error("Unknown MIME Type") // 默认资源类型

		val regexURI = """.*/[^/]+\.(.+)""".toRegex()

		// 通过正则匹配文件后缀名, 并映射type
		val matchResult = regexURI.matchEntire(request.uri)
		matchResult ?.let {
			it.groups[1] ?.value.let { mimeKey -> mime[mimeKey] ?.let { mime -> type = mime } }
		}

		val file = File("$baseDir${ request.uri }")

		log.d("file: $file")

		if (file.isDirectory) {
			log.d("目录${ file }重定向至index.html")
			return response(ous, request.let { HttpRequest(it.method, "${it.uri}/index.html", it.version, it.headers) })
		}

		if (!file.isFile) {
			ous.write("""
			   |HTTP/2.0 404 NOT FOUND
			   |Content-Length: 0
			   |
			   |
			   |
			   """.trimMargin().toByteArray()
			)
			ous.flush()
			return
		}

		val length = file.length()

		when (request.method) {
			"HEAD", "GET" -> {
				ous.write("""
					|HTTP/2.0 200 OK
					|Content-Type: $type
					|Content-Length: $length
					|
					|
					""".trimMargin().toByteArray()
				)
				if ("GET" == request.method) {
					// send file
					if (file.length() < 2 * 1024) {
                        log.d(msg = "一次性发送")
						ous.write(file.readBytes())
					} else {
						log.d(msg = "文件太大，分段发送")
						val fr = BufferedInputStream(FileInputStream(file), 2 * 1024 * 1024)
                        try {
							val buf = ByteArray(1024 * 1024)
							while (true) {
								val nb = fr.read(buf)
								ous.write(buf, 0, nb)
							}
						} catch (e: Exception) {
							fr.close()
						}
					}
				}
				ous.flush()
			}
		}
	}

	fun service() {
		log.d("HttpServer监听于$host : $port", "HttpServer")
		val sock = ServerSocket(port)

		while (true) {
			val conn = sock.accept()

			Thread {
				val name = cth().name

				log.d((conn.remoteSocketAddress as InetSocketAddress).let{ "${ it.address.hostAddress }:${ it.port }" }, "$name: 新的客户端连接")

				val ins = conn.getInputStream()
				val ous = conn.getOutputStream()
				val reader = BufferedReader(InputStreamReader(ins, Charset.forName("UTF-8")) )

				fun handleConn() : Boolean {

					// first line
					var line: String? = reader.readLine()

					if (line == null) {
						log.d("连接被关闭", name)
						return false
					}

					val regexRequest = """(GET|HEAD) ([^\s]*) HTTP/(\d\.\d)""".toRegex()
					var matchResult = regexRequest.matchEntire(line)

					if (matchResult == null) {
						log.d("Bad request: $line", name)
						return false
					}

					// 合法的请求
					var (method, uri, version) = matchResult.destructured
					uri = URLDecoder.decode(uri, "UTF-8")

					log.d("HTTP请求: $method $uri HTTP/$version", name)

					log.d("uri: $uri", name)

					val headers = LinkedList<Header>()
					while (true) {
						line = reader.readLine()
						if (line == null) {
							log.d("客户端断开连接", name)
							return false
						}

						if ("" == line.trim()) {
							log.d("Headers: $headers", name)
							break
						}

						val regexHeader = """(.*):\s(.*)""".toRegex()
						matchResult = regexHeader.matchEntire(line)
						if (matchResult == null) {
							log.d("Bad header: $line", name)
							continue
						}
						val (key, value) = matchResult.destructured
						headers.add(Header(key, value))
					}

					// response
					response(ous, HttpRequest(method, uri, version, headers))

					val headerConnection = try {
						headers.filter{ "Connection" == it.name }[0].value
					} catch(e: Exception) {
						"unknown"
					}

					return when (headerConnection) {
						"Close", "close" -> {
							log.d("处理完成, 正常断开", name)
							false
						}
						"Keep-Alive" -> {
							log.d("处理完成, Keep-Alive", name)
							true
						}
						else -> {
							log.d("处理完成, 继续等待", name)
							true
						}
					}
				}

				while (handleConn()) {
					log.d("await handle...")
				}

				conn.close()

			}.start()
		}
	}
}
