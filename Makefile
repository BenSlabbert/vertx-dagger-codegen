#!make

M := "mvn"

.PHONY: build
build: fmt
	${M} install

.PHONY: compile
compile: fmt
	${M} compile test-compile

.PHONY: deploy
deploy: fmt
	${M} deploy

.PHONY: test
test: fmt
	${M} test

.PHONY: package
package: fmt
	${M} package

.PHONY: verify
verify: fmt
	${M} verify

.PHONY: fmtCheck
fmtCheck:
	${M} spotless:check

.PHONY: fmt
fmt:
	${M} spotless:apply

.PHONY: clean
clean:
	${M} clean
	rm -rf mvnw mvnw.cmd *.tar.gz
