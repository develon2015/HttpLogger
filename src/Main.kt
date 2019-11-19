import lib.log.Logger
import lib.http.HttpServer

val logger = Logger("全局日志")

fun main(args: Array<String>) {
	if (args.size < 3) {
		println("请提供Host Port baseDir, 如: 0.0.0.0 80 ./www")
		return
	}
	val server = HttpServer(args[0], args[1].toInt(), args[2])// HttpServer("", 8080, "./www")
	server.service()
}
