.ONESHELL:
SHELL := /bin/bash

DIR := ./out/production/HttpLogger
Target := $(DIR)/MainKt.class
Proxy := makefile_proxy
KC := kc
JK := jk
#CP := -cp bin -cp '*/*/ref/*.jar'


.PHONY: all
all: $(Proxy) | $(DIR)
	@
	echo 开始构建目标$(Target)
	time make -f $<

$(Proxy): src
	@
	codefs=$$(echo $$(find src -regex '.*\.kt$$'))
	echo 查找源文件 $$codefs
	echo 生成makefile代理文件$@,内容如下:
	echo "$(Target): $$codefs" > $@
	echo "	$(KC) -cp $(DIR) -d $(DIR) \$$^" >> $@
	cat $@

$(DIR):
	mkdir -p $@

.PHONY: run
run:
	$(JK) -cp $(DIR) MainKt

.PHONY:test 
test:
	$(JK) -cp $(DIR) TestKt

.PHONY: clean
clean:
	rm -rf $(DIR)/* $(Proxy)
