import lib.config.JsonConfig
import lib.log.Logger
import lib.http.HttpServer

val logger = Logger("全局日志")
val config = JsonConfig("config.json")

fun main() {
	val server = HttpServer(config.get("address"), config.get("port").toInt(), config.get("dir"))// HttpServer("", 8080, "./www")
	server.service()
}
