package com.servion.dao;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;

public interface CustomerProfileAggregateDAO {
	CallerIdentification_HostRes getCallerIdentificationHostRes(CallInfo callinfo, String customerID,String requestType)throws DaoException;

}
