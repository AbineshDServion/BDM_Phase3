package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CCAcctStmtInqDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.recentTransactionCards.CCTrxnDetails;
import com.servion.model.recentTransactionCards.RecentTransactionCards_HostRes;
import com.servion.model.recentTransactionCards.Transaction;
import com.servion.model.reporting.HostReportDetails;

public class RecentTransactionCardsImpl implements IRecentTransactionCards{
	private static Logger logger = LoggerObject.getLogger();

	private CCAcctStmtInqDAO ccAcctStmtInqDAO;
	public CCAcctStmtInqDAO getCcAcctStmtInqDAO() {
		return ccAcctStmtInqDAO;
	}

	public void setCcAcctStmtInqDAO(CCAcctStmtInqDAO ccAcctStmtInqDAO) {
		this.ccAcctStmtInqDAO = ccAcctStmtInqDAO;
	}

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	@Override
	public String getRecentTransactionsCards(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RecentTransactionCardsImpl.getRecentTransactionsCards()");}
		String code = Constants.EMPTY_STRING;
		//		getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			//			ICEFeatureData ivr_ICEFeatureDate = (ICEFeatureData) callInfo.getICEFeatureData();
			//
			//			if(util.isNullOrEmpty(ivr_ICEFeatureDate)){
			//				throw new ServiceException("ivr_ICEFeatureDate object is null");
			//			}


			String cardEmbossNum = null;

			//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
			//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
			//					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber())){
			//						cardEmbossNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber();
			//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card Emboss Number is " + cardEmbossNum);}
			//					}
			//				}
			//
			//			}


//			String cardAcctNo = (String)callInfo.getField(Field.SRCNO);
//			if(!util.isNullOrEmpty(cardAcctNo))
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number ending with " + util.getSubstring(cardAcctNo, Constants.GL_FOUR));}

			//TODO - CC
			cardEmbossNum = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number ending with " + util.maskCardOrAccountNumber(cardEmbossNum));}

//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
//					cardAcctNo = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getFirstCCAccountNo();
//				}
//			}


			/**
			 * For Credit Card Account Statement Request Host access , below are the constants values need to be sent
			 */
			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("ICEGlobal object is null or empty");
			}

			String statementType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_STMTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Statement Type" + statementType);}

			String reqType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_REQTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Req Type " + reqType);}

			String returnContent = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_RETURNCONTENT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured returnContent" + returnContent);}

			String ccyCodeType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_CCYCODETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CcycodeType" + ccyCodeType);}

			String groupTrxn = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_GROUPTRXN);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Group Trxn" + groupTrxn);}

			String entitySize = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_CCSTMTENQUIRY_SIZE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured entitySize" + entitySize);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the start Date and end Date value as null, since the requested statement type is mini");}


			XMLGregorianCalendar startDate = null;

			XMLGregorianCalendar endDate = null;

			/**
			 * Setting the start Date and end date as null
			 */
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_REQTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCSTMTENQUIRY_REQTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);

			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCACCTSTMTINQ);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(startTime); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting
			
			/*
			 *  Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);
			
			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);
			
			/* END */

			RecentTransactionCards_HostRes recentTransactionCards_HostRes = ccAcctStmtInqDAO.getRecentTransactionCardsHostRes(callInfo, statementType, reqType, 
					returnContent, startDate, endDate, cardEmbossNum, ccyCodeType, groupTrxn, entitySize);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CallerIdenf_DebitCardDetails Object is :"+ recentTransactionCards_HostRes);}
			callInfo.setRecentTransactionCards_HostRes(recentTransactionCards_HostRes);

			code = recentTransactionCards_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = recentTransactionCards_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = recentTransactionCards_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String duration = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The host time duration result is " + duration);}
			
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
						
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(cardEmbossNum)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + callInfo.getField(Field.SRCTYPE)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + statementType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION + Constants.EQUALTO + duration
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			
			
			
			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
				int totTransAvail = Constants.GL_ZERO;
				boolean transAvail = false;
				if(!util.isNullOrEmpty(recentTransactionCards_HostRes)){

					if(!util.isNullOrEmpty(recentTransactionCards_HostRes.getTransactionDetailEntity()) && 
							!util.isNullOrEmpty(recentTransactionCards_HostRes.getTransaction().getTransactionMap())){

						transAvail = recentTransactionCards_HostRes.getTransaction().getTransactionMap().size() > Constants.GL_ZERO;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "is Transaction available is " + transAvail);}

						totTransAvail = recentTransactionCards_HostRes.getTransaction().getTransactionMap().size(); 
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Transaction available is " + recentTransactionCards_HostRes.getTransaction().getTransactionMap().size());}
						
					}
				}
				
				callInfo.setField(Field.NOOFTRANSACTION,totTransAvail);
				
				if(transAvail)
					callInfo.setField(Field.ISTRANSACTIONAVAILABLE, true);
				else
					callInfo.setField(Field.ISTRANSACTIONAVAILABLE, false);
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(recentTransactionCards_HostRes.getErrorDesc()) ?"NA" :recentTransactionCards_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for CCAcctStmtInq service");}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Update CCAcctStmtInq host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + recentTransactionCards_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCACCTSTMTINQ, recentTransactionCards_HostRes.getHostResponseCode());
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.getCallerIdentification() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getRecentTransactionsCardsPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RecentTransactionCardsImpl.getRecentTransactionsCardsPhrases()");}
		String str_GetMessage, finalResult = Constants.EMPTY_STRING;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Global Object values");}
			ICEGlobalConfig ivr_ICEGlobal = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			String transactionAnncOrder = Constants.EMPTY_STRING;
			int noOfTransaction = Constants.GL_FIVE;

			transactionAnncOrder = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TRANSACTIONANNOUNCEORDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The transaction Order is " + transactionAnncOrder);}

			noOfTransaction = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NOOFTRANSFORANNC))?
					Constants.GL_FIVE:Integer.parseInt((String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NOOFTRANSFORANNC));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The No of Transaction can be announced" + noOfTransaction);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			ArrayList<CCTrxnDetails> cardStmtTypeInfoList = null;
			CCTrxnDetails cardStatementInformation = null;

			HashMap<Long, CCTrxnDetails> trxnDateMap = new HashMap<Long, CCTrxnDetails>();
			HashMap<String, ArrayList<CCTrxnDetails>> transactionMap = null;
			ArrayList<Long> trxnDateList = new ArrayList<Long>();
			Transaction transaction = null;
			Iterator iter = null;
			Map.Entry entry = null;

			if(!util.isNullOrEmpty(callInfo.getRecentTransactionCards_HostRes()) && !util.isNullOrEmpty(callInfo.getRecentTransactionCards_HostRes().getTransaction())
					&& !util.isNullOrEmpty(callInfo.getRecentTransactionCards_HostRes().getTransaction().getTransactionMap())){

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the transaction map object" + callInfo.getRecentTransactionCards_HostRes().getTransaction().getTransactionMap());}

				transaction = (Transaction)callInfo.getRecentTransactionCards_HostRes().getTransaction();
				//				cardStmtTypeInfoList = callInfo.getRecentTransactionCards_HostRes().getTransactionDetailEntity().getTransactionEntityList();
				transactionMap = transaction.getTransactionMap();

				iter = (Iterator) transactionMap.entrySet().iterator();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The iterator value for transaction map is " + iter);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Iterator hasnext ? " + iter.hasNext());}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "transactionMap.size() " + transactionMap.size());}

				if(transactionMap.size() > Constants.GL_ZERO){
					entry = (Map.Entry) iter.next();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The next iterator value is  " + entry);}
					cardStmtTypeInfoList = (ArrayList<CCTrxnDetails>)entry.getValue();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "cardStmtTypeInfoList is " + cardStmtTypeInfoList);}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Stmt Type Info" + cardStmtTypeInfoList);}

				if(!util.isNullOrEmpty(cardStmtTypeInfoList)){
					Date tempDate = null;
					String strTempDate = null;
					XMLGregorianCalendar xmlGregorianCalendar = null;
					int sec = Constants.GL_ZERO;

					for(int count = Constants.GL_ZERO; count < cardStmtTypeInfoList.size() ; count++) {
						cardStatementInformation = cardStmtTypeInfoList.get(count);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " Cards Transaction type is " + cardStatementInformation.getTransactionDesc());}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " Cards Transaction Amount is " + cardStatementInformation.getAmount());}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " Cards Transaction Date is " + cardStatementInformation.getPostingDate());}

						if(cardStatementInformation.getPostingDate()!=null){
							strTempDate = cardStatementInformation.getPostingDate();
							xmlGregorianCalendar = util.convertDateStringtoXMLGregCalendar(strTempDate, Constants.DATEFORMAT_YYYY_MM_DD);
							tempDate = xmlGregorianCalendar.toGregorianCalendar().getTime();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Convertered Date format is " + tempDate);}
							
							/**
							 * Following are the fixes done by vinoth on 12-Aug-2014 for Prodcution fixes
							 */
							sec = tempDate.getSeconds();
							sec = sec + 60;	
							tempDate.setSeconds(sec);
							//END - Vinoth

							if(trxnDateMap.containsKey(tempDate.getTime())){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Transaction Map / List contains the same key already " + tempDate);}
								sec = tempDate.getSeconds();
								
								sec = sec - Constants.GL_ONE;	
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Second has decremented by one" + sec);}

								tempDate.setSeconds(sec);
								
								for(int k=0; k<cardStmtTypeInfoList.size(); k++){
									if(trxnDateMap.containsKey(tempDate.getTime())){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " After decremented the sec also having the same key so re incrementing the sec" );}
										sec = tempDate.getSeconds();
										sec = sec - Constants.GL_ONE;	
									
										
										//For safety sake just multiplying with minus one
										if(sec < Constants.GL_ZERO){
											sec =  sec * -1;
										}
										//END 
										
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Second has decremented by one" + sec);}
										tempDate.setSeconds(sec);
									}else{
										break;
									}
								}
								
								
								trxnDateList.add(tempDate.getTime());
								trxnDateMap.put(tempDate.getTime(), cardStatementInformation);
							}else{
								trxnDateMap.put(tempDate.getTime(), cardStatementInformation);
								trxnDateList.add(tempDate.getTime());
							}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "The card Information inserted for the key is " + tempDate.getTime() +" and the value is "+ cardStatementInformation);}	

						}
					}

					if(Constants.TRANSACTION_ORDER_ASCENDING.equalsIgnoreCase(transactionAnncOrder)){
						Collections.sort(trxnDateList);					
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Done Ascending order sorting of Transaction dates" + tempDate.getTime());}
					}else{
						Collections.sort(trxnDateList, Collections.reverseOrder());					
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Done Descending order sorting of Transaction dates" + tempDate.getTime());}

					}

					Long key = null;
					CCTrxnDetails value = null;
					String strDate = Constants.EMPTY_STRING;
					SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT_yyyy_MM_ddHH_mm);
					String amount = Constants.EMPTY_STRING;
					int loopCount = Constants.GL_ZERO;


					String language = (String)callInfo.getField(Field.LANGUAGE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected language is" + language );}
					String promptsPath = Constants.EMPTY_STRING;

					if(Constants.English.equalsIgnoreCase(language) || Constants.Eng.equalsIgnoreCase(language) || Constants.ALPHA_E.equalsIgnoreCase(language)){
						promptsPath =  (String)ivr_ICEGlobal.getConfig().getParamValue(Constants.CUI_PROMPTS_LOCATION_ENGLISH);
					}else if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
						promptsPath =  (String)ivr_ICEGlobal.getConfig().getParamValue(Constants.CUI_PROMPTS_LOCATION_ARABIC);
					}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
						promptsPath =  (String)ivr_ICEGlobal.getConfig().getParamValue(Constants.CUI_PROMPTS_LOCATION_HINDI);
					}else if(Constants.Urudu.equalsIgnoreCase(language) || Constants.Uru.equalsIgnoreCase(language) || Constants.ALPHA_U.equalsIgnoreCase(language)){
						promptsPath =  (String)ivr_ICEGlobal.getConfig().getParamValue(Constants.CUI_PROMPTS_LOCATION_HINDI);
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured prompts path is " + promptsPath );}
					boolean isPromptExist = false;

					if(!util.isNullOrEmpty(trxnDateList)){

						for(int count_prompt = Constants.GL_ZERO; count_prompt < noOfTransaction; count_prompt++){

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction list is " + trxnDateList.size());}

							if(count_prompt < trxnDateList.size()){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the CCTrxnDetails for the account" + trxnDateList.get(count_prompt));}
								key = trxnDateList.get(count_prompt);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Key Date for retrieving the record is " + key );}

								value = trxnDateMap.get(key);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The CCTrxnDetails Record is" + value );}


								isPromptExist = util.isFileExistsInLocal(promptsPath+value.getTransactionDesc()+Constants.WAV_EXTENSION);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the prompt file exist" + isPromptExist );}

								if(isPromptExist){
									dynamicValueArray.add((value.getTransactionDesc()+Constants.WAV_EXTENSION).trim());
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Will play the original transaction type announcement" + value.getTransactionDesc()+Constants.WAV_EXTENSION );}

									if(!util.isNullOrEmpty(value.getAmount())){
										amount = value.getAmount();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Amount is " + amount );}

										if(amount.contains(Constants.MINUS)){
											amount = amount.substring(Constants.GL_ONE, amount.length());
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount sub striged value is " + amount );}
										}
									}
								}else{
									if(!util.isNullOrEmpty(value.getAmount())){
										amount = value.getAmount();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Amount is " + amount );}

										if(amount.contains(Constants.MINUS)){
											amount = amount.substring(Constants.GL_ONE, amount.length());
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount sub striged value is " + amount );}

											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mapping the credit wave phrase" + value );}
											dynamicValueArray.add(DynaPhraseConstants.RecentTransaction_Debit);
										}else{

											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mapping the debit wave phrase" + value );}
											dynamicValueArray.add(DynaPhraseConstants.RecentTransaction_Credit);
										}
									}
								}

								dynamicValueArray.add(amount);

								strDate = formatter.format(key);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Before Converting the date into yyyyMMdd" + strDate );}
								strDate = util.convertDateStringFormat(strDate, Constants.DATEFORMAT_YYYY_MM_DD, "yyyyMMdd");
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyyMMdd" + strDate );}
								dynamicValueArray.add(strDate);
								loopCount++;

								if(loopCount == noOfTransaction){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of transaction announced attains its maximum count so terminating the loop, the no of trans count :" + noOfTransaction );}
									break;
								}

							}
						}


					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

					String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
					Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

					//String menuID = MenuIDMap.getMenuID("PRODUCT_INFORMATION_SELECTION");
					String anncID = AnncIDMap.getAnncID("Card_Recent_Transaction_Message");
					String featureID = FeatureIDMap.getFeatureID("Recent_Transactions_Cards");
					String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;

//					/**
//					 * Following are the modification done for configuring the more option of menus
//					 */
//					combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
//					//END - Vinoth
					
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					Object[] object = new Object[dynamicValueArray.size()];
					for(int count=0; count<dynamicValueArray.size();count++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
						object[count] = dynamicValueArray.get(count);

					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}


					int totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}

					//Overriding the total prompts, received from the property file

					String moreCount = (String)callInfo.getField(Field.MORECOUNT);
					int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

					
					
					
					//Following changes have done on 20-Aug-2014 to get the max transaction limit
					String max_Trans_Annc_Limit = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_MAX_TRANS_ANNC_LIMIT))?
							Constants.FIFTEEN : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_MAX_TRANS_ANNC_LIMIT);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Max Transaction Annc Limit is " + max_Trans_Annc_Limit);}

					
					int int_Max_Trans_Annc_Limit = Integer.parseInt(max_Trans_Annc_Limit);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Converted Integer type for max trans limit is " + int_Max_Trans_Annc_Limit);}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Date List " + trxnDateList.size());}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of Transaction count is " + noOfTransaction);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No of Max transaction limit count is " + int_Max_Trans_Annc_Limit);}
					
					if(trxnDateList.size() < noOfTransaction && trxnDateList.size() < int_Max_Trans_Annc_Limit){
						totalPrompt = Constants.GL_SIX * trxnDateList.size();	
					}
					else if(noOfTransaction>int_Max_Trans_Annc_Limit && trxnDateList.size() > int_Max_Trans_Annc_Limit){
						totalPrompt = Constants.GL_SIX * int_Max_Trans_Annc_Limit;
					}
					else if(noOfTransaction <= trxnDateList.size()){
						totalPrompt = Constants.GL_SIX * noOfTransaction;
					}


					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}



					callInfo.setField(Field.DYNAMICLIST, grammar);
					callInfo.setField(Field.MOREOPTION, moreOption);

					String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
					String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

					finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

					/**
					 * Following are the manipulation of appending "|" while sending phrase for OD application
					 */
					/*
					String[] strArray  = finalResult.split(Constants.ASTERISK);
					String tempStr = Constants.EMPTY_STRING;
					String origStr = null;
					int tempInt = Constants.GL_ZERO;
					if(!util.isNullOrEmpty(finalResult)){
						if(trxnDateList.size() > noOfTransaction){
							tempInt = noOfTransaction * Constants.GL_SIX;
							tempInt = tempInt -1;
							tempStr = strArray[tempInt];
							tempStr = tempStr + "|";
							strArray[tempInt] = tempStr.trim();

							for(int i=0; i<strArray.length; i++){
								if(origStr==null){
									origStr = strArray[i].trim();
								}else{
									origStr = (origStr + strArray[i]).trim();
								}
							}

							finalResult = origStr;
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
						}
					}

					 */

				}

			}


			/**
			 * Note temp_Str is nothing but the Transaction type.  The wave file also should recorded in the same product name
			 * 
			 * eg check/debit/credit/withdrawal etc.. (For future use) --> check.wav / debit.wav / credit.wav
			 * 
			 * 
			 * "CP_1036.wav*Credit.wav*CP_1034.wav*SS:1000:currency*Bank_Transaction_1001.wav*SS:20140205:date"
			 *	+ "CP_1036.wav*Debit.wav*CP_1034.wav*SS:2000:currency*Bank_Transaction_1001.wav*SS:20140207:date"
			 * + "CP_1036.wav*Credit.wav*CP_1034.wav*SS:3202:currency*Bank_Transaction_1001.wav*SS:20140209:date"
			 *	+ "CP_1036.wav*Debit.wav*CP_1034.wav*SS:4302:currency*Bank_Transaction_1001.wav*SS:20140215:date";
			 */

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: RecentTransactionCardsImpl.getRecentTransactionsCards()");}


		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at RecentTransactionCardsImpl.getRecentTransactionsCards() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
