package lib.log

class Logger(val name: String = "Logger") {
	fun d(msg: String, title: String = "") {
		val trace: Array<StackTraceElement>?  = Thread.currentThread().getStackTrace()

		if (trace == null || trace.size < 3) return println("Logger@${ name } ${ title } ->${ msg }")

		val caller = trace[2]

		val fileName = caller.getFileName()
		val lineNumber = caller.getLineNumber()

		val className = caller.getClassName()
		val methodName = caller.getMethodName()

		var output = "Logger@${ name } ~ ${ fileName }(${ lineNumber }) ~ ${ className }.${ methodName }(): "
		output += "${ title } -> ${ msg }"
		println(output)
	}
}
