package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.beneficiarymanagementservice.TelecomPrepaidNumberValResType;
import com.bankmuscat.esb.beneficiarymanagementservice.TopupMSISDNInfoType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.TelecomPrepaidNumberValDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.TelecomPrepaidNumberVal_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.TelecomPrepaidNumberValService;
import com.servion.ws.util.DAOLayerUtils;

//public class TelecomPrepaidNumberValDAOImpl{
public class TelecomPrepaidNumberValDAOImpl implements TelecomPrepaidNumberValDAO{
	private static Logger logger = LoggerObject.getLogger();

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

	//TODO - CR
	@Autowired
	TelecomPrepaidNumberValService telecomPrepaidNumberValService;
	
	
	public TelecomPrepaidNumberValService getTelecomPrepaidNumberValService() {
		return telecomPrepaidNumberValService;
	}

	public void setTelecomPrepaidNumberValService(
			TelecomPrepaidNumberValService telecomPrepaidNumberValService) {
		this.telecomPrepaidNumberValService = telecomPrepaidNumberValService;
	}

	@Override
	public TelecomPrepaidNumberVal_HostRes getTelecomPrepaidNumberVal_HostRes(CallInfo callInfo,String requestType, String providerType, String utilityCode, 
			String serviceProviderCode, String MSISDN) throws DaoException{
		// TODO Auto-generated method stub
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: TelecomPostpaidBalanceDetailsDAOImpl.getTelecomPrepaidNumberVal()");}
		
		TelecomPrepaidNumberVal_HostRes beanResponse = new TelecomPrepaidNumberVal_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
		
			TelecomPrepaidNumberValResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callTelecomPrepaidNumberValResType host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = telecomPrepaidNumberValService.callTelecomPrepaidNumberValResType(logger, sessionID, requestType, providerType, utilityCode, serviceProviderCode, MSISDN, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callTelecomPrepaidNumberValResType is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			beanResponse.setHostResponseCode(code);
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getTelecomPrepaidNumberVal_HostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### TelecomPrepaidNumberVal HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_TelecomPrepaidNumberVal_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_TelecomPrepaidNumberVal_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_TelecomPrepaidNumberVal_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					if(response.getTopupMSISDNInfo()!=null){
						
						TopupMSISDNInfoType topupInfo = response.getTopupMSISDNInfo();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received TopUp Number validation information is "+topupInfo);}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Max Top Up Amount is "+topupInfo.getMaxTopUpAmount());}
						beanResponse.setMaxTopUpAmount(topupInfo.getMaxTopUpAmount() + Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Min Top up Amount is "+topupInfo.getMinTopUpAmount() );}
						beanResponse.setMinTopUpAmount(topupInfo.getMinTopUpAmount() + Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received MSISDN number is "+topupInfo.getMSISDN());}
						beanResponse.setMSISDN(topupInfo.getMSISDN() + Constants.EMPTY);
					}
				}
			}
			
			callInfo.setTelecomPrepaidNumberVal_HostRes(beanResponse);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the bean response object into the call info object"+ callInfo.getTelecomPrepaidNumberVal_HostRes());}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at TelecomPrepaidNumberVal_HostRes.getTelecomPrepaidNumberVal_HostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TelecomPrepaidNumberVal_HostRes.getTelecomPrepaidNumberVal_HostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: TelecomPrepaidNumberVal_HostRes.getTelecomPrepaidNumberVal_HostRes()");}
		return beanResponse;
		
	}

}
