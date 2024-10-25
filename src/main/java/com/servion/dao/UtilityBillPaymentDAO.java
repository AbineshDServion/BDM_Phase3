package com.servion.dao;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.UpdatePaymentDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface UtilityBillPaymentDAO {
	
	/**
	 * @param paymentType
	 * @param utilityCode
	 * @param serviceProviderCode
	 * @param contractNo
	 * @param billNo
	 * @param msisdn
	 * @param payAmt
	 * @param bonusRechrgAmt
	 * @param cardNumber
	 * @param traceNo
	 * @param merchantCategoryCode
	 * @param acquiringInstitutionCountry
	 * @param merchantID
	 * @param retrievalReference
	 * @param terminalID
	 * @param transactionCCY
	 * @param addlPOSInfo
	 * @return
	 * @throws DaoException
	 */
	UpdatePaymentDetails_HostRes getUtilityBillUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, String serviceProviderCode, String electricityType, String billerCode, String benfCode,
			String contractNo, String billNo, String accessChannel, String paymentMethod, String paymentStatus, int msisdn, String debitAcctID, BigDecimal debitAmt, XMLGregorianCalendar debitValueDate,
			BigDecimal payAmt, BigDecimal dueAmt, BigDecimal bonusRechrgAmt, String requestType) throws DaoException;
	
//	/**
//	 * @param paymentType
//	 * @param utilityCode
//	 * @param serviceProviderCode
//	 * @param contractNo
//	 * @param billNo
//	 * @param msisdn
//	 * @param payAmt
//	 * @param bonusRechrgAmt
//	 * @param cardNumber
//	 * @param traceNo
//	 * @param merchantCategoryCode
//	 * @param acquiringInstitutionCountry
//	 * @param merchantID
//	 * @param retrievalReference
//	 * @param terminalID
//	 * @param transactionCCY
//	 * @param addlPOSInfo
//	 * @return
//	 * @throws DaoException
//	 */
//	UpdatePaymentDetails_HostRes getWaterBillUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String benfCode,
//			String contractNo, String billNo, int msisdn, String debitAcctID, BigDecimal debitAmt, XMLGregorianCalendar debitValueDate,
//			BigDecimal payAmt, BigDecimal bonusRechrgAmt) throws DaoException;
//	
//	/**
//	 * @param paymentType
//	 * @param utilityCode
//	 * @param serviceProviderCode
//	 * @param contractNo
//	 * @param billNo
//	 * @param msisdn
//	 * @param payAmt
//	 * @param bonusRechrgAmt
//	 * @param cardNumber
//	 * @param traceNo
//	 * @param merchantCategoryCode
//	 * @param acquiringInstitutionCountry
//	 * @param merchantID
//	 * @param retrievalReference
//	 * @param terminalID
//	 * @param transactionCCY
//	 * @param addlPOSInfo
//	 * @return
//	 * @throws DaoException
//	 */
//	UpdatePaymentDetails_HostRes getElecBillUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String benfCode,
//			String contractNo, String billNo, int msisdn, String debitAcctID, BigDecimal debitAmt, XMLGregorianCalendar debitValueDate,
//			BigDecimal payAmt, BigDecimal bonusRechrgAmt) throws DaoException;
//	
//	/**
//	 * @param paymentType
//	 * @param utilityCode
//	 * @param serviceProviderCode
//	 * @param contractNo
//	 * @param billNo
//	 * @param msisdn
//	 * @param payAmt
//	 * @param bonusRechrgAmt
//	 * @param cardNumber
//	 * @param traceNo
//	 * @param merchantCategoryCode
//	 * @param acquiringInstitutionCountry
//	 * @param merchantID
//	 * @param retrievalReference
//	 * @param terminalID
//	 * @param transactionCCY
//	 * @param addlPOSInfo
//	 * @return
//	 * @throws DaoException
//	 */
//	UpdatePaymentDetails_HostRes getMobBroadUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String benfCode,
//			String contractNo, String billNo, int msisdn, String debitAcctID, BigDecimal debitAmt, XMLGregorianCalendar debitValueDate,
//			BigDecimal payAmt, BigDecimal bonusRechrgAmt) throws DaoException;
//	
//	/**
//	 * @param paymentType
//	 * @param utilityCode
//	 * @param serviceProviderCode
//	 * @param contractNo
//	 * @param billNo
//	 * @param msisdn
//	 * @param payAmt
//	 * @param bonusRechrgAmt
//	 * @param cardNumber
//	 * @param traceNo
//	 * @param merchantCategoryCode
//	 * @param acquiringInstitutionCountry
//	 * @param merchantID
//	 * @param retrievalReference
//	 * @param terminalID
//	 * @param transactionCCY
//	 * @param addlPOSInfo
//	 * @return
//	 * @throws DaoException
//	 */
//	UpdatePaymentDetails_HostRes getTopUpUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String benfCode,
//			String contractNo, String billNo, int msisdn, String debitAcctID, BigDecimal debitAmt, XMLGregorianCalendar debitValueDate,
//			BigDecimal payAmt, BigDecimal bonusRechrgAmt) throws DaoException;
//	
//	
//	/**
//	 * @param paymentType
//	 * @param utilityCode
//	 * @param serviceProviderCode
//	 * @param contractNo
//	 * @param billNo
//	 * @param msisdn
//	 * @param payAmt
//	 * @param bonusRechrgAmt
//	 * @param cardNumber
//	 * @param traceNo
//	 * @param merchantCategoryCode
//	 * @param acquiringInstitutionCountry
//	 * @param merchantID
//	 * @param retrievalReference
//	 * @param terminalID
//	 * @param transactionCCY
//	 * @param addlPOSInfo
//	 * @return
//	 * @throws DaoException
//	 */
//	UpdateFTUtilityPaymentCharity_HostRes getFTCharityUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String benfCode,
//			String contractNo, String billNo, int msisdn, String debitAcctID, BigDecimal debitAmt, XMLGregorianCalendar debitValueDate,
//			BigDecimal payAmt, BigDecimal bonusRechrgAmt) throws DaoException;
//	
}
