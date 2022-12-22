package com.indusnet.model;

import java.math.BigInteger;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.indusnet.model.common.ProcessName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * This class for otp model to create otp table
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "otp_table")
public class OtpModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private String otpId;
	@Column(nullable = false)
	private BigInteger countryCode;
	@Column(unique = true, nullable = false)
	private BigInteger mobileNumber;
	private ProcessName processName;
	private String status;
	@Column(nullable = false)
	private String createdBy;
	@Column(nullable = false)
	private Timestamp createdOn;
}
