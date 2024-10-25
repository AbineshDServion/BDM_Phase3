package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.TelecomPostpaidBalanceDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface TelecomPostpaidBalanceDetailsDAO {

	/**
	 * 
	 * @param callInfo
	 * @param requestType
	 * @param providerType
	 * @param serviceProviderCode
	 * @param accountNumber
	 * @return
	 */
	
	TelecomPostpaidBalanceDetails_HostRes getTelecomPostpaidBalanceDetails_HostRes(CallInfo callInfo,String requestType,
			String providerType, String serviceProviderCode,
			String accountNumber)throws DaoException;
	
}
