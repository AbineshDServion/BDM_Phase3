package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.creditCardBalance.CreditCardGroupInq_HostRes;
import com.servion.ws.exception.DaoException;

public interface CCGroupInquiryDAO {
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param cardEmbossNum
	 * @return
	 * @throws DaoException
	 */

	CreditCardGroupInq_HostRes getCCAvailableBalanceHostRes(CallInfo callInfo, String cardEmbossNum, String reference, String extraOption, String maxNoAuth, String requestType) throws DaoException;

}
