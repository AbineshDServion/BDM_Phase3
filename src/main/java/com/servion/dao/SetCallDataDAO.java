package com.servion.dao;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.rapCTI.SetData_HostRes;

public interface SetCallDataDAO {

	/**
	 * 
	 * @param callinfo
	 * @param callID
	 * @param deviceID
	 * @param mode
	 * @param data
	 * @return
	 * @throws DaoException
	 */
	SetData_HostRes getCTISetCallDataHostRes(CallInfo callinfo,int callID, int deviceID, int mode, String data)throws DaoException;
}
