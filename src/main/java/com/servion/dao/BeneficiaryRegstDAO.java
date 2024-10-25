package com.servion.dao;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.payeeRegistration.BeneficiaryRegistration_HostRes;

public interface BeneficiaryRegstDAO {

	
	/**
	 * 
	 * @param callInfo
	 * @param beneficiaryID
	 * @param shortDescription
	 * @param customerId
	 * @param customerDebitAcctNumber
	 * @param channelRequired
	 * @param beneficiaryName
	 * @param beneficiaryAcctType
	 * @param beneficiaryAcctNo
	 * @param beneficiaryMobNo
	 * @param paymentType
	 * @param serviceProviderCode
	 * @param utilityCode
	 * @param creditCardNo
	 * @param billNo
	 * @param contractNo
	 * @param gsmNo
	 * @param telephoneNo
	 * @param studentName
	 * @param classSection
	 * @param bankCode
	 * @param bankName
	 * @param bankBranch
	 * @param bankIFSCCode
	 * @param bankLocation
	 * @return
	 * @throws DaoException
	 */
	BeneficiaryRegistration_HostRes getPayeeRegistrationHostRes(
			CallInfo callInfo, String beneficiaryID,
			String shortDescription, String customerId,
			String customerDebitAcctNumber, String channelRequired,
			String beneficiaryName, String beneficiaryAcctType,
			String beneficiaryAcctNo, String beneficiaryMobNo,
			String paymentType, String serviceProviderCode, String utilityCode,
			String creditCardNo, String billNo, String contractNo, int gsmNo,
			int telephoneNo, String studentName, String classSection,
			String bankCode, String bankName, String bankBranch,
			String bankIFSCCode, String bankLocation, String requestType)throws DaoException ;
	
}
