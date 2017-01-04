SRCFILES := $(shell find src -name \*.java)
RESFILES := $(shell (find src -name \*.dtd ; find src -name \*.ttf) | sed s/^src\\///)
PACKAGES := $(shell find src/com/kreative/vexillo -type d \! -path \*/.\* | sed s/^src\\/// | tr / .)

all: clean bin doc vexillo.jar vexillo-src.tgz

eclipseall: eclipseclean vexillo.jar vexillo-src.tgz

clean:
	rm -rf bin
	rm -rf doc
	rm -rf vexillo*.jar
	rm -rf vexillo*.tgz

eclipseclean:
	rm -rf vexillo*.jar
	rm -rf vexillo*.tgz

bin: src
	mkdir -p bin
	javac -sourcepath src $(SRCFILES) -d bin
	$(foreach res,$(RESFILES),cp src/$(res) bin/$(res);)

doc: src
	mkdir -p doc
	javadoc -sourcepath src $(PACKAGES) -d doc

vexillo.jar: bin
	export COPYFILE_DISABLE=true
	find bin -name .DS_Store -delete
	jar cmf dep/MANIFEST.MF vexillo.jar -C bin com/kreative

vexillo-src.tgz: src
	export COPYFILE_DISABLE=true
	find src -name .DS_Store -delete
	tar -czf vexillo-src.tgz src/com/kreative

install: vexillo.jar
	sudo cp -i vexillo.jar /usr/local/bin/
	sudo cp -i dep/ascii85??code /usr/local/bin/
	sudo cp -i dep/base64??code /usr/local/bin/
	sudo cp -i dep/rewritesvgpath /usr/local/bin/
	sudo cp -i dep/vex???? /usr/local/bin/

.PHONY: all eclipseall clean eclipseclean install
