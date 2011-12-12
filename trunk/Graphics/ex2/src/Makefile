CC = g++ -Wall -Wno-deprecated
EXEC = ex1
HDRS = ex1.h
SRCS = ex1.cpp
OBJS = $(subst .cpp,.o,$(SRCS)) 		#substituting .cpp -> .o

#OpenMesh definitions
CG_HOME = /cs/course/current/cg
OM_DIR = $(CG_HOME)/OpenMesh
OM_LIBS = -L$(OM_DIR)/lib -lOpenMeshCore
OM_INCLUDE=  -I$(OM_DIR)/include

LINK_FLAGS = -L/usr/lib -lm -lglut -lGL -lGLU -ldl -L/usr/X11R6/lib $(OM_LIBS)
COMP_FLAGS = -O2 -I/usr/include -I/usr/X11R6/include $(OM_INCLUDE)

all: ex1 

ex1: arcBall.cpp circle.cpp ex1.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS)  arcBall.cpp circle.cpp ex1.cpp  -o ex1	

tar:
	tar cvf $(EXEC).tar Makefile README $(SRCS) $(HDRS)
clean:
	/bin/rm -f *.o *.bak ex1 *core *~
    
