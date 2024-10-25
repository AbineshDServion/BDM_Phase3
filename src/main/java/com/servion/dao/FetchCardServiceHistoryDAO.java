package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.fetchCardServiceHistory.FetchCardServiceHistory_HostRes;
import com.servion.ws.exception.DaoException;

public interface FetchCardServiceHistoryDAO {
	
	FetchCardServiceHistory_HostRes getFetchCardServiceHistoryHostRes(CallInfo callInfo, String entity, String entityIdentifier, String serviceType, boolean activateServices, String requestType) throws DaoException;

}
