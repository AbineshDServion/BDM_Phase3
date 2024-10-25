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
import com.servion.dao.LoanDtlsInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.loanBalance.LoanBalanceDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class LoanBalanceImpl implements ILoanBalance{

	private static Logger logger = LoggerObject.getLogger();

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	LoanDtlsInquiryDAO loanDtlsInquiryDAO;


	public LoanDtlsInquiryDAO getLoanDtlsInquiryDAO() {
		return loanDtlsInquiryDAO;
	}

	public void setLoanDtlsInquiryDAO(LoanDtlsInquiryDAO loanDtlsInquiryDAO) {
		this.loanDtlsInquiryDAO = loanDtlsInquiryDAO;
	}

	@Override
	public String getLoanBalance(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LoanBalanceImpl.getLoanBalance()");}
		String code = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String contractID = (String)callInfo.getField(Field.SRCNO);
			if(util.isNullOrEmpty(contractID)){
				throw new ServiceException("Selected loan Acct No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "selected loan acct no ending with" + util.getSubstring(contractID, Constants.GL_FOUR));}


			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

			String strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_LOANDTLSINQUIRY);

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

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_LOANDETAILSENQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_LOANDETAILSENQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			LoanBalanceDetails_HostRes loanBalanceDetails_HostRes = loanDtlsInquiryDAO.getLoanBalanceHostRes(callInfo, contractID, requestType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "loanBalanceDetails_HostRes Object is :"+ loanBalanceDetails_HostRes);}
			callInfo.setLoanBalanceDetails_HostRes(loanBalanceDetails_HostRes);

			code = loanBalanceDetails_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = loanBalanceDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = loanBalanceDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(loanBalanceDetails_HostRes.getErrorDesc()) ?"NA" :loanBalanceDetails_HostRes.getErrorDesc());

			hostReportDetails.setHostOutParams(hostOutputParam);


			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO +util.maskCardOrAccountNumber(contractID)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);


			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Cheque book order request service");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Loan Details Inquiry  order host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + loanBalanceDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LOANDTLSINQUIRY, loanBalanceDetails_HostRes.getHostResponseCode());
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);

			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LoanBalanceImpl.getLoanBalance() "+ e.getMessage());
			throw new ServiceException(e);

		}
		return code;
	}

	@Override
	public String getLoanBalancePhrases(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage = Constants.EMPTY_STRING;
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LoanBalanceImpl.getLoanBalancePhrases()");}

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String>LoanBalanceFieldsAndOrder = new ArrayList<String>();

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LoanBalanceFieldsAndOrder))){
				LoanBalanceFieldsAndOrder = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LoanBalanceFieldsAndOrder);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Bank Account Balance fields and Order in the a local variable");}
			}

			if(!util.isNullOrEmpty(LoanBalanceFieldsAndOrder)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Loan Account Balance Fields and Order size is" + LoanBalanceFieldsAndOrder.size());}
			}

			callInfo.setField(Field.LoanBalanceFieldsAndOrder, LoanBalanceFieldsAndOrder);



			ArrayList<Object> dynamicValueArray = null;
			

			LoanBalanceDetails_HostRes loanBalanceDetails_HostRes = callInfo.getLoanBalanceDetails_HostRes();
			ArrayList<String> balanceListForAnnc = (ArrayList<String>)callInfo.getField(Field.LoanBalanceFieldsAndOrder);


			if(util.isNullOrEmpty(loanBalanceDetails_HostRes)){
				throw new ServiceException("Loan balance host response object bean is null");
			}

			if(util.isNullOrEmpty(balanceListForAnnc)){
				throw new ServiceException("User doesn't configure the balance types in UI");
			}

			String outstandingAmount = loanBalanceDetails_HostRes.getOutstandingAmount();
			String dueDate = loanBalanceDetails_HostRes.getDueDate();
			String emi = loanBalanceDetails_HostRes.geteMI();


			if(util.isNullOrEmpty(outstandingAmount))
				outstandingAmount = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(dueDate))
				dueDate = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(emi))
				emi = Constants.EMPTY_STRING;


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Outstanding Amount for the Loan Account is"+ outstandingAmount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "dueDate for the loan account is"+ dueDate);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "emi for the Loan account is"+ emi);}


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
			anncID = AnncIDMap.getAnncID("Loan_Balance_Message");
			featureID = FeatureIDMap.getFeatureID("Account_Balance_Loans");
			combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
			
			//END 

			String temp_Str;
			for(int count=0; count<balanceListForAnnc.size(); count++){
				temp_Str = balanceListForAnnc.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Balance type for announcement is"+ temp_Str);}

				switch(temp_Str)
				{
				case Constants.BALANCE_TYPE_OUTSTANDING_AMT:
					
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(outstandingAmount)){
						dynamicValueArray.add(DynaPhraseConstants.Loan_Balance_1000);
						dynamicValueArray.add(outstandingAmount);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_OUTSTANDING_AMT;
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

				case Constants.BALANCE_TYPE_DUEDATE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(dueDate)){
						dynamicValueArray.add(DynaPhraseConstants.Loan_Balance_1001);
						String strDate = Constants.EMPTY_STRING;
						//					SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT_yyyy_MM_ddHH_mm);
						//					strDate = formatter.format(dueDate);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Before Converting the date into yyyyMMdd" + dueDate );}
						strDate = util.convertDateStringFormat(dueDate, Constants.DATEFORMAT_YYYY_MM_DD, "yyyyMMdd");
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyyMMdd" + strDate );}
						dynamicValueArray.add(strDate);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}

					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_DUEDATE;
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
				case Constants.BALANCE_TYPE_EMI:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(emi)){
						dynamicValueArray.add(DynaPhraseConstants.Loan_Balance_1002);
						dynamicValueArray.add(emi);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}

					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_EMI;
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
			//grammar = util.getGrammar(str_GetMessage);
			grammar = Constants.NA;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Grammar value is"+grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting moreoption as false");}
			callInfo.setField(Field.MOREOPTION, false);
			//End

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: LoanBalanceImpl.getLoanBalancePhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardBalanceImpl.getCreditCardBalancePhrases() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}
}
