CC = g++ -Wall -Wno-deprecated
EXEC = ex2
HDRS = circle.h arcBall.h ex2.h
SRCS = circle.cpp arcBall.cpp ex2.cpp
OBJS = $(subst .cpp,.o,$(SRCS)) 		#substituting .cpp -> .o

#OpenMesh definitions
CG_HOME = /cs/course/current/cg
OM_DIR = $(CG_HOME)/OpenMesh
OM_LIBS = -L$(OM_DIR)/lib -lOpenMeshCore
OM_INCLUDE=  -I$(OM_DIR)/include

LINK_FLAGS = -L/usr/lib -lm -lglut -lGL -lGLU -ldl -L/usr/X11R6/lib $(OM_LIBS)
COMP_FLAGS = -O2 -I/usr/include -I/usr/X11R6/include $(OM_INCLUDE)

all: ex2

ex2: arcBall.cpp circle.cpp ex2.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS)  arcBall.cpp circle.cpp ex2.cpp  -o ex2	

tar:
	tar cvf $(EXEC).tar Makefile README $(SRCS) $(HDRS)
clean:
	/bin/rm -f *.o *.bak ex2 *core *~
    
