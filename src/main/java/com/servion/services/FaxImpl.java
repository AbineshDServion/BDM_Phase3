package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.bankmuscat.esb.commontypes.AddressInfoType;
import com.bankmuscat.esb.commontypes.PersonInfoType;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.SendFaxDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.creditCardBalance.CCEntityFields;
import com.servion.model.creditCardBalance.CreditCardBalanceDetails_HostRes;
import com.servion.model.fax.LoggingFaxRequest_HostRes;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.transactionDetailBank.BankStatementInformation;
import com.servion.model.transactionDetaitCards.TransDtls_CCTrxnDetails;
import com.servion.model.transactionDetaitCards.TransDtls_CardStmtDetails;

public class FaxImpl implements IFax {
	private static Logger logger = LoggerObject.getLogger();

	private MessageSource messageSource;
	private SendFaxDAO sendFaxDAO;

	public SendFaxDAO getSendFaxDAO() {
		return sendFaxDAO;
	}

	public void setSendFaxDAO(SendFaxDAO sendFaxDAO) {
		this.sendFaxDAO = sendFaxDAO;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	@Override
	public String GetAlreadyExistingFaxNoPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FaxImpl.GetAlreadyExistingFaxNoPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			//Need to get the FeatureConfig Data

			String faxNumber = (String)callInfo.getField(Field.LASTSELECTEDFAXNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Selected Fax number is " + faxNumber);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(faxNumber);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("FAX_EXISTING_NUMBER");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("FAX");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

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

			//To have the property file grammar, need to call that util method here

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FaxImpl.GetAlreadyExistingFaxNoPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FaxImpl.GetAlreadyExistingFaxNoPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String GetConfirmationFaxPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FaxImpl.GetConfirmationFaxPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			String selectedFaxno = (String)callInfo.getField(Field.LASTSELECTEDFAXNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected fax number is " + selectedFaxno);}


			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(selectedFaxno);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("FAX_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("FAX");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

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

			//To have the property file grammar, need to call that util method here

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FaxImpl.GetConfirmationFaxPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FaxImpl.GetConfirmationFaxPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String GetEnterFaxNoPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FaxImpl.GetEnterFaxNoPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			String faxLength = (String)callInfo.getField(Field.FAXLENGTH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax number length is " + faxLength);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(faxLength);			

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("ENTER_FAX_NUMBER");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("FAX");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

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

			//To have the property file grammar, need to call that util method here


			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT:FaxImpl.GetEnterFaxNoPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FaxImpl.GetEnterFaxNoPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String SendLogFaxRequest(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FaxImpl.SendLogFaxRequest()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FaxImpl.SendLogFaxRequest()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
				throw new ServiceException("ivr_ICEGlobalObject is null / empty");
			}

			String featureType =  util.isNullOrEmpty(callInfo.getField(Field.FEATURETYPE))? Constants.EMPTY : (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature type is "+ featureType);}

			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature name is "+ featureName);}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			

			String faxNumber = (String) callInfo.getField(Field.LASTSELECTEDFAXNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested faxNumber "+ faxNumber);}
			
			// 29-03-2015 based on kaarthik & vijay request for report
			String custAccNum = util.isNullOrEmpty(callInfo.getField(Field.ACCOUNTNUMBER))?Constants.NA : (String) callInfo.getField(Field.ACCOUNTNUMBER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Number going to insert in host is " + custAccNum);}

			
			/**  
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = 
					Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID) + Constants.COMMA 
					+ Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + callInfo.getField(Field.ACCOUNTNUMBER) + Constants.COMMA 
					+ Constants.HOST_INPUT_PARAM_FAX_NUMBER + Constants.EQUALTO + faxNumber + Constants.COMMA 
					+ Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + custAccNum + Constants.COMMA// 29-03-2015 based on kaarthik & vijay request for report
					+ Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_FORM_TYPE + Constants.EQUALTO + "NA" + Constants.COMMA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_NAME + Constants.EQUALTO + "NA" + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.FORMS_REQUEST_TYPE_FAX;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_WRIREFAX);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_WRITEFAX);


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


			String faxFileCopyLoc = null;
			//(String)callInfo.getField(Field.FAXFILELOCATION);

			faxFileCopyLoc = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FAX_FILE_COPY_LOCATION))? null : (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FAX_FILE_COPY_LOCATION);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested fax file copy Location "+ faxFileCopyLoc);}


			String faxFileServiceLocation = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FAX_FILE_LOCATION))? null : (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FAX_FILE_LOCATION);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested fax file Log Location "+ faxFileServiceLocation);}

			
			
			String faxFileName = Constants.EMPTY_STRING;

			String hostRequestingFaxFileName = Constants.EMPTY_STRING;

			if(Constants.FEATURENAME_FORMS.equalsIgnoreCase(featureName)){

				/**
				 * Following has been added to handled language specific folder structure for forms
				 */
				String language  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language key is "+ language);}
				
				if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Arabic Form Path");}
					faxFileServiceLocation = faxFileServiceLocation + Constants.Arabic + Constants.DOUBLE_SLASH;
				}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Hidni Form Path");}
					faxFileServiceLocation = faxFileServiceLocation + Constants.Hindi + Constants.DOUBLE_SLASH;
				}else if(Constants.Urudu.equalsIgnoreCase(language) || Constants.Uru.equalsIgnoreCase(language) || Constants.ALPHA_U.equalsIgnoreCase(language)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"For Urudu too returuning hindi as Form Path");}
					faxFileServiceLocation = faxFileServiceLocation + Constants.Hindi + Constants.DOUBLE_SLASH;
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring English Form Path");}
					faxFileServiceLocation = faxFileServiceLocation + Constants.English + Constants.DOUBLE_SLASH;
				}
				//End Vinoth
				
				String selectedFormType =util.isNullOrEmpty(callInfo.getField(Field.SELECTEDFORMTYPE))?Constants.NA : (String)callInfo.getField(Field.SELECTEDFORMTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected form type is "+ selectedFormType);}

				if(util.isNullOrEmpty(selectedFormType)){
					throw new ServiceException("Selected form type is null or empty");
				}

				String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}


				strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_TYPE + Constants.EQUALTO + selectedFormType
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_NAME + Constants.EQUALTO + selectedFormType
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.FORMS_REQUEST_TYPE_FAX
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FAX_NUMBER + Constants.EQUALTO + faxNumber
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);

				faxFileName = selectedFormType + Constants.PDF_EXTENSION;
				hostRequestingFaxFileName = faxFileName.trim();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Forms fax file name is "+ faxFileName);}

				/**
				 * Since for forms, we are not going to use the FAX copy location because we are not to create any fax files..we just going to take the static fax file and
				 * going to send fax irrespective of bank or cards
				 */
				faxFileName = faxFileServiceLocation + faxFileName;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File name is "+ faxFileName);}
				callInfo.setField(Field.FAXFILENAME, faxFileName);

			}else{

				//Generating Fax file
				HashMap<String, Object> faxInfo = new HashMap<String, Object>();

				String customerName = Constants.EMPTY;
				String customerAddr1 = Constants.EMPTY;
				String customerAddr2= Constants.EMPTY;
				String customerAddr3 = Constants.EMPTY;
				String customerAddr4 = Constants.EMPTY;
				String customerAddr5 = Constants.EMPTY;
				String zip = Constants.EMPTY_STRING;
				String country = Constants.EMPTY_STRING;
				String state = Constants.EMPTY_STRING;
				String street = Constants.EMPTY_STRING;
				String cardOrAcctNumber = Constants.EMPTY;
				String currency = Constants.EMPTY;
				String accountType = Constants.EMPTY;
				String customerBranchCode = Constants.EMPTY;
				String period = Constants.EMPTY;
				String branchCode = Constants.EMPTY;


				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())){
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPersonalInfoTypeList())){
						if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPersonalInfoTypeList().get(Constants.GL_ZERO))){
							PersonInfoType personInfoType = callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPersonalInfoTypeList().get(Constants.GL_ZERO);
							customerName = personInfoType.getFirstName();
							if(!util.isNullOrEmpty(personInfoType.getAddress())){
								AddressInfoType addressInfoType =  personInfoType.getAddress().get(Constants.GL_ZERO);
								customerAddr1 = addressInfoType.getAddr1();
								customerAddr2 = addressInfoType.getAddr2();
								customerAddr3 = addressInfoType.getAddr3();
								customerAddr4 = addressInfoType.getAddr4();
								customerAddr5 = addressInfoType.getAddr5();
								zip = addressInfoType.getZip();
								country = addressInfoType.getCountry();
								state = addressInfoType.getState();
								street = addressInfoType.getStreet();
								branchCode = addressInfoType.getDelBranch();
							}
						}
					}
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 1 " + customerAddr1);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 2 " + customerAddr2);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 3 " + customerAddr3);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 4 " + customerAddr4);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer Address 5 " + customerAddr5);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "zip " + zip);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "country "  +country);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "state " + state);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "street " + street);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Branch Code is  " + branchCode);}

				String srcNo = (String)callInfo.getField(Field.SRCNO);
				cardOrAcctNumber = Constants.MASKING_START + util.getSubstring(srcNo, Constants.GL_FOUR);


				if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
						AcctInfo acctInfo = callInfo.getCallerIdentification_HostRes().getAccountDetailMap().get(srcNo);

						if(!util.isNullOrEmpty(acctInfo)){
							currency = acctInfo.getAcctCurr();
							customerBranchCode = acctInfo.getBranchCode();
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account / Card Currency is " + currency) ;}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account / Card Branch Code is " + customerBranchCode) ;}
				}

				accountType = util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE))? Constants.EMPTY : (String)callInfo.getField(Field.SRCTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account type is " + accountType) ;}

				//End Reporting
				int deviceID = util.isNullOrEmpty(callInfo.getField(Field.DEVICE_ID))? Constants.GL_ZERO :  Integer.parseInt((String)callInfo.getField(Field.DEVICE_ID));
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Device ID "+ deviceID);}


				String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))? Constants.EMPTY : (String)callInfo.getField(Field.CUSTOMERID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID is "+ customerId);}

				if(!util.isNullOrEmpty(customerId)){
					faxFileName = customerId;
					faxFileName =faxFileName +  Constants.UNDERSCORE;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id is "+ faxFileName);}
//				if(!util.isNullOrEmpty(srcNo)){
//					faxFileName = faxFileName +  srcNo;
//					faxFileName = faxFileName + Constants.UNDERSCORE;
//				}
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id + source no is "+ faxFileName);}
				if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					faxFileName = faxFileName + Constants.FEATURE_TYPE_BANK;
					faxFileName = faxFileName + Constants.UNDERSCORE;
				}else{
					faxFileName = faxFileName + Constants.FEATURE_TYPE_CARD;
					faxFileName = faxFileName + Constants.UNDERSCORE;
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file with customer id + source no  + feature type is "+ faxFileName);}

				String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's date and time is "+ currentDate);}

				faxFileName = faxFileName + currentDate;
				faxFileName = faxFileName + Constants.PDF_EXTENSION;

				hostRequestingFaxFileName = faxFileName;

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final fax file name is "+ faxFileName);}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final fax file location is "+ faxFileCopyLoc);}

				if(util.isNullOrEmpty(faxFileCopyLoc)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file location is null / empty");}
					throw new ServiceException("Fax file location is null or empty");
				}

				faxFileName = faxFileCopyLoc + faxFileName;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File name is "+ faxFileName);}
				callInfo.setField(Field.FAXFILENAME, faxFileName);


				String customerAddress = Constants.EMPTY_STRING;

				if(!util.isNullOrEmpty(customerAddr1)){
					customerAddress = customerAddr1+"\n";
				}

				if(!util.isNullOrEmpty(customerAddr1)){
					customerAddress = customerAddr1+"\n";
				}
				if(!util.isNullOrEmpty(customerAddr2)){
					customerAddress = customerAddress + customerAddr2+"\n";
				}
				if(!util.isNullOrEmpty(customerAddr3)){
					customerAddress = customerAddress + customerAddr3+"\n";
				}
				if(!util.isNullOrEmpty(customerAddr4)){
					customerAddress = customerAddress + customerAddr4+"\n";
				}
				if(!util.isNullOrEmpty(customerAddr5)){
					customerAddress = customerAddress + customerAddr5+"\n";
				}
				if(!util.isNullOrEmpty(street)){
					customerAddress = customerAddress + street+"\n";
				}
				if(!util.isNullOrEmpty(state)){
					customerAddress = customerAddress + state+"\n";
				}
				if(!util.isNullOrEmpty(country)){
					customerAddress = customerAddress + country+"\n";
				}
				if(!util.isNullOrEmpty(zip)){
					customerAddress = customerAddress + zip+"\n";
				}

				String mergingFaxFileLoc = util.isNullOrEmpty(callInfo.getField(Field.MERGINGFAXLOCATION))? Constants.EMPTY : (String)callInfo.getField(Field.MERGINGFAXLOCATION);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Merging fax file location is "+ mergingFaxFileLoc);}


				if(util.isNullOrEmpty(mergingFaxFileLoc)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Merging fax file location is null / empty");}
					throw new ServiceException("Merging fax file location is null or empty");
				}

				String channelNo = util.isNullOrEmpty(callInfo.getField(Field.CHANNELNO))?Constants.EMPTY : (String)callInfo.getField(Field.CHANNELNO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Channel No is "+ channelNo);}

				mergingFaxFileLoc = mergingFaxFileLoc + channelNo + Constants.BACKSLASH;
				mergingFaxFileLoc = mergingFaxFileLoc + Constants.FAX_MODULE_MERGEFAXFILE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Updated merging fax location with channel no is "+ mergingFaxFileLoc);}

				boolean faxSent = false;

				String fileTemplatePath = (String)callInfo.getField(Field.FAXTEMPLATEPATH);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fax file template path is "+ fileTemplatePath);}

				String noOfTransaction = (String)callInfo.getField(Field.NOOFTRANSPERPAGE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of Transaction per page"+ noOfTransaction);}


				faxInfo.put(Constants.FAX_MODULE_FAXFILENAME, faxFileName);
				faxInfo.put(Constants.FAX_MODULE_NOOFTRANSPERPAGE, noOfTransaction );
				faxInfo.put(Constants.FAX_MODULE_ADDRESS1, customerAddr1);
				faxInfo.put(Constants.FAX_MODULE_ADDRESS2, customerAddr2);
				faxInfo.put(Constants.FAX_MODULE_ADDRESS3, customerAddr3);
				faxInfo.put(Constants.FAX_MODULE_ADDRESS4, customerAddr4);
				faxInfo.put(Constants.FAX_MODULE_ADDRESS5, customerAddr5);
				faxInfo.put(Constants.FAX_MODULE_ZIP, zip);
				faxInfo.put(Constants.FAX_MODULE_STATE, state);
				faxInfo.put(Constants.FAX_MODULE_STREET, street);
				faxInfo.put(Constants.FAX_MODULE_COUNTRY, country);
				faxInfo.put(Constants.FAX_MODULE_BRANCH, branchCode);
				faxInfo.put(Constants.FAX_MODULE_MERGEDTEMPLATEFORTHISCALLER, mergingFaxFileLoc);


				if(Constants.FEATURE_TYPE_BANK.equalsIgnoreCase(featureType)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from a Banking Features " );}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Upding the Card Template file name along with the fax template name "+ fileTemplatePath);}
					fileTemplatePath = fileTemplatePath + Constants.ACCOUNT_TEMPLATE_NAME;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Template final template file name and path is "+ fileTemplatePath);}

					ArrayList<BankStatementInformation> transactionList = null;
					if(!util.isNullOrEmpty(callInfo.getTransactionDetailsBank_HostRes()) && !util.isNullOrEmpty(callInfo.getTransactionDetailsBank_HostRes().getBankStmtTypeInfoList()))
					{
						transactionList = callInfo.getTransactionDetailsBank_HostRes().getBankStmtTypeInfoList();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "transactionList obtained from the transaction details of bank is " + transactionList);}

						if(!util.isNullOrEmpty(transactionList.get(Constants.GL_ZERO).getOrigDate())){
							period = util.convertXMLCalendarToString(transactionList.get(Constants.GL_ZERO).getOrigDate(), Constants.DATEFORMAT_YYYYMMDD);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Period with initial transaction orig date" + period );}
							period = period +"\t" + Constants.MINUS + "\t";
							period = period  + util.convertXMLCalendarToString(transactionList.get(transactionList.size()-Constants.GL_ONE).getOrigDate(), Constants.DATEFORMAT_YYYYMMDD);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Period with last transaction orig date" + period );}
						}

					}

					/**
					 * Setting Fax file credentials
					 */
					faxInfo.put(Constants.FAX_MODULE_FAXTEMPLATEPATH, fileTemplatePath);
					faxInfo.put(Constants.FAX_MODULE_ACCOUNTTYPE, accountType);
					faxInfo.put(Constants.FAX_MODULE_TRANSACTIONLIST, transactionList);
					faxInfo.put(Constants.FAX_MODULE_CUSTOMERNAME, customerName);
					faxInfo.put(Constants.FAX_MODULE_CUSTOMERADDR, customerAddress);
					faxInfo.put(Constants.FAX_MODULE_CARDORACCTNUMBER, srcNo);
					faxInfo.put(Constants.FAX_MODULE_CURRENCY, currency);
					faxInfo.put(Constants.FAX_MODULE_CUSTOMERBRANCHCODE, customerBranchCode);
					faxInfo.put(Constants.FAX_MODULE_PERIOD, period);

					faxSent = util.generateFaxFileForBank(faxInfo, callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the fax file sent to the destination path " + faxSent);}

				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to fetch the transaction details of Cards");}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Upding the Card Template file name along with the fax template name "+ fileTemplatePath);}
					fileTemplatePath = fileTemplatePath + Constants.CARD_TEMPLATE_NAME;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account final template file name and path is "+ fileTemplatePath);}
					faxInfo.put(Constants.FAX_MODULE_FAXTEMPLATEPATH, fileTemplatePath);

					ArrayList<TransDtls_CCTrxnDetails> transDtls_CcTrxnDetails = null;

					//					String cashAdv = Constants.EMPTY_STRING;
					String paymentAndOtherCredit = Constants.EMPTY_STRING;
					String cashAdv = Constants.EMPTY_STRING;
					double temp = 0;
					double temp_CashAdv = 0;
					double double_paymentAndOtherCredit = 0;
					double double_CashAdv = 0;
					double double_PreviousBalance = 0;
					double double_PurchaseAndCharges = 0;

					/**
					 * Following are handled as per Faraz confirmation
					 */
					
					String cardAcctNo = util.isNullOrEmpty(callInfo.getField(Field.CCACCTNOFORSTMTREQ))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.CCACCTNOFORSTMTREQ);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected card account number is "+ cardAcctNo);}
					
					String sourceNo = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected credit card number is "+ util.maskCardOrAccountNumber(sourceNo));}
					
					if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq()) && !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction())
							&& !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap())){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to fetch the transaction details of Cards");}
						//08-Sep-16 fetch txn details based on card number. becuase txn details stroed in map with key as card number 
						//transDtls_CcTrxnDetails = callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap().get(cardAcctNo);
						transDtls_CcTrxnDetails = callInfo.getTransactionDetailCards_HostReq().getTransDtls_Transaction().getTransactionMap().get(sourceNo);

						String mcc = Constants.EMPTY_STRING;
						String procCode = Constants.EMPTY_STRING;

						String cui_Mcc = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CASH_ADV_MCC_CODE);
						String cui_Proc = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_CASH_ADV_PROC_CODE);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured cash Advance MCC code is "  + cui_Mcc);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured cash Advance Proc code is " + cui_Proc);}

						TransDtls_CCTrxnDetails transDtls_CCTrxnDetails = null;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transaction list of selected credit card is " + transDtls_CcTrxnDetails);}

						if(!util.isNullOrEmpty(transDtls_CcTrxnDetails) ){
							if(!util.isNullOrEmpty(cui_Mcc) || !util.isNullOrEmpty(cui_Proc)){
								for(int i= 0; i < transDtls_CcTrxnDetails.size(); i ++){

									transDtls_CCTrxnDetails = transDtls_CcTrxnDetails.get(i);

									if(!util.isNullOrEmpty(transDtls_CCTrxnDetails)){

										mcc = transDtls_CCTrxnDetails.getMcc();
										procCode = transDtls_CCTrxnDetails.getProcCode();
										
										paymentAndOtherCredit = transDtls_CCTrxnDetails.getAmount();
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "paymentAndOtherCredit is "  + paymentAndOtherCredit);}
										
										temp = !util.isNullOrEmpty(paymentAndOtherCredit)? Double.parseDouble(paymentAndOtherCredit): Constants.GL_ZERO;
										
										if(temp > Constants.GL_ZERO){
											double_paymentAndOtherCredit = double_paymentAndOtherCredit + temp;
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the payment and other credit calculation since, it is a not a negative amount" + double_paymentAndOtherCredit);}
										}else{
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Neglecting the payment and other credit calculation since, it is a negative amount");}
										}
									
										if(util.isCodePresentInTheConfigurationList(mcc, cui_Mcc) ||
												util.isCodePresentInTheConfigurationList(procCode, cui_Proc)){
											
											cashAdv = transDtls_CCTrxnDetails.getAmount();
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cash Advance is "  + cashAdv);}

											temp_CashAdv = !util.isNullOrEmpty(cashAdv)? Double.parseDouble(cashAdv): Constants.GL_ZERO;
											double_CashAdv = double_CashAdv + temp_CashAdv;

										}
									}
								}
							}
						}
					}

					double_paymentAndOtherCredit = Math.round(double_paymentAndOtherCredit*1000)/1000;
					paymentAndOtherCredit = double_paymentAndOtherCredit + Constants.EMPTY_STRING;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final paymentAndOtherCreditvalue is " + paymentAndOtherCredit);}

					double_CashAdv = Math.round(double_CashAdv*1000)/1000;
					cashAdv = double_CashAdv + Constants.EMPTY_STRING;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Cash Advance value is " + cashAdv);}


					String previousBalance = Constants.EMPTY_STRING;
					String lastPaymentDate = Constants.EMPTY_STRING;
					double totalAmountDue = Constants.GL_ZERO;
					String minAmountDue = Constants.EMPTY_STRING;
					String creditLimit = Constants.EMPTY_STRING;

					TransDtls_CardStmtDetails transDtls_CardStmtDetails = null;

					if(!util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq()) && !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader())
							&& !util.isNullOrEmpty(callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap())){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to fetch the transDtls_CardStmtDetails of Cards");}

						transDtls_CardStmtDetails = callInfo.getTransactionDetailCards_HostReq().getTransDtls_CardStatementHeader().getCardStamtDetailMap().get(cardAcctNo);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained transDtls_CardStmtDetails object of selected credit card is " + transDtls_CardStmtDetails);}
					previousBalance = util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getOpeningBalance();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Previous Balance is  " + previousBalance);}


					double_PreviousBalance = !util.isNullOrEmpty(previousBalance)?Double.parseDouble(previousBalance): Constants.GL_ZERO;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Double converted Previous Balance is  " + previousBalance);}

					double_PreviousBalance = Math.round(double_PreviousBalance*1000)/1000;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final double_PreviousBalance value is " + cashAdv);}

					/**
					 * Modified as per Faraz confirmation - 12-Jun-2014
					 */
					lastPaymentDate = util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getDueDate();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last payment Date is "+ lastPaymentDate);}

					totalAmountDue =  util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.GL_ZERO : Double.parseDouble(transDtls_CardStmtDetails.getClosingBalance());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Amount due is "+ totalAmountDue);}


//					/**
//					 * Calculation of purchase and other charge amount - as per the confirmation by Faraz
//					 */
//					
//					
//					/**
//					 * Previous balance + purchased amount + cash adv taken - payment done to credit card = total amount due still we have to pay for the credit card
//					 * 
//					 * the above formula has been convert into below in order to get correct result
//					 * (take what we get from host( i.e -ve for DR & +ve for CR)) + (always -ve) + (always -ve) + (always +ve) = (take what we get from host ( i.e -ve for DR & +ve for CR))
//					 * 
//					 * 
//					 */
//					
//					double_paymentAndOtherCredit = double_paymentAndOtherCredit < 0 ? (-1 * double_paymentAndOtherCredit) : double_paymentAndOtherCredit;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Considering payment as always positive "+ double_paymentAndOtherCredit);}
//					
//					double_CashAdv = double_CashAdv > 0? (-1 * double_CashAdv):double_CashAdv;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Cash advance as always negative "+ double_CashAdv);}
//					
//					double_PurchaseAndCharges = totalAmountDue - (double_paymentAndOtherCredit + double_PreviousBalance + double_CashAdv);
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final purchase and other charges is "+ double_PurchaseAndCharges);}

					
					/**
					 * As per Hussain's advise done the below on 30-Jun-2014
					 */
					double_PurchaseAndCharges = (-1 * totalAmountDue) - (-1 * double_PreviousBalance) - double_CashAdv + double_paymentAndOtherCredit;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final purchase and other charges is "+ double_PurchaseAndCharges);}

					double_PurchaseAndCharges = Math.round(double_PurchaseAndCharges*1000)/1000;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After rounding off the final purchase and other charges is "+ double_PurchaseAndCharges);}

					minAmountDue =  util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getMinDueAmt();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Minimum Amount due is "+ minAmountDue);}

					creditLimit = util.isNullOrEmpty(transDtls_CardStmtDetails)? Constants.EMPTY_STRING : transDtls_CardStmtDetails.getCreditLimit();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "creditLimit is "+ creditLimit);}

					ICardBalance iCardBalance = Context.getiCardBalance();
					String ccBalance_Code = iCardBalance.getCreditCardBalance(callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Call For CC Balance host access is " + ccBalance_Code);}

					String statementDate = Constants.EMPTY_STRING;
					//					String lastPaymentAmount = Constants.EMPTY_STRING;
					//
					//					String balance = Constants.EMPTY_STRING;
					//					String overDueAmount = Constants.EMPTY_STRING;


					if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(ccBalance_Code)){
						CreditCardBalanceDetails_HostRes creditCardBalanceDetails_HostRes = callInfo.getCreditCardBalanceDetails_HostRes();
						if(util.isNullOrEmpty(creditCardBalanceDetails_HostRes)){
							throw new ServiceException("Card balance host response object bean is null");
						}

						Iterator iterator = null;
						HashMap<String, CCEntityFields> acctNo_AccountDetailMap =  null;
						CCEntityFields ccEntityFields = null;

						if(!util.isNullOrEmpty(creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap())  && 
								creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap().size() > Constants.GL_ZERO){

							acctNo_AccountDetailMap = creditCardBalanceDetails_HostRes.getAcctNo_AccountDetailMap();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail object from the host is "+ acctNo_AccountDetailMap);}

							if(!util.isNullOrEmpty(acctNo_AccountDetailMap) && acctNo_AccountDetailMap.size() > Constants.GL_ZERO){

								iterator = acctNo_AccountDetailMap.entrySet().iterator();
								Map.Entry mapEntry = (Map.Entry) iterator.next();

								String accountNumber = (String) mapEntry.getKey();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account number retrieved from the host is "+ accountNumber);}

								ccEntityFields = (CCEntityFields) mapEntry.getValue();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the CCEntityField object "+ ccEntityFields);}

								if(!util.isNullOrEmpty(ccEntityFields)){

									accountType = ccEntityFields.getAcctProduct();
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account product type is "+ accountType);}

									statementDate = ccEntityFields.getStmtGenerateDate();
									statementDate = util.convertDateStringFormat(statementDate, Constants.DATEFORMAT_YYYYMMDD, Constants.DATEFORMAT_YYYY_MM_DD);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyy-MM-dd the stateme generation date is" + statementDate );}

									//									lastPaymentAmount = ccEntityFields.getPaymentAmount();
									//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last payment amount is "+ lastPaymentAmount);}

									//									balance = ccEntityFields.getStmtClosingDate();
									//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Closing balance is "+ balance);}
									//
									//									overDueAmount = ccEntityFields.getOverDueAmt();
									//									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Over Due amount is "+ overDueAmount);}
								}
							}
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Failure response for the host Call of CC Balance host access is " + ccBalance_Code);}
						throw new ServiceException("CCEntityInquiry host access is failed");
					}

					/**
					 * Commenting below as per the confirmation from Faraz 12-Jun-2014
					 */
					//					double int_balance = balance!=null ? Double.parseDouble(balance) : Constants.GL_ZERO;
					//					double int_overDueAmt = balance!=null ? Double.parseDouble(overDueAmount) : Constants.GL_ZERO;
					//					totalAmountDue = int_balance + int_overDueAmt;

					srcNo = util.maskCardOrAccountNumber(srcNo);

					faxInfo.put(Constants.FAX_MODULE_CARDNUMBER, srcNo);
					faxInfo.put(Constants.FAX_MODULE_CARDTYPE, accountType);
					faxInfo.put(Constants.FAX_MODULE_STATEMENTDATE, statementDate);
					faxInfo.put(Constants.FAX_MODULE_PAYMENTDUEDATE, lastPaymentDate);

					faxInfo.put(Constants.FAX_MODULE_TOTALAMOUNTDUE, (totalAmountDue + ""));

				//	double minAmtDue_dbl = util.isNullOrEmpty(minAmountDue)?Constants.GL_ZERO : Double.parseDouble(minAmountDue);
					//minAmountDue = Constants.FAX_LOCAL_CURR + minAmtDue_dbl;

					faxInfo.put(Constants.FAX_MODULE_MINAMOUNTDUE, minAmountDue );
					faxInfo.put(Constants.FAX_MODULE_PREVIOUSBALANCE, previousBalance);
					faxInfo.put(Constants.FAX_MODULE_PURCHASECHARGES, double_PurchaseAndCharges + Constants.EMPTY_STRING);
					faxInfo.put(Constants.FAX_MODULE_CASHADVANCE, cashAdv);
					faxInfo.put(Constants.FAX_MODULE_PAYMENTCREDIT, paymentAndOtherCredit);
					faxInfo.put(Constants.FAX_MODULE_TRANSACTIONLIST, transDtls_CcTrxnDetails);
					faxInfo.put(Constants.FAX_MODULE_CREDITLIMIT, creditLimit);
					faxInfo.put(Constants.FAX_MODULE_CASHLIMIT, Constants.ZERO); //As per the confirmation given by Faraz it was kept as 0 for all customer 12-Jun-2014


					faxSent = util.generateFaxFileForCard(faxInfo, callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the fax file sent to the destination path " + faxSent);}
				}	

			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final FaxNumber is " + faxNumber);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File Location is " + faxFileCopyLoc);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File Name is " + hostRequestingFaxFileName);}

			LoggingFaxRequest_HostRes loggingFaxRequest_HostRes = sendFaxDAO.getSendFaxHostRes(callInfo, faxNumber, faxFileServiceLocation, hostRequestingFaxFileName);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "LoggingFaxRequest_HostRes Object is :"+ loggingFaxRequest_HostRes);}
			code = loggingFaxRequest_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String hostEndTime = loggingFaxRequest_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = loggingFaxRequest_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for WriteFax host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + loggingFaxRequest_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_WRIREFAX, loggingFaxRequest_HostRes.getHostResponseCode());

			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(loggingFaxRequest_HostRes.getErrorDesc()) ?"NA" :loggingFaxRequest_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FaxImpl.SendLogFaxRequest()");}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FaxImpl.SendLogFaxRequest() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

}
