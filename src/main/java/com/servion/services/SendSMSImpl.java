package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import com.db.beans.TblSMSRequest;
import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.SMSNotificationDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.jce.JCEWrapper;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.sendSMS.SendingSMS_HostRes;

public class SendSMSImpl implements ISendSMS{
	private static Logger logger = LoggerObject.getLogger();

	private SMSNotificationDAO sMSNotificationDAO;
	public SMSNotificationDAO getsMSNotificationDAO() {
		return sMSNotificationDAO;
	}

	public void setsMSNotificationDAO(SMSNotificationDAO sMSNotificationDAO) {
		this.sMSNotificationDAO = sMSNotificationDAO;
	}

	@Override
	public boolean isFromOTPGeneration(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: SendSMSImpl. isFromOTPGeneration()");}
			
			String isCalledFromOTP = Constants.EMPTY_STRING + callInfo.getField(Field.ISSMSCALLEDFRMOTP);
		
			if(Constants.TRUE.equalsIgnoreCase(isCalledFromOTP)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its called from OTP module");}
				return true;
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its not called from OTP module");}
		
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  SendSMSImpl. isFromOTPGeneration ()" + e.getMessage());
			throw new ServiceException(e);
		}
		return false;
	}

	@Override
	public String sendSMSToRegisteredNumber(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: SendSMSImpl.sendSMSToRegisteredNumber()");}
		String code = Constants.ONE;
		//		getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
//			String userID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_UserID);
//		
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting User ID value is " + userID);}
//			
//			String password = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_Password);
//
//			String appId = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_AppID);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting app ID value is " + appId);}
//		
//			String deptID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_DeptId);
//			int int_DeptID = util.isNullOrEmpty(deptID)?Constants.GL_ZERO : Integer.parseInt(deptID);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting Department ID value is " + int_DeptID);}
//			
//			String sendAsChannel = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_SendAsChannel);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting SendAsChannel value is " + sendAsChannel);}
//			
//			int smsTemplateID = Constants.GL_ZERO;  
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting Template ID is " + smsTemplateID);}
//			
//			String language = (String)callInfo.getField(Field.LANGUAGE);
//			int langID = Constants.GL_ZERO;
//			if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
//				langID = Constants.GL_ONE;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language is Arabic");}
//			}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The requesting language id is "+ langID);}
//			
//			XMLGregorianCalendar transactionDate = util.getXMLGregorianCalendarNow();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The requesting Transaction Date is "+ transactionDate);}
//			
//			String gsmNo = Constants.EMPTY_STRING;
//			
//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())){
//					gsmNo = callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getGSM();
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Registered GSM number is "+ gsmNo);}
//				}
//			}
//			
//			long long_GsmNo = util.isNullOrEmpty(gsmNo)?Constants.GL_ZERO : Long.parseLong(gsmNo);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The requesting GSM is "+ long_GsmNo);}
//			
//			String msgTxt = getSMSMessage(callInfo);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DB output is "+ msgTxt);}
//			
//			boolean isFromOTPModule = isFromOTPGeneration(callInfo);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from OTP module "+ isFromOTPModule);}
//			
//			String referenceNo = Constants.EMPTY_STRING;
//		
//			String featureName = (String)callInfo.getField(Field.FEATURENAME);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}
//			
//			String smsScript = (String)callInfo.getField(Field.SMSMessageScript);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved SMS scripting is "+ smsScript);}
//			
//			if(isFromOTPModule){
//				referenceNo = Constants.EMPTY_STRING+callInfo.getField(Field.GENERATEDOTPREFNO);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated OTP Reference No is "+ referenceNo);}
//				
//				String generatedOTP = (String)callInfo.getField(Field.GENERATEDOTP);
//				//TODO need to remove the below logging lines
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated OTP is "+ generatedOTP);}
//				
//				
//				smsScript = smsScript.replace(Constants.SMS_INDEX_0, featureName);
//				smsScript = smsScript.replace(Constants.SMS_INDEX_1, generatedOTP);
//				smsScript = smsScript.replace(Constants.SMS_INDEX_2, referenceNo);
//				
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final OTP sms script is "+ smsScript);}
//				
//				
//			}else{
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its called from Transaction success message ");}
//				
//				referenceNo = (String)callInfo.getField(Field.Transaction_Ref_No);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Host Transaction Reference No is "+ referenceNo);}
//				
//				smsScript = smsScript.replace(Constants.SMS_INDEX_0, featureName);
//				smsScript = smsScript.replace(Constants.SMS_INDEX_1, referenceNo);
//				
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Success Transaction sms script is "+ smsScript);}
//				
//			}
//			
//			/**
//			 * For Reporting Purpose
//			 */
//			HostReportDetails hostReportDetails = new HostReportDetails();
//			
//			String featureId = (String)callInfo.getField(Field.FEATUREID);
//			hostReportDetails.setHostActiveMenu(featureId);
//			//hostReportDetails.setHostCounter(hostCounter);
//			//hostReportDetails.setHostEndTime(hostEndTime);
//			String strHostInParam = Constants.HOST_INPUT_PARAM_REGISTEREDGSM + Constants.EQUALTO + long_GsmNo ;
//			hostReportDetails.setHostInParams(strHostInParam);
//			hostReportDetails.setHostMethod(Constants.HOST_METHOD_SMSNOTIFICATION);
//			//hostReportDetails.setHostOutParams(hostOutParams);
//			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
//			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
//			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
//			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
//			
//			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
//			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
//			//End Reporting
//			
//			SendingSMS_HostRes sendingSMS_HostRes = sMSNotificationDAO.getSendSMSHostRes(callInfo, userID, password, appId, int_DeptID, langID, transactionDate,
//					sendAsChannel, smsTemplateID, long_GsmNo, msgTxt);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "sendingSMS_HostRes Object is :"+ sendingSMS_HostRes);}
//			callInfo.setSendingSMS_HostRes(sendingSMS_HostRes);
//
//			code = sendingSMS_HostRes.getErrorCode();
//			
//			/*
//			 * For Reporting Start
//			 */
//			
//			String hostEndTime = sendingSMS_HostRes.getHostEndTime();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
//			hostReportDetails.setHostEndTime(hostEndTime);
//			
//			String hostResCode = sendingSMS_HostRes.getHostResponseCode();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
//			hostReportDetails.setHostResponse(hostResCode);
//			
//			String responseDesc = Constants.HOST_FAILURE;
//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				responseDesc = Constants.HOST_SUCCESS;
//			}
//			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
//					+ Constants.EQUALTO + hostResCode;
//				
//			hostReportDetails.setHostOutParams(hostOutputParam);
//			
//			callInfo.setHostReportDetails(hostReportDetails);
//			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
//			
//			callInfo.insertHostDetails(ivrdata);
//			//End Reporting
//			
//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for SMS Notification services");}
//			}else{
//
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for SMS notification host service");}
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + sendingSMS_HostRes.getHostResponseCode());}
//
//				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_SMSNOTIFICATION, sendingSMS_HostRes.getHostResponseCode());
//			
//			}
//			
			
			
			/**
			 * Insert SMS Request Data
			 */
			
			boolean isFromOTPModule = isFromOTPGeneration(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from OTP module "+ isFromOTPModule);}
			
			String referenceNo = Constants.EMPTY_STRING;
			
			String amount = (String)callInfo.getField(Field.AMOUNT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction amount is " + amount);}
			
			String callId = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Call ID is " + callId);}
			
			String custId = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction CustID is " + custId);}
			
			String destNo = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction destNo is " + util.maskCardOrAccountNumber(destNo));}
			
			String sourceNo = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction SourceNo is " + util.maskCardOrAccountNumber(sourceNo));}
			
			String featureID = (String)callInfo.getField(Field.FEATUREID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Feature ID is " + featureID);}
			
			String lang = (String)callInfo.getField(Field.LANGUAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Language is " + lang);}
			
			String mobileNo = (String)callInfo.getField(Field.REG_MOBILENO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Mobile no is " + mobileNo);}
			
			String otp = (String)callInfo.getField(Field.ENCRYPTEDOTP);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Encrypted OTP is " + otp);}
			
			if(isFromOTPModule){
				referenceNo = Constants.EMPTY_STRING+callInfo.getField(Field.GENERATEDOTPREFNO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated OTP Reference No is "+ referenceNo);}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its called from Transaction success message ");}
				
				//TODO: As requested by Vijay on 14-09-2015
				/***************************************************/
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Data values");}
				ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				String enableSendingSMS = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.EnableSendingSMS))? "" : (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.EnableSendingSMS);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "is Enable Sending SMS:"+ enableSendingSMS);}
				if(enableSendingSMS == null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Sending SMS Enabled");}
				}
				else if(enableSendingSMS.equalsIgnoreCase(Constants.FALSE)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Sending SMS Disabled: returning 0");}
					return "0";
				}
				/***************************************************/
				referenceNo = (String)callInfo.getField(Field.Transaction_Ref_No);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Host Transaction Reference No is "+ referenceNo);}
			}
			
			String reqChannel = Constants.IVR;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Requesting Channel is " + reqChannel);}
			
			String reqDateTime = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			SimpleDateFormat sdfSource = new SimpleDateFormat(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			Date date_ReqDateTime = sdfSource.parse(reqDateTime);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Req Date Time is " + date_ReqDateTime);}
			
			//Template id should be a feature name
			String smsTemplateId = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction smsTemplate ID is " + smsTemplateId);}
			
			
			String calledFrom = (String) callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Send SMS is called from " + calledFrom);}
			
			
			
			if(Constants.FEATURENAME_CARDPINSET.equalsIgnoreCase(calledFrom) ||
					Constants.FEATURENAME_CARDPINRESET.equalsIgnoreCase(calledFrom)){
				String cinType = (String)callInfo.getField(Field.ENTEREDCINTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction cinType is " + cinType);}
				String cardNo = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction cardNo is " + util.maskCardOrAccountNumber(cardNo));}
				
				/**
				 * Doing triple desk encryption using JCEWrapper code
				 */
				String otpKey = util.isNullOrEmpty(callInfo.getField(Field.OTPKEY)) ? Constants.OTP_KEY : (String)callInfo.getField(Field.OTPKEY);
//				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The OTP key stored is " + otpKey);}

				otpKey = util.convertTo48BitKey(otpKey);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP key has been converted to 48 bits");}

				if(util.isNullOrEmpty(otpKey)){
					throw new ServiceException("otpKey conversion result is null or empty");
				}
				
				JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
				SecretKey sKey = jceWrap.toSecretKey(otpKey);
				String encryptedCardNo = jceWrap.encrypt(cardNo, sKey);
				String hostName = Constants.EMPTY_STRING;
				
				ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				
				if(util.isNullOrEmpty(iceFeatureData)){
					throw new ServiceException("iceFeatureData object is null or empty");
				}
				
				
				if(cinType.equalsIgnoreCase(Constants.CIN_TYPE_CREDIT) || cinType.equalsIgnoreCase(Constants.CIN_TYPE_PREPAID)){
					hostName = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CI_CreditCardHostNameForOTP))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CI_CreditCardHostNameForOTP);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "hostName configured is " + hostName);}
					//hostName = "PRM";
				}else if(cinType.equalsIgnoreCase(Constants.CIN_TYPE_DEBIT)){
					hostName = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CI_DebitCardHostNameForOTP))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CI_DebitCardHostNameForOTP);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "hostName configured is " + hostName);}
					//hostName = "T24";
				}
				custId = custId + Constants.TILD + encryptedCardNo + Constants.TILD + hostName; 
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction CustID after appending cusotm values is " + custId);}
			}
			
			if(Constants.FEATURENAME_STATEMENTREQUESTBANKS.equalsIgnoreCase(calledFrom) ||
					Constants.FEATURENAME_STATEMENTREQUESTCARDS.equalsIgnoreCase(calledFrom)){
				
				String selectedStmtProcessType = (String) callInfo.getField(Field.SELECTEDSTMTPROCESSINGTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Statment processing type is " + selectedStmtProcessType);}

				if(Constants.STMTPROCESSING_TYPE_FAX.equalsIgnoreCase(selectedStmtProcessType)){
					smsTemplateId = smsTemplateId + Constants.FOR_FAX;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final SMS template id for transaction details fax is " + smsTemplateId);}
				}else{
					smsTemplateId = smsTemplateId + Constants.FOR_EMAIL;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final SMS template id for transaction details Email is " + smsTemplateId);}
				}

			}
			//END - Vinoth
			
			
			TblSMSRequest tblSMSRequest = new TblSMSRequest();
			tblSMSRequest.setAmount(amount);
			tblSMSRequest.setCallUniqueId(callId);
			tblSMSRequest.setCustId(custId);
			tblSMSRequest.setDestNo(destNo);
			tblSMSRequest.setFeatureId(featureID);
			tblSMSRequest.setLang(lang);
			tblSMSRequest.setMobileNo(mobileNo);
			tblSMSRequest.setOtp(otp);
			tblSMSRequest.setReferenceNo(referenceNo);
			tblSMSRequest.setReqChannel(reqChannel);
			tblSMSRequest.setReqDateTime(date_ReqDateTime);
			tblSMSRequest.setSmsTemplateId(smsTemplateId);
			tblSMSRequest.setSourceNo(sourceNo);
			
			tblSMSRequest.setSmsStatus(Constants.N);
			tblSMSRequest.setSmsNoOfTries(Constants.ZERO);
			
			HashMap<String, Object> configMap = new HashMap<String, Object>();
			configMap.put(DBConstants.SMSDATA, tblSMSRequest);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting with configMap" + configMap);}
			
			
			String sessionId = (String)callInfo.getField(Field.SESSIONID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session ID is " + sessionId);}
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI value is " + uui);}
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.insertSMSData(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Insert SMS data DB method resonse code is " + code );}
//				String smsMessage = Constants.EMPTY_STRING;
				if(Constants.ZERO.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for send sms DB method call");}
//					smsMessage = (String)configMap.get("SMSDATA");
//					callInfo.setField(Field.SMSMessageScript, smsMessage);
				}
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			//END - Vinoth
			
			
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at SendSMSImpl.sendSMSToRegisteredNumber() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}
	
	@Override
	public String sendSMSToMobileNumber(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: SendSMSImpl.sendSMSToMobileNumber()");}
		String code = Constants.ONE;
		//		getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null or empty");
			}
			
			String userID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_UserID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting User ID value is " + userID);}

			String password = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_Password);

			String appId = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_AppID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting app ID value is " + appId);}

			String deptID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_DeptId);
			//int int_DeptID = util.isNullOrEmpty(deptID)?Constants.GL_ZERO : Integer.parseInt(deptID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting Department ID value is " + deptID);}

			String sendAsChannel = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_SMSNotification_SendAsChannel);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting SendAsChannel value is " + sendAsChannel);}

			int smsTemplateID = Constants.GL_ZERO;  
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting Template ID is " + smsTemplateID);}

			String language = (String)callInfo.getField(Field.LANGUAGE);
			int langID = Constants.GL_ZERO;
			if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
				langID = Constants.GL_ONE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language is Arabic");}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The requesting language id is "+ langID);}

			XMLGregorianCalendar transactionDate = util.getXMLGregorianCalendarNow();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The requesting Transaction Date is "+ transactionDate);}

			String gsmNo = (String) callInfo.getField(Field.DESTNO);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Registered GSM number is "+ gsmNo);}

			long long_GsmNo = util.isNullOrEmpty(gsmNo)?Constants.GL_ZERO : Long.parseLong(gsmNo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The requesting GSM is "+ long_GsmNo);}
			
			String infoType = (String) callInfo.getField(Field.SELECTEDPRODUCT);
			if(util.isNullOrEmpty(infoType)){
				throw new ServiceException("SMS Info Type object is null=="+infoType);
			}
			
			String langCode = Constants.EMPTY_STRING;
			if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
				langCode = "ARA";
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SMS Info Language is "+langCode);}
			}else if(Constants.Urudu.equalsIgnoreCase(language) || Constants.Uru.equalsIgnoreCase(language) || Constants.ALPHA_U.equalsIgnoreCase(language)){
				langCode = "URD";
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SMS Info Language is "+langCode);}
			}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
				langCode = "HIN";
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SMS Info Language is "+langCode);}
			}else{
				langCode = "ENG";
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SMS Info Language is "+langCode);}
			}
			
			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}
			
			StringBuilder key = new StringBuilder(Constants.CUI_);
			if(featureName.equalsIgnoreCase(Constants.FEATURENAME_MOBILENUMBERCHANGE)){
				key.append(Constants.CUI_MobChange_Success_SMSText_).append(langCode);
			}else{
				key.append(infoType).append(Constants._SMS_TEXT_).append(langCode);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SMS Info SMS Text key is "+key.toString());}
			
			String msgTxt = (String) ivr_ICEFeatureData.getConfig().getParamValue(key.toString());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Message Text from config is "+ msgTxt);}

			

			String smsScript = (String)callInfo.getField(Field.SMSMessageScript);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved SMS scripting is "+ smsScript);}

			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_REGISTEREDGSM + Constants.EQUALTO + long_GsmNo 
					+ Constants.COMMA + Constants.TEMPLATE + Constants.EQUALTO + infoType;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_SMSNOTIFICATION);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting

			SendingSMS_HostRes sendingSMS_HostRes = sMSNotificationDAO.getSendSMSHostRes(callInfo, userID, password, appId, deptID, langID, transactionDate,
					sendAsChannel, smsTemplateID, long_GsmNo, msgTxt, "");
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "sendingSMS_HostRes Object is :"+ sendingSMS_HostRes);}
			callInfo.setSendingSMS_HostRes(sendingSMS_HostRes);

			code = sendingSMS_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = sendingSMS_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = sendingSMS_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode;
				
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.insertHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for SMS Notification services");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for SMS notification host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + sendingSMS_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_SMSNOTIFICATION, sendingSMS_HostRes.getHostResponseCode());

			}
			//END - Vinoth
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at SendSMSImpl.sendSMSToMobileNumber() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}
	
	public String getSMSMessage(CallInfo callInfo) throws ServiceException{

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: SendSMSImpl. getSMSMessage()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String language = (String)callInfo.getField(Field.LANGUAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language is " + language);}
			
			String smsTemplate = (String)callInfo.getField(Field.FEATURENAME);
		
			
			boolean isFromOTPModule = isFromOTPGeneration(callInfo);
			if(isFromOTPModule)
				smsTemplate=smsTemplate+Constants._OTP;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Sms template is "+ smsTemplate);}
			
			configMap.put(DBConstants.SMSTEMPLATEID, smsTemplate);
			configMap.put(DBConstants.LANGUAGE, language);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language" + configMap.get(Constants.PREFERRED_LANGUAGE));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI is " + configMap.get(Constants.CLI) );}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.getSMSData(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The DB method resonse code is " + code );}
				String smsMessage = Constants.EMPTY_STRING;
				if(Constants.ZERO.equalsIgnoreCase(code)){
					smsMessage = (String)configMap.get("SMSDATA");
					callInfo.setField(Field.SMSMessageScript, smsMessage);
				}
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: ()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  ()" + e.getMessage());
			throw new ServiceException(e);
		}
	}
	/*
	 * public static void main(String[] args) { String smsScript
	 * ="Your OTP number for the transaction {0} is {1}. Your reference number is {2}"
	 * ; smsScript = smsScript.replace(Constants.SMS_INDEX_0, "featureName");
	 * smsScript = smsScript.replace(Constants.SMS_INDEX_1, "generatedOTP");
	 * smsScript = smsScript.replace(Constants.SMS_INDEX_2, "referenceNo");
	 * 
	 * System.out.println("The final OTP sms script is "+ smsScript);
	 * 
	 * }
	 */
}
