package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.updatePin.UpdatePIN_HostRes;
import com.servion.ws.exception.DaoException;

public interface UpdatePinDAO {
	
	
	
	/**
	 * @param callInfo
	 * @param pan
	 * @param processingCode
	 * @param transmissionDate
	 * @param systemTraceAudit
	 * @param localTransactionTime
	 * @param localTransactionDate
	 * @param expirationDate
	 * @param pointOfServiceEntryMode
	 * @param cardSeqNum
	 * @param pointOfserviceConditionCode
	 * @param pointOfServiceCaptureCode
	 * @param cardAccpTerminalID
	 * @param cardAccpIDCode
	 * @param cardAccpName
	 * @param pin
	 * @param recvInstitutionID
	 * @param posDataCode
	 * @param structureData
	 * @param extendedTransType
	 * @param requestType
	 * @return
	 * @throws DaoException
	 */
	UpdatePIN_HostRes getUpdatePINHostRes (
			CallInfo callInfo,
			String pan,
			String processingCode,
			String transmissionDate,
			String systemTraceAudit,
			String localTransactionTime,
			String localTransactionDate,
			String expirationDate,
			String pointOfServiceEntryMode,
			String cardSeqNum,
			String pointOfserviceConditionCode,
			String pointOfServiceCaptureCode,
			String cardAccpTerminalID,
			String cardAccpIDCode,
			String cardAccpName,
			String pin,
			String recvInstitutionID,
			String posDataCode,
			String structureData, String extendedTransType, String requestType
			) throws DaoException;

}
