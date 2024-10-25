package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.chequeBookRequest.UpdateChequeBookOrder_HostRes;
import com.servion.ws.exception.DaoException;

public interface ChqBookOrderDAO {

	
	/**
	 * 
	 * @param callInfo
	 * @param acctID
	 * @param leafCount
	 * @return
	 * @throws DaoException
	 */
	UpdateChequeBookOrder_HostRes getChequeBookOrderUpdHostRes(CallInfo callInfo, String acctID, String leafCount, String requestType)throws DaoException;

}
