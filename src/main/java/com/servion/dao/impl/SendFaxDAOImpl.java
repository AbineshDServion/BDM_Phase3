package com.servion.dao.impl;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.tempuri.SendFaxResponse;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.SendFaxDAO;
import com.servion.exception.DaoException;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.fax.LoggingFaxRequest_HostRes;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.service.SendFaxService;

public class SendFaxDAOImpl implements SendFaxDAO {
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	SendFaxService sendFaxService;
	
	public SendFaxService getSendFaxService() {
		return sendFaxService;
	}

	public void setSendFaxService(SendFaxService sendFaxService) {
		this.sendFaxService = sendFaxService;
	}


	@Override
	public LoggingFaxRequest_HostRes getSendFaxHostRes(CallInfo callInfo, String faxNumber, String fileLoc, String fileName)	throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: SendFaxDAOImpl.getSendFaxHostRes()");}
		String sessionID = (String)callInfo.getField(Field.SESSIONID);
		
		if(util.isNullOrEmpty(sessionID))
			throw new DaoException("Session ID is null / empty");
		
		SendFaxResponse response = null;
		LoggingFaxRequest_HostRes beanResponse = new LoggingFaxRequest_HostRes();
		
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Fetching the Feature Object values");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Passing the requested faxNumber :" + faxNumber);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Passing the requested fileLoc :" + fileLoc);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Passing the requested fileName :" + fileName);}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = sendFaxService.callSendFaxHost(logger, sessionID, faxNumber, fileLoc, fileName, str_UUI, generateXML);
			
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			String responseCode = response.getSendFaxResult();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Response code for SendFax Service is" + responseCode);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), responseCode);
			
//			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
//			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
//				throw new ServiceException("ICEGlobalConfig object is null");
//			}
//			
//			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SendFax_Succ_ErrorCode); 
//			
//			String code = util.isCodePresentInTheList(responseCode, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			
			String code = Constants.EMPTY_STRING;
			try{
				
				/**
				 * Following are the hanlding for send fax service error response code handling
				 */
				if(Constants.MINUS_ONE.equalsIgnoreCase(responseCode)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Its a failure response from the host, hence the request for send fax is to be considered as failure -1");}
					code = Constants.ONE;
				}
				else{
					BigInteger bigInteger = new BigInteger(responseCode);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Its a valid integer , hence the request for send fax is to be considered as success");}
					code = Constants.ZERO;
				}
				//END Vinoth
			}catch(Exception e){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Got failure response from send fax service");}
				code = Constants.ONE;
			}
			
			beanResponse.setErrorCode(code);
			beanResponse.setHostResponseCode(responseCode);
			beanResponse.setSendFaxResult(responseCode);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The OD application error code is" + code);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Host service response code is " + responseCode);}
			
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at SendFaxDAOImpl.getSendFaxHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at SendFaxDAOImpl.getSendFaxHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: SendFaxDAOImpl.getSendFaxHostRes()");}
		return beanResponse;
		
	}


}
