package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.debitCardActivation.ActivateCard_HostRes;
import com.servion.model.reportLostCard.LostStolenCard_HostRes;
import com.servion.ws.exception.DaoException;

public interface DebitCardServiceRequestDAO {

	/**
	 * @param primaryAccountNum
	 * @param processingCode
	 * @param transactionDate
	 * @param sysTraceAuditNumber
	 * @param localTime
	 * @param localDate
	 * @param expirationDate
	 * @param settlementDate
	 * @param merchantType
	 * @param serviceEntryMode
	 * @param cardSequenceNum
	 * @param serviceCondCode
	 * @param serviceCaptureCode
	 * @param institutionIdnefCode
	 * @param trackTwoData
	 * @param cardAcceptorTerminalID
	 * @param cardAcceptorIDCode
	 * @param cardAcceptorName
	 * @param pin
	 * @param securityControlInfo
	 * @param mesgReasonCode
	 * @param originalDataElement
	 * @param institutionIDCode
	 * @param accountIdentification
	 * @param posDataCode
	 * @param bitMap
	 * @param terminalOwner
	 * @param posGeographicData
	 * @param sponsorBank
	 * @param addrVerfData
	 * @param iccData
	 * @param originalNode
	 * @param mesgAuthCode
	 * @return
	 * @throws DaoException
	 */
	
	LostStolenCard_HostRes getReportLostCardHostRes(CallInfo callInfo, String requestType, String primaryAccountNum, String processingCode,String amtTransaction, String transactionDate, String sysTraceAuditNumber, String localTime, String localDate, 
			String expirationDate, String settlementDate, String merchantType, String serviceEntryMode, String cardSequenceNum, String serviceCondCode, 
			String serviceCaptureCode, String institutionIdnefCode, String trackTwoData, String cardAcceptorTerminalID,
			String cardAcceptorIDCode, String cardAcceptorName, String pin, String securityControlInfo, String mesgReasonCode,
			String originalDataElement, String institutionIDCode, String accountIdentification, String posDataCode, String bitMap,
			String terminalOwner, String posGeographicData, String sponsorBank, String addrVerfData, String iccData, String originalNode, 
			String mesgAuthCode, String transCurrCode, String structureData, String extendedTransType) throws DaoException;

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param primaryAccountNum
	 * @param processingCode
	 * @param amtTransaction
	 * @param transactionDate
	 * @param sysTraceAuditNumber
	 * @param localTime
	 * @param localDate
	 * @param expirationDate
	 * @param settlementDate
	 * @param merchantType
	 * @param serviceEntryMode
	 * @param cardSequenceNum
	 * @param serviceCondCode
	 * @param serviceCaptureCode
	 * @param institutionIdnefCode
	 * @param trackTwoData
	 * @param cardAcceptorTerminalID
	 * @param cardAcceptorIDCode
	 * @param cardAcceptorName
	 * @param pin
	 * @param securityControlInfo
	 * @param mesgReasonCode
	 * @param originalDataElement
	 * @param institutionIDCode
	 * @param accountIdentification
	 * @param posDataCode
	 * @param bitMap
	 * @param terminalOwner
	 * @param posGeographicData
	 * @param sponsorBank
	 * @param addrVerfData
	 * @param iccData
	 * @param originalNode
	 * @param mesgAuthCode
	 * @param transCurrCode
	 * @param structureData
	 * @param extendedTransType
	 * @return
	 * @throws DaoException
	 */
	ActivateCard_HostRes getDebitCardActivationHostRes(CallInfo callInfo, String requestType, String primaryAccountNum, String processingCode,String amtTransaction, String transactionDate, String sysTraceAuditNumber, String localTime, String localDate, 
			String expirationDate, String settlementDate, String merchantType, String serviceEntryMode, String cardSequenceNum, String serviceCondCode, 
			String serviceCaptureCode, String institutionIdnefCode, String trackTwoData, String cardAcceptorTerminalID,
			String cardAcceptorIDCode, String cardAcceptorName, String pin, String securityControlInfo, String mesgReasonCode,
			String originalDataElement, String institutionIDCode, String accountIdentification, String posDataCode, String bitMap,
			String terminalOwner, String posGeographicData, String sponsorBank, String addrVerfData, String iccData, String originalNode, 
			String mesgAuthCode, String transCurrCode, String structureData, String extendedTransType) throws DaoException;

}
