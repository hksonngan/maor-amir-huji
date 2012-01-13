

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



void addPropertiesToMesh(Mesh& mesh);
void removePropertiesFromMesh(Mesh& mesh);
void addFacePoint(Mesh& mesh,Mesh& newMesh);
void addEdgePoint(Mesh& mesh,Mesh& newMesh);
void addVertexPoint(Mesh& mesh,Mesh& newMesh);
void refineMesh(Mesh& mesh,Mesh& newMesh);


void addPropertiesToMesh(Mesh& mesh){


	/** adding handles to properties **/
		mesh.add_property(ep_point_handle);
		mesh.add_property(ep_bool_handle);
		mesh.add_property(fp_point_handle);
		mesh.add_property(vp_point_handle);

}

void removePropertiesFromMesh(Mesh& mesh){

	mesh.remove_property(ep_point_handle);
	mesh.remove_property(ep_bool_handle);
	mesh.remove_property(fp_point_handle);
	mesh.remove_property(vp_point_handle);

}


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

	cout << "here1" << endl;

	//remove used properties
	removePropertiesFromMesh(*mesh);

	//add normals to each face,
	newMesh->request_face_normals();

	//add normals to each vertex
	mesh->request_vertex_normals();

	//this command updates both the face and the vertex normals, can use update_face_normals and then update_vertex_normals
	mesh->update_normals();

	cout << "here2" << endl;

	//delete mesh;


	return newMesh;
}

int calcVertexFaces(Mesh& mesh, Mesh::VertexHandle handle){
	//calculating average of faces
	Mesh::VertexFaceIter faceIter = mesh.vf_iter(handle);
	Mesh::FaceHandle fHandle;
	int counter = 0;
	for (;faceIter;++faceIter){
		fHandle = faceIter.handle();
		counter++;
	}
	return counter;


}


void addVertexPoint(Mesh& mesh,Mesh& newMesh)
{

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
		//cout << "vertex valence(calc): "<< vertexValence << "    . vertex valence(func): " << mesh.valence(vHandle) << endl;
		center = Mesh::Point(0,0,0);
		//checking if the current half-edge is a boundary edge
		if (vertexValence <= 2){
			cout << "entered boundary" << endl;
			Mesh::VertexEdgeIter edgeIter = mesh.ve_iter(vHandle);
			for (;edgeIter;++edgeIter){
				heHandle = mesh.halfedge_handle(edgeIter.handle(),0);
				tempHandle = mesh.to_vertex_handle(heHandle);
				if (tempHandle == vHandle){
					tempHandle = mesh.from_vertex_handle(heHandle);
				}
				center += mesh.point(tempHandle)*0.125;
			}
			center += mesh.point(vHandle)*0.75;

			//center = center / 8.0;
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
			tempPoint = tempPoint*coef;
			tempPoint = tempPoint / (float)counter;
			center += tempPoint;

			tempPoint = Mesh::Point(0,0,0);

			//calculation average of edges
			Mesh::VertexEdgeIter edgeIter = mesh.ve_iter(vHandle);
			counter = 0;
			for (;edgeIter;++edgeIter){
				eHandle = edgeIter.handle();
				tempPoint += newMesh.point(mesh.property(ep_point_handle,eHandle));
				counter++;

			}
			//cout << "vertex valence(calc): "<< counter << "    . vertex valence(func): " << vertexValence << endl;
			tempPoint = tempPoint*2.0;
			tempPoint = tempPoint*coef;
			tempPoint = tempPoint / (float)counter;
			center += tempPoint;

			//adding this vertex to the calculation
			tempPoint = mesh.point(vHandle);
			tempPoint = tempPoint*((float)vertexValence - 3.0);
			tempPoint = tempPoint / (float)vertexValence;
			center += tempPoint;
		}
		mesh.property(vp_point_handle,vHandle) = newMesh.add_vertex(center);
		//cout << "current point is: " << center << endl;
	}
	return;
}

void addEdgePoint(Mesh& mesh,Mesh& newMesh){

	Mesh::HalfedgeIter heIter = mesh.halfedges_begin();
	Mesh::HalfedgeIter heIterEnd = mesh.halfedges_end();
	Mesh::HalfedgeHandle heHandle,opposite_heHandle;
	Mesh::EdgeHandle eHandle;
	Mesh::Point center;
	Mesh::FaceHandle fHandle,ofHandle;

	for (;heIter != heIterEnd;++heIter){

		//get current halfEdge and its opposite
		heHandle = heIter.handle();
		opposite_heHandle = mesh.opposite_halfedge_handle(heHandle);

		//get edge handle
		eHandle = mesh.edge_handle(heHandle);

		center = Mesh::Point(0,0,0);

		//check if this half-edge already been calculated
		if (mesh.property(ep_bool_handle,eHandle))
			continue;

		//adding the vertices to the calculation of point

		center += mesh.point(mesh.to_vertex_handle(heHandle));
		center += mesh.point(mesh.from_vertex_handle(heHandle));

		//getting handles to faces
		fHandle = mesh.face_handle(heHandle);
		ofHandle = mesh.face_handle(opposite_heHandle);

		//checking if the current half-edge is a boundary edge
		if (!fHandle.is_valid() || !ofHandle.is_valid()){
			cout << "boundary edge" << endl;
			center = center / 2.0 ;
		}
		else{
			center +=  newMesh.point(mesh.property(fp_point_handle,fHandle));
			center +=  newMesh.point(mesh.property(fp_point_handle,ofHandle));
			center = center / 4.0;
			//cout << "this face point: " << mesh.property(fp_point_handle,fHandle) << endl;
			//cout << "opposite face point: " << mesh.property(fp_point_handle,ofHandle) << endl;
		}

		//adding center point to edge and set edge's boolean property to true
		mesh.property(ep_point_handle,eHandle) = newMesh.add_vertex(center);
		mesh.property(ep_bool_handle,eHandle) = true;
	}


}


void addFacePoint(Mesh& mesh,Mesh& newMesh)
{

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
      // cout <<"center of face "<< fhandle.idx()<<" is "<< mesh.property(fp_point_handle,fhandle)<<endl;
    }
}



void refineMesh(Mesh& mesh,Mesh& newMesh){


	Mesh::FaceIter fiter = mesh.faces_begin();
	Mesh::FaceIter fend = mesh.faces_end();
	Mesh::FaceHandle fhandle;
	vector<Mesh::VertexHandle> faceHandles(4);
	//faceHandles.clear();
	int faceCounter = 0;
	int newFaceCounter = 0;
	for (;fiter != fend;++fiter){
		fhandle = fiter.handle();
		faceCounter++;
		Mesh::FaceHalfedgeIter fhIter = mesh.fh_iter(fhandle);
		Mesh::VertexHandle vHandle,temp;
		Mesh::HalfedgeHandle fhHandle,lastvHandle,firstvHandle;
		firstvHandle = fhIter.handle();
		lastvHandle = firstvHandle;
		vHandle = mesh.to_vertex_handle(lastvHandle);
		++fhIter;
		for (;fhIter;++fhIter){
			fhHandle = fhIter.handle();
			temp = mesh.property(fp_point_handle,fhandle);
			cout << "temp1:" << temp << "  is: " << newMesh.point(temp) << endl;
			faceHandles[0] = mesh.property(fp_point_handle,fhandle);;
			temp = mesh.property(ep_point_handle,mesh.edge_handle(lastvHandle));
			cout << "temp2:" << temp << "  is: " << newMesh.point(temp) << endl;
			faceHandles[1] = mesh.property(ep_point_handle,mesh.edge_handle(lastvHandle));
			temp = mesh.property(vp_point_handle,vHandle);
			faceHandles[2] = mesh.property(vp_point_handle,vHandle);
			cout << "temp3:" << temp << "  is: " << newMesh.point(temp) << endl;
			temp = mesh.property(ep_point_handle,mesh.edge_handle(fhHandle));
			faceHandles[3] = mesh.property(ep_point_handle,mesh.edge_handle(fhHandle));
			cout << "temp4:" << temp << "  is: " << newMesh.point(temp) << endl;
		//	cout << "faceHandles:(0):" << newMesh.point(faceHandles[0]) <<", (1):" << newMesh.point(faceHandles[1])<<", (2):" << newMesh.point(faceHandles[2])<< ", (3):" << newMesh.point(faceHandles[3]) << endl;
			newMesh.add_face(faceHandles);
			newFaceCounter++;
			lastvHandle = fhHandle;
			vHandle = mesh.to_vertex_handle(lastvHandle);
		}

		//adding last quad in face
		temp = mesh.property(fp_point_handle,fhandle);
		faceHandles[0] = mesh.property(fp_point_handle,fhandle);
		cout << "temp1:" << temp << "  is: " << newMesh.point(temp) << endl;
		temp = mesh.property(ep_point_handle,mesh.edge_handle(lastvHandle));
		faceHandles[1] = mesh.property(ep_point_handle,mesh.edge_handle(lastvHandle));
		cout << "temp2:" << temp << "  is: " << newMesh.point(temp) << endl;
		temp = mesh.property(vp_point_handle,vHandle);
		faceHandles[2] = mesh.property(vp_point_handle,vHandle);
		cout << "temp3:" << temp << "  is: " << newMesh.point(temp) << endl;
		temp = mesh.property(ep_point_handle,mesh.edge_handle(firstvHandle));
		faceHandles[3] = mesh.property(ep_point_handle,mesh.edge_handle(firstvHandle));
		cout << "temp4:" << temp << "  is: " << newMesh.point(temp) << endl;

		newMesh.add_face(faceHandles);
		newFaceCounter++;
		cout << "orig face num: " << faceCounter << " new faces: " << newFaceCounter << endl;

	}
	cout << "number of faces: " << faceCounter << endl;
}































