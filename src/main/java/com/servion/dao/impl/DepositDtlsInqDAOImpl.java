package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.accountmanagementservice.DepositAcctInfoType;
import com.bankmuscat.esb.accountmanagementservice.DepositDtlsInqResType;
import com.bankmuscat.esb.commontypes.BankInfoType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.DepositDtlsInqDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.fixedDepositBalance.FixedDepositBalance_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.DepositDtlsInqService;
import com.servion.ws.util.DAOLayerUtils;

public class DepositDtlsInqDAOImpl implements DepositDtlsInqDAO{

	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	DepositDtlsInqService depositDtlsInqService;


	public DepositDtlsInqService getDepositDtlsInqService() {
		return depositDtlsInqService;
	}


	public void setDepositDtlsInqService(DepositDtlsInqService depositDtlsInqService) {
		this.depositDtlsInqService = depositDtlsInqService;
	}


	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public FixedDepositBalance_HostRes getFDBalanceHostRes(CallInfo callInfo,
			String contractID, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: DepositDtlsInqDAOImpl.getFDBalanceHostRes()");}
		FixedDepositBalance_HostRes beanResponse = new FixedDepositBalance_HostRes();
		
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			DepositDtlsInqResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callDebitCardDetailsHost");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = depositDtlsInqService.callFDAccountBalanceHost(logger, sessionID, contractID, requestType, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of getFDBalanceHostRes is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getFDBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}
	
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### DepositDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_DepositDtlsInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_DepositDtlsInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DepositDtlsInq_Succ_ErrorCode);
			}
			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received valid CustomerID response field value is :" + response.getCustomerId());}
					beanResponse.setCustomerID(response.getCustomerId());
					
					BankInfoType bankInfoType = response.getBankInfo();
					DepositAcctInfoType depositAcctInfoType = response.getDepositAcctInfo();
					
//					if(!util.isNullOrEmpty(bankInfoType)){
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received valid Account Officer response field value is :" + bankInfoType.getAccountOfficer());}
//						beanResponse.setAccountOfficer(bankInfoType.getAccountOfficer()+Constants.EMPTY_STRING);						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received valid Branch Name response field value is :" + bankInfoType.getBranchName());}
//						beanResponse.setBranchName(bankInfoType.getBranchName());
//					
//					}
					
					if(!util.isNullOrEmpty(depositAcctInfoType)){
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received Principal Amount response field value is :" + depositAcctInfoType.getPrincipalAmount());}
						beanResponse.setPrincipalAmount(depositAcctInfoType.getPrincipalAmount()+Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received Category response field value is :" + depositAcctInfoType.getCategory());}
						beanResponse.setCategory(depositAcctInfoType.getCategory()+Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received CCy response field value is :" + depositAcctInfoType.getCcy());}
						beanResponse.setCcy(depositAcctInfoType.getCcy());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received Date response field value is :" + depositAcctInfoType.getValueDate());}
						beanResponse.setValueDate(depositAcctInfoType.getValueDate()+Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received Deposit Mature Date response field value is :" + depositAcctInfoType.getDepMatureDt());}
						beanResponse.setMaturityDate(depositAcctInfoType.getDepMatureDt()+Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received tenure response field value is :" + depositAcctInfoType.getDepTenure());}
						beanResponse.setTenor(depositAcctInfoType.getDepTenure()+Constants.EMPTY_STRING);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received Interest rate response field value is :" + depositAcctInfoType.getInterestRate());}
						beanResponse.setInterestRate(depositAcctInfoType.getInterestRate()+Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Received maturity Amount response field value is :" + depositAcctInfoType.getMaturityAmount());}
						beanResponse.setMaturityAmount(depositAcctInfoType.getMaturityAmount()+Constants.EMPTY_STRING);
					}
					
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
				
			}
			
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at DepositDtlsInqDAOImpl.getFDBalanceHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at DepositDtlsInqDAOImpl.getFDBalanceHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: DepositDtlsInqDAOImpl.getFDBalanceHostRes()");}
		return beanResponse;
		
	}

}
