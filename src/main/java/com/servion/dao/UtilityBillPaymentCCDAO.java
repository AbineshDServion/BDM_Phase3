package com.servion.dao;

import java.math.BigDecimal;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.UpdatePaymentDetailsCC_HostRes;
import com.servion.ws.exception.DaoException;

public interface UtilityBillPaymentCCDAO {
	
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
	UpdatePaymentDetailsCC_HostRes getCCUtilityBillUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, String serviceProviderCode, String contractNo, 
			String billNo, int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt, String cardNumber, String traceNo, String merchantCategoryCode, 
			String acquiringInstitutionCountry, String merchantID, String retrievalReference, String terminalID, String transactionCCY,
			String addlPOSInfo, String requestType) throws DaoException;
	
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
//	UpdatePaymentDetails_HostRes getCCWaterBillUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String contractNo, 
//			String billNo, int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt, String cardNumber, String traceNo, String merchantCategoryCode, 
//			String acquiringInstitutionCountry, String merchantID, String retrievalReference, String terminalID, String transactionCCY,
//			String addlPOSInfo) throws DaoException;
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
//	UpdatePaymentDetails_HostRes getCCElecBillUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String contractNo, 
//			String billNo, int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt, String cardNumber, String traceNo, String merchantCategoryCode, 
//			String acquiringInstitutionCountry, String merchantID, String retrievalReference, String terminalID, String transactionCCY,
//			String addlPOSInfo) throws DaoException;
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
//	UpdatePaymentDetails_HostRes getCCMobBroadUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String contractNo, 
//			String billNo, int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt, String cardNumber, String traceNo, String merchantCategoryCode, 
//			String acquiringInstitutionCountry, String merchantID, String retrievalReference, String terminalID, String transactionCCY,
//			String addlPOSInfo) throws DaoException;
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
//	UpdatePaymentDetails_HostRes getCCTopUpUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String contractNo, 
//			String billNo, int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt, String cardNumber, String traceNo, String merchantCategoryCode, 
//			String acquiringInstitutionCountry, String merchantID, String retrievalReference, String terminalID, String transactionCCY,
//			String addlPOSInfo) throws DaoException;
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
//	UpdateFTUtilityPaymentCharity_HostRes getCCFTCharityUpdPaymentHostRes(CallInfo callInfo, String paymentType, String utilityCode, int serviceProviderCode, String contractNo, 
//			String billNo, int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt, String cardNumber, String traceNo, String merchantCategoryCode, 
//			String acquiringInstitutionCountry, String merchantID, String retrievalReference, String terminalID, String transactionCCY,
//			String addlPOSInfo) throws DaoException;
//	
}
