package com.servion.dao;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.keyExAuth.KeyExAuth_HostRes;

public interface KeyExAuthDAO {

	
	/**
	 * 
	 * @param callinfo
	 * @param transmissionDateTime
	 * @param traceAuditNo
	 * @param localTransTime
	 * @param localTransDate
	 * @param networkInfoCode
	 * @param messageAuthCode
	 * @return
	 * @throws DaoException
	 */
	
	KeyExAuth_HostRes getKeyExAuthHostRes(CallInfo callinfo, String transmissionDateTime, String traceAuditNo, String localTransTime,
			String localTransDate, int networkInfoCode, String messageAuthCode, String requestType)throws DaoException;
}
