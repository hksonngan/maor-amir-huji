#ifndef __OPENMESHFUNCTIONS_H__
#define __OPENMESHFUNCTIONS_H__


//////////////////////////////
// OpenMesh Includes        //
//////////////////////////////

#include "OpenMesh/Core/IO/MeshIO.hh"
#include "OpenMesh/Core/Mesh/PolyMesh_ArrayKernelT.hh"

//////////////////////////////
// Type Definitions         //
//////////////////////////////

// Mesh object  //
typedef OpenMesh::PolyMesh_ArrayKernelT<> Mesh;

// Vector of 3 floats  //
typedef OpenMesh::VectorT<float, 3> Vector3F;


//void faceCenter(Mesh & mesh);
void computeCenterAndBoundingBox(Mesh& mesh);
//void faceValenceCounter(Mesh& mesh);
//void edgesLengths(Mesh& mesh);
//void faceCenter(Mesh & mesh);
//void vectorDemo();

Mesh* subDivitionWithCutmullClark(Mesh* mesh);




#endif
