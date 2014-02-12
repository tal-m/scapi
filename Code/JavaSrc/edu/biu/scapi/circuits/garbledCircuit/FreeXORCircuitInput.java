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
package edu.biu.scapi.circuits.garbledCircuit;

import edu.biu.scapi.circuits.circuit.BooleanCircuit;
import edu.biu.scapi.circuits.encryption.MultiKeyEncryptionScheme;
import edu.biu.scapi.primitives.kdf.KeyDerivationFunction;

/**
 * This is the input class for Free XOR circuit.<p>
 * Free XOR circuit's inputs are:<p>
 * 1. The boolean circuit needs to be garbled. <p>
 * 2. A MultiKeyEncryptionScheme.<p>
 * 3. A KeyDerivationFunction, in case the user wants to use the Row Reduction algorithm.<p>
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public class FreeXORCircuitInput implements CircuitInput{
	
	private BooleanCircuit ungarbledCircuit;
	private MultiKeyEncryptionScheme mes;
	private KeyDerivationFunction kdf;
	private boolean isRowReductionWithFixedOutputKeys;
	
	/**
	 * This constructor creates an input object for a regular representation of FreeXORGarbledBooleanCircuit.
	 * @param ungarbledCircuit the boolean circuit needs to be garbled. 
	 * @param mes a MultiKeyEncryptionScheme to use.
	 */
	public FreeXORCircuitInput(BooleanCircuit ungarbledCircuit, MultiKeyEncryptionScheme mes){
		this.ungarbledCircuit = ungarbledCircuit;
		this.mes = mes;
		this.kdf = null;
	}
	
	/**
	 * This constructor creates an input object for a row reduction representation of FreeXORGarbledBooleanCircuit.
	 * @param ungarbledCircuit the boolean circuit needs to be garbled. 
	 * @param mes a MultiKeyEncryptionScheme to use.
	 * @param kdf to use in the row reduction algorithm.
	 * @param isRowReductionWithFixedOutputKeys indicates if the user is going to use sample the wires' keys out of given output keys. 
	 * In this case, the circuit representation should be a little different. 
	 * See {@link BooleanCircuit#BooleanCircuit(File f)} for more information.
	 */
	public FreeXORCircuitInput(BooleanCircuit ungarbledCircuit, MultiKeyEncryptionScheme mes, KeyDerivationFunction kdf, boolean isRowReductionWithFixedOutputKeys){
		this.ungarbledCircuit = ungarbledCircuit;
		this.mes = mes;
		this.kdf = kdf;
		this.isRowReductionWithFixedOutputKeys = isRowReductionWithFixedOutputKeys;
	}

	@Override
	public BooleanCircuit getUngarbledCircuit() {
		
		return ungarbledCircuit;
	}
	
	/**
	 * If the constructor with the KDF object was called, the returned utility class uses the rowReduction technology.
	 * If not, the returned utility class uses the regular garbled table.
	 */
	@Override
	public CircuitTypeUtil createCircuitUtil() {
		if (kdf == null){ //There is no kdf, return the regular Free XOR utility.
			return new FreeXORGarbledBooleanCircuitUtil(mes);
		} else { //There is a kdf, return the Row Reduction Free XOR utility.
			return new FreeXORRowReductionGarbledBooleanCircuitUtil(mes, kdf, isRowReductionWithFixedOutputKeys, ungarbledCircuit.getOutputWireLabels());
		}
	}
	
	public boolean isRowReductionWithFixedOutputKeys(){
		return isRowReductionWithFixedOutputKeys;
	}
	
	public KeyDerivationFunction getKDF(){
		return kdf;
	}

}
