package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.TelecomSubscriberInfo_HostRes;
import com.servion.ws.exception.DaoException;

public interface TelecomSubscriberInfoDAO {

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param providerType
	 * @param serviceProviderCode
	 * @param MSISDN
	 * @return
	 */
	
	TelecomSubscriberInfo_HostRes getTelecomSubscriberInfo_HostRes(CallInfo callInfo,String requestType, String providerType, String serviceProviderCode, String MSISDN) throws DaoException;
}
