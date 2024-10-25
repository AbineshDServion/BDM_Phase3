package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.tempuri.SetCallDataResponse;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.SetCallDataDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.rapCTI.SetData_HostRes;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.service.SetCallDataService;

public class SetCallDataDAOImpl implements SetCallDataDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	SetCallDataService setCallDataService;
	
	public SetCallDataService getSetCallDataService() {
		return setCallDataService;
	}

	public void setSetCallDataService(SetCallDataService setCallDataService) {
		this.setCallDataService = setCallDataService;
	}

	@Override
	public SetData_HostRes getCTISetCallDataHostRes(CallInfo callInfo, int callID, int deviceID, int mode, String data)	throws DaoException {
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: SetCallDataDAOImpl.getCTISetCallDataHostRes()");}
		String sessionID = (String)callInfo.getField(Field.SESSIONID);
		
		if(util.isNullOrEmpty(sessionID))
			throw new DaoException("Session ID is null / empty");
		
		
		SetCallDataResponse response = null;
		SetData_HostRes beanResponse = new SetData_HostRes();
		
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Fetching the Feature Object values");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}

			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Passing the request device id :" + deviceID);}
			response = setCallDataService.callSetCallDataRAPCTIService(logger, sessionID, callID, deviceID, mode, data, str_UUI, generateXML);
			
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			String responseCode = response.getSetCallDataResult();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Response code for SetCallData Service is" + responseCode);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), responseCode);
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SetCallData_Succ_ErrorCode); 
			
			String code = util.isCodePresentInTheList(responseCode, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
		
			
			beanResponse.setErrorCode(code);
			beanResponse.setHostResponseCode(responseCode);
			beanResponse.setSetCallDataResult(responseCode);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### SetCallData HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The OD application error code is" + code);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Host service response code is " + responseCode);}
			
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at SetCallDataDAOImpl.getCTISetCallDataHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at SetCallDataDAOImpl.getCTISetCallDataHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: SetCallDataDAOImpl.getCTISetCallDataHostRes()");}
		return beanResponse;
		
	}

}
