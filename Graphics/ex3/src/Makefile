CC = g++ -Wall -Wno-deprecated
EXEC = ex3
HDRS = GLee.h openMeshIncludes.h circle.h arcBall.h ex3.h
SRCS = GLee.cpp catmullclark.cpp circle.cpp arcBall.cpp  ex3.cpp
OBJS = $(subst .cpp,.o,$(SRCS)) 		#substituting .cpp -> .o

#OpenMesh definitions
CG_HOME = /cs/course/current/cg
OM_DIR = $(CG_HOME)/OpenMesh
OM_LIBS = -L$(OM_DIR)/lib -lOpenMeshCore
OM_INCLUDE=  -I$(OM_DIR)/include

LINK_FLAGS = -L/usr/lib -lm -lglut -lGL -lGLU -ldl -L/usr/X11R6/lib $(OM_LIBS)
COMP_FLAGS = -O2 -I/usr/include -I/usr/X11R6/include $(OM_INCLUDE)

all: ex3

ex3: GLee.c catmullclark.cpp arcBall.cpp circle.cpp ex3.cpp
	$(CC) $(COMP_FLAGS) $(LINK_FLAGS)  GLee.c catmullclark.cpp arcBall.cpp circle.cpp ex3.cpp  -o ex3	

tar:
	tar cvf $(EXEC).tar Makefile README $(SRCS) $(HDRS)
clean:
	/bin/rm -f *.o *.bak ex3 *core *~
    
