package com.indusnet.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.indusnet.exception.ValidationException;
import com.indusnet.model.OtpModel;
import com.indusnet.model.common.GenerateCount;
import com.indusnet.model.common.LocalDateTimeConverter;
import com.indusnet.model.common.LoggingResponseMessage;
import com.indusnet.model.common.Message;
import com.indusnet.model.common.MessageTypeConst;
import com.indusnet.model.common.OtpRequestModel;
import com.indusnet.model.common.OtpResponse;
import com.indusnet.model.common.ProcessName;
import com.indusnet.model.common.ValidationResponse;
import com.indusnet.repository.IOtpRepository;
import com.indusnet.service.IOtpService;
import com.indusnet.util.Util;
import lombok.extern.slf4j.Slf4j;

//  This is service layer to hold all business logic
@Service
@Slf4j
public class OtpServiceImpl implements IOtpService {

	// Initialize the repository for database connection
	@Autowired
	IOtpRepository otpRepository;

	// Initialize the Util class for generate otp
	@Autowired
	Util util;

	List<GenerateCount> generateList = new ArrayList<>();
	OtpModel existedOtpModel = new OtpModel();

	@Autowired
	Message msg ;

	GsonBuilder builder = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter()); 
	Gson gson = builder.create();

	public OtpServiceImpl(IOtpRepository otpRepository, Util util,Message msg) {
		super();
		this.otpRepository = otpRepository;
		this.util = util;
		this.msg = msg;
	}

	// This method to create otp
	@Override
	public OtpResponse createOtp(OtpRequestModel otpRequestModel) {

		LoggingResponseMessage generateMessage1 = LoggingResponseMessage.builder()
				.message("Start createOtp method.")
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(generateMessage1));

		// This is checking for operation type
		if (!otpRequestModel.getOperationType().equalsIgnoreCase("Generate")) {

			LoggingResponseMessage logMessageOperation = LoggingResponseMessage.builder()
					.message(msg.getOperationTypeMessage())
					.data(otpRequestModel.getOperationType())
					.messageTypeId(MessageTypeConst.ERROR)
					.build();

			log.error(gson.toJson(logMessageOperation));

			throw new ValidationException(msg.getOperationTypeMessage());

		}

		// This is date time formater for convert date of string to date time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = null;

		try {

			dateTime = LocalDateTime.parse(otpRequestModel.getStartTime(), formatter);

		} catch (Exception e) {

			LoggingResponseMessage logMessageDate = LoggingResponseMessage.builder()
					.message(msg.getDateFormateMessage())
					.data(dateTime)
					.messageTypeId(MessageTypeConst.ERROR)
					.build();

			log.error(gson.toJson(logMessageDate));

			throw new ValidationException(msg.getDateFormateMessage());
		}

		LoggingResponseMessage generateMessage2 = LoggingResponseMessage.builder()
				.message("Date time successfully converted.")
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(generateMessage2));

		String processName = ProcessName.valueOf(otpRequestModel.getProcessName().toUpperCase()).name();

		List<OtpModel> otpModelExisted2 = otpRepository.getOtpModel(LocalDateTime.now().minusMinutes(10),
				otpRequestModel.getMobile(), "GENERATED", processName);

		LoggingResponseMessage logMessage1 = LoggingResponseMessage.builder()
				.message("OTP model list is fetch successfully.")
				.data(otpModelExisted2)
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(logMessage1));

		List<GenerateCount> gen = new ArrayList<>();
		if(!otpModelExisted2.isEmpty() && !generateList.isEmpty()) {

			existedOtpModel = otpModelExisted2.get(otpModelExisted2.size()-1);
			log.info(gson.toJson(generateList));

			gen = generateList.stream().filter(x-> x.getOtpId().equals(existedOtpModel.getOtpId()) && x.getDate().plusMinutes(10).isAfter(LocalDateTime.now())).toList();

			log.info(gson.toJson(gen));

			CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(()->{
				List<GenerateCount> gen2 = generateList.stream().filter(x-> x.getDate().plusMinutes(10).isBefore(LocalDateTime.now())).toList();
				if(!gen2.isEmpty()) {
					generateList.removeAll(gen2);
				}
			});
		}
		if ((!gen.isEmpty() && gen.get(gen.size()-1).getDate().plusMinutes(10).isAfter(LocalDateTime.now())) || (otpModelExisted2.size() >= 3 && otpModelExisted2.get(otpModelExisted2.size()-1).getCreatedOn().plusMinutes(10).isAfter(LocalDateTime.now()))) {
			LoggingResponseMessage logMessageEx = LoggingResponseMessage.builder()
					.message(msg.getLimitExceedMessage())
					.data(otpModelExisted2.get(otpModelExisted2.size()-1))
					.messageTypeId(MessageTypeConst.ERROR)
					.build();

			log.error(gson.toJson(logMessageEx));
			log.info(gson.toJson(gen));

			if(gen.isEmpty()) {
				log.info(gson.toJson(gen));
				generateList.add(new GenerateCount(otpModelExisted2.get(otpModelExisted2.size()-1).getOtpId(), otpModelExisted2.get(otpModelExisted2.size()-1).getCreatedOn()));
				log.info(gson.toJson(generateList));
			}
			throw new ValidationException(msg.getLimitExceedMessage());
		}

		Timestamp timeStamp = Timestamp.valueOf(dateTime);

		LoggingResponseMessage logMessageTime = LoggingResponseMessage.builder()
				.message("Timestamp generated successfully.")
				.data(timeStamp.toString())
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(logMessageTime));

		byte[] channel = otpRequestModel.getChannel().getBytes();

		String channelKey = "";

		for (int i = 0; i < channel.length; i++) {

			channelKey = channelKey.concat("" + channel[i]);

		}

		log.info(gson.toJson(channelKey));

		byte[] proceName = otpRequestModel.getProcessName().getBytes();
		String proceNameKey = "";

		for (int i = 0; i < proceName.length; i++) {
			proceNameKey = proceNameKey.concat("" + proceName[i]);
		}

		// This is secret key for otp generation
		String secret = "" + timeStamp.toInstant().toEpochMilli() + otpRequestModel.getMobile() + channelKey
				+ proceNameKey;

		LoggingResponseMessage logMessageSecretKey = LoggingResponseMessage.builder()
				.message("Secret key generated successfully.")
				.data(secret)
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(logMessageSecretKey));

		// This OTP is generated by Util class with help of HmacSHA256
		String otp = Util.generateTOTP256(secret, Integer.parseInt(otpRequestModel.getDuration()),
				otpRequestModel.getDigit());

		LoggingResponseMessage logMessage2 = LoggingResponseMessage.builder()
				.message("OTP is created successfully.")
				.data(otp)
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(logMessage2));

		// This is otpId and created with UUID
		String otpId = UUID.randomUUID().toString().substring(0, 8);
		log.info(gson.toJson(otpId));

		OtpModel newModel = OtpModel.builder()
				.countryCode(Long.parseLong(otpRequestModel.getCountryCode()))
				.mobileNumber(otpRequestModel.getMobile())
				.createdBy(msg.getCreatedby())
				.createdOn(LocalDateTime.now())
				.otpId(otpId)
				.processName(ProcessName.valueOf(otpRequestModel.getProcessName().toUpperCase()).name())
				.status(msg.getGenerated())
				.build();

		OtpModel saveModel = otpRepository.save(newModel);

		LoggingResponseMessage logMessage3 = LoggingResponseMessage.builder()
				.message("OTP model is save successfully.")
				.data(saveModel)
				.messageTypeId(MessageTypeConst.SUCCESS)
				.build();

		log.info(gson.toJson(logMessage3));

		return new OtpResponse(HttpStatus.OK.value(), msg.getGeneratedMessage(), otp,
				otpRequestModel.getDuration(), otpId);
	}


	// This method to validate otp 
	@Override
	public ValidationResponse validatedOtp(OtpRequestModel otpValidationModel) {

		LoggingResponseMessage logMessageV1 = LoggingResponseMessage.builder()
				.message("OTP validation method start.")
				.messageTypeId(MessageTypeConst.SUCCESS)
				.data(otpValidationModel)
				.build();

		log.info(gson.toJson(logMessageV1));

		// This is checking for operation type
		if (!otpValidationModel.getOperationType().equalsIgnoreCase("Validate")) {

			LoggingResponseMessage logMessage3 = LoggingResponseMessage.builder()
					.message(msg.getOperationTypeMessage())
					.messageTypeId(MessageTypeConst.ERROR)
					.data(otpValidationModel.getOperationType())
					.build();

			log.error(gson.toJson(logMessage3));

			throw new ValidationException(msg.getOperationTypeMessage());
		}

		ProcessName.valueOf(otpValidationModel.getProcessName().toUpperCase()).name();

		// This is date time formater for convert date of string to date time
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = null;

		try {

			dateTime = LocalDateTime.parse(otpValidationModel.getStartTime(), formatter);

		} catch (Exception e) {

			LoggingResponseMessage logMessage2 = LoggingResponseMessage.builder()
					.message(msg.getDateFormateMessage())
					.messageTypeId(MessageTypeConst.ERROR)
					.data(dateTime)
					.build();

			log.error(gson.toJson(logMessage2));
			throw new ValidationException(msg.getDateFormateMessage());

		}

		Timestamp timeStamp = Timestamp.valueOf(dateTime);

		LoggingResponseMessage logMessageV2 = LoggingResponseMessage.builder()
				.message("Create timestamp is successfully.")
				.messageTypeId(MessageTypeConst.SUCCESS)
				.data(timeStamp.toString())
				.build();

		log.info(gson.toJson(logMessageV2));

		byte[] channel = otpValidationModel.getChannel().getBytes();
		String channelKey = "";

		for (int i = 0; i < channel.length; i++) {
			channelKey = channelKey.concat("" + channel[i]);
		}

		byte[] proceName = otpValidationModel.getProcessName().getBytes();
		String proceNameKey = "";

		for (int i = 0; i < proceName.length; i++) {
			proceNameKey = proceNameKey.concat("" + proceName[i]);
		}

		String secret = "" + timeStamp.toInstant().toEpochMilli() + otpValidationModel.getMobile() + channelKey
				+ proceNameKey;

		LoggingResponseMessage logMessageV3 = LoggingResponseMessage.builder()
				.message("Secret key created is successfully.")
				.messageTypeId(MessageTypeConst.SUCCESS)
				.data(secret)
				.build();

		log.info(gson.toJson(logMessageV3));

		// This OTP is regenerated by Util class with help of HmacSHA256 for otp
		// validation
		String otp = Util.generateTOTP256(secret, Integer.parseInt(otpValidationModel.getDuration()),
				otpValidationModel.getDigit());

		List<OtpModel> generateddList = otpRepository.findByOtpIdAndStatus(otpValidationModel.getOtpId(), "GENERATED");

		if (generateddList.isEmpty()) {

			LoggingResponseMessage logMessage1 = LoggingResponseMessage.builder()
					.message(msg.getNotGeneratedMessage())
					.messageTypeId(MessageTypeConst.ERROR)
					.data(generateddList)
					.build();

			log.error(gson.toJson(logMessage1));
			throw new ValidationException(msg.getNotGeneratedMessage());
		}

		OtpModel otpModelData = generateddList.get(generateddList.size()-1);

		LoggingResponseMessage logMessage1 = LoggingResponseMessage.builder()
				.message("Get Otp model successfully")
				.messageTypeId(MessageTypeConst.SUCCESS)
				.data(otpModelData)
				.build();

		log.info(gson.toJson(logMessage1));
		ValidationResponse response = null;

		if (otpModelData.getCreatedOn().plusSeconds(180).isAfter(LocalDateTime.now())
				&& otp.equals(otpValidationModel.getGeneratedOtp())) {

			List<OtpModel> validList = otpRepository.findByOtpIdAndStatus(otpValidationModel.getOtpId(), "VERIFIED");

			if (!validList.isEmpty()) {

				LoggingResponseMessage logMessageV4 = LoggingResponseMessage.builder()
						.message(msg.getVerfiedBeforeMsg())
						.messageTypeId(MessageTypeConst.ERROR)
						.data(validList)
						.build();

				log.error(gson.toJson(logMessageV4));
				throw new ValidationException(msg.getVerfiedBeforeMsg());
			}

			OtpModel newModel = OtpModel.builder()
					.countryCode(Long.parseLong(otpValidationModel.getCountryCode()))
					.mobileNumber(otpValidationModel.getMobile())
					.createdBy(otpModelData.getCreatedBy())
					.createdOn(otpModelData.getCreatedOn())
					.otpId(otpValidationModel.getOtpId())
					.processName(otpValidationModel.getProcessName().toUpperCase())
					.status(msg.getVerified())
					.build();

			OtpModel saveModel = otpRepository.save(newModel);

			LoggingResponseMessage logMessageV4 = LoggingResponseMessage.builder()
					.message("OTP model is save with VERIFIED status.")
					.messageTypeId(MessageTypeConst.SUCCESS)
					.data(saveModel)
					.build();

			log.info(gson.toJson(logMessageV4));

			response = ValidationResponse.builder()
					.status(HttpStatus.OK.value())
					.message(msg.getValidateMesssage())
					.build();

		} 
		else if (dateTime.plusSeconds(180).isBefore(LocalDateTime.now())
				&& otp.equals(otpValidationModel.getGeneratedOtp())) {

			OtpModel newModel = OtpModel.builder()
					.countryCode(Long.parseLong(otpValidationModel.getCountryCode()))
					.mobileNumber(otpValidationModel.getMobile())
					.createdBy(otpModelData.getCreatedBy())
					.createdOn(otpModelData.getCreatedOn())
					.otpId(otpValidationModel.getOtpId())
					.processName(otpValidationModel.getProcessName().toUpperCase())
					.status(msg.getFailed())
					.build();

			OtpModel saveModel = otpRepository.save(newModel);
			LoggingResponseMessage logMessageV4 = LoggingResponseMessage.builder()
					.message("OTP model is save with FAILED status.")
					.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
					.data(saveModel)
					.build();

			log.warn(gson.toJson(logMessageV4));

			response =  ValidationResponse.builder()
					.status(HttpStatus.UNPROCESSABLE_ENTITY.value())
					.message(msg.getExpiryMessage())
					.build();
		} 
		else {

			List<OtpModel> validList1 = otpRepository.findByOtpIdAndStatus(otpValidationModel.getOtpId(), "FAILED");

			if (validList1.size() >= 3) {

				LoggingResponseMessage logMessageV4 = LoggingResponseMessage.builder()
						.message(msg.getVerifiedExceedMessage())
						.messageTypeId(MessageTypeConst.ERROR)
						.data(validList1)
						.build();

				log.warn(gson.toJson(logMessageV4));

				throw new ValidationException(msg.getVerifiedExceedMessage());
			}

			OtpModel newModel = OtpModel.builder()
					.countryCode(Long.parseLong(otpValidationModel.getCountryCode()))
					.mobileNumber(otpValidationModel.getMobile())
					.createdBy(otpModelData.getCreatedBy())
					.createdOn(otpModelData.getCreatedOn())
					.otpId(otpValidationModel.getOtpId())
					.processName(otpValidationModel.getProcessName().toUpperCase())
					.status(msg.getFailed())
					.build();

			OtpModel saveModel = otpRepository.save(newModel);
			LoggingResponseMessage logMessageV4 = LoggingResponseMessage.builder()
					.message("OTP model is save with FAILED status.")
					.messageTypeId(MessageTypeConst.INTERNAL_ERROR)
					.data(saveModel)
					.build();

			log.warn(gson.toJson(logMessageV4));

			response =  ValidationResponse.builder()
					.status(HttpStatus.UNPROCESSABLE_ENTITY.value())
					.message(msg.getInvalidMessage())
					.build();
		}
		return response;
	}

}
