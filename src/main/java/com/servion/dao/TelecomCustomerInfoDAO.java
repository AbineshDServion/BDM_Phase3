package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.TelecomCustomerInfo_HostRes;
import com.servion.ws.exception.DaoException;

public interface TelecomCustomerInfoDAO {

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param providerType
	 * @param serviceProviderCode
	 * @param MSISDN
	 * @return
	 */
	
	TelecomCustomerInfo_HostRes getTelecomCustomerInfo_HostRes(CallInfo callInfo,String requestType, String providerType, String serviceProviderCode, String subscriberNumber, String contractNumber, String subscriberType) throws DaoException;
}
