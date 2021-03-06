/**
* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
* 
* Copyright (c) 2012 - SCAPI (http://crypto.biu.ac.il/scapi)
* This file is part of the SCAPI project.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
* to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
* and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
* FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
* 
* We request that any publication and/or code referring to and/or based on SCAPI contain an appropriate citation to SCAPI, including a reference to
* http://crypto.biu.ac.il/SCAPI.
* 
* SCAPI uses Crypto++, Miracl, NTL and Bouncy Castle. Please see these projects for any further licensing issues.
* %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
* 
*/

#include "StdAfx.h"
#include <jni.h>
#include "FpPoint.h"
#include <openssl/ec.h>
#include <stdio.h>
#include <iostream>
#include "DlogEC.h"

using namespace std;

/* 
 * function createPoint		: Creates the Fp point.
 * param dlog				: Pointer to the native dlog group.
 * param xBytes				: The x coordinate of the point.
 * param yBytes				: The x y coordinate of the point.
 * return					: Pointer to the created point.
 */
JNIEXPORT jlong JNICALL Java_edu_biu_scapi_primitives_dlog_openSSL_ECFpPointOpenSSL_createPoint
  (JNIEnv *env, jobject, jlong dlog, jbyteArray xBytes, jbyteArray yBytes){

	  BIGNUM *x, *y;
	  EC_POINT *point;
	  EC_GROUP *curve = ((DlogEC*) dlog)->getCurve();

	  //Convert the jbyteArrays to c++ notation.
	  unsigned char* x_bytes  = (unsigned char*) env->GetByteArrayElements(xBytes, 0);
	  unsigned char* y_bytes  = (unsigned char*) env->GetByteArrayElements(yBytes, 0);
	  
	  // Convert the arrays to BIGNUM objects.
	  if(NULL == (x = BN_bin2bn(x_bytes, env->GetArrayLength(xBytes), NULL))) return 0;
	  if(NULL == (y = BN_bin2bn(y_bytes, env->GetArrayLength(yBytes), NULL))) return 0;

	   // Create the element.
	  if(NULL == (point = EC_POINT_new(curve))) return 0;
	  if(1 != EC_POINT_set_affine_coordinates_GFp(curve, point, x, y, ((DlogEC*) dlog)->getCTX()))
		  return 0;

	  //Release the allocated memory.
	  BN_free(x);
	  BN_free(y);
	  env ->ReleaseByteArrayElements(xBytes, (jbyte*) x_bytes, 0);
	  env ->ReleaseByteArrayElements(yBytes, (jbyte*) y_bytes, 0);

	  return (long) point;
}

/* 
 * function getX		: Returns the x coordinate of the given point.
 * param dlog			: Pointer to the native dlog group.
 * param point			: Pointer to the point.
 * return				: the bytes of the point's x coordinate.
 */
JNIEXPORT jbyteArray JNICALL Java_edu_biu_scapi_primitives_dlog_openSSL_ECFpPointOpenSSL_getX
  (JNIEnv *env, jobject, jlong dlog, jlong point){
	  BIGNUM *x, *y;
	  
	  // Set up BIGNUM objects for x and y.
	  if(NULL == (x = BN_new())) return 0;
	  if(NULL == (y = BN_new())) return 0;

	  //Get x and y values.
	  EC_POINT_get_affine_coordinates_GFp(((DlogEC*) dlog)->getCurve(), (EC_POINT*) point , x, y, ((DlogEC*) dlog)->getCTX());
	  
	  //Convert x into a char array.
	  int size = BN_num_bytes(x);
	  unsigned char *xBytes = new unsigned char[size];
	  if(0 == (BN_bn2bin(x, xBytes))) return 0;
		  
	  //Build jbyteArray from the char array.
	  jbyteArray result = env-> NewByteArray(size);
	
	  //Release the allocated memory.
	  env->SetByteArrayRegion(result, 0, size, (jbyte*)xBytes);
	  delete (xBytes);

	  return result;
}

/* 
 * function getY		: Returns the y coordinate of the given point.
 * param dlog			: Pointer to the native dlog group.
 * param point			: Pointer to the point.
 * return				: the bytes of the point's y coordinate.
 */
JNIEXPORT jbyteArray JNICALL Java_edu_biu_scapi_primitives_dlog_openSSL_ECFpPointOpenSSL_getY
  (JNIEnv *env, jobject, jlong dlog, jlong point){
	  BIGNUM *x, *y;
	
	  // Set up BIGNUM objects for x and y.
	  if(NULL == (x = BN_new())) return 0;
	  if(NULL == (y = BN_new())) return 0;

	  //Get x and y values.
	  EC_POINT_get_affine_coordinates_GFp(((DlogEC*) dlog)->getCurve(), (EC_POINT*) point , x, y, ((DlogEC*) dlog)->getCTX());
	  
	  //Convert x into a char array.
	  int size = BN_num_bytes(y);
	  unsigned char *yBytes = new unsigned char[size];
	  if(0 == (BN_bn2bin(y, yBytes))) return 0;
		  
	  //Build jbyteArray from the char array.
	  jbyteArray result = env-> NewByteArray(size);
	
	  //Release the allocated memory.
	  env->SetByteArrayRegion(result, 0, size, (jbyte*)yBytes);
	  delete (yBytes);

	  return result;
}

/* 
 * function checkInfinity		: Check if the given point is the infinity.
 * param dlog					: Pointer to the native dlog group.
 * param point					: Pointer to the checked point.
 * return						: True if the given point is the infinity; False, otherwise.
 */
JNIEXPORT jboolean JNICALL Java_edu_biu_scapi_primitives_dlog_openSSL_ECFpPointOpenSSL_checkInfinity
  (JNIEnv *, jobject, jlong dlog, jlong point){

	   return EC_POINT_is_at_infinity(((DlogEC*) dlog)->getCurve(), (EC_POINT*) point);
}

/* 
 * function deletePoint		: Deletes the given point.
 * param point				: Pointer to the point.
 */
JNIEXPORT void JNICALL Java_edu_biu_scapi_primitives_dlog_openSSL_ECFpPointOpenSSL_deletePoint
  (JNIEnv *, jobject, jlong point){
	  EC_POINT_free((EC_POINT*) point);
}
