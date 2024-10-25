package com.servion.dao;

import javax.xml.datatype.XMLGregorianCalendar;

import com.servion.model.CallInfo;
import com.servion.model.recentTransactionCards.RecentTransactionCards_HostRes;
import com.servion.model.transactionDetaitCards.TransactionDetailCards_HostReq;
import com.servion.ws.exception.DaoException;

public interface CCAcctStmtInqDAO {

	/**
	 * 
	 * @param callInfo
	 * @param statementType
	 * @param reqType
	 * @param returnContent
	 * @param startDate
	 * @param endDate
	 * @param cardAcctNo
	 * @param cardEmbossNo
	 * @return
	 * @throws DaoException
	 */
	RecentTransactionCards_HostRes getRecentTransactionCardsHostRes(
			CallInfo callInfo, String statementType, String reqType, String returnContent,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, String cardEmbossNo, String ccyCodeType,String groupTrxn, String entitySize)throws DaoException;
	
	/**
	 * 
	 * @param callInfo
	 * @param statementType
	 * @param reqType
	 * @param returnContent
	 * @param startDate
	 * @param endDate
	 * @param cardAcctNo
	 * @param cardEmbossNo
	 * @return
	 * @throws DaoException
	 */
	TransactionDetailCards_HostReq getTransactionDeatilCardsHostRes(
			CallInfo callInfo, String statementType, String reqType, String returnContent,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, String cardAcctNo, String cardEmbossNo, String ccyCodeType,String groupTrxn)throws DaoException;
}
