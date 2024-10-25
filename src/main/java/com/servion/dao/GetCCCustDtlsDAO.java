package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.getCCCustDtls.GetCCCustDtls_HostRes;
import com.servion.ws.exception.DaoException;

public interface GetCCCustDtlsDAO {
	
	GetCCCustDtls_HostRes getGetCCCustDtlsHostRes(CallInfo callInfo, String cardNo, String requestType) throws DaoException;

}
