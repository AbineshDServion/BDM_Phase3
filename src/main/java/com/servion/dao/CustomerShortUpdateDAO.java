package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.mobileNumberChange.MobileNumberChange_HostRes;
import com.servion.ws.exception.DaoException;

public interface CustomerShortUpdateDAO {
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param cardEmbossNum
	 * @return
	 * @throws DaoException
	 */

	MobileNumberChange_HostRes getMobileNumberChange_HostRes(CallInfo callInfo, String customerId, String mobileNumber, String countryCode, String requestType) throws DaoException;

}
