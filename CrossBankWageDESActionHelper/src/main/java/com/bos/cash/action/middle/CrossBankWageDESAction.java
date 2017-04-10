package com.bos.cash.action.middle;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossBankWageDESAction {
	private final static String encoding = "UTF-8";
	public static final String KEY_ALGORITHM = "DES";
	public static final String CIPHER_ALGORITHM = "DES/ECB/NoPadding";
	private static final Logger logger = LoggerFactory.getLogger(CrossBankWageDESAction.class);
	//public static final byte[] key = new byte[] { (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36,
		//(byte) 0x37, (byte) 0x38 };
	// 测试
	public static void main(String[] a) {
		// 待加密内容
		String bizRecAccount = "31663803002789228";
		String amt = "0.10";
		byte[] result;
		byte[] result2;
		byte[] key = "shanghai".getBytes();
		while(true){
			if(bizRecAccount.length() % 8 == 0){
				break;
			} else {
				bizRecAccount += " ";
			}
		}
		while(true){
			if(amt.length() % 8 == 0){
				break;
			} else {
				amt = "0" + amt;
			}
		}
		try {
			result = encrypt(bizRecAccount, key);
			result2 = encrypt(amt, key);
			String test = byte2Hex(result);
			String test2 = byte2Hex(result2);
			System.out.println("加密后账号：" + test);
			System.out.println("加密后金额：" + test2);
			// 直接将如上内容解密
			byte[] decryResult = decrypt((test), key);
			byte[] decryResult2 = decrypt((test2), key);
			System.out.println("解密后账号：" + new String(decryResult));
			System.out.println("解密后金额：" + new String(decryResult2));
		} catch (Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("异常: {}", e.getMessage());
			}
		}
	}

	/**
	 * 加密
	 * 
	 * @param datasource
	 *            byte[]
	 * @param password
	 *            String
	 * @return byte[]
	 */
	public static byte[] encrypt(String str, byte[] password) {
		try {
			byte[] datasource=str.getBytes(encoding);
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(password);
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			// 现在，获取数据并加密
			// 正式执行加密操作
			byte[] b1 = cipher.doFinal(datasource);
			return b1;
		} catch (Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("异常: {}", e.getMessage());
			}
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param src
	 *            byte[]
	 * @param password
	 *            String
	 * @return byte[]
	 */
	public static byte[] decrypt(String str, byte[] password) {
		try {
			byte[]src = hex2Byte(str);
			// DES算法要求有一个可信任的随机数源
			SecureRandom random = new SecureRandom();
			// 创建一个DESKeySpec对象
			DESKeySpec desKey = new DESKeySpec(password);
			// 创建一个密匙工厂
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
			// 将DESKeySpec对象转换成SecretKey对象
			SecretKey securekey = keyFactory.generateSecret(desKey);
			// Cipher对象实际完成解密操作
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			// 真正开始解密操作
			return cipher.doFinal(src);
		} catch (Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("异常: {}", e.getMessage());
			}
		}
		return null;
	}

	/**
	 * 
	 * 
	 * Title: byte2Hex
	 * </p>
	 * Date: Mar 30, 2015 2:36:18 PM
	 * </p>
	 * Description: 
	 * </p>
	 * 
	 * @param
	 * @param b
	 * @return
	 *         </p>
	 *         throws
	 */
	public static String byte2Hex(byte[] b) {
		StringBuffer hs = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs.append("0").append(stmp);
			} else {
				hs.append(stmp);
			}
		}
		return hs.toString().toUpperCase();
	}

	/**
	 * 
	 * 
	 * Title: hex2Byte
	 * </p>
	 * Date: Mar 30, 2015 2:36:26 PM
	 * </p>
	 * Description: 
	 * </p>
	 * 
	 * @param
	 * @param str
	 * @return
	 *         </p>
	 */
	public static byte[] hex2Byte(String str) {
		if (str == null)
			return null;
		str = str.trim();
		int len = str.length();
		if (len == 0 || len % 2 == 1)
			return null;
		byte[] b = new byte[len / 2];
		try {
			for (int i = 0; i < str.length(); i += 2) {
				b[i / 2] = (byte) Integer
						.decode("0x" + str.substring(i, i + 2)).intValue();
				//System.out.print(b[i / 2]);
			}
			//System.out.println();
			return b;
		} catch (Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("异常: {}", e.getMessage());
			}
			return null;
		}
	}
}