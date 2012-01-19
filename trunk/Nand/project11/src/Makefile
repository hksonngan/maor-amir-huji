###############################################################################
#
# Makefile for JAVA project
# Amir Lazarovitch, ID 300331592, ichamir.lazarov@mail.huji.ac.il
# Maor Dayan, ID 300564770, maor.dayan@mail.huji.ac.il
#
###############################################################################

JAVAC=javac


SRCS=JackCompiler.java JackTokenizer.java Keyword.java Kind.java SymbolTable.java VMWriter.java CompilationEngine.java TokenType.java
EXEC=JackCompiler

TAR=tar
TARFLAGS=cvf
TARNAME=project11.tar
TARSRCS=$(SRCS) $(EXEC) README Makefile

all: compile

compile: $(SRCS)
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)

tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)

clean:
	rm -f *.class *~

