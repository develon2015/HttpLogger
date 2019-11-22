SHELL := /bin/bash

DIR := ./out/production/HttpLogger
Target := $(DIR)/MainKt.class
Jar := ./http.jar
KC := kotlinc
JK := kotlin

.PHONY: all jar

all: $(Target)

$(Target): $(shell find src -name '*.kt') | $(DIR)
	time $(KC) -cp $(DIR) -d $(DIR) $^

jar: $(Jar)

$(Jar): $(shell find src -name '*.kt')
	time $(KC) -cp $(DIR) -d $@ $^

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
