package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.complaintAlert.CheckComplaintID_HostRes;
import com.servion.ws.exception.DaoException;

public interface FetchCaseListOrCountDAO {
	/**
	 * @param loggerObject
	 * @param sessionId
	 * @param cardEmbossNum
	 * @return
	 * @throws DaoException
	 */

	CheckComplaintID_HostRes getFetchCaseListOrCountHostRes(CallInfo callInfo, String customerId, String bankingWith, String status, String requestType) throws DaoException;

}
