package com.indusnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import com.indusnet.model.common.OtpRequestModel;
import com.indusnet.model.common.OtpResponse;
import com.indusnet.model.common.ValidationResponce;
import com.indusnet.repository.IOtpRepository;
import com.indusnet.service.IOtpService;
import com.indusnet.service.impl.OtpServiceImpl;
import com.indusnet.util.Util;

@SpringBootTest
class OtpServiceTests {
	@Autowired
	IOtpService iOtpService;
//	@MockBean
	@Autowired
	IOtpRepository otpRepository;
	@Autowired
	Util util;

	@BeforeEach
	void setUp() {
		this.iOtpService = new OtpServiceImpl(otpRepository, util);
	}

	@Test
	@Order(1)
	void otpGenerateTest() {
		OtpRequestModel otpModel = OtpRequestModel.builder().channel("app").digit(6).duration(180).mobile("6070223318")
				.operationType("generate").startTime("2022-12-18 12:43:47").build();
		OtpResponse otpResponse = iOtpService.createOtp(otpModel);
		assertEquals(HttpStatus.OK.value(), otpResponse.getStatus());
	}

	@Test
	@Order(2)
	void otpValidateTest() {
		OtpRequestModel otpModel = OtpRequestModel.builder().channel("app").digit(6).duration(180).mobile("6070223318")
				.operationType("generate").startTime("2022-12-18 12:43:47").build();
		OtpResponse otpResponse = iOtpService.createOtp(otpModel);
		OtpRequestModel otpModel2 = OtpRequestModel.builder().channel("app").digit(6).generatedOtp(otpResponse.getOtp())
				.duration(180).mobile("6070223318").operationType("validate").startTime("2022-12-18 12:43:47").build();
		ValidationResponce vaResponce = iOtpService.validatedOtp(otpModel2);
		assertEquals(HttpStatus.OK.value(), vaResponce.getStatus());
	}
}
