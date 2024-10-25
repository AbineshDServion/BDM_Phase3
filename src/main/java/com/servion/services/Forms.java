package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.reporting.HostReportDetails;

public class Forms implements IForms{
	private static Logger logger = LoggerObject.getLogger();

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	@Override
	public String getFormsMenuPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: Forms.getFormsMenuPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String> formList = null;
			formList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FORMS_KEY);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Form list retrieved is :" + formList);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			/**
			 * Note temp_Str is nothing but the form name.  The wave file also should recorded in the same form name
			 * 
			 * eg Form1 --> Form1.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;

			for(int count=Constants.GL_ZERO;count<formList.size();count++){
				temp_Str = formList.get(count);
				dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());

				if(count == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
					moreOption = true;
					callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}

				if(util.isNullOrEmpty(grammar)){
					grammar = temp_Str;
				}else{
					grammar = grammar + Constants.COMMA + temp_Str;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("FORMS_SELECTION");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Forms");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
			//END - Vinoth


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
			if(formList.size()>int_moreCount){
				totalPrompt = Constants.GL_THREE * formList.size();
				//totalPrompt = totalPrompt + Constants.GL_TWO;
				
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = formList.size() / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//				int temp2 =  formList.size() % int_moreCount;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//				if(temp2 > 0){
//					temp1++;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
				
			}
			else{
				totalPrompt = Constants.GL_THREE * formList.size();
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
			//"CP_1033.wav*FORM1.wav*CP_1019.wav*CP_1033.wav*FORM2.wav*CP_1020.wav*CP_1033.wav*FORM3.wav*CP_1021.wav";

			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT:  Forms.getFormsMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  Forms.getFormsMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public boolean getStaticFormsList(CallInfo arg0) throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String sendFormsRequestByEmail(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: Forms.sendFormsRequestByEmail()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Forms.sendFormsRequestByEmail()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
				throw new ServiceException("ivr_ICEGlobalObject is null / empty");
			}

			String featureType = util.isNullOrEmpty(callInfo.getField(Field.FEATURETYPE))? Constants.EMPTY : (String)callInfo.getField(Field.FEATURETYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature type is "+ featureType);}

			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature name is "+ featureName);}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String selectedFormType = util.isNullOrEmpty(callInfo.getField(Field.SELECTEDFORMTYPE))?Constants.NA : (String)callInfo.getField(Field.SELECTEDFORMTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected form type is "+ selectedFormType);}

			if(util.isNullOrEmpty(selectedFormType)){
				throw new ServiceException("Selected form type is null or empty");
			}


			/**  
			 * For Reporting Purpose
			 */
			String smtp_ToEmailID = Constants.EMPTY_STRING + callInfo.getField(Field.REG_EMAIL);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Email ID is "+ smtp_ToEmailID);}
			

			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			
			
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
			
			// 29-03-2015 based on kaarthik & vijay request for report
			String custAccNum = util.isNullOrEmpty(callInfo.getField(Field.ACCOUNTNUMBER))?Constants.NA : (String) callInfo.getField(Field.ACCOUNTNUMBER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Number going to insert in host is " + custAccNum);}
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_TYPE + Constants.EQUALTO + selectedFormType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_NAME + Constants.EQUALTO + selectedFormType
					
					//TODO: As per Judes requested on 14-09-2015
					/*****************/
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE)) ? Constants.NA : (String) callInfo.getField(Field.SRCTYPE))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STMT_TYPE +Constants.EQUALTO + "NA"
					/*****************/
					
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.FORMS_REQUEST_TYPE_EMAIL
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_EMAIL_ID + Constants.EQUALTO + smtp_ToEmailID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + custAccNum // 29-03-2015 based on kaarthik & vijay request for report 
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_SMTPEMAIL);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_METHOD_SMTPEMAIL);

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
			
			String emailFileCopyLoc = null;
			//(String)callInfo.getField(Field.FAXFILELOCATION);

			emailFileCopyLoc = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EMAIL_FILE_COPY_LOCATION))? null : (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EMAIL_FILE_COPY_LOCATION);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested fax file copy Location "+ emailFileCopyLoc);}

			String emailFileName = Constants.EMPTY_STRING;


			emailFileName = selectedFormType + Constants.PDF_EXTENSION;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Forms fax file name is "+ emailFileName);}


			
			/**
			 * Following has been added to handled language specific folder structure for forms
			 */
			String language  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language key is "+ language);}
			
			if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Arabic Form Path");}
				emailFileCopyLoc = emailFileCopyLoc + Constants.Arabic + Constants.DOUBLE_SLASH;
			}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Hidni Form Path");}
				emailFileCopyLoc = emailFileCopyLoc + Constants.Hindi + Constants.DOUBLE_SLASH;
			}else if(Constants.Urudu.equalsIgnoreCase(language) || Constants.Uru.equalsIgnoreCase(language) || Constants.ALPHA_U.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"For Urudu too returuning hindi as Form Path");}
				emailFileCopyLoc = emailFileCopyLoc + Constants.Hindi + Constants.DOUBLE_SLASH;
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring English Form Path");}
				emailFileCopyLoc = emailFileCopyLoc + Constants.English + Constants.DOUBLE_SLASH;
			}
			
			
			emailFileName = emailFileCopyLoc + emailFileName;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File name is "+ emailFileName);}
			callInfo.setField(Field.FAXFILENAME, emailFileName);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Fax File Location is " + emailFileCopyLoc);}

		

			String smtp_HostName = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILHOSTNAME);
			String smtp_UserName = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILUSERNAME);
			String smtp_Password = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILPASSWORD);
			String smtp_Auth = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILAUTH);
			String smtp_FromEmailId = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILFROMEMAILID);
			String smtp_SubjectLine = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILFORMSUBJECTLINE);
			String smtp_mailBodyText = Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILBODYTEXT);
			String smtp_mailPort =  Constants.EMPTY_STRING  + callInfo.getField(Field.EMAILPORT);
			String smtp_emailGifLocation = Constants.EMPTY_STRING + callInfo.getField(Field.EMAILGIFLOCATION);

			callInfo.setField(Field.EMAILFILELOCATION, emailFileName);
			String smtp_FileName =  emailFileName;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Host Name " + smtp_HostName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement User Name " + smtp_UserName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Auth details " + smtp_Auth);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement To Eamil id " + smtp_ToEmailID);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement From Eamil id " + smtp_FromEmailId);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement subject line " + smtp_SubjectLine);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement mail Body text " + smtp_mailBodyText);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement file name " + selectedFormType + Constants.PDF_EXTENSION);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement mail port" + smtp_mailPort);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Registered email id is " + smtp_ToEmailID);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Statement Email Gif Loction is " + smtp_emailGifLocation);}

			HashMap<String, String> statementInfoMap = new HashMap<>();

			statementInfoMap.put(Constants.SMTP_HOSTNAME, smtp_HostName);
			statementInfoMap.put(Constants.SMTP_USERNAME, smtp_UserName);
			statementInfoMap.put(Constants.SMTP_AUTH, smtp_Auth);
			statementInfoMap.put(Constants.SMTP_filename, smtp_FileName);
			statementInfoMap.put(Constants.SMTP_FROMEMAILID, smtp_FromEmailId);
			statementInfoMap.put(Constants.SMTP_subjectLine, smtp_SubjectLine);
			statementInfoMap.put(Constants.SMTP_mailBodyText, smtp_mailBodyText);
			statementInfoMap.put(Constants.SMTP_filename, emailFileName);
			statementInfoMap.put(Constants.SMTP_mailPort, smtp_mailPort);
			statementInfoMap.put(Constants.SMTP_ToEmailId, smtp_ToEmailID);
			statementInfoMap.put(Constants.SMTP_PASSWORD,smtp_Password);
			statementInfoMap.put(Constants.SMTP_FORMTYPE, (selectedFormType + Constants.PDF_EXTENSION));
			statementInfoMap.put(Constants.SMTP_EMAILGIFLOCATION, smtp_emailGifLocation);
			
			/**
			 * Added on 26-Dec-2014 for Forms description issue
			 */
			statementInfoMap.put(Constants.SMTP_CALLEDFROM_FORMS, "true");
			//END

			code = util.statementByEmail(statementInfoMap);
			/*
			 * For Reporting Start
			 */
			String hostEndTime = util.getCurrentDateTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = code;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for WriteFax host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + code);}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_SMTPEMAIL, code);

			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_TYPE + Constants.EQUALTO + selectedFormType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_FORM_NAME + Constants.EQUALTO + selectedFormType
					
					//TODO: As per Judes requested on 14-09-2015
					/*****************/
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.SRCTYPE)) ? Constants.NA : (String) callInfo.getField(Field.SRCTYPE))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STMT_TYPE +Constants.EQUALTO + "NA"
					/*****************/
					
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + Constants.FORMS_REQUEST_TYPE_EMAIL
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_EMAIL_ID + Constants.EQUALTO + smtp_ToEmailID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + custAccNum // 29-03-2015 based on kaarthik & vijay request for report 
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode;
			if(code== null){
					hostOutputParam += Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +"ESB Connection TimedOut";
			} else if(code.equalsIgnoreCase(Constants.STR_NULL)){
				hostOutputParam += Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +"ESB Connection TimedOut";
			} else if (code.isEmpty()){
				hostOutputParam += Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +"No/Empty Respone from Host";
			}
			hostReportDetails.setHostOutParams(hostOutputParam);
			
			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: Forms.sendFormsRequestByEmail()");}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at Forms.sendFormsRequestByEmail() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

}
