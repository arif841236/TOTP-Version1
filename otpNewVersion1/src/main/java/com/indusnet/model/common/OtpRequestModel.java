package com.indusnet.model.common;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class for take input request for otp generate and validate
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OtpRequestModel {
	private String operationType;
	@Size(min = 6, max = 6, message = "please enter valid otp")
	private String generatedOtp;
	private String channel;
	private String startTime;
	@Range(min = 10, max = 180, message = "please enter valid duration")
	private Integer duration;
	@Range(min = 6, max = 6, message = "please enter six digit otp")
	private Integer digit;
	@Size(min = 10, max = 10, message = "please enter valid mobile number")
	@Pattern(regexp = "(0|91)?[6-9]\\d{9}", message = "please enter valid mobile number")
	private String mobile;

	public OtpRequestModel(String operationType, String channel, String startTime,
			@Range(min = 10, max = 180, message = "please enter valid duration") Integer duration,
			@Range(min = 6, max = 6, message = "please enter six digit otp") Integer digit,
			@Size(min = 10, max = 10, message = "please enter valid mobile number") @Pattern(regexp = "(0|91)?[6-9]\\d{9}", message = "please enter valid mobile number") String mobile) {
		super();
		this.operationType = operationType;
		this.channel = channel;
		this.startTime = startTime;
		this.duration = duration;
		this.digit = digit;
		this.mobile = mobile;
	}
}
