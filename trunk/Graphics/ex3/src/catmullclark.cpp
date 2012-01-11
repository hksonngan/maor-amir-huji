

#include "openMeshIncludes.h"

#include <iostream>

using namespace std;


void calculateFaceAverage(Mesh & mesh);
//void calculateEdgePoint(Mesh & mesh);





void subDivitionWithCutmullClark(Mesh& mesh){

	//moving over all the faces and calculating the face average
	calculateFaceAverage(mesh);

	//
	//calculateEdgePoint(mesh);


}
/*

void calculateEdgePoint(Mesh& mesh)
{

	Mesh::EdgeIter eIt , eBegin , eEnd;
	eBegin = mesh.edges_begin();
	eEnd = mesh.edges_end();

	int faceCounter = 0;
	for( eIt = eBegin ; eIt != eEnd ; ++eIt )
	{
		Mesh::EdgeFaceIter efIt , efBegin;
		efBegin = mesh.ef_iter(vIt);
		for( efIt = efBegin ; efIt ; ++efIt)
		{
			faceCounter++;
		}
	}



}*/



void calculateFaceAverage(Mesh& mesh)
{
  /** defining handles to property **/
  // handle for properties associated with a vertex
  //OpenMesh::VPropHandleT<int> vp_int_handle;
  // handle for properties associated with a faces
  OpenMesh::FPropHandleT< Mesh::Point > fp_point_handle;
  // handle for properties associated with an edge
  //OpenMesh::EPropHandleT<bool > ep_bool_handle;
  // handle for properties associated with an edge
 // OpenMesh::EPropHandleT<MyMesh::Point> ep_point_handle;

  /** adding handles to properties **/
 // mesh.add_property(vp_int_handle);
  mesh.add_property(fp_point_handle);
 // mesh.add_property(ep_point_handle);
 // mesh.add_property(ep_bool_handle);

//  MyMesh::VertexIter viter = mesh.vertices_begin();
//  MyMesh::VertexIter vend = mesh.vertices_end();
////  MyMesh::VertexHandle handle;

  /** set and check the vertex the property  **/
 // int counter = 0;
 // for (;viter != vend;++viter)
 //   {
  //    handle = viter.handle();
 //     mesh.property(vp_int_handle,handle) = counter++;
 //     cout <<"int property of vertex "<< handle.idx() <<" is "<< mesh.property(vp_int_handle,handle)<<endl;
  //  }
  Mesh::FaceIter fiter = mesh.faces_begin();
  Mesh::FaceIter fend = mesh.faces_end();
  Mesh::FaceHandle fhandle;
  Mesh::Point center;
  /** for each face, store the center of its vertices as a property **/
  for (;fiter != fend;++fiter)
    {
      fhandle = fiter.handle();
      /*
	  you could use vertex circulator as should in example code for ex2
	  (in faceCenter() function)
	  Iterating the mesh with the fh_iter enables you to iterate both on the edges and the verties simultaniously.
	  This is important for when you'll want to refine the mesh and need a 2 edges vertices,
	  one face vertex and one original vertex to create your mesh...
      */
      Mesh::FaceHalfedgeIter fhIter = mesh.fh_iter(fhandle);
      center = Mesh::Point(0,0,0);
      Mesh::VertexHandle vHandle;
      Mesh::HalfedgeHandle fhHandle;
      int valence = 0;
      for (;fhIter;++fhIter)
	{
	  fhHandle = fhIter.handle();
	  vHandle = mesh.to_vertex_handle(fhHandle);
	  center += mesh.point(vHandle);
	  valence++;
	}
      mesh.property(fp_point_handle,fhandle) = center / (double) valence;
      cout <<"center of face "<< fhandle.idx()<<" is "<< mesh.property(fp_point_handle,fhandle)<<endl;
    }

  /** finally once you're done with the properties you can remove them **/
 // mesh.remove_property(vp_int_handle);
 // mesh.remove_property(fp_point_handle);
 // mesh.remove_property(ep_bool_handle);
 // mesh.remove_property(ep_point_handle);

}
