package com.servion.dao;

import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.fax.LoggingFaxRequest_HostRes;

public interface SendFaxDAO {

	/**
	 * 
	 * @param callinfo
	 * @param faxNumber
	 * @param fileLoc
	 * @param fileName
	 * @return
	 * @throws DaoException
	 */
	LoggingFaxRequest_HostRes getSendFaxHostRes(CallInfo callinfo, String faxNumber, String fileLoc, String fileName)throws DaoException;
}
