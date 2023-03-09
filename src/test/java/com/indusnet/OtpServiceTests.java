package com.indusnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import com.indusnet.model.common.Message;
import com.indusnet.model.common.OtpRequestModel;
import com.indusnet.model.common.OtpResponse;
import com.indusnet.model.common.ValidationResponse;
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

	@Autowired
	Message msg;

	@BeforeEach
	void setUp() {
		this.iOtpService = new OtpServiceImpl(otpRepository, util,msg);
	}

	@Test
	@Order(1)
	void otpGenerateTest() {

		OtpRequestModel otpModel = setGenerate();
		OtpResponse otpResponse = iOtpService.createOtp(otpModel);

		assertEquals(HttpStatus.OK.value(), otpResponse.getStatus());
	}

	@Test
	@Order(2)
	void otpValidateTest() {

		OtpRequestModel otpModel = setGenerate();
		OtpResponse otpResponse = iOtpService.createOtp(otpModel);

		OtpRequestModel otpModel2 = setValidate(otpResponse);
		ValidationResponse vaResponce =iOtpService.validatedOtp(otpModel2);

		assertEquals(HttpStatus.OK.value(), vaResponce.getStatus());
	}

	public OtpRequestModel setGenerate() {
		return OtpRequestModel.builder()
				.channel("app")
				.digit("6")
				.duration("180")
				.mobile("6070223315")
				.operationType("generate")
				.startTime("2022-12-18 12:43:47")
				.countryCode("91")
				.processName("PAYMENTS")
				.build();
	}

	public OtpRequestModel setValidate(OtpResponse otpResponse) {
		return OtpRequestModel.builder()
				.channel("app")
				.digit("6")
				.generatedOtp(otpResponse.getOtp())
				.duration("180")
				.mobile("6070223315")
				.operationType("validate")
				.startTime("2022-12-18 12:43:47")
				.countryCode("91")
				.processName("PAYMENTS")
				.otpId(otpResponse.getOtpId())
				.build();
	}
}
