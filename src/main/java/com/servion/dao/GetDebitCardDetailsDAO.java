package com.servion.dao;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.callerIdentification.CallerIdenf_DebitCardDetails;

public interface GetDebitCardDetailsDAO {

	/**
	 * 
	 * @param callinfo
	 * @param pan
	 * @param processingCode
	 * @param amtTransaction
	 * @param amtSettlement
	 * @param transmissionDate
	 * @param conversionRate
	 * @param sysTraceAuditNo
	 * @param localTransTime
	 * @param localTansDate
	 * @param expirationDate
	 * @param settlementDate
	 * @param dateConversion
	 * @param merchantType
	 * @param pointOfServiceMode
	 * @param cardSeqNum
	 * @param pointOfServCondCode
	 * @param pointOfServCaptureCode
	 * @param authIDRespLength
	 * @param amtSettlementFee
	 * @param amtSettlementProcFee
	 * @param AcquiringInstitutionID
	 * @param trackTwoData
	 * @param cardAccpTerminalID
	 * @param cardAccpIDCode
	 * @param cardAccpName
	 * @param currCode
	 * @param currCodeSettlement
	 * @param pin
	 * @param securityContInfo
	 * @param additionalAmt
	 * @param extendedPaymentCode
	 * @param originalDataElement
	 * @param payee
	 * @param recvInstIDCode
	 * @param acctIdenfOne
	 * @param acctIdenfTwo
	 * @param posDataCode
	 * @param bitMap
	 * @param switchKey
	 * @param checkData
	 * @param terminalOwner
	 * @param posGeographicData
	 * @param sponsorBank
	 * @param addrVerfData
	 * @param bankDetails
	 * @param payeeName
	 * @param iccData
	 * @param origData
	 * @param macField
	 * @return
	 * @throws DaoException
	 */
	CallerIdenf_DebitCardDetails getCallerIdenfCustomerIDHostRes(CallInfo callinfo, String pan, String processingCode, String amtTransaction, 
			BigDecimal amtSettlement, String transmissionDate, int conversionRate, String sysTraceAuditNo, String localTransTime,
			String localTansDate, String expirationDate, String settlementDate, XMLGregorianCalendar dateConversion, String merchantType, String pointOfServiceMode,
			String cardSeqNum, String pointOfServCondCode, String pointOfServCaptureCode, int authIDRespLength, BigDecimal amtSettlementFee, 
			BigDecimal amtSettlementProcFee, String AcquiringInstitutionID, String trackTwoData, String cardAccpTerminalID,
			String cardAccpIDCode, String cardAccpName, String currCode, String currCodeSettlement, String pin, 
			String securityContInfo, String additionalAmt, int extendedPaymentCode, String originalDataElement, String payee,
			String recvInstIDCode, String acctIdenfOne, String acctIdenfTwo, String posDataCode, String bitMap, String switchKey,
			String checkData, String terminalOwner, String posGeographicData, String sponsorBank, String addrVerfData, 
			String bankDetails, String payeeName, String iccData, String origData, String macField, String structureData, String extendedTransType, String requestType
			)throws DaoException;
//
//	EnterCard_CallerIdentification_HostRes getEnterDbtCallerIdenfCustIDfHostRes(CallInfo callinfo, String pan, String processingCode, String amtTransaction, 
//			int amtSettlement, String transmissionDate, int conversionRate, int sysTraceAuditNo, String localTransTime,
//			String localTansDate, String expirationDate, String settlementDate, XMLGregorianCalendar dateConversion, int merchantType, int pointOfServiceMode,
//			int cardSeqNum, int pointOfServCondCode, int pointOfServCaptureCode, int authIDRespLength, int amtSettlementFee, 
//			int amtSettlementProcFee, String AcquiringInstitutionID, String trackTwoData, String cardAccpTerminalID,
//			String cardAccpIDCode, String cardAccpName, String currCode, String currCodeSettlement, String pin, 
//			String securityContInfo, String additionalAmt, int extendedPaymentCode, String originalDataElement, String payee,
//			String recvInstIDCode, String acctIdenfOne, String acctIdenfTwo, String posDataCode, String bitMap, String switchKey,
//			String checkData, String terminalOwner, String posGeographicData, String sponsorBank, String addrVerfData, 
//			String bankDetails, String payeeName, String iccData, String origData, String macField
//			)throws DaoException;
	
}
