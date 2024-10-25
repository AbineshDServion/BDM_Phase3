package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.accountBalance.AccountBalance_HostRes;
import com.servion.model.bankingFlashBalance.BankingBalanceFlashDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface AcctDtlsInquiryDAO {

	
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param acctId
	 * @param noOfTxn
	 * @return
	 * @throws DaoException
	 */
	
	AccountBalance_HostRes getAcctBalanceHostRes(CallInfo callInfo, String acctId, String deptAcctOfficerDtlFlg, String requestType) throws DaoException;
	
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param acctId
	 * @param noOfTxn
	 * @return
	 * @throws DaoException
	 */
	
	BankingBalanceFlashDetails_HostRes getBankBalFlashHostRes(CallInfo callInfo, String acctId, String deptAcctOfficerDtlFlg, String requestType) throws DaoException;
}
