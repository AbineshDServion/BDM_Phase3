package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.accountmanagementservice.BalanceInformationType;
import com.bankmuscat.esb.accountmanagementservice.BankAcctMiniStmtResType;
import com.bankmuscat.esb.commontypes.StmtTxnInfoType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.LastNNumTransInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.recentTransactionBank.BankStatementInformation;
import com.servion.model.recentTransactionBank.RecentTransactionBank_HostRes;
import com.servion.model.transactionDetailBank.TransactionDetailsBank_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.LastNNumTransInquiryService;
import com.servion.ws.util.DAOLayerUtils;

public class LastNNumTransInquiryDAOImpl implements LastNNumTransInquiryDAO {
	private static Logger logger = LoggerObject.getLogger();
	
	
	@Autowired
	LastNNumTransInquiryService lastNNumTransInquiryService;
	
	
	public LastNNumTransInquiryService getLastNNumTransInquiryService() {
		return lastNNumTransInquiryService;
	}

	public void setLastNNumTransInquiryService(
			LastNNumTransInquiryService lastNNumTransInquiryService) {
		this.lastNNumTransInquiryService = lastNNumTransInquiryService;
	}

	
	
	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

		
	
//	@Override
//	public AccountBalance_HostRes getAcctBalanceHostRes(CallInfo callInfo,
//			String acctId, int noOfTxn, String requestType) throws DaoException {
//		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
//		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: LastNNumTransInquiryDAOImpl.getAcctBalanceHostRes()");}
//		
//		AccountBalance_HostRes beanResponse = new AccountBalance_HostRes();
//		
//		try{
//			String sessionID = (String)callInfo.getField(Field.SESSIONID);
//			
//			if(util.isNullOrEmpty(sessionID))
//				throw new DaoException("Session ID is null / empty");
//			
//			BankAcctMiniStmtResType response = null;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method lastNNumTransInquiryService host");}
//			
//			response = lastNNumTransInquiryService.callBankAccountBalanceHost(logger, sessionID, acctId, noOfTxn, requestType);
//			
//			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
//			String code = ws_ResponseHeader.getEsbErrCode();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBankAccountBalanceHost is : "+code);}
//			
//			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
//			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
//				throw new ServiceException("ICEGlobalConfig object is null");
//			}
//			
//			beanResponse.setHostResponseCode(code);
//			
//			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getBankBalFlashHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}
//
//
//			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode); 
//		
//			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
//			beanResponse.setErrorCode(code);
//			String hostEndTime = util.getCurrentDateTime();
//			beanResponse.setHostEndTime(hostEndTime);
//			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
//			
//			if(Constants.WS_SUCCESS_CODE.equals(code)){
//				if(!util.isNullOrEmpty(response)){
//			
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Account id  is "+ response.getAcctId());}
//					beanResponse.setAcctID(response.getAcctId());
//					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Ccy  is "+response.getAcctCcy() );}
//					beanResponse.setAcctCcy(response.getAcctCcy());
//					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Record Count  is "+ response.getRecordCount() + Constants.EMPTY_STRING);}
//					beanResponse.setRecordCount(response.getRecordCount() + Constants.EMPTY_STRING);
//					
//					BalanceInformationType balanceInformationType = response.getBalanceInfo();
//					
//					if(!util.isNullOrEmpty(balanceInformationType)){
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Actual Balance   is "+balanceInformationType.getActualBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setActualBalance(balanceInformationType.getActualBalance() + Constants.EMPTY_STRING);
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Available Balance  is "+balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setAvlblBalance(balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING);
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Uncleared Balance  is "+balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setUnClearedBalance(balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING);
//						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cleared Balance  is "+balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setClearedBalance(balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING);
//						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Forward Balance  is "+balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setForwardBalance(balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING);
//						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Hold Balance  is "+balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setHoldBalance(balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING);
//						
//					}
//				}
//			}
//		}catch(PersistenceException pe){
//			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at LastNNumTransInquiryDAOImpl.getAcctBalanceHostRes() "	+ pe.getMessage());}
//			throw new DaoException(pe);
//		}catch(Exception e){
//			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LastNNumTransInquiryDAOImpl.getAcctBalanceHostRes() "	+ e.getMessage());}
//			throw new DaoException(e);
//		}
//
//		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getAcctBalanceHostRes()");}
//		return beanResponse;
//		
//	}

//	@Override
//	public BankingBalanceFlashDetails_HostRes getBankBalFlashHostRes(
//			CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException {
//		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
//		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: LastNNumTransInquiryDAOImpl.getBankBalFlashHostRes()");}
//		
//		BankingBalanceFlashDetails_HostRes beanResponse = new BankingBalanceFlashDetails_HostRes();
//		
//		try{
//			String sessionID = (String)callInfo.getField(Field.SESSIONID);
//			
//			if(util.isNullOrEmpty(sessionID))
//				throw new DaoException("Session ID is null / empty");
//			
//			BankAcctMiniStmtResType response = null;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}
//			
//			response = lastNNumTransInquiryService.callBankAccountBalanceHost(logger, sessionID, acctId, noOfTxn, requestType);
//			
//			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
//			String code = ws_ResponseHeader.getEsbErrCode();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBankAccountBalanceHost is : "+code);}
//			
//			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
//			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
//				throw new ServiceException("ICEGlobalConfig object is null");
//			}
//			
//			beanResponse.setHostResponseCode(code);
//			
//			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getBankBalFlashHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}
//
//
//			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode); 
//		
//			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
//			beanResponse.setErrorCode(code);
//			String hostEndTime = util.getCurrentDateTime();
//			beanResponse.setHostEndTime(hostEndTime);
//			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
//			
//			if(Constants.WS_SUCCESS_CODE.equals(code)){
//				if(!util.isNullOrEmpty(response)){
//			
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Account id  is "+ response.getAcctId());}
//					beanResponse.setAcctID(response.getAcctId());
//					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Ccy  is "+response.getAcctCcy() );}
//					beanResponse.setAcctCcy(response.getAcctCcy());
//					
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Record Count  is "+ response.getRecordCount() + Constants.EMPTY_STRING);}
//					beanResponse.setRecordCount(response.getRecordCount() + Constants.EMPTY_STRING);
//					
//					BalanceInformationType balanceInformationType = response.getBalanceInfo();
//					
//					if(!util.isNullOrEmpty(balanceInformationType)){
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Actual Balance   is "+balanceInformationType.getActualBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setActualBalance(balanceInformationType.getActualBalance() + Constants.EMPTY_STRING);
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Available Balance  is "+balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setAvlblBalance(balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING);
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Uncleared Balance  is "+balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setUnClearedBalance(balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING);
//						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cleared Balance  is "+balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setClearedBalance(balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING);
//						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Forward Balance  is "+balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setForwardBalance(balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING);
//						
//						
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Hold Balance  is "+balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING );}
//						beanResponse.setHoldBalance(balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING);
//						
//					}
//				}
//			}
//		}catch(PersistenceException pe){
//			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at LastNNumTransInquiryDAOImpl.getBankBalFlashHostRes() "	+ pe.getMessage());}
//			throw new DaoException(pe);
//		}catch(Exception e){
//			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LastNNumTransInquiryDAOImpl.getBankBalFlashHostRes() "	+ e.getMessage());}
//			throw new DaoException(e);
//		}
//
//		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCEntityInquiryDAOImpl.getBankBalFlashHostRes()");}
//		return beanResponse;
//		
//	}

	@Override
	public RecentTransactionBank_HostRes getRecentTransBankHostRes(
			CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: LastNNumTransInquiryDAOImpl.getRecentTransBankHostRes()");}
		
		RecentTransactionBank_HostRes beanResponse = new RecentTransactionBank_HostRes();
		
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			BankAcctMiniStmtResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = lastNNumTransInquiryService.callBankAccountBalanceHost(logger, sessionID, acctId, noOfTxn, requestType, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBankAccountBalanceHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getBankBalFlashHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### LastNNumTransInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode);
			}
			
		
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
			
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Account id  is "+ response.getAcctId());}
					beanResponse.setAcctID(response.getAcctId());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Ccy  is "+response.getAcctCcy() );}
					beanResponse.setAcctCcy(response.getAcctCcy());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Record Count  is "+ response.getRecordCount() + Constants.EMPTY_STRING);}
					beanResponse.setRecordCount(response.getRecordCount() + Constants.EMPTY_STRING);
					
					List<StmtTxnInfoType> stmtTxnInfoTypeList = response.getStmtTxnInfo();
					StmtTxnInfoType stmtTxnInfoType = null;
					BankStatementInformation bankStatementInformation = null;
					ArrayList<BankStatementInformation> bankStatementInformationList = new ArrayList<BankStatementInformation>();
					
					if(!util.isNullOrEmpty(stmtTxnInfoTypeList)){
						for(int count = Constants.GL_ZERO; count<stmtTxnInfoTypeList.size();count++){
							bankStatementInformation = new BankStatementInformation();
							stmtTxnInfoType = stmtTxnInfoTypeList.get(count);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The "+count+" stmtTxnInfoType object is " + stmtTxnInfoType);}
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Transaction Type is "+ stmtTxnInfoType.getTxnType());}
							bankStatementInformation.setTxnType(stmtTxnInfoType.getTxnType());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Eff Date is "+stmtTxnInfoType.getEffDate() + Constants.EMPTY_STRING );}
							bankStatementInformation.setEffDate(stmtTxnInfoType.getEffDate());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Orig Date"+ stmtTxnInfoType.getOrigDate() + Constants.EMPTY_STRING);}
							bankStatementInformation.setOrigDate(stmtTxnInfoType.getOrigDate());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Posting Date"+ stmtTxnInfoType.getPostingDate() + Constants.EMPTY_STRING);}
							bankStatementInformation.setPostingDate(stmtTxnInfoType.getPostingDate());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Running Balance"+ stmtTxnInfoType.getRunningBalance() + Constants.EMPTY_STRING);}
							bankStatementInformation.setRunningBalance(stmtTxnInfoType.getRunningBalance() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Txn Amount is "+ stmtTxnInfoType.getTxnAmount() + Constants.EMPTY_STRING);}
							bankStatementInformation.setTxnAmount(stmtTxnInfoType.getTxnAmount() + Constants.EMPTY_STRING);
							
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Narrative is "+ stmtTxnInfoType.getNarrative() + Constants.EMPTY_STRING);}
							bankStatementInformation.setNarrative(stmtTxnInfoType.getNarrative() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Narrative Short is "+ stmtTxnInfoType.getNarrativeShort() + Constants.EMPTY_STRING);}
							bankStatementInformation.setNarrativeShort(stmtTxnInfoType.getNarrativeShort() + Constants.EMPTY_STRING);
							
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the BankStatmentInformation Object into the BankStmtInfoTypeList" + bankStatementInformation );}
							bankStatementInformationList.add(bankStatementInformation);
						}
					}
					
					beanResponse.setBankStmtTypeInfoList(bankStatementInformationList);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setted the Bank Stmt Type Info List into Bean object and its size is " + bankStatementInformationList.size() );}
					
					BalanceInformationType balanceInformationType = response.getBalanceInfo();
					
					if(!util.isNullOrEmpty(balanceInformationType)){
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Actual Balance   is "+balanceInformationType.getActualBalance() + Constants.EMPTY_STRING );}
						beanResponse.setActualBalance(balanceInformationType.getActualBalance() + Constants.EMPTY_STRING);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Available Balance  is "+balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING );}
						beanResponse.setAvlblBalance(balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Uncleared Balance  is "+balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING );}
						beanResponse.setUnClearedBalance(balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cleared Balance  is "+balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING );}
						beanResponse.setClearedBalance(balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Forward Balance  is "+balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING );}
						beanResponse.setForwardBalance(balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Hold Balance  is "+balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING );}
						beanResponse.setHoldBalance(balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING);
						
					}
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at LastNNumTransInquiryDAOImpl.getBankBalFlashHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LastNNumTransInquiryDAOImpl.getBankBalFlashHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: LastNNumTransInquiryDAOImpl.getRecentTransBankHostRes()");}
		return beanResponse;
		
	}

	@Override
	public TransactionDetailsBank_HostRes getTransactioDetailsBankHostRes(
			CallInfo callInfo, String acctId, int noOfTxn, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: LastNNumTransInquiryDAOImpl.getTransactioDetailsBankHostRes()");}
		
		TransactionDetailsBank_HostRes beanResponse = new TransactionDetailsBank_HostRes();
		
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			BankAcctMiniStmtResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = lastNNumTransInquiryService.callBankAccountBalanceHost(logger, sessionID, acctId, noOfTxn, requestType,str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBankAccountBalanceHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getBankBalFlashHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### LastNNumTransInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_LastNNumTransInq_Succ_ErrorCode);
			}
		
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
			
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Account id  is "+ response.getAcctId());}
					beanResponse.setAcctID(response.getAcctId());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Acct Ccy  is "+response.getAcctCcy() );}
					beanResponse.setAcctCcy(response.getAcctCcy());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Record Count  is "+ response.getRecordCount() + Constants.EMPTY_STRING);}
					beanResponse.setRecordCount(response.getRecordCount() + Constants.EMPTY_STRING);
					
					List<StmtTxnInfoType> stmtTxnInfoTypeList = response.getStmtTxnInfo();
					StmtTxnInfoType stmtTxnInfoType = null;
					
					com.servion.model.transactionDetailBank.BankStatementInformation bankStatementInformation = null;
					ArrayList<com.servion.model.transactionDetailBank.BankStatementInformation> bankStatementInformationList = new ArrayList<com.servion.model.transactionDetailBank.BankStatementInformation>();
					
					if(!util.isNullOrEmpty(stmtTxnInfoTypeList)){
						for(int count = Constants.GL_ZERO; count<stmtTxnInfoTypeList.size();count++){
							bankStatementInformation = new com.servion.model.transactionDetailBank.BankStatementInformation();
							stmtTxnInfoType = stmtTxnInfoTypeList.get(count);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The "+count+" stmtTxnInfoType object is " + stmtTxnInfoType);}
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Transaction Type is "+ stmtTxnInfoType.getTxnType());}
							bankStatementInformation.setTxnType(stmtTxnInfoType.getTxnType());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Eff Date is "+stmtTxnInfoType.getEffDate() + Constants.EMPTY_STRING );}
							bankStatementInformation.setEffDate(stmtTxnInfoType.getEffDate());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Orig Date"+ stmtTxnInfoType.getOrigDate() + Constants.EMPTY_STRING);}
							bankStatementInformation.setOrigDate(stmtTxnInfoType.getOrigDate());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Posting Date"+ stmtTxnInfoType.getPostingDate() + Constants.EMPTY_STRING);}
							bankStatementInformation.setPostingDate(stmtTxnInfoType.getPostingDate());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  is Running Balance"+ stmtTxnInfoType.getRunningBalance() + Constants.EMPTY_STRING);}
							bankStatementInformation.setRunningBalance(stmtTxnInfoType.getRunningBalance() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Txn Amount is "+ stmtTxnInfoType.getTxnAmount() + Constants.EMPTY_STRING);}
							bankStatementInformation.setTxnAmount(stmtTxnInfoType.getTxnAmount() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Narrative is "+ stmtTxnInfoType.getNarrative() + Constants.EMPTY_STRING);}
							bankStatementInformation.setNarrative(stmtTxnInfoType.getNarrative() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Narrative Short is "+ stmtTxnInfoType.getNarrativeShort() + Constants.EMPTY_STRING);}
							bankStatementInformation.setNarrativeShort(stmtTxnInfoType.getNarrativeShort() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the BankStatmentInformation Object into the BankStmtInfoTypeList" + bankStatementInformation );}
							bankStatementInformationList.add(bankStatementInformation);
						}
					}
					
					beanResponse.setBankStmtTypeInfoList(bankStatementInformationList);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the Bank Stmt Type info list into bean object and its size is " + bankStatementInformationList.size() );}
					
					BalanceInformationType balanceInformationType = response.getBalanceInfo();
					
					if(!util.isNullOrEmpty(balanceInformationType)){
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Actual Balance   is "+balanceInformationType.getActualBalance() + Constants.EMPTY_STRING );}
						beanResponse.setActualBalance(balanceInformationType.getActualBalance() + Constants.EMPTY_STRING);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Available Balance  is "+balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING );}
						beanResponse.setAvlblBalance(balanceInformationType.getAvlblBalance() + Constants.EMPTY_STRING);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Uncleared Balance  is "+balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING );}
						beanResponse.setUnClearedBalance(balanceInformationType.getUnClearedBalance() + Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cleared Balance  is "+balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING );}
						beanResponse.setClearedBalance(balanceInformationType.getClearedBalance() + Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Forward Balance  is "+balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING );}
						beanResponse.setForwardBalance(balanceInformationType.getForwardBalance() + Constants.EMPTY_STRING);
						
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Hold Balance  is "+balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING );}
						beanResponse.setHoldBalance(balanceInformationType.getHoldBalance() + Constants.EMPTY_STRING);
						
					}
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at LastNNumTransInquiryDAOImpl.getTransactioDetailsBankHostRess() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LastNNumTransInquiryDAOImpl.getTransactioDetailsBankHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: LastNNumTransInquiryDAOImpl.getTransactioDetailsBankHostRes()");}
		return beanResponse;
		
	}

}
