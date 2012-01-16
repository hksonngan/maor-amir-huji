
#include "openMeshIncludes.h"

#include <iostream>

using namespace std;


/** defining handles to property **/
// handle for properties associated with an edge
OpenMesh::EPropHandleT<bool> ep_bool_handle;
// handle for properties associated with an edge
OpenMesh::EPropHandleT<Mesh::VertexHandle> ep_point_handle;
// handle for properties associated with a faces
OpenMesh::FPropHandleT< Mesh::VertexHandle > fp_point_handle;
// handle for properties associated with a vertex
OpenMesh::VPropHandleT<Mesh::VertexHandle> vp_point_handle;


//reference to the used functions in this file
void addPropertiesToMesh(Mesh& mesh);
void removePropertiesFromMesh(Mesh& mesh);
void addFacePoint(Mesh& mesh,Mesh& newMesh);
void addEdgePoint(Mesh& mesh,Mesh& newMesh);
void addVertexPoint(Mesh& mesh,Mesh& newMesh);
void refineMesh(Mesh& mesh,Mesh& newMesh);


/*
 * given a mesh, adding all the required properties to it.
 */
void addPropertiesToMesh(Mesh& mesh){
	mesh.add_property(ep_point_handle);
	mesh.add_property(ep_bool_handle);
	mesh.add_property(fp_point_handle);
	mesh.add_property(vp_point_handle);

}

/*
 * given a mesh, removes all the added properties from it
 */
void removePropertiesFromMesh(Mesh& mesh){
	mesh.remove_property(ep_point_handle);
	mesh.remove_property(ep_bool_handle);
	mesh.remove_property(fp_point_handle);
	mesh.remove_property(vp_point_handle);
}

/*
 * given a pointer to a mesh, subdividing this mesh using the cutmull-clark algorithm.
 * This function deletes the old mesh, and returns a pointer to the new mesh.
 */
Mesh* subDivitionWithCutmullClark(Mesh* mesh){

	//creating new mesh;
	Mesh* newMesh = new Mesh();

	//adding properties to mesh
	addPropertiesToMesh(*mesh);

	//running over all the faces adding the new face points
	addFacePoint(*mesh,*newMesh);

	//running over all edges adding the new edge points
	addEdgePoint(*mesh,*newMesh);

	//running over all vertices adding the new edge points
	addVertexPoint(*mesh,*newMesh);

	//build new refined mesh
	refineMesh(*mesh,*newMesh);

	//remove used properties
	removePropertiesFromMesh(*mesh);

	//add normals to each face,
	newMesh->request_face_normals();

	//add normals to each vertex
	newMesh->request_vertex_normals();

	//update both the face and the vertex normals
	newMesh->update_normals();

	//deleting old mesh
	delete mesh;

	return newMesh;
}

/*
 * given a mesh and a vertex handle in the mesh, returning the
 * number of faces sharing this vertex
 */
int calcVertexFaces(Mesh& mesh, Mesh::VertexHandle handle){
	Mesh::VertexFaceIter faceIter = mesh.vf_iter(handle);
	Mesh::FaceHandle fHandle;
	int counter = 0;
	for (;faceIter;++faceIter){
		fHandle = faceIter.handle();
		counter++;
	}
	return counter;


}

/*
 * given a mesh and a new mesh, calculating the new vertex point
 * of every vertex in the old mesh, adding it to the new mesh, and setting
 * a property of type vertexHandle in the vertex at the old mesh.
 */
void addVertexPoint(Mesh& mesh,Mesh& newMesh){

	Mesh::VertexIter vIter = mesh.vertices_begin();
	Mesh::VertexIter vIterEnd = mesh.vertices_end();
	Mesh::VertexHandle vHandle,tempHandle;
	Mesh::HalfedgeHandle heHandle;
	Mesh::EdgeHandle eHandle;
	Mesh::FaceHandle fHandle;
	Mesh::Point tempPoint,center;

	int vertexValence;
	float coef;

	//run over all the vertices
	for (;vIter != vIterEnd;++vIter){

		//get current vertex
		vHandle = vIter.handle();

		//get vertex valence
		vertexValence = calcVertexFaces(mesh,vHandle);
		center = Mesh::Point(0,0,0);
		int edgesCounter = 0;
		//checking if the current half-edge is a boundary edge
		if (mesh.is_boundary(vHandle)){
			Mesh::VertexEdgeIter edgeIter = mesh.ve_iter(vHandle);
			for (;edgeIter;++edgeIter){
				heHandle = mesh.halfedge_handle(edgeIter.handle(),0);
				tempHandle = mesh.to_vertex_handle(heHandle);
				if (tempHandle == vHandle){
					tempHandle = mesh.from_vertex_handle(heHandle);
				}
				center += mesh.point(tempHandle);
				edgesCounter++;
			}
			center = (center / edgesCounter)*0.25;
			center += mesh.point(vHandle)*0.75;
		}
		else{ //not in boundary
			coef = 1.0/((float)vertexValence);

			//calculating average of faces
			Mesh::VertexFaceIter faceIter = mesh.vf_iter(vHandle);
			tempPoint = Mesh::Point(0,0,0);
			int counter = 0;
			for (;faceIter;++faceIter){
				fHandle = faceIter.handle();
				tempPoint += newMesh.point(mesh.property(fp_point_handle,fHandle));
				counter++;
			}
			center += tempPoint*coef/ (float)counter;

			tempPoint = Mesh::Point(0,0,0);

			//calculation average of edges
			Mesh::VertexEdgeIter edgeIter = mesh.ve_iter(vHandle);
			counter = 0;
			for (;edgeIter;++edgeIter){
				eHandle = edgeIter.handle();
				tempPoint += newMesh.point(mesh.property(ep_point_handle,eHandle));
				counter++;
			}
			center += tempPoint*2.0*coef / (float) counter;

			//adding this vertex to the calculation
			center += mesh.point(vHandle)* ((float)vertexValence - 3.0) * coef;
		}
		mesh.property(vp_point_handle,vHandle) = newMesh.add_vertex(center);
	}
	return;
}

/*
 * given a mesh and a new mesh, calculating the new edge point
 * of every edge in the old mesh, adding it to the new mesh, and setting
 * a property of type vertexHandle in the edge at the old mesh.
 */
void addEdgePoint(Mesh& mesh,Mesh& newMesh){

	Mesh::HalfedgeIter heIter = mesh.halfedges_begin();
	Mesh::HalfedgeIter heIterEnd = mesh.halfedges_end();
	Mesh::HalfedgeHandle heHandle;
	Mesh::EdgeHandle eHandle;
	Mesh::FaceHandle fHandle,ofHandle;
	Mesh::Point center;

	for (;heIter != heIterEnd;++heIter){

		//get current halfEdge and its opposite
		heHandle = heIter.handle();

		//initialize center for the current edge
		center = Mesh::Point(0,0,0);

		//get edge handle
		eHandle = mesh.edge_handle(heHandle);

		//check if this half-edge already been calculated
		if (mesh.property(ep_bool_handle,eHandle))
			continue;

		//adding the vertices to the calculation of point
		center += mesh.point(mesh.to_vertex_handle(heHandle));
		center += mesh.point(mesh.from_vertex_handle(heHandle));

		//getting handles to faces
		fHandle = mesh.face_handle(heHandle);
		ofHandle = mesh.face_handle(mesh.opposite_halfedge_handle(heHandle));

		//checking if the edge is a boundary edge
		if (mesh.is_boundary(eHandle)){
			center = center / 2.0 ;
		}
		else{
			center +=  newMesh.point(mesh.property(fp_point_handle,fHandle));
			center +=  newMesh.point(mesh.property(fp_point_handle,ofHandle));
			center = center / 4.0;
		}

		//adding center point to edge and set edge's boolean property to true
		mesh.property(ep_point_handle,eHandle) = newMesh.add_vertex(center);
		mesh.property(ep_bool_handle,eHandle) = true;
	}


}

/*
 * given a mesh and a new mesh, calculating the new face point(the average of every vertex)
 * of every face in the old mesh, adding it to the new mesh, and setting
 * a property of type vertexHandle in the face at the old mesh.
 */
void addFacePoint(Mesh& mesh,Mesh& newMesh){

  Mesh::FaceIter fiter = mesh.faces_begin();
  Mesh::FaceIter fend = mesh.faces_end();
  Mesh::FaceHandle fhandle;
  Mesh::Point center;

  /** for each face, store the center of its vertices as a property **/
  for (;fiter != fend;++fiter)
    {
      fhandle = fiter.handle();
      Mesh::FaceHalfedgeIter fhIter = mesh.fh_iter(fhandle);
      center = Mesh::Point(0,0,0);
      Mesh::VertexHandle vHandle;
      Mesh::HalfedgeHandle fhHandle;
      int valence = 0;
      for (;fhIter;++fhIter){
	  fhHandle = fhIter.handle();
	  vHandle = mesh.to_vertex_handle(fhHandle);
	  center += mesh.point(vHandle);
	  valence++;

	  //initialize boolean property
	  mesh.property(ep_bool_handle,mesh.edge_handle(fhHandle)) = false;

	}
      //adding vertex point to face
       center = center / (double) valence;
       mesh.property(fp_point_handle,fhandle) = newMesh.add_vertex(center);
    }
}


/*
 * given a mesh with new calculated edge,face and vertex point(set as properties of
 * type vertexHandle and added to the new mesh as new vertices) building the 'newMesh'
 * mesh from the calculated points (adding the new faces to it)
 */
void refineMesh(Mesh& mesh,Mesh& newMesh){

	Mesh::FaceIter fiter = mesh.faces_begin();
	Mesh::FaceIter fend = mesh.faces_end();
	Mesh::FaceHandle fhandle;
	vector<Mesh::VertexHandle> faceHandles(4);

	for (;fiter != fend;++fiter){
		fhandle = fiter.handle();
		Mesh::FaceHalfedgeIter fhIter = mesh.fh_iter(fhandle);
		Mesh::VertexHandle vHandle,temp;
		Mesh::HalfedgeHandle fhHandle,lastvHandle,firstvHandle;
		firstvHandle = fhIter.handle();
		lastvHandle = firstvHandle;
		vHandle = mesh.to_vertex_handle(lastvHandle);
		++fhIter;
		for (;fhIter;++fhIter){
			fhHandle = fhIter.handle();
			faceHandles[0] = mesh.property(fp_point_handle,fhandle);
			faceHandles[1] = mesh.property(ep_point_handle,mesh.edge_handle(lastvHandle));
			faceHandles[2] = mesh.property(vp_point_handle,vHandle);
			faceHandles[3] = mesh.property(ep_point_handle,mesh.edge_handle(fhHandle));
			newMesh.add_face(faceHandles);
			lastvHandle = fhHandle;
			vHandle = mesh.to_vertex_handle(lastvHandle);
		}
		//adding last quad in face
		faceHandles[0] = mesh.property(fp_point_handle,fhandle);
		faceHandles[1] = mesh.property(ep_point_handle,mesh.edge_handle(lastvHandle));
		faceHandles[2] = mesh.property(vp_point_handle,vHandle);
		faceHandles[3] = mesh.property(ep_point_handle,mesh.edge_handle(firstvHandle));
		newMesh.add_face(faceHandles);
	}
}
