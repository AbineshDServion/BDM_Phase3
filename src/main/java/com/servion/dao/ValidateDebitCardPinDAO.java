package com.servion.dao;

import java.math.BigDecimal;

import com.servion.model.CallInfo;
import com.servion.model.apinValidation.ValidatePIN_HostRes;
import com.servion.ws.exception.DaoException;

public interface ValidateDebitCardPinDAO {
	
	/**
	 * 
	 * @param pan
	 * @param processingCode
	 * @param amtTransaction
	 * @param amtSettlement
	 * @param transmissionDate
	 * @param convRateSettlement
	 * @param systemTraceAudit
	 * @param localTransactionTime
	 * @param localTransactionDate
	 * @param expirationDate
	 * @param settlementDate
	 * @param conversionDate
	 * @param merchantType
	 * @param pointOfServiceEntryMode
	 * @param cardSeqNum
	 * @param pointOfserviceConditionCode
	 * @param pointOfServiceCaptureCode
	 * @param authIDRespLength
	 * @param amtSettlementFee
	 * @param amtSettlementProcessingFee
	 * @param acquInstitutionCode
	 * @param trackTwoData
	 * @param cardAccpTerminalID
	 * @param cardAccpIDCode
	 * @param cardAccpName
	 * @param currCode
	 * @param pin
	 * @param securityContrInfo
	 * @param additionalAmt
	 * @param extendedPaymentCode
	 * @param origDataElements
	 * @param payee
	 * @param recvInstitutionID
	 * @param acctIdentfOne
	 * @param acctIdentfTwo
	 * @param posDataCode
	 * @param bitMap
	 * @param checkData
	 * @param termOwner
	 * @param posGeographicData
	 * @param sponsorBank
	 * @param addrVerfData
	 * @param bankDetails
	 * @param payeeNameAddr
	 * @param iccData
	 * @param origalData
	 * @param MACField
	 * @return
	 * @throws DaoException
	 */
	
	ValidatePIN_HostRes getAPINValidateHostRes (
			CallInfo callInfo,
			String pan,
			String processingCode,
			String amtTransaction,
			BigDecimal amtSettlement,
			String transmissionDate,
			BigDecimal convRateSettlement,
			String systemTraceAudit,
			String localTransactionTime,
			String localTransactionDate,
			String expirationDate,
			String settlementDate,
			String conversionDate,
			String merchantType,
			String pointOfServiceEntryMode,
			String cardSeqNum,
			String pointOfserviceConditionCode,
			String pointOfServiceCaptureCode,
			int authIDRespLength,
			BigDecimal amtSettlementFee,
			BigDecimal amtSettlementProcessingFee,
			String acquInstitutionCode,
			String trackTwoData,
			String cardAccpTerminalID,
			String cardAccpIDCode,
			String cardAccpName,
			String currCode,
			String currCodeSettlement,
			String pin,
			String securityContrInfo,
			String additionalAmt,
			int extendedPaymentCode,
			String origDataElements,
			String payee,
			String recvInstitutionID,
			String acctIdentfOne,
			String acctIdentfTwo,
			String posDataCode,
			String bitMap,
			String checkData,
			String termOwner,
			String posGeographicData,
			String sponsorBank,
			String addrVerfData,
			String bankDetails,
			String payeeNameAddr,
			String iccData,
			String origalData,
			String MACField,
			String lastUpdTimeStamp, String requestType
			) throws DaoException;

}
