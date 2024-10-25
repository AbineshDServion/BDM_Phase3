package com.servion.dao;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import com.servion.model.CallInfo;
import com.servion.model.creditCardPayment.UpdateCreditCardPaymenTxnPosttDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface CCtxnPostRqDAO {
	/**
	 * @param reference
	 * @param postTo
	 * @param acctId
	 * @param cardNo
	 * @param trxnPostingDate
	 * @param trxnDate
	 * @param origCcyAmt
	 * @param origCcyCode
	 * @param description
	 * @return
	 * @throws DaoException
	 */
	
	UpdateCreditCardPaymenTxnPosttDetails_HostRes getCCPaymentUpdateHostRes(CallInfo callInfo, String reference, String postTo, String acctId, String cardNo, XMLGregorianCalendar trxnPostingDate, XMLGregorianCalendar trxnDate, 
			BigDecimal origCcyAmt, String origCcyCode, String description, String requestType) throws DaoException;

}
