package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.TelecomPrepaidNumberVal_HostRes;
import com.servion.model.billPayment.TelecomSubscriberInfo_HostRes;
import com.servion.ws.exception.DaoException;

public interface TelecomPrepaidNumberValDAO {

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param providerType
	 * @param serviceProviderCode
	 * @param MSISDN
	 * @return
	 */
	
	TelecomPrepaidNumberVal_HostRes getTelecomPrepaidNumberVal_HostRes(CallInfo callInfo,String requestType, String providerType, String utilityCode, String serviceProviderCode, String MSISDN) throws DaoException;
}
