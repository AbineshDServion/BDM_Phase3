package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.CustDtls.CustDtls_HostRes;
import com.servion.ws.exception.DaoException;

public interface CustDtlsDAO {
	
	CustDtls_HostRes getCustDtlsHostRes(CallInfo callInfo, String cardNo, String customerId, String requestType) throws DaoException;

}
