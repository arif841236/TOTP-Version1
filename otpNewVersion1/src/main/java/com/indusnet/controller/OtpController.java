package com.indusnet.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.indusnet.model.common.OtpRequestModel;
import com.indusnet.model.common.OtpResponse;
import com.indusnet.model.common.ValidationResponce;
import com.indusnet.service.IOtpService;
/**
 * This is controller class to take all mapping
 */
@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {
	/**
	 * Initialize the service layer.
	 */
	@Autowired
	IOtpService otpService;
/**
 * This method to generate and validate otp.
 */
	@PostMapping
	public ResponseEntity<Object> generateOtp(@RequestBody @Valid OtpRequestModel model) {
		if (model.getOperationType().equalsIgnoreCase("Generate")) {
			OtpResponse responce = otpService.createOtp(model);
			return new ResponseEntity<>(responce, HttpStatus.OK);
		} else {
			ValidationResponce message = otpService.validatedOtp(model);
			return new ResponseEntity<>(message, HttpStatus.OK);
		}
	}
}
