LIB_ANTLR := lib/antlr.jar
ANTLR_SCRIPT := Micro.g
ANTLR_GRAMMAR_SCRIPT := TreeGrammar.g

all: generate
	rm -rf classes
	mkdir classes
	javac -cp $(LIB_ANTLR) -d classes src/*.java generated/src/*.java

generate:
	rm -rf generated
	mkdir generated
	java -cp $(LIB_ANTLR) org.antlr.Tool -o generated src/$(ANTLR_SCRIPT)
	java -cp $(LIB_ANTLR) org.antlr.Tool -o generated src/$(ANTLR_GRAMMAR_SCRIPT)

clean:
	rm -rf classes generated out.tiny
	
group:
	@echo "msoni"

.PHONY: all generate clean
