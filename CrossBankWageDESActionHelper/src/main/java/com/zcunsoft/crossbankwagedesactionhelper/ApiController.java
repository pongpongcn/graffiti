package com.zcunsoft.crossbankwagedesactionhelper;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bos.cash.action.middle.CrossBankWageDESAction;
import com.zcunsoft.crossbankwagedesactionhelper.model.DecryptRequest;
import com.zcunsoft.crossbankwagedesactionhelper.model.DecryptResponse;
import com.zcunsoft.crossbankwagedesactionhelper.model.EncryptRequest;
import com.zcunsoft.crossbankwagedesactionhelper.model.EncryptResponse;

@RequestMapping("api")
@RestController
public class ApiController {
	@RequestMapping(value = "encrypt", method = RequestMethod.POST)
	public EncryptResponse encrypt(@Valid @RequestBody EncryptRequest request) {
		String adjustedData = request.getData();
		char padding = request.getPadding().charAt(0);
		while (true) {
			if (adjustedData.length() % request.getKey().length() == 0) {
				break;
			} else {
				if (request.isPaddingLeft()) {
					adjustedData = padding + adjustedData;
				} else {
					adjustedData += padding;
				}
			}
		}

		byte[] key = request.getKey().getBytes();
		byte[] result = CrossBankWageDESAction.encrypt(adjustedData, key);
		String encryptedData = CrossBankWageDESAction.byte2Hex(result);

		EncryptResponse response = new EncryptResponse();
		response.setAdjustedData(adjustedData);
		response.setEncryptedData(encryptedData);

		return response;
	}
	
	@RequestMapping(value = "decrypt", method = RequestMethod.POST)
	public DecryptResponse decrypt(@Valid @RequestBody DecryptRequest request) {
		byte[] key = request.getKey().getBytes();
		byte[] decryResult = CrossBankWageDESAction.decrypt(request.getEncryptedData(), key);
		String data = new String(decryResult);
		
		DecryptResponse response = new DecryptResponse();
		response.setData(data);
		
		return response;
	}
}