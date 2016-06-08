package com.thatsit.android.encryption.helper;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

/**
 * 
 * In this class, messages are being encrypted before sending them and then decrypted
 * at the receiver end.
 */

public class EncryptionManager {

	private static Cipher ecipher;
	private static Cipher dcipher;

	private static final int iterationCount = 10;

	// 8-byte Salt
	private static byte[] salt = {(byte)0xB2, (byte)0x12, (byte)0xD5, (byte)0xB2,
		(byte)0x44, (byte)0x21, (byte)0xC3, (byte)0xC3};
	private final String passPhrase = "My Secret Password";

/**
 *  Perform encryption while sending messages and decryption for received messages
 */

	public EncryptionManager(){
		try {
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());

			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * encrypt the outgoing payload
	 * @param message - outgoing message
	 * @return
	 */
	public String encryptPayload(String message){
		try{
			byte[] utf8 = message.getBytes("UTF8");
			byte[] enc = ecipher.doFinal(utf8);
			enc = BASE64EncoderStream.encode(enc);
			return new String(enc);
		}catch(Exception e){
			
			return message;
		}
	}
	
	/**
	 * decrypt the incoming payload
	 * @param message - incoming message
	 * @return
	 */
	public String decryptPayload(String message){
		try{
			
			byte[] dec = BASE64DecoderStream.decode(message.getBytes());
			byte[] utf8 = dcipher.doFinal(dec);
			return new String(utf8, "UTF8");
		}catch(Exception e){
			return message;
		}
	}
}
