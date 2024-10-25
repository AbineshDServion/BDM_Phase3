package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.UtilitySubscriberInfo_HostRes;
import com.servion.ws.exception.DaoException;

public interface UtilitySubscriberInfoDAO {

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param providerType
	 * @param serviceProviderCode
	 * @param MSISDN
	 * @return
	 */
	
	UtilitySubscriberInfo_HostRes getUtilitySubscriberInfo_HostRes(
			CallInfo callInfo, String requestType, String providerType,
			String serviceProviderCode, String utilityCode, String contractNo, boolean isForOIFCElectricityService, String electricityType) throws DaoException;
}
