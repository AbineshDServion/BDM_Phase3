package com.servion.dao;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.rapCTI.GetData_HostRes;

public interface GetCallDataDAO {

	/**
	 * 
	 * @param callinfo
	 * @param deviceID
	 * @return
	 * @throws DaoException
	 */
	
	GetData_HostRes getCTIGetCallDataHostRes(CallInfo callinfo, int deviceID)throws DaoException;
}
