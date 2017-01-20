package com.tianxiapai.wxademo.interfaces;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import com.tianxiapai.wxademo.interfaces.dto.UploadResponse;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;

@RestController
@RequestMapping("/api/v1/file")
public class FileController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MinioClient minioClient;

	public FileController(MinioClient minioClient) {
		this.minioClient = minioClient;
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public UploadResponse upload(@RequestParam("file") MultipartFile file, Principal user) {
		if (logger.isDebugEnabled()) {
			logger.debug("file size: {}, contentType: {}, name: {}, originalFilename: {}.", file.getSize(),
					file.getContentType(), file.getName(), file.getOriginalFilename());
			logger.debug("user: {}.", user);
		}

		String bucketName = "txpwxademo";
		String objectName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(file.getOriginalFilename());

		try {

			minioClient.putObject(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());
			
			UploadResponse response = new UploadResponse();
			response.setObjectName(objectName);
			response.setObjectUrl(minioClient.getObjectUrl(bucketName, objectName));

			return response;
		} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
				| NoResponseException | ErrorResponseException | InternalException | InvalidArgumentException
				| IOException | XmlPullParserException e) {
			if (logger.isErrorEnabled()) {
				logger.error("putObject failed.", e);
			}
			
			throw new RuntimeException("putObject failed.", e);
		}
	}
}
