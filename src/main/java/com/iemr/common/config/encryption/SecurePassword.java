/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.config.encryption;

import java.math.BigInteger;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;

@Service
public class SecurePassword {
	public String generateStrongPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = 1001;
		char[] chars = password.toCharArray();
		byte[] salt = getSalt();

		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 512);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return iterations + ":" + toHex(salt) + ":" + toHex(hash);
	}

	private byte[] getSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	private String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = array.length * 2 - hex.length();
		if (paddingLength > 0) {
			return String.format(new StringBuilder().append("%0").append(paddingLength).append("d").toString(),
					new Object[] { Integer.valueOf(0) }) + hex;
		}
		return hex;
	}

	public int validatePassword(String originalPassword, String storedPassword)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		int validCount = 0;
		String[] parts = storedPassword.split(":");
		int iterations = Integer.parseInt(parts[0]);
		byte[] salt = fromHex(parts[1]);
		byte[] hash = fromHex(parts[2]);
		if (iterations == 1000) {
			PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, 1000, hash.length * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] testHash = skf.generateSecret(spec).getEncoded();
			int diff = hash.length ^ testHash.length;
			for (int i = 0; (i < hash.length) && (i < testHash.length); i++) {
				diff |= hash[i] ^ testHash[i];
			}
			if (diff == 0) {
				// return 1 if using SHA1 algorithm to execute save and login Operation
				validCount = 1;
				return validCount;
			} else {
				PBEKeySpec spec1 = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
				SecretKeyFactory skf1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
				byte[] testHash1 = skf1.generateSecret(spec1).getEncoded();

				int diff1 = hash.length ^ testHash1.length;
				for (int i = 0; (i < hash.length) && (i < testHash1.length); i++) {
					diff1 |= hash[i] ^ testHash1[i];
				}
				if (diff1 == 0) {
					// return 2 if using SHA512 algorithm to execute login Operation
					validCount = 2;
					return validCount;
				} else {
					// return 0 if wrong password
					validCount = 0;
					return validCount;
				}
			}
		}
		if (iterations == 1001) {

			PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			byte[] testHash = skf.generateSecret(spec).getEncoded();

			int diff = hash.length ^ testHash.length;
			for (int i = 0; (i < hash.length) && (i < testHash.length); i++) {
				diff |= hash[i] ^ testHash[i];
			}
			if (diff == 0) {
				// return 3 if using SHA512 algorithm to execute login Operation
				validCount = 3;
				return validCount;
			} else {
				validCount = 0;
				return validCount;
			}
		}
		return validCount;
	}

	public boolean validatePasswordExisting(String originalPassword, String storedPassword)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		String[] parts = storedPassword.split(":");
		int iterations = Integer.parseInt(parts[0]);
		byte[] salt = fromHex(parts[1]);
		byte[] hash = fromHex(parts[2]);

		PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] testHash = skf.generateSecret(spec).getEncoded();

		int diff = hash.length ^ testHash.length;
		for (int i = 0; (i < hash.length) && (i < testHash.length); i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}

	private byte[] fromHex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = ((byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16));
		}
		return bytes;
	}
}
