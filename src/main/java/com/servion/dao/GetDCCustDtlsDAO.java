package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.getDCCustDtls.GetDCCustDtls_HostRes;
import com.servion.ws.exception.DaoException;

public interface GetDCCustDtlsDAO {
	
	GetDCCustDtls_HostRes getGetDCCustDtlsHostRes(CallInfo callInfo, String cardNo, String requestType) throws DaoException;

}
