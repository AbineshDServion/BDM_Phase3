package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.LastNNumTransInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;
import com.servion.model.recentTransactionBank.BankStatementInformation;
import com.servion.model.recentTransactionBank.RecentTransactionBank_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class RecentTransactionBankImpl implements IRecentTransactionBank{

	private static Logger logger = LoggerObject.getLogger();

	private LastNNumTransInquiryDAO lastNNumTransInquiryDAO;
	public LastNNumTransInquiryDAO getLastNNumTransInquiryDAO() {
		return lastNNumTransInquiryDAO;
	}

	public void setLastNNumTransInquiryDAO(
			LastNNumTransInquiryDAO lastNNumTransInquiryDAO) {
		this.lastNNumTransInquiryDAO = lastNNumTransInquiryDAO;
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
	public String getRecentTransactionsBanks(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RecentTransactionBankImpl.getRecentTransactionsBanks()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}


			String SelectedCardOrAcctNo = Constants.EMPTY_STRING;
			CallerIdentification_HostRes callerIdentification_HostRes = callInfo.getCallerIdentification_HostRes();
			if(util.isNullOrEmpty(callerIdentification_HostRes)){
				throw new ServiceException("CallerIdentification Object is null / EMpty");
			}

			SelectedCardOrAcctNo = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first account ID number of the retrieved account type is :" + util.maskCardOrAccountNumber(SelectedCardOrAcctNo));}



			if(util.isNullOrEmpty(SelectedCardOrAcctNo)){
				throw new ServiceException("Selected Acct No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting selected card or acct no as entered cin" + util.getSubstring(SelectedCardOrAcctNo, Constants.GL_FOUR));}


			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
//			String strHostInParam = Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + SelectedCardOrAcctNo;
//			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_LASTNNUMTRANSINQ);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(startTime); //It should be in the formate of 31/07/2013 18:11:11
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

			String noOfTxn = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NOOFTRANSACTION_TOFETCH_FROM_HOST); 
			int intNoOFTxn = util.isNullOrEmpty(noOfTxn)?Constants.GL_ZERO: Integer.parseInt(noOfTxn);

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_LASTNNUMTRANSINQ_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_LASTNNUMTRANSINQ_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			RecentTransactionBank_HostRes recentTransactionBank_HostRes = lastNNumTransInquiryDAO.getRecentTransBankHostRes(callInfo, SelectedCardOrAcctNo, intNoOFTxn, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "recentTransactionBank_HostRes Object is :"+ recentTransactionBank_HostRes);}
			callInfo.setRecentTransactionBank_HostRes(recentTransactionBank_HostRes);

			code = recentTransactionBank_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = recentTransactionBank_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = recentTransactionBank_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}

			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(SelectedCardOrAcctNo)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE +Constants.EQUALTO +  callInfo.getField(Field.SRCTYPE)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE +Constants.EQUALTO + requestType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION + Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);


			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(recentTransactionBank_HostRes.getErrorDesc()) ?"NA" :recentTransactionBank_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for RecentTransactionBank.getRecentTransactionsBanks");}
				int totTransAvail = Constants.GL_ZERO;
				if(!util.isNullOrEmpty(callInfo.getRecentTransactionBank_HostRes())){

					if(!util.isNullOrEmpty(callInfo.getRecentTransactionBank_HostRes().getRecordCount())){
						totTransAvail = Integer.parseInt((String)callInfo.getRecentTransactionBank_HostRes().getRecordCount());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available transaction total count "+ totTransAvail );}
					}
				}
				callInfo.setField(Field.NOOFTRANSACTION, totTransAvail);
				if(totTransAvail > Constants.GL_ZERO)
					callInfo.setField(Field.ISTRANSACTIONAVAILABLE, true);
				else
					callInfo.setField(Field.ISTRANSACTIONAVAILABLE, false);

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Acccount balance host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + recentTransactionBank_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LASTNNUMTRANSINQ, recentTransactionBank_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);

			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: RecentTransactionBankImpl.getRecentTransactionsBanks()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at RecentTransactionBankImpl.getRecentTransactionsBanks() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getRecentTransactionsBanksPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RecentTransactionBankImpl.getRecentTransactionsBanksPhrases()");}
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
					Constants.GL_FIVE:Integer.parseInt( (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NOOFTRANSFORANNC));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The No of Transaction can be announced" + noOfTransaction);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			ArrayList<BankStatementInformation> bankStmtTypeInfoList = null;
			BankStatementInformation bankStatementInformation = null;

			HashMap<Long, BankStatementInformation> trxnDateMap = new HashMap<>();
			ArrayList<Long> trxnDateList = new ArrayList<>();

			if(!util.isNullOrEmpty(callInfo.getRecentTransactionBank_HostRes())){
				bankStmtTypeInfoList = callInfo.getRecentTransactionBank_HostRes().getBankStmtTypeInfoList();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bank Stmt Type Info" + bankStmtTypeInfoList);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bank Stmt Type Info Object size" + bankStmtTypeInfoList.size());}
				int sec = Constants.GL_ZERO;
				if(!util.isNullOrEmpty(bankStmtTypeInfoList)){
					Date tempDate = null;
					for(int count = Constants.GL_ZERO; count < bankStmtTypeInfoList.size() ; count++) {
						bankStatementInformation = bankStmtTypeInfoList.get(count);
						/**
						 * Following changes have been done based on Faisal approval (changed Posting date to Orig Date) for recent transaction banks
						 */
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " Bank Transaction type is " + bankStatementInformation.getTxnType());}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " Bank Transaction Amount is " + bankStatementInformation.getTxnAmount());}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " Bank Transaction Date is " + bankStatementInformation.getOrigDate());}

						if(bankStatementInformation.getOrigDate()!=null){
							tempDate = bankStatementInformation.getOrigDate().toGregorianCalendar().getTime();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Convertered Date format is " + tempDate);}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Convertered Date format time is " + tempDate.getTime());}

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
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Second has decrement by one" + sec);}

								tempDate.setSeconds(sec);

								for(int k=0; k<bankStmtTypeInfoList.size(); k++){
									if(trxnDateMap.containsKey(tempDate.getTime())){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + " After decrementing the sec also having the same key so re decrementing the sec" );}
										sec = tempDate.getSeconds();
										sec = sec - Constants.GL_ONE;	

										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "Second has decremented by one" + sec);}
										
										//For safety sake just multiplying with minus one
										if(sec < Constants.GL_ZERO){
											sec =  sec * -1;
										}
										//END 
										tempDate.setSeconds(sec);
									}else{
										break;
									}
								}


								trxnDateList.add(tempDate.getTime());
								trxnDateMap.put(tempDate.getTime(), bankStatementInformation);
							}else{

								trxnDateMap.put(tempDate.getTime(), bankStatementInformation);
								trxnDateList.add(tempDate.getTime());
							}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, count + "The Bank Information inserted for the key is " + tempDate.getTime() +" and the value is "+ bankStatementInformation);}	
						}
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total transaction count is " +trxnDateList.size() );}
					
					if(Constants.TRANSACTION_ORDER_ASCENDING.equalsIgnoreCase(transactionAnncOrder)){
						Collections.sort(trxnDateList);					
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Done Ascending order sorting of Transaction dates" + tempDate.getTime());}
					}else{
						Collections.sort(trxnDateList, Collections.reverseOrder());					
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Done Descending order sorting of Transaction dates" + tempDate.getTime());}
					}

					Long key = null;
					BankStatementInformation value = null;
					String strDate = Constants.EMPTY_STRING;
					SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT_YYYYMMDD);
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
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Date List " + trxnDateList.size());}
							if(count_prompt < trxnDateList.size()){
								if(count_prompt <= trxnDateList.size())
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the BankStatementInformation for the account" + trxnDateList.get(count_prompt));}
								key = trxnDateList.get(count_prompt);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Key Date for retrieving the record is " + key );}

								value = trxnDateMap.get(key);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The BankStatementInformation Record is" + value );}


								isPromptExist = util.isFileExistsInLocal(promptsPath+value.getTxnType()+Constants.WAV_EXTENSION);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the prompt file exist" + isPromptExist );}


								if(isPromptExist){
									dynamicValueArray.add((value.getTxnType()+Constants.WAV_EXTENSION).trim());
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Will play the original transaction type announcement" + value.getTxnType()+Constants.WAV_EXTENSION );}

									if(!util.isNullOrEmpty(value.getTxnAmount())){
										amount = value.getTxnAmount();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Amount is " + amount );}

										if(amount.contains(Constants.MINUS)){
											amount = amount.substring(Constants.GL_ONE, amount.length());
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount sub striged value is " + amount );}
										}
									}

								}else{
									if(!util.isNullOrEmpty(value.getTxnAmount())){
										amount = value.getTxnAmount();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Amount is " + amount );}

										if(amount.contains(Constants.MINUS)){
											amount = amount.substring(Constants.GL_ONE, amount.length());
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount sub striged value is " + amount );}
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mapping the debit wave phrase" + value );}
											dynamicValueArray.add(DynaPhraseConstants.RecentTransaction_Debit);
										}else{

											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mapping the credit wave phrase" + value );}
											dynamicValueArray.add(DynaPhraseConstants.RecentTransaction_Credit);
										}
									}
								}

								dynamicValueArray.add(amount);

								strDate = formatter.format(key);
								//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Before Converting the date into yyyyMMdd" + strDate );}
								//						strDate = util.convertDateStringFormat(strDate, Constants.DATEFORMAT_YYYY_MM_DD, "yyyyMMdd");
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
					String anncID = AnncIDMap.getAnncID("Bank_Recent_Transaction_Message");
					String featureID = FeatureIDMap.getFeatureID("Recent_Transactions_Banks");
					String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;

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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: RecentTransactionBankImpl.getRecentTransactionsBanksPhrases()");}


		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at RecentTransactionBankImpl.getRecentTransactionsBanksPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
