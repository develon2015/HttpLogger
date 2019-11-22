let $ = window.$ || {
    isInited: false,
    /** 是否生成页脚 */
    needFooter: true,

    /** 打印日志 */
    log: function (any) {
        console.log(any);
        $.ready(fun => {
            let divLog = document.getElementById("log");
            if (divLog !== null) divLog.innerHTML += `${any}<hr>`;
        });
    },

    initHTML: function () {
        // 设置viewport
        document.head.innerHTML += `<meta content="width=device-width,initial-scale=1.0" name="viewport">`;
        // this.log(document.querySelector("#app"))
    },

    /** 初始化$库，要使用$()函数之前必须调用，否则可能会有不可预料的错误 */
    init: function () {
        if (this.isInited) {
            console.log("$ library has already inited");
            return;
        }
        console.log("init $ library");
        // 设置onload监听, 保证this.ready()至少被调用一次
        // 对于不支持window.onload()方法的浏览器，我只想说一句：再见
        this.ready(_ => {
            this.initHTML();
        });
        return true;
    },

    /** 等价于jQuery中的$(Function) */
    ready: function (func) {
        if (this.isInited) {
            // console.log("direct call func")
            return func();
        }
        // console.log("add linstener func")
        let copyOnload = window.onload;
        // 递归调用func
        window.onload = fun => {
            // console.log("window.onload() has called")
            this.isInited = true;
            if (typeof copyOnload === "function") {
                copyOnload(fun);
            }
            func()
        }
    },

    setTitle: function (strTitle) {
        $.ready(
            _ => {
                let title = document.querySelectorAll("title");
                console.log(`title: ${title.length}`);
                for (let i = 0; i < title.length; i ++ ) {
                    console.log(`移除TITLE: ${title[i].innerText}`);
                    document.head.removeChild(title[i]);
                }
                let newTitle = document.createElement("title");
                newTitle.innerText = strTitle;
                document.head.appendChild(newTitle);
            }
        )
    },

};

// 初始化$库
if (!$.init()) {
    alert("init $ library failed")
}

/**
 * 传递命名参数, Just for 可读性
 * foo(val(_string => "testString"), val(_int => 8))
 * @param lambda for example: paramName => paramValue
 */
function val(lambda) {
    if (typeof lambda === "function")
        return lambda()
    return lambda
}
