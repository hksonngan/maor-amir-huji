CC = g++ -Wall -Wno-deprecated
EXEC = ex3
HDRS = GLee.h openMeshIncludes.h circle.h arcBall.h shaderFunctions.h ex3.h
SRCS = GLee.cpp catmullclark.cpp circle.cpp arcBall.cpp shaderFunctions.cpp  ex3.cpp
OBJS = $(subst .cpp,.o,$(SRCS)) 		#substituting .cpp -> .o

#OpenMesh definitions
CG_HOME = /cs/course/current/cg
OM_DIR = $(CG_HOME)/OpenMesh
OM_LIBS = -L$(OM_DIR)/lib -lOpenMeshCore
OM_INCLUDE=  -I$(OM_DIR)/include

#for home compilation
OM_LIBS = -L/usr/local/lib/OpenMesh/ -lOpenMeshCore
OM_INCLUDE = -I/usr/local/include/OpenMesh/

LINK_FLAGS = -lGL -lGLU -lglut $(OM_LIBS)
COMP_FLAGS =  -I/usr/include -I/usr/X11R6/include #$(OM_INCLUDE)

all: ex3

GLee.o: GLee.h GLee.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) -c GLee.cpp -o GLee.o

ex3.o: ex3.h ex3.cpp 
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) -c ex3.cpp

catmullclark.o: catmullclark.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) -c catmullclark.cpp -o catmullclark.o

arcBall.o: arcBall.h arcBall.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) -c arcBall.cpp -o arcBall.o

circle.o: circle.h circle.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) -c circle.cpp -o circle.o

shaderFunctions.o: shaderFunctions.h shaderFunctions.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS) -c shaderFunctions.cpp -o shaderFunctions.o	

ex3: GLee.o arcBall.o circle.o catmullclark.o shaderFunctions.o ex3.o
	$(CC) $(COMP_FLAGS) GLee.o arcBall.o circle.o catmullclark.o shaderFunctions.o ex3.o -o ex3 $(LINK_FLAGS)

tar:
	tar cvf $(EXEC).tar Makefile README $(SRCS) $(HDRS) shaders
clean:
	/bin/rm -f *.o *.bak ex3 *core *~
    
