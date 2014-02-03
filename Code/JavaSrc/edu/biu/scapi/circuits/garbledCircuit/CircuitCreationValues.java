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

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

/**
 * A class that hold the values used to create the circuit. <p>
 * These values are:<P>
 * 1. Both keys of  input and output wires.<p>
 * 2. Translation table of the circuit.<p>
 * 3. Signal bits of the input wires. They are returned in order to enable generate keys out of the input wires' keys.<p>
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public class CircuitCreationValues {
	private Map<Integer, SecretKey[]> allInputWireValues;
	private HashMap<Integer, Byte> inputSignaBits;
	private Map<Integer, SecretKey[]> allOutputWireValues;
	private HashMap<Integer, Byte> translationTable;
	
	/**
	 * Sets the given arguments.
	 * @param allInputWireValues both keys for all input wires.
	 * @param inputSignaBits signal bits of all input wires.
	 * @param allOutputWireValues both keys for all output wires.
	 * @param translationTable signal bits of all output wires.
	 */
	public CircuitCreationValues(Map<Integer, SecretKey[]> allInputWireValues, HashMap<Integer, Byte> inputSignaBits, 
			Map<Integer, SecretKey[]> allOutputWireValues, HashMap<Integer, Byte> translationTable) {
		this.allInputWireValues = allInputWireValues;
		this.inputSignaBits = inputSignaBits;
		this.allOutputWireValues = allOutputWireValues;
		this.translationTable = translationTable;
	}

	public Map<Integer, SecretKey[]> getAllInputWireValues() {
		return allInputWireValues;
	}
	
	public HashMap<Integer, Byte> getInputSignalBits() {
		return inputSignaBits;
	}

	public Map<Integer, SecretKey[]> getAllOutputWireValues() {
		return allOutputWireValues;
	}

	public HashMap<Integer, Byte> getTranslationTable() {
		return translationTable;
	}
}
