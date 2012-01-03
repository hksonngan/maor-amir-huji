CC = g++ -Wall -Wno-deprecated
EXEC = Assembler
HDRS = Code.h Parser.h SymbolTable.h 
SRCS = Code.cpp Parser.cpp SymbolTable.cpp Assembler.cpp

all: Assembler

Assembler: $(HDRS) $(SRCS) 
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) $(SRCS) -o Assembler

clean:
	/bin/rm -f *.o *.bak Assembler *core *~
    
