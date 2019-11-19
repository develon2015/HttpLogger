function setTitle(defaultTitle) {
	var title = document.getElementById("title")
	if (title == null) {
		var newTitle = document.createElement("title")
		newTitle.innerText = defaultTitle
		document.head.appendChild(newTitle)
	}
}

setTitle("TITLE")
