package com.indusnet.service;

import com.indusnet.model.common.OtpRequestModel;
import com.indusnet.model.common.OtpResponse;
import com.indusnet.model.common.ValidationResponce;

/**
 * This interface to take all business logic method with no body
 */
public interface IOtpService {
	public OtpResponse createOtp(OtpRequestModel otpModel);

	public ValidationResponce validatedOtp(OtpRequestModel otpValidationModel);
}
