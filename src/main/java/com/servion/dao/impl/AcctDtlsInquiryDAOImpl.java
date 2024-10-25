package com.servion.dao.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.accountmanagementservice.AcctDetailsRespType;
import com.bankmuscat.esb.accountmanagementservice.AcctDtlsInquiryResType;
import com.bankmuscat.esb.commontypes.BalanceType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.AcctDtlsInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.accountBalance.AccountBalance_HostRes;
import com.servion.model.bankingFlashBalance.BankingBalanceFlashDetails_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.AcctDtlsInquiryService;
import com.servion.ws.util.DAOLayerUtils;

public class AcctDtlsInquiryDAOImpl implements AcctDtlsInquiryDAO {
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	AcctDtlsInquiryService acctDtlsInquiryService;

	public AcctDtlsInquiryService getAcctDtlsInquiryService() {
		return acctDtlsInquiryService;
	}

	public void setAcctDtlsInquiryService(
			AcctDtlsInquiryService acctDtlsInquiryService) {
		this.acctDtlsInquiryService = acctDtlsInquiryService;
	}
	
	
	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	
	

	@Override
	public AccountBalance_HostRes getAcctBalanceHostRes(CallInfo callInfo,
			String acctId, String deptAcctOfficerDtlFlg, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: AcctDtlsInquiry.getAcctBalanceHostRes()");}
		
		AccountBalance_HostRes beanResponse = new AccountBalance_HostRes();
		
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			AcctDtlsInquiryResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method acctDtlsInquiryService host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			
			response = acctDtlsInquiryService.callAccountDtlsInquiryBalance(logger, sessionID, acctId, deptAcctOfficerDtlFlg, requestType,str_UUI,generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callAccountDtlsInquiryBalance is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			beanResponse.setHostResponseCode(code);
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getAcctBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### AcctDtlsInquiryService HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
//			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode); 
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode);
			}

			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
			
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Account id  is "+ acctId);}
					beanResponse.setAcctID(acctId);
					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Ccy  is "+response.getAcctCcy() );}
//					beanResponse.setAcctCcy(response.getAcctCcy());
//					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Record Count  is "+ response.getRecordCount() + Constants.EMPTY_STRING);}
//					beanResponse.setRecordCount(response.getRecordCount() + Constants.EMPTY_STRING);
//					
					AcctDetailsRespType balanceInformationType = response.getAcctDetailsResponse();
					
					if(!util.isNullOrEmpty(balanceInformationType)){
						
						List<BalanceType> listBalanceType = balanceInformationType.getAcctBal();
						
						if(!util.isNullOrEmpty(listBalanceType)){
							BalanceType  balanceType = null;
							String balType = Constants.EMPTY_STRING;
							String balValue = Constants.EMPTY_STRING;
							
							for(int i=0; i <  listBalanceType.size(); i++){
								balanceType = listBalanceType.get(i);
								
								
								balType = balanceType.getBalType();
								balValue = balanceType.getBalAmt() + Constants.EMPTY;
								
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## The balance Type is  "+ balType);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## The balance Value is  "+ balValue);}
								
								if(Constants.ACCTDTLSINQ_BALANCETYPE_OPENACTUALBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## OPEN Actual Balance  is "+ balValue );}
									beanResponse.setOpenActualBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_OPENCLEAREDBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Open Cleared Balance  is "+ balValue );}
									beanResponse.setOpenClearedBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_ONLINEACTUALBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Online Actual Balance  is "+ balValue );}
									beanResponse.setOnLineActualBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_ONLINECLEAREDBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Online cleared Balance  is "+ balValue );}
									beanResponse.setOnlineClearedBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_WORKINGBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Working Balance  is "+ balValue );}
									beanResponse.setWorkingBalance(balValue);
									
								}
								else if (Constants.ACCTDTLSINQ_BALANCETYPE_UNCLEAREDBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Uncleared Balance  is "+ balValue );}
									beanResponse.setUnclearedBalance(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_BLOCKEDAMT.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Blocked Amt Balance  is "+ balValue);}
									beanResponse.setBlockedAmount(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_LOCKEDAMT.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Locked AMt Balance  is "+ balValue );}
									beanResponse.setLockedAmount(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_AVGBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Avg  Balance  is "+ balValue);}
									beanResponse.setAvgBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_MINBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Min Balance  is "+ balValue );}
									beanResponse.setMinBal(balValue);
									
								}
								else if (Constants.ACCTDTLSINQ_BALANCETYPE_AVAILBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Avail Balance  is "+ balValue );}
									beanResponse.setAvailBal(balValue);
									
								}
								
							}
							
						}
						
					}
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at AcctDtlsInquiry.getAcctBalanceHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AcctDtlsInquiry.getAcctBalanceHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: AcctDtlsInquiry.getAcctBalanceHostRes()");}
		return beanResponse;
		
	}

	@Override
	public BankingBalanceFlashDetails_HostRes getBankBalFlashHostRes(
			CallInfo callInfo, String acctId, String deptAcctOfficerDtlFlg, String requestType)
			throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: AcctDtlsInquiry.getBankBalFlashHostRes()");}
		
		BankingBalanceFlashDetails_HostRes beanResponse = new BankingBalanceFlashDetails_HostRes();
		
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			AcctDtlsInquiryResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method acctDtlsInquiryService host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = acctDtlsInquiryService.callAccountDtlsInquiryBalance(logger, sessionID, acctId, deptAcctOfficerDtlFlg, requestType, str_UUI,generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callAccountDtlsInquiryBalance is : "+code);}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			beanResponse.setHostResponseCode(code);
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getAcctBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### AcctDtlsInquiryService HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_AcctDltsInquiry_Succ_ErrorCode);
			}

			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
			
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Account id  is "+ acctId);}
					beanResponse.setAcctID(acctId);
					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Ccy  is "+response.getAcctCcy() );}
//					beanResponse.setAcctCcy(response.getAcctCcy());
//					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Record Count  is "+ response.getRecordCount() + Constants.EMPTY_STRING);}
//					beanResponse.setRecordCount(response.getRecordCount() + Constants.EMPTY_STRING);
//					
					AcctDetailsRespType balanceInformationType = response.getAcctDetailsResponse();
					
					if(!util.isNullOrEmpty(balanceInformationType)){
						
						List<BalanceType> listBalanceType = balanceInformationType.getAcctBal();
						
						if(!util.isNullOrEmpty(listBalanceType)){
							BalanceType  balanceType = null;
							String balType = Constants.EMPTY_STRING;
							String balValue = Constants.EMPTY_STRING;
							
							for(int i=0; i <  listBalanceType.size(); i++){
								balanceType = listBalanceType.get(i);
								
								
								balType = balanceType.getBalType();
								balValue = balanceType.getBalAmt() + Constants.EMPTY;
								
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## The balance Type is  "+ balType);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## The balance Value is  "+ balValue);}
								
								if(Constants.ACCTDTLSINQ_BALANCETYPE_OPENACTUALBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## OPEN Actual Balance  is "+ balValue );}
									beanResponse.setOpenActualBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_OPENCLEAREDBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Open Cleared Balance  is "+ balValue );}
									beanResponse.setOpenClearedBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_ONLINEACTUALBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Online Actual Balance  is "+ balValue );}
									beanResponse.setOnLineActualBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_ONLINECLEAREDBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Online cleared Balance  is "+ balValue );}
									beanResponse.setOnlineClearedBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_WORKINGBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Working Balance  is "+ balValue );}
									beanResponse.setWorkingBalance(balValue);
									
								}
								else if (Constants.ACCTDTLSINQ_BALANCETYPE_UNCLEAREDBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Uncleared Balance  is "+ balValue );}
									beanResponse.setUnclearedBalance(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_BLOCKEDAMT.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Blocked Amt Balance  is "+ balValue);}
									beanResponse.setBlockedAmount(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_LOCKEDAMT.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Locked AMt Balance  is "+ balValue );}
									beanResponse.setLockedAmount(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_AVGBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Avg  Balance  is "+ balValue);}
									beanResponse.setAvgBal(balValue);
									
								}
								else if(Constants.ACCTDTLSINQ_BALANCETYPE_MINBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Min Balance  is "+ balValue );}
									beanResponse.setMinBal(balValue);
									
								}
								else if (Constants.ACCTDTLSINQ_BALANCETYPE_AVAILBAL.equalsIgnoreCase(balType)){
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Avail Balance  is "+ balValue );}
									beanResponse.setAvailBal(balValue);
									
								}
								
							}
							
						}
						
					}
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at AcctDtlsInquiry.getBankBalFlashHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AcctDtlsInquiry.getBankBalFlashHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: AcctDtlsInquiry.getBankBalFlashHostRes()");}
		return beanResponse;
		
	}

	
	
}
