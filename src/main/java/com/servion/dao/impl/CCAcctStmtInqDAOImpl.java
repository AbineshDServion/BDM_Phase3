package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType;
import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType.StatementHeader;
import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType.StatementHeader.Address;
import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType.StatementHeader.Balance;
import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType.StatementHeader.Interest;
import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType.Transaction;
import com.bankmuscat.esb.cardmanagementservice.CCAcctStmtInqResType.TransactionDetails;
import com.bankmuscat.esb.cardmanagementservice.TransactionDetailsEntity;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CCAcctStmtInqDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.recentTransactionCards.CCTrxnDetails;
import com.servion.model.recentTransactionCards.CardStatementHeader;
import com.servion.model.recentTransactionCards.CardStmtDetails;
import com.servion.model.recentTransactionCards.Paging;
import com.servion.model.recentTransactionCards.RecentTransactionCards_HostRes;
import com.servion.model.recentTransactionCards.TransactionDetailEntity;
import com.servion.model.transactionDetaitCards.TransDtls_CCTrxnDetails;
import com.servion.model.transactionDetaitCards.TransDtls_CardStatementHeader;
import com.servion.model.transactionDetaitCards.TransDtls_CardStmtDetails;
import com.servion.model.transactionDetaitCards.TransDtls_Paging;
import com.servion.model.transactionDetaitCards.TransDtls_TransactionDetailEntity;
import com.servion.model.transactionDetaitCards.TransactionDetailCards_HostReq;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.CCAcctStmtInqService;
import com.servion.ws.util.DAOLayerUtils;

public class CCAcctStmtInqDAOImpl implements CCAcctStmtInqDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	CCAcctStmtInqService ccAcctStmtInqService;

	public CCAcctStmtInqService getCcAcctStmtInqService() {
		return ccAcctStmtInqService;
	}

	public void setCcAcctStmtInqService(CCAcctStmtInqService ccAcctStmtInqService) {
		this.ccAcctStmtInqService = ccAcctStmtInqService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public RecentTransactionCards_HostRes getRecentTransactionCardsHostRes(
			CallInfo callInfo, String statementType, String reqType,
			String returnContent, XMLGregorianCalendar startDate,
			XMLGregorianCalendar endDate, String cardEmbossNo, String ccyCodeType,String groupTrxn, String entitySize) throws DaoException{

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes()");}

		RecentTransactionCards_HostRes beanResponse = new RecentTransactionCards_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CCAcctStmtInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callAccountStmtTransactionHost host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = ccAcctStmtInqService.callAccountStmtTransactionHost(logger, sessionID, statementType, reqType, returnContent, startDate, endDate, 
					cardEmbossNo, ccyCodeType, groupTrxn, entitySize,str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callAccountStmtTransactionHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getRecentTransactionCardsHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCAcctStmt HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCAcctStmtInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCAcctStmtInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CCAcctStmtInq_Succ_ErrorCode);
			}

			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for the Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){

				if(!util.isNullOrEmpty(response)){

					List<Transaction> transactionList = response.getTransaction();
					Transaction hostTransaction = null;
					List<TransactionDetailsEntity> transactionDetailsEntityList = null;
					TransactionDetailsEntity transactionDetailsEntity = null;

					//Bean Transaction Details
					com.servion.model.recentTransactionCards.Transaction beanTransation = new com.servion.model.recentTransactionCards.Transaction();

					HashMap<String, ArrayList<CCTrxnDetails>>transactionMap = new HashMap<String, ArrayList<CCTrxnDetails>>();
					ArrayList<CCTrxnDetails> ccTrxnDetailsList = new ArrayList<CCTrxnDetails>();
					CCTrxnDetails ccTrxnDetails = null;
					String number = Constants.EMPTY_STRING;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Transaction details for the number is "+code);}

					if(transactionList != null){
						for(int i = 0; i < transactionList.size() ; i++){

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, i + " object of transactionList "+ transactionList.get(i));}
							hostTransaction = transactionList.get(i);

							number = hostTransaction.getNumber();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response ## Transaction Details for the number  "+ util.getSubstring(number, Constants.GL_FOUR));}

							transactionDetailsEntityList = hostTransaction.getDetails();

							if(!util.isNullOrEmpty(transactionDetailsEntityList)){
								for(int j=0; j< transactionDetailsEntityList.size(); j++){
									 ccTrxnDetails = new CCTrxnDetails();
									transactionDetailsEntity = transactionDetailsEntityList.get(j);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, j + "Object of the transactionDetailsEntity is "+ transactionDetailsEntity);}

									ccTrxnDetails.setPostingDate(transactionDetailsEntity.getPostingDate());
									ccTrxnDetails.setAmount(transactionDetailsEntity.getAmount() + Constants.EMPTY_STRING);
									ccTrxnDetails.setOrigMsgType(transactionDetailsEntity.getOrigMsgType());
									ccTrxnDetails.setMsgType(transactionDetailsEntity.getMsgType());
									ccTrxnDetails.setProcCode(transactionDetailsEntity.getProcCode());
									ccTrxnDetails.setTransactionCurrency(transactionDetailsEntity.getTransactionCurrency());
									ccTrxnDetails.setTrxnAmount(transactionDetailsEntity.getTrxnAmount() + Constants.EMPTY_STRING);
									ccTrxnDetails.setTrxnDate(transactionDetailsEntity.getTrxnDate());
									ccTrxnDetails.setMcc(transactionDetailsEntity.getMCC());
									ccTrxnDetails.setMerNumber(transactionDetailsEntity.getMerNumber());
									ccTrxnDetails.setMerName(transactionDetailsEntity.getMerName());
									ccTrxnDetails.setMerCountry(transactionDetailsEntity.getMerCountry());
									ccTrxnDetails.setTransactionDesc(transactionDetailsEntity.getTransactionDescription());
									ccTrxnDetails.setMerCity(transactionDetailsEntity.getMerCity());
									ccTrxnDetails.setTrxnTime(transactionDetailsEntity.getTrxnTime());
									
									ccTrxnDetailsList.add(ccTrxnDetails);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, j + "Adding the ccTrxnDetails object into the list, the list size is "+ ccTrxnDetailsList.size());}
								}
							}
						}
						transactionMap.put(number, ccTrxnDetailsList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Transaction Details Entity list into a HashMap and the hashmap size is "+ transactionMap.size());}
					}

					beanTransation.setTransactionMap(transactionMap);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setted the Transaction Details map into the bean class and its size"+ transactionMap.size());}
					beanResponse.setTransaction(beanTransation);

					TransactionDetails transactionDetails = response.getTransactionDetails();
					ccTrxnDetailsList = new ArrayList<CCTrxnDetails>();
					TransactionDetailEntity transactionDetailEntity = new TransactionDetailEntity();
					
					if(!util.isNullOrEmpty(transactionDetails)){
						transactionDetailsEntityList = transactionDetails.getTransactionDetailsEntity();
						
						if(!util.isNullOrEmpty(transactionDetailsEntityList)){
							
							for(int k=0; k<transactionDetailsEntityList.size(); k++){
								ccTrxnDetails = new CCTrxnDetails();
								transactionDetailsEntity = transactionDetailsEntityList.get(k);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, k + "Object of the transactionDetailsEntity is "+ transactionDetailsEntity);}

								ccTrxnDetails.setPostingDate(transactionDetailsEntity.getPostingDate());
								ccTrxnDetails.setAmount(transactionDetailsEntity.getAmount() + Constants.EMPTY_STRING);
								ccTrxnDetails.setOrigMsgType(transactionDetailsEntity.getOrigMsgType());
								ccTrxnDetails.setMsgType(transactionDetailsEntity.getMsgType());
								ccTrxnDetails.setProcCode(transactionDetailsEntity.getProcCode());
								ccTrxnDetails.setTransactionCurrency(transactionDetailsEntity.getTransactionCurrency());
								ccTrxnDetails.setTrxnAmount(transactionDetailsEntity.getTrxnAmount() + Constants.EMPTY_STRING);
								ccTrxnDetails.setTrxnDate(transactionDetailsEntity.getTrxnDate());
								ccTrxnDetails.setMcc(transactionDetailsEntity.getMCC());
								ccTrxnDetails.setMerNumber(transactionDetailsEntity.getMerNumber());
								ccTrxnDetails.setMerName(transactionDetailsEntity.getMerName());
								ccTrxnDetails.setMerCountry(transactionDetailsEntity.getMerCountry());
								ccTrxnDetails.setTransactionDesc(transactionDetailsEntity.getTransactionDescription());
								ccTrxnDetails.setMerCity(transactionDetailsEntity.getMerCity());
								ccTrxnDetails.setTrxnTime(transactionDetailsEntity.getTrxnTime());
								
								ccTrxnDetailsList.add(ccTrxnDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, k + "Adding the ccTrxnDetails object into the list, the list size is "+ ccTrxnDetailsList.size());}
								
							}
						}
						transactionDetailEntity.setTransactionEntityList(ccTrxnDetailsList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"Adding the Transaction Details Entity list into a List and the List size is "+ ccTrxnDetailsList.size());}
					}

					beanResponse.setTransactionDetailEntity(transactionDetailEntity);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the transaction Detail Entity in the bean response");}
				
					CardStatementHeader cardstatementHeader = new CardStatementHeader();
					HashMap<String, CardStmtDetails> cardStmtDetailMap = new HashMap<String, CardStmtDetails>();
					CardStmtDetails cardStmtDetails = new CardStmtDetails();
					
					StatementHeader statementHeader = response.getStatementHeader();
					Address address = null;
					Balance balance = null;
					Interest interest = null;
					
					if(!util.isNullOrEmpty(statementHeader)){
						
						number = statementHeader.getAccountNo();
						address = statementHeader.getAddress();
						balance = statementHeader.getBalance();
						interest = statementHeader.getInterest();
						
						cardStmtDetails.setTitle(address.getTitle());
						cardStmtDetails.setFirstName(address.getFirstName());
						cardStmtDetails.setMiddleName(address.getMiddleName());
						cardStmtDetails.setLastName(address.getLastName());
						cardStmtDetails.setAddr1(address.getAddress1());
						cardStmtDetails.setAddr2(address.getAddress2());
						cardStmtDetails.setAddr3(address.getAddress3());
						cardStmtDetails.setAddr4(address.getAddress4());
						cardStmtDetails.setAddr5(address.getAddress5());
						cardStmtDetails.setCity(address.getCity());
						cardStmtDetails.setCountry(address.getCountry());
						cardStmtDetails.setZip(address.getZIP());
						/**
						 * 26th Feb 2017
						 * Issue#7125 As per BM comments, Balance will not come for this enquiry. So, we have handled the balance fetching.
						 * */
						if(balance!=null){
						cardStmtDetails.setOpeningBalance(balance.getOpeningBalance() + Constants.EMPTY_STRING);
						cardStmtDetails.setClosingBalance(balance.getClosingBalance()+ Constants.EMPTY_STRING);
						cardStmtDetails.setRewardPoint(balance.getRewardPoints()+ Constants.EMPTY_STRING);
						cardStmtDetails.setCurrency(balance.getCurrency());
						cardStmtDetails.setMinDueAmt(balance.getMinDueAmount()+ Constants.EMPTY_STRING);
						cardStmtDetails.setOverDueAmt(balance.getOverDueAmount()+ Constants.EMPTY_STRING);
						cardStmtDetails.setDueDate(balance.getDueDate());
						cardStmtDetails.setCreditLimit(balance.getCreditLimit()+ Constants.EMPTY_STRING);
						cardStmtDetails.setBankAcctNumber(balance.getBankAccNumber());
						cardStmtDetails.setPointsAdded(balance.getPointsAdded()+ Constants.EMPTY_STRING);
						cardStmtDetails.setPointsRedeemed(balance.getPointsRedeemed()+ Constants.EMPTY_STRING);
						cardStmtDetails.setTotalPoints(balance.getTotalPoints()+ Constants.EMPTY_STRING);
						cardStmtDetails.setPrintDueDate(balance.getPrintDueDate());
						}
						/**
						 * 28th Feb 2017
						 * Issue#7125 As per BM comments, Balance will not come for this enquiry. So, we have handled the balance fetching.
						 * */
						if(interest!=null){
						cardStmtDetails.setInterestAmount(interest.getInterestAmount()+ Constants.EMPTY_STRING);
						cardStmtDetails.setOpeningInterest(interest.getOpeningInterest()+ Constants.EMPTY_STRING);
						}
						cardStmtDetailMap.put(number, cardStmtDetails);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a hashmap" + cardStmtDetailMap);}
						
						cardstatementHeader.setCardStamtDetailMap(cardStmtDetailMap);
					}
					
					beanResponse.setCardStatementHeader(cardstatementHeader);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a Beanresponse class" + cardstatementHeader);}
					
					
					Paging paging = new Paging();
					com.bankmuscat.esb.cardmanagementservice.Paging hostPaging= response.getPaging();
					
					if(!util.isNullOrEmpty(hostPaging)){
						paging.setKey(hostPaging.getKey());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a hashmap" + cardStmtDetailMap);}
						
						paging.setSize(hostPaging.getSize() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a hashmap" + cardStmtDetailMap);}
						
					}
					
					beanResponse.setPagingInfo(paging);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the paging object into bean calss");}
					
					beanResponse.setAddlMsg(response.getAddlMsg() + Constants.EMPTY_STRING);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "AddlMsg is " + response.getAddlMsg());}
					
				}
			
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CCAcctStmtInq service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes()");}
		return beanResponse;
	}


	@Override
	public TransactionDetailCards_HostReq getTransactionDeatilCardsHostRes (
			CallInfo callInfo, String statementType, String reqType,
			String returnContent, XMLGregorianCalendar startDate,
			XMLGregorianCalendar endDate, String cardAcctNo, String cardEmbossNo, String ccyCodeType, String groupTrxn) throws DaoException{

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes()");}

		TransactionDetailCards_HostReq beanResponse = new TransactionDetailCards_HostReq();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CCAcctStmtInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callAccountStmtTransactionHost host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = ccAcctStmtInqService.callAccountStmtTransactionHost(logger, sessionID, statementType, reqType, returnContent, startDate, endDate, cardAcctNo, 
					cardEmbossNo, ccyCodeType, groupTrxn,str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callAccountStmtTransactionHost is : "+code);}

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getRecentTransactionCardsHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCAcctStmt HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCAcctStmtInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCAcctStmtInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CCAcctStmtInq_Succ_ErrorCode);
			}
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for the Application layer is "+code);}
			String sourceNo = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The selected credit card number is "+ util.maskCardOrAccountNumber(sourceNo));}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					List<Transaction> transactionList = response.getTransaction();
					Transaction hostTransaction = null;
					List<TransactionDetailsEntity> transactionDetailsEntityList = null;
					TransactionDetailsEntity transactionDetailsEntity = null;

					//Bean Transaction Details
					com.servion.model.transactionDetaitCards.TransDtls_Transaction beanTransation = new com.servion.model.transactionDetaitCards.TransDtls_Transaction();

					HashMap<String, ArrayList<TransDtls_CCTrxnDetails>>transactionMap = new HashMap<String, ArrayList<TransDtls_CCTrxnDetails>>();
					ArrayList<TransDtls_CCTrxnDetails> ccTrxnDetailsList = new ArrayList<TransDtls_CCTrxnDetails>();
					TransDtls_CCTrxnDetails ccTrxnDetails = null;
					String number = Constants.EMPTY_STRING;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Transaction details for the number is "+code);}

					if(transactionList != null){
						for(int i = 0; i < transactionList.size() ; i++){

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, i + " object of transactionList "+ transactionList.get(i));}
							hostTransaction = transactionList.get(i);

							number = hostTransaction.getNumber();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response ## Transaction Details for the number  "+ util.getSubstring(number, Constants.GL_FOUR));}

							transactionDetailsEntityList = hostTransaction.getDetails();

							if(!util.isNullOrEmpty(transactionDetailsEntityList)){
								for(int j=0; j< transactionDetailsEntityList.size(); j++){
									ccTrxnDetails = new TransDtls_CCTrxnDetails();
									transactionDetailsEntity = transactionDetailsEntityList.get(j);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, j + "Object of the transactionDetailsEntity is "+ transactionDetailsEntity);}

									ccTrxnDetails.setPostingDate(transactionDetailsEntity.getPostingDate());
									ccTrxnDetails.setAmount(transactionDetailsEntity.getAmount() + Constants.EMPTY_STRING);
									ccTrxnDetails.setOrigMsgType(transactionDetailsEntity.getOrigMsgType());
									ccTrxnDetails.setMsgType(transactionDetailsEntity.getMsgType());
									ccTrxnDetails.setProcCode(transactionDetailsEntity.getProcCode());
									ccTrxnDetails.setTransactionCurrency(transactionDetailsEntity.getTransactionCurrency());
									ccTrxnDetails.setTrxnAmount(transactionDetailsEntity.getTrxnAmount() + Constants.EMPTY_STRING);
									ccTrxnDetails.setTrxnDate(transactionDetailsEntity.getTrxnDate());
									ccTrxnDetails.setMcc(transactionDetailsEntity.getMCC());
									ccTrxnDetails.setMerNumber(transactionDetailsEntity.getMerNumber());
									ccTrxnDetails.setMerName(transactionDetailsEntity.getMerName());
									ccTrxnDetails.setMerCountry(transactionDetailsEntity.getMerCountry());
									ccTrxnDetails.setTransactionDesc(transactionDetailsEntity.getTransactionDescription());
									ccTrxnDetails.setMerCity(transactionDetailsEntity.getMerCity());
									ccTrxnDetails.setTrxnTime(transactionDetailsEntity.getTrxnTime());

									ccTrxnDetailsList.add(ccTrxnDetails);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, j + "Adding the ccTrxnDetails object into the list, the list size is "+ ccTrxnDetailsList.size());}
								}
							}
						}
						//transactionMap.put(number, ccTrxnDetailsList);
						//TODO: As per vijay comment need to map with requested card number
						transactionMap.put(sourceNo, ccTrxnDetailsList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Transaction Details Entity list into a HashMap with key as card number and the hashmap size is "+ transactionMap.size());}
					}

					beanTransation.setTransactionMap(transactionMap);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setted the Transaction Details map into the bean class and its size"+ transactionMap.size());}
					beanResponse.setTransDtls_Transaction(beanTransation);
					
					
					TransactionDetails transactionDetails = response.getTransactionDetails();
					ccTrxnDetailsList = new ArrayList<TransDtls_CCTrxnDetails>();
					TransDtls_TransactionDetailEntity transactionDetailEntity = new TransDtls_TransactionDetailEntity();

					if(!util.isNullOrEmpty(transactionDetails)){
						transactionDetailsEntityList = transactionDetails.getTransactionDetailsEntity();

						if(!util.isNullOrEmpty(transactionDetailsEntityList)){

							for(int k=0; k<transactionDetailsEntityList.size(); k++){
								ccTrxnDetails = new TransDtls_CCTrxnDetails();
								transactionDetailsEntity = transactionDetailsEntityList.get(k);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, k + "Object of the transactionDetailsEntity is "+ transactionDetailsEntity);}

								ccTrxnDetails.setPostingDate(transactionDetailsEntity.getPostingDate());
								ccTrxnDetails.setAmount(transactionDetailsEntity.getAmount() + Constants.EMPTY_STRING);
								ccTrxnDetails.setOrigMsgType(transactionDetailsEntity.getOrigMsgType());
								ccTrxnDetails.setMsgType(transactionDetailsEntity.getMsgType());
								ccTrxnDetails.setProcCode(transactionDetailsEntity.getProcCode());
								ccTrxnDetails.setTransactionCurrency(transactionDetailsEntity.getTransactionCurrency());
								ccTrxnDetails.setTrxnAmount(transactionDetailsEntity.getTrxnAmount() + Constants.EMPTY_STRING);
								ccTrxnDetails.setTrxnDate(transactionDetailsEntity.getTrxnDate());
								ccTrxnDetails.setMcc(transactionDetailsEntity.getMCC());
								ccTrxnDetails.setMerNumber(transactionDetailsEntity.getMerNumber());
								ccTrxnDetails.setMerName(transactionDetailsEntity.getMerName());
								ccTrxnDetails.setMerCountry(transactionDetailsEntity.getMerCountry());
								ccTrxnDetails.setTransactionDesc(transactionDetailsEntity.getTransactionDescription());
								ccTrxnDetails.setMerCity(transactionDetailsEntity.getMerCity());
								ccTrxnDetails.setTrxnTime(transactionDetailsEntity.getTrxnTime());

								ccTrxnDetailsList.add(ccTrxnDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, k + "Adding the ccTrxnDetails object into the list, the list size is "+ ccTrxnDetailsList.size());}

							}
						}
						transactionDetailEntity.setTransactionEntityList(ccTrxnDetailsList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"Adding the Transaction Details Entity list into a List and the List size is "+ ccTrxnDetailsList.size());}

					}

					beanResponse.setTransDtls_TransactionDetailsEntity(transactionDetailEntity);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the transaction Detail Entity in the bean response");}

					TransDtls_CardStatementHeader cardstatementHeader = new TransDtls_CardStatementHeader();
					HashMap<String, TransDtls_CardStmtDetails> cardStmtDetailMap = new HashMap<String, TransDtls_CardStmtDetails>();
					TransDtls_CardStmtDetails cardStmtDetails = new TransDtls_CardStmtDetails();

					StatementHeader statementHeader = response.getStatementHeader();
					Address address = null;
					Balance balance = null;
					Interest interest = null;

					if(!util.isNullOrEmpty(statementHeader)){

						number = statementHeader.getAccountNo();
						address = statementHeader.getAddress();
						balance = statementHeader.getBalance();
						interest = statementHeader.getInterest();

						cardStmtDetails.setTitle(address.getTitle());
						cardStmtDetails.setFirstName(address.getFirstName());
						cardStmtDetails.setMiddleName(address.getMiddleName());
						cardStmtDetails.setLastName(address.getLastName());
						cardStmtDetails.setAddr1(address.getAddress1());
						cardStmtDetails.setAddr2(address.getAddress2());
						cardStmtDetails.setAddr3(address.getAddress3());
						cardStmtDetails.setAddr4(address.getAddress4());
						cardStmtDetails.setAddr5(address.getAddress5());
						cardStmtDetails.setCity(address.getCity());
						cardStmtDetails.setCountry(address.getCountry());
						cardStmtDetails.setZip(address.getZIP());

						cardStmtDetails.setOpeningBalance(balance.getOpeningBalance() + Constants.EMPTY_STRING);
						cardStmtDetails.setClosingBalance(balance.getClosingBalance()+ Constants.EMPTY_STRING);
						cardStmtDetails.setRewardPoint(balance.getRewardPoints()+ Constants.EMPTY_STRING);
						cardStmtDetails.setCurrency(balance.getCurrency());
						cardStmtDetails.setMinDueAmt(balance.getMinDueAmount()+ Constants.EMPTY_STRING);
						cardStmtDetails.setOverDueAmt(balance.getOverDueAmount()+ Constants.EMPTY_STRING);
						cardStmtDetails.setDueDate(balance.getDueDate());
						cardStmtDetails.setCreditLimit(balance.getCreditLimit()+ Constants.EMPTY_STRING);
						cardStmtDetails.setBankAcctNumber(balance.getBankAccNumber());
						cardStmtDetails.setPointsAdded(balance.getPointsAdded()+ Constants.EMPTY_STRING);
						cardStmtDetails.setPointsRedeemed(balance.getPointsRedeemed()+ Constants.EMPTY_STRING);
						cardStmtDetails.setTotalPoints(balance.getTotalPoints()+ Constants.EMPTY_STRING);
						cardStmtDetails.setPrintDueDate(balance.getPrintDueDate());

						cardStmtDetails.setInterestAmount(interest.getInterestAmount()+ Constants.EMPTY_STRING);
						cardStmtDetails.setOpeningInterest(interest.getOpeningInterest()+ Constants.EMPTY_STRING);

						cardStmtDetailMap.put(number, cardStmtDetails);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a hashmap" + cardStmtDetailMap);}

						cardstatementHeader.setCardStamtDetailMap(cardStmtDetailMap);
					}

					beanResponse.setTransDtls_CardStatementHeader(cardstatementHeader);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a Beanresponse class" + cardstatementHeader);}


					TransDtls_Paging paging = new TransDtls_Paging();
					com.bankmuscat.esb.cardmanagementservice.Paging hostPaging= response.getPaging();

					if(!util.isNullOrEmpty(hostPaging)){
						paging.setKey(hostPaging.getKey());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a hashmap" + cardStmtDetailMap);}

						paging.setSize(hostPaging.getSize() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Card Statement Detail map into a hashmap" + cardStmtDetailMap);}

					}

					beanResponse.setTransDtls_Paging(paging);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the paging object into bean calss");}

					beanResponse.setAddlMsg(response.getAddlMsg() + Constants.EMPTY_STRING);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "AddlMsg is " + response.getAddlMsg());}

				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CCAcctStmtInq service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CCAcctStmtInqDAOImpl.getRecentTransactionCardsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCAcctStmtInqDAOImpl.getTransactionDeatilCardsHostRes()");}
		return beanResponse;
	}

}
