package edu.biu.scapi.primitives.kdf;

import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.biu.scapi.exceptions.FactoriesException;
import edu.biu.scapi.exceptions.UnInitializedException;
import edu.biu.scapi.generals.Logging;
import edu.biu.scapi.primitives.prf.Hmac;
import edu.biu.scapi.tools.Factories.PrfFactory;


/** 
 * Concrete class of key derivation function for HKDF.
 * This is a key derivation function that has a rigorous justification as to its security.
 * 
  * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Meital Levy)
 */
public final class HKDF implements KeyDerivationFunction {
	
	private Hmac hmac; // the underlying hmac

	/**
	 * Constructor that accepts a name of hmac and creates the HKDF object with it.
	 * @param hmac the underlying object
	 * @throws FactoriesException if this object is not initialized
	 */
	public HKDF(String hmac) throws FactoriesException{
		//creates the hmac by the prf factory
		this.hmac = (Hmac) PrfFactory.getInstance().getObject(hmac);
	}
	
	/**
	 * Constructor that accepts an HMAC to be the underlying object.
	 * The given Hmac MUST be initialized.
	 * @param hmac the underlying hmac. 
	 * @throws UnInitializedException if the given Hmac is not initialized
	 */
	public HKDF(Hmac hmac) throws UnInitializedException{
		
		//first checks that the hmac is initialized.
		if(hmac.isInitialized()){
			this.hmac = hmac;
		}
		else{//the user must pass an initialized object, otherwise throw an exception
			throw new UnInitializedException("The input variable must be initialized");
		}
			
	}
	
	
	public void init(SecretKey secretKey) throws InvalidKeyException {
		//init the underlying hmac
		hmac.init(secretKey);
		
	}

	public void init(SecretKey secretKey, AlgorithmParameterSpec params) throws InvalidKeyException {
		// there are no params. ignore the params and sends to the other init function
		init(secretKey);
		
	}

	public boolean isInitialized() {

		//if the hmac is initialized than the HKDF is initialized as well.
		return hmac.isInitialized(); 
	}

	/**
	 * generates a new key from the source key material key.
	 * The pseudocode of this function is as follows:
	 * 
	 *   COMPUTE PRK = HMAC(XTS, SKM) [key=XTS, data=SKM]
	 *   Let t be the smallest number so that t * |H|>L where |H| is the HMAC output length
	 *   K(1) = HMAC(PRK,(CTXinfo,1)) [key=PRK, data=(CTXinfo,1)]
	 *   FOR i = 2 TO t
	 *     K(i) = HMAC(PRK,(K(i-1),CTXinfo,i)) [key=PRK, data=(K(i-1),CTXinfo,i)]
	 *   OUTPUT the first L bits of K(1),�,K(t)
	 *   
	 *   @param iv - CTXInfo 
	 * @throws UnInitializedException 
	 * 
	 */
	public SecretKey generateKey(SecretKey seedForGeneration, int outLen, byte[] iv) throws UnInitializedException {
		if (!isInitialized()){
			throw new UnInitializedException();
		}
		
		int hmacLength = hmac.getBlockSize();                           //the size of the output of the hmac.
		byte[] inBytes = seedForGeneration.getEncoded();                              //gets the input key to work on
		byte[] outBytes = new byte[outLen];                             //the output key
		byte[] roundKey = new byte[hmacLength];							//PRK from the pseudocode
		byte[] intermediateOutBytes = new byte[hmacLength];             //round result K(i) in the pseudocode
		
		
		//first computes the new key. The new key is the result of computing the hmac function.
		try {
			//roundKey is now K(0)
			hmac.computeBlock(inBytes, 0, inBytes.length, roundKey, 0);
		} catch (IllegalBlockSizeException e) {//should not happen since the roundKey is of the right size.
			
			Logging.getLogger().log(Level.WARNING, e.toString());
		}
		
		
		//init the hmac with the new key. From now on this is the key for all the rounds.
		try {
			hmac.init(new SecretKeySpec(roundKey, "HKDF"));
		} catch (InvalidKeyException e) {
			//shoudln't happen since the key is the output of compute block and its length is ok
			Logging.getLogger().log(Level.WARNING, e.toString());
		}
		
		//calculates the first round
		//K(1) = HMAC(PRK,(CTXinfo,1)) [key=PRK, data=(CTXinfo,1)]
		firstRound(outBytes, iv, intermediateOutBytes, hmacLength);
		
		//calculates the next rounds
		//FOR i = 2 TO t
		//K(i) = HMAC(PRK,(K(i-1),CTXinfo,i)) [key=PRK, data=(K(i-1),CTXinfo,i)]
		nextRounds(outLen, iv, hmacLength, outBytes, 
				intermediateOutBytes);
		
		//creates the secret key from the generated bytes
		return new SecretKeySpec(outBytes, "HKDF");
	}

	/**
	 * Does round 2 to t of HKDF algorithm. The pseudo code:
	 * FOR i = 2 TO t
	 * K(i) = HMAC(PRK,(K(i-1),CTXinfo,i)) [key=PRK, data=(K(i-1),CTXinfo,i)]
	 * @param outLen the required output key length
	 * @param iv the iv : ctxInfo
	 * @param hmacLength the size of the output of the hmac.
	 * @param outBytes the result of the overall computation
	 * @param intermediateOutBytes round result K(i) in the pseudocode
	 */
	private void nextRounds(int outLen, byte[] iv, int hmacLength,
			byte[] outBytes, byte[] intermediateOutBytes) {
		
		int rounds = (int) Math.ceil((float)outLen/(float)hmacLength); //the smallest number so that  hmacLength * rounds >= outLen
		
		int currentInBytesSize;	//the size of the CTXInfo and also the round;
		
		if(iv!=null)
			currentInBytesSize = hmacLength + iv.length + 1;//the size of the CTXInfo and also the round;
		else//no CTXInfo
			currentInBytesSize = hmacLength + 1;//the size without the CTXInfo and also the round;
		
		//the result of the current computation
		byte[] currentInBytes = new byte[currentInBytesSize];
		
		
		Integer roundIndex;
		//for rounds 2 to t 
		if(iv!=null)
			//in case we have an iv. puts it (ctxInfo after the K from the previous round at position hmacLength).
			System.arraycopy(iv, 0, currentInBytes, hmacLength , iv.length);
				
		for(int i=2;i<=rounds; i++){
			
			roundIndex = new Integer(i); //creates the round integer for the data
			
			//copies the output of the last results
			System.arraycopy(intermediateOutBytes, 0, currentInBytes, 0, hmacLength);
				
			//copies the round integer to the data array
			currentInBytes[currentInBytesSize - 1] = roundIndex.byteValue();
			
			
			//operates the hmac to get the round output 
			try {
				hmac.computeBlock(currentInBytes, 0, currentInBytes.length, intermediateOutBytes, 0);
			} catch (IllegalBlockSizeException e) {
				// souldn't happen since the offsets and length are within the arrays
				Logging.getLogger().log(Level.WARNING, e.toString());
			} catch (UnInitializedException e) {
				// souldn't happen since the underlying object is initialized
				Logging.getLogger().log(Level.WARNING, e.toString());
			}
			
			if(i==rounds){//We fill the rest of the array with a portion of the last result.
				
				//copies the results to the output array
				System.arraycopy(intermediateOutBytes, 0,outBytes , hmacLength*(i-1), outLen - hmacLength*(i-1));
			}
			else{
				//copies the results to the output array
				System.arraycopy(intermediateOutBytes, 0,outBytes , hmacLength*(i-1), hmacLength);
			}				
		}
	}

	/**
	 * First round of HKDF algorithm. The pseudo code: 
	 * K(1) = HMAC(PRK,(CTXinfo,1)) [key=PRK, data=(CTXinfo,1)]
	 * @param iv ctxInfo
	 * @param intermediateOutBytes round result K(1) in the pseudocode
	 * @param hmacLength the size of the output of the hmac.
	 * @param outBytes the result of the overall computation
	 */
	private void firstRound(byte [] outBytes, byte[] iv, byte[] intermediateOutBytes, int hmacLength)  {
		Integer one;
		//round 1
		byte[] firstRoundInput;//data for the creating K(1)
		if(iv!=null)
			firstRoundInput = new  byte[iv.length + 1];
		else
			firstRoundInput = new  byte[1];
		
		//copies the CTXInfo - iv
		if(iv!=null)
			System.arraycopy(iv, 0, firstRoundInput,0 , iv.length);
		
		one = new Integer(1);//creates the round integer for the data
			
		//copies the integer with zero to the data array
		firstRoundInput[firstRoundInput.length - 1] = one.byteValue();
		
			
		//first computes the new key. The new key is the result of computing the hmac function.
		try {
			//calculate K(1) and put it in intermediateOutBytes.
			hmac.computeBlock(firstRoundInput, 0, firstRoundInput.length, intermediateOutBytes, 0);
		} catch (IllegalBlockSizeException e) {	
			// souldn't happen since the offsets and length are within the arrays
			Logging.getLogger().log(Level.WARNING, e.toString());
		} catch (UnInitializedException e) {
			// souldn't happen since the underlying object is initialized
			Logging.getLogger().log(Level.WARNING, e.toString());
		}
		
		//copies the results to the output array
		System.arraycopy(intermediateOutBytes, 0,outBytes , 0, hmacLength);
	}

	public SecretKey generateKey(SecretKey seedForGeneration, int outLen) throws UnInitializedException {
		//there is no auxiliary information, sends an empty iv.
		return generateKey(seedForGeneration, outLen, null);
	}

	public void generateKey(byte[] seedForGeneration, int inOff, int inLen, byte[] outKey,
			int outOff, int outLen) throws UnInitializedException {
		//there is no auxiliary information, sends an empty iv.
		generateKey(seedForGeneration, inOff, inLen, outKey, outOff, outLen, null);
		
	}

	public void generateKey(byte[] seedForGeneration, int inOff, int inLen, byte[] outKey,
			int outOff, int outLen, byte[] iv) throws UnInitializedException {
		//checks that the object is initialized
		if (!isInitialized()){
			throw new UnInitializedException();
		}
		
		//checks that the offset and length are correct
		if ((inOff > seedForGeneration.length) || (inOff+inLen > seedForGeneration.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given input buffer");
		}
		if ((outOff > outKey.length) || (outOff+outLen > outKey.length)){
			throw new ArrayIndexOutOfBoundsException("wrong offset for the given output buffer");
		}
		
		//creates a SecretKey object out of the byte array key.
		SecretKey secretKey = new SecretKeySpec(seedForGeneration, inOff, inLen, "");
		
		//calls the generate key with the created key and get the generated key bytes
		byte[] output = generateKey(secretKey, outLen, iv).getEncoded();
		
		//copies the gwnrated key bytes to outKey byte array
		System.arraycopy(output, 0, outKey, outOff, outLen);
		
	}

}