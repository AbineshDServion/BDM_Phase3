package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.customermgmtservice.CustomerShortupdateResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CustomerShortUpdateDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.mobileNumberChange.MobileNumberChange_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.CustomerShortUpdateService;
import com.servion.ws.util.DAOLayerUtils;

public class CustomerShortUpdateDAOImpl implements CustomerShortUpdateDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	CustomerShortUpdateService customerShortUpdateService;

	public CustomerShortUpdateService getCustomerShortUpdateService() {
		return customerShortUpdateService;
	}

	public void setCustomerShortUpdateService(
			CustomerShortUpdateService customerShortUpdateService) {
		this.customerShortUpdateService = customerShortUpdateService;
	}

	public WS_ResponseHeader getWs_ResponseHeader() {
		return ws_ResponseHeader;
	}

	public void setWs_ResponseHeader(WS_ResponseHeader ws_ResponseHeader) {
		this.ws_ResponseHeader = ws_ResponseHeader;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

	
	@Override
	public MobileNumberChange_HostRes getMobileNumberChange_HostRes(
			CallInfo callInfo, String customerId, String mobileNumber, String countryCode, String requestType)
			throws DaoException {
		// TODO Auto-generated method stub
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: FetchCaseListOrCountDAOImpl.getFetchCaseListOrCountHostRes()");}

		MobileNumberChange_HostRes beanResponse = new MobileNumberChange_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CustomerShortupdateResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCustomerShortUpdateHost host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			response = customerShortUpdateService.callCustomerShortUpdateHost(logger, sessionID, customerId, mobileNumber, countryCode, requestType, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			//String code = ws_ResponseHeader.getEsbErrCode();
			String code = String.valueOf(ws_ResponseHeader.getEsbResStatus());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCustomerShortUpdateHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getMobileNumberChange_HostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CustomerShortupdateResType HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CustomerShortUpdate_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CustomerShortUpdate_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CustomerShortUpdate_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Successfully updated the mobile number");}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CustomerShortupdateResType Response field Received null / empty so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CustomerShortupdateResType Response field Received null / empty so setting error code as 1");}
				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at FetchCaseListOrCountDAOImpl.getFetchCaseListOrCountHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FetchCaseListOrCountDAOImpl.getFetchCaseListOrCountHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: FetchCaseListOrCountDAOImpl.getFetchCaseListOrCountHostRes()");}
		return beanResponse;
	}

	

}