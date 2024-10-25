package com.servion.services;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.AcctDtlsInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.accountBalance.AccountBalance_HostRes;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class AccountBalanceImpl implements IAccountBalance {
	private static Logger logger = LoggerObject.getLogger();

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	private AcctDtlsInquiryDAO acctDtlsInquiryDAO;


	public AcctDtlsInquiryDAO getAcctDtlsInquiryDAO() {
		return acctDtlsInquiryDAO;
	}

	public void setAcctDtlsInquiryDAO(AcctDtlsInquiryDAO acctDtlsInquiryDAO) {
		this.acctDtlsInquiryDAO = acctDtlsInquiryDAO;
	}

	@Override
	public String getBankAccountBalance(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountBalanceImpl.getAccountBalanceFlash()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
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


			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_ACCOUNTDTLSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_ACCOUNTDTLSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

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
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_ACCTDTLSINQUIRY);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
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
			

			String deptAcctOfficerDtlFlag = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_AcctDtlsInquiry_DeptAcctOfficerDtlFlag); 
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DeptAcctOfficerDtlsFlage value is  :"+ deptAcctOfficerDtlFlag);}

			AccountBalance_HostRes accountBalance_HostRes = acctDtlsInquiryDAO.getAcctBalanceHostRes(callInfo, SelectedCardOrAcctNo, deptAcctOfficerDtlFlag, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CallerIdenf_DebitCardDetails Object is :"+ accountBalance_HostRes);}
			callInfo.setAccountBalance_HostRes(accountBalance_HostRes);

			code = accountBalance_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = accountBalance_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = accountBalance_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}

			
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(SelectedCardOrAcctNo)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime 
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);


			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(accountBalance_HostRes.getErrorDesc()) ?"NA" :accountBalance_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for AccountBalanceImpl.getAccountBalance");}
				/**
				 * Setting the selected account available balance in the field variable
				 */
				if(util.isNullOrEmpty(accountBalance_HostRes.getAvailBal())){
					callInfo.setField(Field.CURRENTACCTAVAILBALANCE, Constants.ZERO);
				}else{
					callInfo.setField(Field.CURRENTACCTAVAILBALANCE, ""+accountBalance_HostRes.getAvailBal());
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Account available balance set to" + callInfo.getField(Field.CURRENTACCTAVAILBALANCE));}
				//END
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Acccount balance host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + accountBalance_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_ACCTDTLSINQUIRY, accountBalance_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);

			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountBalanceImpl.getAccountBalanceFlash()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountBalanceImpl.getAccountBalanceFlash() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getBankAccountBalancePhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage = Constants.EMPTY_STRING;
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: AccountBalanceImpl.getBankAccountBalancePhrases()");}

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String>BankAcctBalanceFieldsAndOrder = new ArrayList<String>();

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_BankAcctBalanceFieldsAndOrder))){
				BankAcctBalanceFieldsAndOrder = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_BankAcctBalanceFieldsAndOrder);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Bank Account Balance fields and Order in the a local variable");}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bank Account Balance Fields and Order size is" + BankAcctBalanceFieldsAndOrder.size());}
				throw new ServiceException("Banking balance order is not configured in UI");
			}
			callInfo.setField(Field.BankAcctBalanceFieldsAndOrder, BankAcctBalanceFieldsAndOrder);


			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			AccountBalance_HostRes accountBalance_HostRes = callInfo.getAccountBalance_HostRes();
			ArrayList<String> balanceListForAnnc = (ArrayList<String>)callInfo.getField(Field.BankAcctBalanceFieldsAndOrder);


			if(util.isNullOrEmpty(accountBalance_HostRes)){
				throw new ServiceException("Account balance host response object bean is null");
			}

			if(util.isNullOrEmpty(balanceListForAnnc)){
				throw new ServiceException("User doesn't configure the balance types in UI");
			}

			String availableBalance = accountBalance_HostRes.getAvailBal();
			String unclearBalance = accountBalance_HostRes.getUnclearedBalance();
			String ledgerBalance = accountBalance_HostRes.getOnLineActualBal(); // as per the confirmation of ESB team 


			if(util.isNullOrEmpty(availableBalance))
				availableBalance = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(unclearBalance))
				unclearBalance = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(ledgerBalance))
				ledgerBalance = Constants.EMPTY_STRING;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available balance for the account is"+ availableBalance);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Unclear balance for the account is"+ unclearBalance);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Ledger balance for the account is"+ ledgerBalance);}

			
			
			/**
			 * Following are the parameter declared to make the prompt announcement / balance announcement as dynamic
			 */


			String languageKey = Constants.EMPTY_STRING;
			Locale locale = null;
			String anncID = Constants.EMPTY_STRING;
			String featureID = Constants.EMPTY_STRING;
			String combinedKey = Constants.EMPTY_STRING;
			Object[] object = null;
			int totalPrompt = Constants.GL_ZERO;
			String dynamicPhraseKey = Constants.EMPTY_STRING;
			String dynamicMessageValue = Constants.EMPTY_STRING;
			String grammar = Constants.EMPTY_STRING;
			
			
			languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("REPORT_LOSS_CARD_CONFIRMATION");
			anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			featureID = FeatureIDMap.getFeatureID("Account_Balance_Bank");
			combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
			//END 
			
			String temp_Str;
			for(int count=0; count<balanceListForAnnc.size(); count++){
				temp_Str = balanceListForAnnc.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Balance type for announcement is"+ temp_Str);}

				switch(temp_Str)
				{
				case Constants.BALANCE_TYPE_AVAIL_BALANE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(availableBalance)){
						dynamicValueArray.add(DynaPhraseConstants.Bank_Balance_1000);
						dynamicValueArray.add(availableBalance);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_AVAIL_BALANE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);

					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}


					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					
					break;
				case Constants.BALANC_TYPE_UNCLEAR_BALANCE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(unclearBalance)){
						if(Double.parseDouble(unclearBalance) > Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Unclear balance is > 0");}
							dynamicValueArray.add(DynaPhraseConstants.Bank_Balance_1001);
							dynamicValueArray.add(unclearBalance);
						}else{
							dynamicValueArray.add(Constants.NA);
							dynamicValueArray.add(Constants.NA);
						}
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANC_TYPE_UNCLEAR_BALANCE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);

					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}


					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

					
					break;
				case Constants.BALANC_TYPE_LEDGER_BALANCE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(unclearBalance) && !util.isNullOrEmpty(ledgerBalance)){
						if(Double.parseDouble(unclearBalance) > Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Ledger balance is > 0");}
							dynamicValueArray.add(DynaPhraseConstants.Bank_Balance_1002);
							dynamicValueArray.add(ledgerBalance);
						}else{
							dynamicValueArray.add(Constants.NA);
							dynamicValueArray.add(Constants.NA);
						}
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANC_TYPE_LEDGER_BALANCE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);

					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				}
			}

			/*
			 * Handling Grammar and MoreOptions for OD Use
			 */
			grammar = Constants.NA;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Grammar value is"+grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting moreoption as false");}
			callInfo.setField(Field.MOREOPTION, false);
			//End

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: AccountBalanceImpl.getBankAccountBalancePhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at AccountBalanceImpl.getBankAccountBalancePhrases() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}

}
