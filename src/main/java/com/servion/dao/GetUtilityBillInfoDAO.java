package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.GetUtilityBillInfo_HostRes;
import com.servion.ws.exception.DaoException;

public interface GetUtilityBillInfoDAO {

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param providerType
	 * @param serviceProviderCode
	 * @param MSISDN
	 * @return
	 */
	
	GetUtilityBillInfo_HostRes getGetUtilityBillInfo_HostRes(
			CallInfo callInfo, String requestType, String providerType,
			String serviceProviderCode, String utilityCode, String contractNo) throws DaoException;
}
