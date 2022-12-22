package com.indusnet.repository;

import java.math.BigInteger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.indusnet.model.OtpModel;
/**
 * This interface to connect with database
 */
@Repository
public interface IOtpRepository extends JpaRepository<OtpModel, Integer> {
	OtpModel findByMobileNumber(BigInteger mobileNumber);
}
