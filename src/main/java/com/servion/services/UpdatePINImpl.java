package com.servion.services;

import java.util.HashMap;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CCEntityInquiryDAO;
import com.servion.dao.KeyExAuthDAO;
import com.servion.dao.UpdatePinDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.jce.JCEWrapper;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.apinValidation.APINCustomerProfileDetails_HostRes;
import com.servion.model.callerIdentification.CallerIdenf_DebitCardDetails;
import com.servion.model.keyExAuth.KeyExAuth_HostRes;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.updatePin.UpdatePIN_HostRes;

public class UpdatePINImpl implements IUpdatePIN {
	private static Logger logger = LoggerObject.getLogger();
	
	private UpdatePinDAO updatePinDAO;
	private CCEntityInquiryDAO ccEntityInquiryDAO;
	private KeyExAuthDAO keyExAuthDAO;

	
	public KeyExAuthDAO getKeyExAuthDAO() {
		return keyExAuthDAO;
	}
	
	public void setKeyExAuthDAO(KeyExAuthDAO keyExAuthDAO) {
		this.keyExAuthDAO = keyExAuthDAO;
	}
	
	public UpdatePinDAO getUpdatePinDAO() {
		return updatePinDAO;
	}

	public void setUpdatePinDAO(
			UpdatePinDAO updatePinDAO) {
		this.updatePinDAO = updatePinDAO;
	}

	public CCEntityInquiryDAO getCcEntityInquiryDAO() {
		return ccEntityInquiryDAO;
	}

	public void setCcEntityInquiryDAO(CCEntityInquiryDAO ccEntityInquiryDAO) {
		this.ccEntityInquiryDAO = ccEntityInquiryDAO;
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
	public String getUpdatePIN(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UpdatePINImpl.getUpdatePIN()");}
		String code = Constants.EMPTY_STRING;
		
		try{
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			/*ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}*/
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
	
		String enteredCINNumber = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered CIN Number ending with " + util.getSubstring(enteredCINNumber, Constants.GL_FOUR));}
		
		
		String pan = enteredCINNumber;
		String processingCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_ProcessingCode);
//		int intProcessingCode = util.isNullOrEmpty(processingCode)?Constants.GL_ZERO:Integer.parseInt(processingCode);
		
		String transmissionDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);

		//Following for the sequencial number generation for System trace audit number for S1 systems
		String db_Code = Constants.ONE;
		int codeLength = Constants.GL_ZERO;
		String sessionId = (String) callInfo.getField(Field.SESSIONID);
		
		
		if(util.isNullOrEmpty(sessionId)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session ID is null or empty");}
			throw new ServiceException("Session id is null or empty");
		}
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
		HashMap<String, Object> configMap = new HashMap<String, Object>();

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the input for getSequenceNo");}

		String uui = (String)callInfo.getField(Field.UUI);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
		
		DataServices dataServices = VRUDBDataServicesInstance.getInstance();
		String strRefNumberOne = Constants.EMPTY_STRING;
		
		try {
			db_Code = dataServices.getSequenceNoS1(logger, sessionId, uui, configMap);

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)){
				strRefNumberOne = (String) configMap.get(DBConstants.SEQUENCENO);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The sequencial number return value " + strRefNumberOne);}
				codeLength = strRefNumberOne.length();

					for(int p=codeLength; p < 6; p ++){
						strRefNumberOne = Constants.ZERO + strRefNumberOne;
						strRefNumberOne.trim();
				}

			}else{
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Error in the S1 sequencial DB response");}
				throw new ServiceException("Sequencial number DB access throwing error");
			}
		} catch (com.db.exception.ServiceException e) {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: GlobalImpl.getSequenceNumber()");}
			throw new ServiceException("S1 Sequencial number DB access throwing error");
			//e.printStackTrace();
		}
		
		String str_SysTraceAuditNo = strRefNumberOne;
//				util.getRandomNumber(999999) + Constants.EMPTY_STRING; 
//		int sysTraceAuditNo = util.isNullOrEmpty(str_SysTraceAuditNo)?Constants.GL_ZERO:Integer.parseInt(str_SysTraceAuditNo); 

		String localTransTime = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
		String localTansDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDD); 
		//String expirationDate = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_ExpirationDate);
		String expirationDate = Constants.EMPTY_STRING;
		String binType = (String) callInfo.getField(Field.ENTEREDCINTYPE);
		if(binType.equalsIgnoreCase(Constants.BIN_TYPE_CREDIT)
				|| binType.equalsIgnoreCase(Constants.BIN_TYPE_PREPAID)){
			APINCustomerProfileDetails_HostRes hostRespObj = callInfo.getAPINCustomerProfileDetails_HostRes();
			expirationDate = hostRespObj.getCardDetailsMap().get((String)callInfo.getField(Field.ENTEREDCINNUMBER)).getExpDate();
			hostRespObj.getCardDetailsMap().get((String)callInfo.getField(Field.ENTEREDCINNUMBER)).getExpDate();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"getCallerIdentification Expiry Date : "+ expirationDate);}
			//expirationDate = util.convertDateStringFormat(expirationDate, Constants.DATEFORMAT_yyyy_MM_dd_Hyphen, Constants.DATEFORMAT_YYMM);
			//23 Dec 2020 Expiry Date format issue
			expirationDate = util.convertDateStringFormat(expirationDate, Constants.DATEFORMAT_yyyy_MM_dd_Hyphen, Constants.DATEFORMAT_yyMM);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"getCallerIdentification Expiry Date After formatted : "+ expirationDate);}
		}else{
			CallerIdenf_DebitCardDetails objCallerIdenf_DebitCardDetails = callInfo.getCallerIdenf_DebitCardDetails();
			expirationDate = objCallerIdenf_DebitCardDetails.getExpiryDate();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"getCallerIdentification Expiry Date : "+ expirationDate);}
			//expirationDate = util.convertDateStringFormat(expirationDate, Constants.DATEFORMAT_yyyy_MM_dd, Constants.DATEFORMAT_YYMM);
			//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"getCallerIdentification Expiry Date After formatted : "+ expirationDate);}
		}
		
		

		String str_PointOfServiceMode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PointOfServiceMode);
//		int pointOfServiceMode = util.isNullOrEmpty(str_PointOfServiceMode)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServiceMode); 

		String str_CardSeqNum = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_CardSeqNum);
//		int cardSeqNum = util.isNullOrEmpty(str_CardSeqNum)?Constants.GL_ZERO:Integer.parseInt(str_CardSeqNum); 

		String str_PointOfServCondCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PointOfServCondCode);
//		int pointOfServCondCode = util.isNullOrEmpty(str_PointOfServCondCode)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServCondCode);

		String str_PointOfServCaptureCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PointOfServCaptureCode);
//		int pointOfServCaptureCode = util.isNullOrEmpty(str_PointOfServCaptureCode)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServCaptureCode); 

		String cardAccpTerminalID = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_CardAccpTerminalID); 
		String cardAccpIDCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_CardAccpIDCode);
		String cardAccpName = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_CardAccpName); 
		
		/**
		 * Doing the process of PIN Blocking and Encryption
		 */
		
		//Setting the User entered APIN value
		String pin = (String)callInfo.getField(Field.APIN);
		//(String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Pin);
		
		String apinKey = (String)callInfo.getField(Field.APINKEY);
		if(util.isNullOrEmpty(apinKey)){
			throw new ServiceException("Apin Key stored in the callinfo is null / empty");
		}
		
		apinKey = apinKey.substring(Constants.GL_ZERO, Constants.GL_THIRTYTWO);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first 32 digit key is retrieved ");}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first 32 digit key is retrieved "+ apinKey);}

		String masterkeyValue = util.isNullOrEmpty(callInfo.getField(Field.APINMASTERKEY))?null : (String)callInfo.getField(Field.APINMASTERKEY);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Master key from the keystore / Config file");}

		if(util.isNullOrEmpty(masterkeyValue)){
			throw new ServiceException("Masterkey conversion result is null or empty");
		}

		masterkeyValue = util.convertTo48BitKey(masterkeyValue);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Master key has been converted to 48 bits");}
		
		JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
		SecretKey sKey = jceWrap.toSecretKey(masterkeyValue);
		String clearKey = jceWrap.decrypt(apinKey, sKey);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received the clear key and the clear key is "+ clearKey);}

		String pinBlocking = util.getISOPinBlock(pan, pin, true, false, Constants.GL_THREE, Constants.GL_ONE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully done the pin blocking");}

		clearKey = util.convertTo48BitKey(clearKey);
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Clear key is converted to 48 digit and the result got "+ clearKey);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Clear key is converted to 48 digit and the result got ");}
		
		sKey = jceWrap.toSecretKey(clearKey);
		String encryptPIN = jceWrap.encrypt(pinBlocking, sKey);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully done the pin encryption");}
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully done the pin encryption"+ encryptPIN);}
		String pinPrefixValue = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PINPrefixValue);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Appending Prefix value to encrypted PIN: "+pinPrefixValue);}
		
		String pinSuffixValue = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PINSuffixValue);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Appending Suffix value to encrypted PIN: "+pinSuffixValue);}
		
		encryptPIN = pinPrefixValue.trim() + encryptPIN + pinSuffixValue.trim();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully appended prefix and suffix value with the pin encryption");}
		
		if(util.isNullOrEmpty(encryptPIN)){
			throw new ServiceException("encryption PIN conversion result is null or empty");
		}
		//String securityContInfo = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_SecurityContInfo);
		
		//Prepaid Pin set/reset changes 07-Jul-2020
		String recvInstIDCode = "";
		if(binType.equalsIgnoreCase(Constants.BIN_TYPE_PREPAID)){
			recvInstIDCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PrepaidCard_RecvInstIDCode);
		}else{
			recvInstIDCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_RecvInstIDCode);
		}
		String posDataCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_PosDataCode); 
		
		String extendedTransType = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UI_UpdatePIN_ExtendedTransactionType);

		
		/**
		 * Following are the modification done on 01-Sep-2014 for the handling of dynamic Debit card length (15 to 19)
		 */
		/*int panLength = util.isNullOrEmpty(pan)?Constants.GL_SIXTEEN : pan.length();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length is "+ panLength);}
		
		String panLengthKey = Constants.UNDERSCORE + panLength;
		panLengthKey = panLengthKey.trim();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length key is "+ panLengthKey);}
		
		panLengthKey = Constants.CUI_UI_UpdatePIN_RequestStructData+panLengthKey;
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The new Request Structure Data key is "+ panLengthKey);}
		//END 
		
		
		
		String structureData = (String)ivr_ICEFeatureData.getConfig().getParamValue(panLengthKey);*/
		String structureData = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UI_UpdatePIN_RequestStructData);
		//structureData = (structureData!=null)? structureData.replace(Constants.STRUCTURED_DATA_PAN_KEY, primaryAccountNum): null;
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Finalized Structured data is "+ structureData);}

		
		
		/**
		 * For Reporting Purpose
		 */
		HostReportDetails hostReportDetails = new HostReportDetails();
		
		String featureId = (String)callInfo.getField(Field.FEATUREID);
		hostReportDetails.setHostActiveMenu(featureId);
		//hostReportDetails.setHostCounter(hostCounter);
		//hostReportDetails.setHostEndTime(hostEndTime);
//		String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
//				Constants.NA + Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
//		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
//		hostReportDetails.setHostInParams(strHostInParam);
		hostReportDetails.setHostMethod(Constants.HOST_METHOD_UPDATEPIN);
		//hostReportDetails.setHostOutParams(hostOutParams);
		hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
		
		hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
		String cinType = (String)callInfo.getField(Field.ENTEREDCINTYPE);
		if(cinType.equalsIgnoreCase(Constants.CIN_TYPE_CREDIT)){
			hostReportDetails.setHostType(Constants.HOST_TYPE_CREDITCARDS);
		}else if(cinType.equalsIgnoreCase(Constants.CIN_TYPE_PREPAID)){
			hostReportDetails.setHostType(Constants.HOST_TYPE_PREPAIDCARDS);
		}else{
			hostReportDetails.setHostType(Constants.HOST_TYPE_DEBITCARDS);
		}
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
		
		
		String requestType = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UPDATEPIN_REQUESTTYPE))? null : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UPDATEPIN_REQUESTTYPE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

		
		UpdatePIN_HostRes updatePIN_HostRes = updatePinDAO.getUpdatePINHostRes(callInfo, pan, processingCode, transmissionDate, 
				str_SysTraceAuditNo, localTransTime, localTansDate, expirationDate, str_PointOfServiceMode, 
				str_CardSeqNum, str_PointOfServCondCode, str_PointOfServCaptureCode,  
				cardAccpTerminalID, cardAccpIDCode, cardAccpName,  encryptPIN, recvInstIDCode, 
				posDataCode, structureData, extendedTransType, requestType);
		
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updatePIN_HostRes Object is :"+ updatePIN_HostRes);}
		callInfo.setUpdatePIN_HostRes(updatePIN_HostRes);

		code = updatePIN_HostRes.getErrorCode();
		
		/*
		 * For Reporting Start
		 */
		String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
				callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetails.setHostInParams(strHostInParam);
		
		String hostEndTime = updatePIN_HostRes.getHostEndTime();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
		hostReportDetails.setHostEndTime(hostEndTime);
		
		String hostResCode = updatePIN_HostRes.getHostResponseCode();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
		hostReportDetails.setHostResponse(hostResCode);
		

		/*
		*//**
		 * Setting the APIN Status 
		 *//*
		String apinStatus = util.isNullOrEmpty(updatePIN_HostRes.getResponseCode())?Constants.NA : updatePIN_HostRes.getResponseCode()+Constants.EMPTY_STRING;
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "APIN Status is " + apinStatus);}
		callInfo.setField(Field.APIN_STATUS, apinStatus);
		//END 
		
		*//**
		 * Rule engine update
		 *//*
		ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();
		
		if(util.isNullOrEmpty(ruleParamObj)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
		}
		
		String apinInactiveCode = (String)callInfo.getField(Field.APINInactiveCode);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "APIN Inactive code is " + apinInactiveCode);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Response code for validate APIN is " + updatePIN_HostRes.getHostResponseCode());}
		
		//boolean isAPINInactive = util.isCodePresentInTheConfigurationList(apinInactiveCode, updatePIN_HostRes.getHostResponseCode());
		boolean isAPINInactive = util.isCodePresentInTheConfigurationList(updatePIN_HostRes.getHostResponseCode(), apinInactiveCode);
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is APIN / CIN is inactive ? " + isAPINInactive);}
		
		if(isAPINInactive){
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCINACTIVATED, Constants.N);
			ruleParamObj.updateIVRFields();
		}else{
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCINACTIVATED, Constants.Y);
			ruleParamObj.updateIVRFields();
		}
		
		//END Rule Engine Updation
*/		
		
		
		
		String responseDesc = Constants.HOST_FAILURE;
		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			responseDesc = Constants.HOST_SUCCESS;
		}
		String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
				+ Constants.EQUALTO + hostResCode
		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(updatePIN_HostRes.getErrorDesc()) ?"NA" :updatePIN_HostRes.getErrorDesc());
		hostReportDetails.setHostOutParams(hostOutputParam);
		
		callInfo.setHostReportDetails(hostReportDetails);
		ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
		
		callInfo.updateHostDetails(ivrdata);
		//End Reporting
		
		
		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer APIN was updated successfully " + hostEndTime);}
			
			//callInfo.setField(Field.APIN_VALIDATED, true);
		}else{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Update PIN host service");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updatePIN_HostRes.getHostResponseCode());}

			util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_UPDATEPIN, updatePIN_HostRes.getHostResponseCode());
		}
		
		
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UpdatePINImpl.getUpdatePIN()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UpdatePINImpl.getUpdatePIN() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	
	public String getNewEncryptionKey(CallInfo callInfo) throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UpdatePINImpl.getNewEncryptionKey()");}
		String code = Constants.EMPTY_STRING;
		
		try{
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
//		String enteredCINNumber = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
//		if(util.isNullOrEmpty(enteredCINNumber)){
//			throw new ServiceException("Entered  CIN number value is null or EMPTY");
//		}
//		
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered CIN Number ending with " + util.getSubstring(enteredCINNumber, Constants.GL_FOUR));}
		
		
		//String transmissionDateTime = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_TransmissionDateTime);
		String transmissionDateTime = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);
		
		String localTransTime = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
		String localTansDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDD); 
		
		
		
		
		//Following for the sequencial number generation for System trace audit number for S1 systems
		String db_Code = Constants.ONE;
		int codeLength = Constants.GL_ZERO;
		String sessionId = (String) callInfo.getField(Field.SESSIONID);


		if(util.isNullOrEmpty(sessionId)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session ID is null or empty");}
			throw new ServiceException("Session id is null or empty");
		}

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
		HashMap<String, Object> configMap = new HashMap<String, Object>();

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the input for getSequenceNo");}

		String uui = (String)callInfo.getField(Field.UUI);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


		DataServices dataServices = VRUDBDataServicesInstance.getInstance();
		String strRefNumberOne = Constants.EMPTY_STRING;
		try {
			db_Code = dataServices.getSequenceNoS1(logger, sessionId, uui, configMap);

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)){
				strRefNumberOne = (String) configMap.get(DBConstants.SEQUENCENO);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The sequencial number return value " + strRefNumberOne);}
				codeLength = strRefNumberOne.length();

				for(int p=codeLength; p < 6; p ++){
					strRefNumberOne = Constants.ZERO + strRefNumberOne;
					strRefNumberOne.trim();
				}

			}else{
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Error in the S1 sequencial DB response");}
				throw new ServiceException("Sequencial number DB access throwing error");
			}
		} catch (com.db.exception.ServiceException e) {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: GlobalImpl.getSequenceNumber()");}
			throw new ServiceException("S1 Sequencial number DB access throwing error");
			//e.printStackTrace();
		}


		String str_SysTraceAuditNo = strRefNumberOne; 
		
		//Need to create 6 or some random number as STAN
//		String str_SysTraceAuditNo = util.getRandomNumber(999999) + Constants.EMPTY_STRING;
				//(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_SysTraceAuditNo); 
//		int sysTraceAuditNo = util.isNullOrEmpty(str_SysTraceAuditNo)?Constants.GL_ZERO:Integer.parseInt(str_SysTraceAuditNo); 

		String networkInfoCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_NetworkInfoCode);
		int int_networkInfoCode = util.isNullOrEmpty(networkInfoCode)?Constants.GL_ZERO:Integer.parseInt(networkInfoCode);
		String messageAuthCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_MessageAuthCode);
		/**
		 * For Reporting Purpose
		 */
		HostReportDetails hostReportDetails = new HostReportDetails();
		
		String featureId = (String)callInfo.getField(Field.FEATUREID);
		hostReportDetails.setHostActiveMenu(featureId);
		//hostReportDetails.setHostCounter(hostCounter);
		//hostReportDetails.setHostEndTime(hostEndTime);
		String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)  + Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetails.setHostInParams(strHostInParam);
		hostReportDetails.setHostMethod(Constants.HOST_METHOD_KEYEXAUTH);
		//hostReportDetails.setHostOutParams(hostOutParams);
		hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
		
		hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
		
		hostReportDetails.setHostType(Constants.HOST_TYPE_DEBITCARDS);
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
		
		String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_KEYEXAUTH_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_KEYEXAUTH_REQUESTTYPE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

		
		KeyExAuth_HostRes keyExAuth_HostRes = keyExAuthDAO.getKeyExAuthHostRes(callInfo, transmissionDateTime, str_SysTraceAuditNo, 
				localTransTime, localTansDate, int_networkInfoCode, messageAuthCode, requestType);
		
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "keyExAuth_HostRes Object is :"+ keyExAuth_HostRes);}
		callInfo.setKeyExAuth_HostRes(keyExAuth_HostRes);

		code = keyExAuth_HostRes.getErrorCode();
		
		/*
		 * For Reporting Start
		 */
		
		String hostEndTime = keyExAuth_HostRes.getHostEndTime();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
		hostReportDetails.setHostEndTime(hostEndTime);
		
		String hostResCode = keyExAuth_HostRes.getHostResponseCode();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
		hostReportDetails.setHostResponse(hostResCode);
		
		String responseDesc = Constants.HOST_FAILURE;
		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			responseDesc = Constants.HOST_SUCCESS;
		}
		/****Duplicate RRN Fix 25012016 *****/
		strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)  + Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetails.setHostInParams(strHostInParam);
		/************************************/
		
		String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
				+ Constants.EQUALTO + hostResCode
		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(keyExAuth_HostRes.getErrorDesc()) ?"NA" :keyExAuth_HostRes.getErrorDesc());
		hostReportDetails.setHostOutParams(hostOutputParam);
		
		callInfo.setHostReportDetails(hostReportDetails);
		ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
		
		callInfo.updateHostDetails(ivrdata);
		//End Reporting
		
		
		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "New key was generated successfully and the host host end time is" + hostEndTime);}
			
			/**
			 * Setting the host response information in the call info
			 */
			
			String macCode = keyExAuth_HostRes.getMessageAuthCode();
			String lastUpdatedTime = keyExAuth_HostRes.getLastUpdateTimeStamp();
			String apinKey = keyExAuth_HostRes.getSecurityControlInfo();
			
			//TODO need to hide the below logging lines
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "APIN Key retrieved from the host is " + apinKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The last updated time value is " + lastUpdatedTime);}
			callInfo.setField(Field.APINKEY, apinKey);
			callInfo.setField(Field.LASTUPDATEDTIME, lastUpdatedTime);
			//Successfully set the value at the call info
		}else{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for KeyExAuth host service");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + keyExAuth_HostRes.getHostResponseCode());}

			util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_EXCHNGRATEINQ, keyExAuth_HostRes.getHostResponseCode());
		}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UpdatePINImpl.getNewEncryptionKey()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UpdatePINImpl.getNewEncryptionKey() "+ e);
			util.getFullStackTrace(e);
			throw new ServiceException(e);
		}
		return code;
	}
	
	/*public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		
	}*/
	
	
	@Override
	public boolean isAPINAConsecutiveNo(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UpdatePINImpl.isAPINAConsecutiveNo()");}
		boolean isAPINConsecuetive = false;
		try{
			
			String apin = (String)callInfo.getField(Field.APIN);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retreived user entered APIN" + apin);}
			
			isAPINConsecuetive = util.checkConsecutive(apin);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is APIN entered is consecuetive" + isAPINConsecuetive);}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UpdatePINImpl.getAPINPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return isAPINConsecuetive;
	}

	
	/*@Override
	public void getAndSetMasterKey(CallInfo callInfo) throws ServiceException {
		// TODO Auto-generated method stub
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UpdatePINImpl.getAndSetMasterKey()");}
		try{
			*//**
			 * Getting the dynamic key and the cipher text of master key from the DB Method
			 *//*
			
			String db_Code = Constants.ONE;
			int codeLength = Constants.GL_ZERO;
			String sessionId = (String) callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionId)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session ID is null or empty");}
				throw new ServiceException("Session id is null or empty");
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the input for getSequenceNo");}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}

			if(util.isNullOrEmpty(uui)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "uui ID is null or empty");}
				throw new ServiceException("Session id is null or empty");
			}
			
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			String apin_dynamicKey = Constants.EMPTY_STRING;
			String apin_MasterKey = Constants.EMPTY_STRING;
			String apin_StaticKey = Constants.APIN_STATIC_KEY;
			String combinedKey = Constants.EMPTY_STRING;
			try {
				db_Code = dataServices.getAPINMasterKey(logger, sessionId, uui, configMap);

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)){
					apin_dynamicKey = (String) configMap.get(DBConstants.APIN_DYNAMICKEY);
					apin_MasterKey = (String) configMap.get(DBConstants.APIN_MASTERKEY);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved APIN Dynamic & Master keys");}

					if(util.isNullOrEmpty(apin_dynamicKey) || util.isNullOrEmpty(apin_MasterKey)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "apin_dynamicKey / apin_MasterKey is null or empty");}
						throw new ServiceException("apin_dynamicKey / apin_MasterKey is null or empty");
					}
					
					if(util.isNullOrEmpty(apin_StaticKey)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "apin_StaticKey is null or empty");}
						throw new ServiceException("apin_StaticKey is null or empty");
					}
					
					combinedKey =apin_StaticKey + apin_dynamicKey;
					combinedKey = combinedKey.trim();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed the combined key ");}
					
					
					combinedKey = util.convertTo48BitKey(combinedKey);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined key has been converted to 48 bits");}

					
					if(util.isNullOrEmpty(combinedKey)){
						throw new ServiceException("Masterkey conversion result is null or empty");
					}
					
					JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
					SecretKey sKey = jceWrap.toSecretKey(combinedKey);
					String plainMasterKey = jceWrap.decrypt(apin_MasterKey, sKey);
//					String plainMasterKey = apin_MasterKey;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received the Plain master key");}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the Plain APIN Master key in the CallInfo field value");}
					callInfo.setField(Field.APINMASTERKEY, plainMasterKey);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Saved the Plain master key at the callinfo field");}
				}else{
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Error in the S1 sequencial DB response");}
					throw new ServiceException("APIN master key retrieval DB access throwing error");
				}
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: UpdatePINImpl.getAndSetMasterKey()");}
				throw new ServiceException("APIN Master key retrieval DB access throwing error");
				//e.printStackTrace();
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UpdatePINImpl.getAndSetMasterKey()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UpdatePINImpl.getAndSetMasterKey() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		
		
		
	}*/
	/*public static void main(String s[]){
		//acdae0b06b5c08dd8aec4571d029acc9 13EC170FE76CE16A
		//EEB4B236CC331EBBB765ACFAB72E8466
		JCEWrapper jceWrap = new JCEWrapper("d:\\parthiban\\JCEWrap_config.properties");
		//d31133806e546d521b637882cfa75418
		SecretKey sKey = jceWrap.toSecretKey(convertTo48BitKey("5A4B3C52CDE629F1"+"8F80FC5ABEB929E0"));
		String plainMasterKey = jceWrap.decrypt("d31133806e546d521b637882cfa75418", sKey);
		System.out.println(plainMasterKey);
		
		SecretKey sKey = jceWrap.toSecretKey(convertTo48BitKey("9FED8E449377530CD18BF0D4566A4BE2"));
		String clearKey = jceWrap.decrypt("7C5E7B7AEEFE868F4AD7FA7A9F882E41", sKey);
		System.out.println("Received the clear key and the clear key is "+ clearKey);

		String pinBlocking = util.getISOPinBlock(pan, pin, true, false, Constants.GL_THREE, Constants.GL_ONE);
		System.out.println("Have Successfully done the pin blocking");

		clearKey = convertTo48BitKey(clearKey);
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Clear key is converted to 48 digit and the result got "+ clearKey);}
		System.out.println("Clear key is converted to 48 digit and the result got "+clearKey);
		
		
	}*/
	
	public static String convertTo48BitKey(String inputString){
		//		if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.INFO, "ENTER: Util.convertTo48BitKey()");}
		int stringLength = Constants.GL_ZERO;
		String k1 = Constants.EMPTY_STRING;
		String k2 = Constants.EMPTY_STRING;
		String k3 = Constants.EMPTY_STRING;
		String result = Constants.EMPTY_STRING;
		try{


			stringLength = inputString.length();
			//			if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "Input string length is "+ stringLength);}


			if(stringLength == 16){
				//System.out.println("Input string of length 16, so treating K1= K2= K3");
				k1 = inputString;
				k2 = inputString;
				k3 = inputString;
				result = k1 + k2 + k3;				
				result = result.trim();
			}

			else if(stringLength == 32){
				//System.out.println("Input string of length 32, so treating K1= K3");

				k1 = inputString.substring(Constants.GL_ZERO, Constants.GL_SIXTEEN);
				//				if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "K1 value is " + k1);}

				k2 = inputString.substring(Constants.GL_SIXTEEN, inputString.length());
				//				if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "K2 value is " + k2);}

				k3 = k1;
				//System.out.println("Considering the result of K1 as K3 ");

				result = k1 + k2 + k3;				
				result = result.trim();
			}

			else if(stringLength == 48){
				//System.out.println("Input string of length 48, so returning the same");

				result = inputString.trim();
			}
			else{

				//System.out.println("Its an invalid key returning the same");
				return inputString;
			}

			//			if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "The final 48 bit length key is "+result);}

		}catch(Exception e){
			System.out.println("EXCEPTION at: Util.convertTo48BitKey()" + e);
			util.getFullStackTrace(e);
		}
		//System.out.println("Result="+result);
		return result;
	}
}
