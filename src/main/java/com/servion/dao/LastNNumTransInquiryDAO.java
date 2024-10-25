package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.recentTransactionBank.RecentTransactionBank_HostRes;
import com.servion.model.transactionDetailBank.TransactionDetailsBank_HostRes;
import com.servion.ws.exception.DaoException;

public interface LastNNumTransInquiryDAO {
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param acctId
	 * @param noOfTxn
	 * @return
	 * @throws DaoException
	 */
	
//	AccountBalance_HostRes getAcctBalanceHostRes(CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException;

	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param acctId
	 * @param noOfTxn
	 * @return
	 * @throws DaoException
	 */
	
//	BankingBalanceFlashDetails_HostRes getBankBalFlashHostRes(CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException;

	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param acctId
	 * @param noOfTxn
	 * @return
	 * @throws DaoException
	 */
	
	RecentTransactionBank_HostRes getRecentTransBankHostRes(CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException;

	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param acctId
	 * @param noOfTxn
	 * @return
	 * @throws DaoException
	 */
	
	TransactionDetailsBank_HostRes getTransactioDetailsBankHostRes(CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException;

}
