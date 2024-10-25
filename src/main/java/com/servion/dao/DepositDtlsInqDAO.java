package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.fixedDepositBalance.FixedDepositBalance_HostRes;
import com.servion.ws.exception.DaoException;

public interface DepositDtlsInqDAO {

	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param contractID
	 * @return
	 * @throws DaoException
	 */
	FixedDepositBalance_HostRes getFDBalanceHostRes(CallInfo callInfo, String contractID, String requestType) throws DaoException;

}
