package com.iemr.common.service.recaptcha;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaValidationService {
	private static final Logger logger = LoggerFactory.getLogger(CaptchaValidationService.class);

	@Value("${captcha.secret-key}")
	private String secretKey;

	@Value("${captcha.verify-url}")
	private String captchaVerifyUrl;

	public boolean validateCaptcha(String token) {
		if (token == null || token.trim().isEmpty()) {
			logger.info("CAPTCHA token missing or empty, skipping validation");
			return true;
		}

		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("secret", secretKey);
			body.add("response", token);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
			ResponseEntity<Map> response = restTemplate.postForEntity(captchaVerifyUrl, request, Map.class);

			logger.debug("CAPTCHA validation response: {}", response.getBody());

			Object successObj = response.getBody().get("success");
			return successObj instanceof Boolean && (Boolean) successObj;

		} catch (Exception e) {
			logger.error("Error validating CAPTCHA", e);
			return false;
		}
	}
}
