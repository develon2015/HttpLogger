SHELL := /bin/bash

DIR := ./out/production/HttpLogger
Target := $(DIR)/MainKt.class
Jar := ./http.jar
KC := kotlinc
JK := kotlin
DEPLOY_DIR := /usr/local/bin
OPTS ?=

.PHONY: all jar install
all: $(Target)
jar: $(Jar)
install: $(DEPLOY_DIR)/$(Jar) $(DEPLOY_DIR)/http $(DEPLOY_DIR)/ls.html

# 安装
$(DEPLOY_DIR)/$(Jar) $(DEPLOY_DIR)/http: $(Jar) http
	cp $^ $(DEPLOY_DIR)

$(DEPLOY_DIR)/ls.html: www/ls.html
	cp $^ $(DEPLOY_DIR)

# 编译主类
$(Target): $(shell find src -name '*.kt') | $(DIR)
	time $(KC) -cp $(DIR) -d $(DIR) $^


# 编译为jar文件
$(Jar): $(shell find src -name '*.kt')
	time $(KC) -cp $(DIR) -d $@ $(OPTS) $^

$(DIR):
	mkdir -p $@

# 运行主类
.PHONY: run
run:
	$(JK) -cp $(DIR) MainKt

# 运行测试类
.PHONY:test 
test:
	$(JK) -cp $(DIR) TestKt

.PHONY: clean
clean:
	rm -rf $(DIR)/* $(Proxy)
