import lib.log.Logger
import lib.http.HttpServer

val logger = Logger("全局日志")

fun main(args: Array<String>) {
	val server = HttpServer()
	server.service()
}