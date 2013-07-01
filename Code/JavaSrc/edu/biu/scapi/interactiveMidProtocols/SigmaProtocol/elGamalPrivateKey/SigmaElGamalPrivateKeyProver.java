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
package edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.elGamalPrivateKey;

import java.security.SecureRandom;

import edu.biu.scapi.exceptions.CheatAttemptException;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.DlogBasedSigma;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.SigmaProverComputation;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.SigmaSimulator;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.dlog.SigmaDlogProver;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.dlog.SigmaDlogProverInput;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.utility.SigmaProtocolInput;
import edu.biu.scapi.interactiveMidProtocols.SigmaProtocol.utility.SigmaProtocolMsg;
import edu.biu.scapi.primitives.dlog.DlogGroup;

/**
 * Concrete implementation of Sigma Protocol prover computation. <p>
 * 
 * This protocol is used for a party to prove that it knows the private key to an ElGamal public key.
 * 
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Moriya Farbstein)
 *
 */
public class SigmaElGamalPrivateKeyProver implements SigmaProverComputation, DlogBasedSigma{
	
	/*	
	  This class uses an instance of SigmaDlogProver with:
	  	�	Common DlogGroup
		�	input: h (the public key) and a value w<- Zq such that h=g^w (the private key).
	
	*/	 
	
	private SigmaDlogProver sigmaDlog;	//underlying SigmaDlogProver to use.
	
	/**
	 * Constructor that gets the underlying DlogGroup, soundness parameter and SecureRandom.
	 * @param dlog
	 * @param t Soundness parameter in BITS.
	 * @param random
	 */
	public SigmaElGamalPrivateKeyProver(DlogGroup dlog, int t, SecureRandom random) {
		
		//Creates the underlying SigmaDlogProver object with the given parameters.
		sigmaDlog = new SigmaDlogProver(dlog, t, random);
	}
	
	/**
	 * Default constructor that chooses default values for the parameters.
	 */
	public SigmaElGamalPrivateKeyProver() {
		//Creates the underlying SigmaDlogProver object with default parameters.
		sigmaDlog = new SigmaDlogProver();
	}
	
	/**
	 * Returns the soundness parameter for this Sigma protocol.
	 * @return t soundness parameter
	 */
	public int getSoundness(){
		//Delegates the computation to the underlying Sigma Dlog prover.
		return sigmaDlog.getSoundness();
	}


	/**
	 * Sets the input for this Sigma protocol
	 * @param input MUST be an instance of SigmaElGamalPrivateKeyProverInput.
	 * @throws IllegalArgumentException if input is not an instance of SigmaElGamalPrivateKeyProverInput.
	 */
	public void setInput(SigmaProtocolInput in) {
		if (!(in instanceof SigmaElGamalPrivateKeyProverInput)){
			throw new IllegalArgumentException("the given input must be an instance of SigmaElGamalPrivateKeyProverInput");
		}
		SigmaElGamalPrivateKeyProverInput input = (SigmaElGamalPrivateKeyProverInput) in;
		
		//Create an input object to the underlying sigma dlog prover.
		SigmaDlogProverInput underlyingInput = new SigmaDlogProverInput(input.getPublicKey().getH(), input.getPrivateKey().getX());
		sigmaDlog.setInput(underlyingInput);
		
	}

	/**
	 * Samples random value r in Zq.
	 */
	public void sampleRandomValues() {
		//Delegates to the underlying Sigma Dlog prover.
		sigmaDlog.sampleRandomValues();
	}

	/**
	 * Computes the first message of the protocol.
	 * @return the computed message
	 */
	public SigmaProtocolMsg computeFirstMsg() {
		//Delegates the computation to the underlying Sigma Dlog prover.
		return sigmaDlog.computeFirstMsg();
	}

	/**
	 * Computes the second message of the protocol.
	 * @param challenge
	 * @return the computed message.
	 * @throws CheatAttemptException if the received challenge's length is not equal to the soundness parameter.
	 */
	public SigmaProtocolMsg computeSecondMsg(byte[] challenge) throws CheatAttemptException {
		//Delegates the computation to the underlying Sigma Dlog prover.
		return sigmaDlog.computeSecondMsg(challenge);
		
	}
	
	/**
	 * Returns the simulator that matches this sigma protocol prover.
	 * @return SigmaDlogSimulator
	 */
	public SigmaSimulator getSimulator(){
		return new SigmaElGamalPrivateKeySimulator(sigmaDlog.getSimulator());
	}

}
