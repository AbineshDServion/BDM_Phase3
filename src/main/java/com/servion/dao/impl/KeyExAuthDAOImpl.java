package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.KeyExAuthResType;
import com.bankmuscat.esb.cardmanagementservice.Message810Type;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.KeyExAuthDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.keyExAuth.KeyExAuth_HostRes;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.KeyExAuthService;
import com.servion.ws.util.DAOLayerUtils;

public class KeyExAuthDAOImpl implements KeyExAuthDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	KeyExAuthService keyExAuthService;
	
	
	public KeyExAuthService getKeyExAuthService() {
		return keyExAuthService;
	}

	public void setKeyExAuthService(KeyExAuthService keyExAuthService) {
		this.keyExAuthService = keyExAuthService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public KeyExAuth_HostRes getKeyExAuthHostRes(CallInfo callInfo,	String transmissionDateTime, String traceAuditNo,String localTransTime, String localTransDate, int networkInfoCode,
			String messageAuthCode, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: KeyExAuthDAOImpl.getKeyExAuthHostRes()");}
		KeyExAuth_HostRes beanResponse = new KeyExAuth_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			KeyExAuthResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callKeyExAuthHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = keyExAuthService.callKeyExAuthHost(logger, sessionID, transmissionDateTime, traceAuditNo, localTransTime, localTransDate, networkInfoCode, messageAuthCode, requestType, str_UUI, generateXML, callInfo);
			
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of keyExAuthService is : "+code);}
			/*
			 * String className = this.getClass().getName();
			 * hostLogger.info(sessionID+" :: [KeyExAuthDAO] : className ["+className+"]");
			 * className = className.replaceFirst(Constants.DAOIMPL_STRING,
			 * Constants.EMPTY_STRING); className =
			 * className.substring(className.lastIndexOf(".")+1,className.length());
			 * hostLogger.info(sessionID+" :: [KeyExAuthDAO] : className1 ["+className+"]");
			 * hostLogger.info(sessionID+" :: ["+className+"] : The Response Code ["+code+
			 * "]");
			 */
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for ValidateDebitCardPinService is : "+ws_ResponseHeader.getEsbErrDesc());}
			
			
			beanResponse.setHostResponseCode(code);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### KeyExAuth HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_Succ_ErrorCode);
			}
			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code of keyExAuthService for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					Message810Type message810Type = null;
					message810Type = response.getMessage810();


					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Last Updated time stamp is "+response.getPinBlockKeyLastUpdateIdentifier());}
					beanResponse.setLastUpdateTimeStamp(response.getPinBlockKeyLastUpdateIdentifier() + Constants.EMPTY_STRING);
					
					if(!util.isNullOrEmpty(message810Type)){
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Response code is"+message810Type.getField039());}
						beanResponse.setResponseCode(message810Type.getField039());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Security Control information is"+message810Type.getField053());}
						beanResponse.setSecurityControlInfo(message810Type.getField053());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Network management information code is"+message810Type.getField070());}
						beanResponse.setNetworkManagementInfoCode(message810Type.getField070()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Network management information is"+message810Type.getField125());}
						beanResponse.setNetworkManagementInfo(message810Type.getField125());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## MAC Code is ***********");}
						beanResponse.setMessageAuthCode(message810Type.getField128());


						
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty message810Type response object so setting error code as 1");}

						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
					
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty KeyExAuthResType response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at KeyExAuthDAOImpl.getKeyExAuthHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at KeyExAuthDAOImpl.getKeyExAuthHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: KeyExAuthDAOImpl.getKeyExAuthHostRes()");}
		return beanResponse;
	}
}
