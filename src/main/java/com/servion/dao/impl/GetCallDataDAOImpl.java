package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.tempuri.GetCallDataResponse;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.GetCallDataDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.rapCTI.GetData_HostRes;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.service.GetCallDataService;

public class GetCallDataDAOImpl implements GetCallDataDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	GetCallDataService getCallDataService;
	
	public GetCallDataService getGetCallDataService() {
		return getCallDataService;
	}

	public void setGetCallDataService(GetCallDataService getCallDataService) {
		this.getCallDataService = getCallDataService;
	}

	
	@Override
	public GetData_HostRes getCTIGetCallDataHostRes(CallInfo callInfo, int deviceID) throws DaoException {
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: GetCallDataDAOImpl.getCTIGetCallDataHostRes()");}
		String sessionID = (String)callInfo.getField(Field.SESSIONID);
		
		if(util.isNullOrEmpty(sessionID))
			throw new DaoException("Session ID is null / empty");
		
		
		GetCallDataResponse response = null;
		GetData_HostRes beanResponse = new GetData_HostRes();
		
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Fetching the Feature Object values");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Passing the request device id :" + deviceID);}
			response = getCallDataService.callGetCallDataRAPCTIService(logger, sessionID, deviceID, str_UUI, generateXML);
			
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			String responseCode = response.getGetCallDataResult();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Response code for getCallData Service is" + responseCode);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), responseCode);
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetCallData_Succ_ErrorCode); 
			
			String code = util.isCodePresentInTheList(responseCode, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			
			beanResponse.setErrorCode(code);
			beanResponse.setHostResponseCode(responseCode);
			beanResponse.setGetCallDataResult(responseCode);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The OD application error code is" + code);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Host service response code is " + responseCode);}
			
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at GetCallDataDAOImpl.getCTIGetCallDataHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GetCallDataDAOImpl.getCTIGetCallDataHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: GetCallDataDAOImpl.getCTIGetCallDataHostRes()");}
		return beanResponse;
		
	}

}
