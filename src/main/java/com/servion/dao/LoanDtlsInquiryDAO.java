package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.loanBalance.LoanBalanceDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface LoanDtlsInquiryDAO {
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param contractID
	 * @return
	 * @throws DaoException
	 */
	
	LoanBalanceDetails_HostRes getLoanBalanceHostRes(CallInfo callInfo, String contractID, String requestType) throws DaoException;

}
