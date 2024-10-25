package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
import com.servion.context.Context;
import com.servion.dao.CustDtlsDAO;
import com.servion.dao.CustomerShortUpdateDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.CustDtls.CustDtls_HostRes;
import com.servion.model.mobileNumberChange.MobileNumberChange_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class MobileNumberChangeImpl implements IMobileNumberChange{
	
	private static Logger logger = LoggerObject.getLogger();
	
	private CustomerShortUpdateDAO customerShortUpdateDAO; 
	private CustDtlsDAO custDtlsDAO;
	private MessageSource messageSource;

	public CustomerShortUpdateDAO getCustomerShortUpdateDAO() {
		return customerShortUpdateDAO;
	}

	public void setCustomerShortUpdateDAO(
			CustomerShortUpdateDAO customerShortUpdateDAO) {
		this.customerShortUpdateDAO = customerShortUpdateDAO;
	}
	public CustDtlsDAO getCustDtlsDAO() {
		return custDtlsDAO;
	}

	public void setCustDtlsDAO(CustDtlsDAO custDtlsDAO) {
		this.custDtlsDAO = custDtlsDAO;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	

	/**
	 * 
	 * @param callInfo
	 * @return
	 * @throws ServiceException
	 * As per the new complaint alert change, need to false the expired status to continue the IVR
	 */
	@Override
	public String getISD(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: MobileNumberChangeImpl.getISD()");}
		String code = Constants.ONE;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			
			
			String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerId);}
			String SelectedCardOrAcctNo = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
			
			String requestType =  "CUEDETS";
			//util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CustDtls_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CustDtls_REQUESTTYPE);
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
			hostReportDetails.setHostInParams(Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)));
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CustDtls);
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
			
			CustDtls_HostRes custDtls_HostRes = getCustDtlsDAO().getCustDtlsHostRes(callInfo, SelectedCardOrAcctNo, customerId, requestType);
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "custDtls_HostRes Object is :"+ custDtls_HostRes);}
			callInfo.setCustDtls_HostRes(custDtls_HostRes);

			code = custDtls_HostRes.getErrorCode();
			
			/*
			 * For Reporting Start
			 */

			String hostEndTime = custDtls_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = custDtls_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId 
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
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(custDtls_HostRes.getErrorDesc()) ?"NA" :custDtls_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for MobileNumberChangeImpl.getISD");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Customer Short Update host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + custDtls_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CustDtls, custDtls_HostRes.getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: MobileNumberChangeImpl.updateMobileNumber()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  MobileNumberChangeImpl.updateMobileNumber() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return code;
	}
	@Override
	public String updateMobileNumber(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: MobileNumberChangeImpl.updateMobileNumber()");}
		String code = Constants.ONE;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String mobileNumber = (String)callInfo.getField(Field.DESTNO);
			if(util.isNullOrEmpty(mobileNumber)){
				throw new ServiceException("Entered Mobile number is empty or null");
			}
			
			String countryCode = (String) callInfo.getField(Field.MOBILE_COUNTRY_CODE);
			if(util.isNullOrEmpty(countryCode)){
				throw new ServiceException("Entered Country code is empty or null");
			}
			
			String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerId);}
			
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CUSTOMERSHORTUPDATE_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CUSTOMERSHORTUPDATE_REQUESTTYPE);
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
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_MOBILENUMBERCHANGE);
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
			
			
			MobileNumberChange_HostRes mobileNumberChange_HostRes =  customerShortUpdateDAO.getMobileNumberChange_HostRes(callInfo, customerId, mobileNumber, countryCode, requestType);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "MobileNumberChange_HostRes Object is :"+ mobileNumberChange_HostRes);}
			callInfo.setMobileNumberChange_HostRes(mobileNumberChange_HostRes);

			code = mobileNumberChange_HostRes.getErrorCode();
			
			/*
			 * For Reporting Start
			 */

			String hostEndTime = mobileNumberChange_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = mobileNumberChange_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId 
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
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(mobileNumberChange_HostRes.getErrorDesc()) ?"NA" :mobileNumberChange_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for MobileNumberChangeImpl.updateMobileNumber");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Customer Short Update host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + mobileNumberChange_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_MOBILENUMBERCHANGE, mobileNumberChange_HostRes.getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: MobileNumberChangeImpl.updateMobileNumber()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  MobileNumberChangeImpl.updateMobileNumber() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return code;
	}
	
	@Override
	public String getMobileNoChangeConfirmationPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: MobileNumberChangeImpl.getMobileNoChangeConfirmationPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			String mobileNo = (String) callInfo.getField(Field.NEW_REG_MOB_NUMBER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile number "+ mobileNo);}
			
			String countryCode = (String) callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile number with out Country code : "+ countryCode);}
			
			String countryCodeMobileNo = mobileNo;

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(countryCodeMobileNo);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("MOBILE_NUMBER_CHANGE_CONF");
			String featureID = FeatureIDMap.getFeatureID("MobileNumberChange");
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
			totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file
			// No need 

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: MobileNumberChangeImpl.getMobileNoChangeConfirmationPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at MobileNumberChangeImpl.getMobileNoChangeConfirmationPhrases "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}
	
	@Override
	public String insertMobileNumberChangeTransaction(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: MobileNumberChangeImpl.insertMobileNumberChangeTransaction()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String existingMobNumber = (String) callInfo.getField(Field.EXISTING_REG_MOB_NUMBER);
			String newMobNumber = (String) callInfo.getField(Field.NEW_REG_MOB_NUMBER);
			String hostResult = (String) callInfo.getField(Field.MOB_CHG_HOST_RESULT);
			String txnResult = (String) callInfo.getField(Field.MOB_CHG_TXN_RESULT);
			
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.EXISTING_MOB_NUM, existingMobNumber);
			configMap.put(DBConstants.NEW_MOB_NUM, newMobNumber);
			configMap.put(DBConstants.HOST_RESULT, hostResult);
			configMap.put(DBConstants.TXN_RESULT, txnResult);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id: " + configMap.get(Constants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "EXISTING_MOB_NUM: " + existingMobNumber);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "NEW_MOB_NUM: " + newMobNumber);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "HOST_RESULT: " + hostResult);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "TXN_RESULT: " + txnResult);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id " + configMap.get(Constants.CUSTOMERID));}
			
			
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				//code = dataServices.getChequeBookOrdered(logger, sessionId, uui, configMap);
				code = dataServices.insertMobileNumberChangeTransaction(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			}catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: MobileNumberChangeImpl.insertMobileNumberChangeTransaction()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: MobileNumberChangeImpl.insertMobileNumberChangeTransaction()");}
			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: MobileNumberChangeImpl.insertMobileNumberChangeTransaction()" + e.getMessage());
			throw new ServiceException(e);
		}
	}
	
	@Override
	public String isMobChangeAllowed(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: MobileNumberChangeImpl.getMobNumChangeSuccessFailureCount()");}
			IGlobal globalService = Context.getIglobal();
			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);
			String ani = globalService.getANIRemovePrefix(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.CLI, ani);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id: " + configMap.get(Constants.CUSTOMERID));}
						
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			int successCount = 99;
			int hostFailureCount = 99;
			try {
				//code = dataServices.getChequeBookOrdered(logger, sessionId, uui, configMap);
				code = String.valueOf(dataServices.getMobNumChangeSuccessCount(logger, sessionId, uui, configMap));
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB getMobNumChangeSuccessCount method call is " + code );}
				if(code.equalsIgnoreCase(DBConstants.SUCCESS)){
					successCount = (Integer) configMap.get(DBConstants.MOBCHG_SUCCESS_COUNT);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success Transaction count is " + successCount );}
					callInfo.setField(Field.MOB_CHG_SUCCESS_COUNT, successCount);
					code = String.valueOf(dataServices.getMobNumChangeFailureCount(logger, sessionId, uui, configMap));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB getMobNumChangeFailureCount method call is " + code );}
					if(code.equalsIgnoreCase(DBConstants.SUCCESS)){
						hostFailureCount = (Integer) configMap.get(DBConstants.MOBCHG_FAILURE_COUNT);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failure Transaction count is " + hostFailureCount );}
						callInfo.setField(Field.MOB_CHG_FAILURE_COUNT, hostFailureCount);
					}
				}
				
				ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

				if(util.isNullOrEmpty(iceFeatureData)){
					throw new ServiceException("iceFeatureData object is null or empty");
				}
				
				int configuredAllowedCount = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_MobileNumberChangeAllowedCountPerDay))? 0 : Integer.parseInt(iceFeatureData.getConfig().getParamValue(Constants.CUI_MobileNumberChangeAllowedCountPerDay).toString());
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "configuredAllowedCount configured is " + configuredAllowedCount);}
				
				int configuredHostFailureAllowedCount = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_MaxHostFailureAllowedCountPerDay))? 0 : Integer.parseInt(iceFeatureData.getConfig().getParamValue(Constants.CUI_MaxHostFailureAllowedCountPerDay).toString());
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "hostFailureAllowedCount configured is " + configuredHostFailureAllowedCount);}
				
				if(successCount >= configuredAllowedCount){
					callInfo.setField(Field.MOB_CHG_PERDAY_LIMIT_EXCEEDED, Constants.TRUE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile Number Change: Success Per day change limit exceeded");}
				}else if(hostFailureCount >= configuredHostFailureAllowedCount){
					callInfo.setField(Field.MOB_CHG_PERDAY_LIMIT_EXCEEDED, Constants.FALSE);
					callInfo.setField(Field.MOB_CHG_HOSTFAIL_LIMIT_EXCEEDED, Constants.TRUE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile Number Change: Host Failure Per day change limit exceeded");}
				}else {
					callInfo.setField(Field.MOB_CHG_HOSTFAIL_LIMIT_EXCEEDED, Constants.FALSE);
					callInfo.setField(Field.MOB_CHG_PERDAY_LIMIT_EXCEEDED, Constants.FALSE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile Number Change: Limit not exceed");}
				}
						
			}catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: MobileNumberChangeImpl.getMobNumChangeSuccessFailureCount()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: MobileNumberChangeImpl.getMobNumChangeSuccessFailureCount()");}
			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: MobileNumberChangeImpl.getMobNumChangeSuccessFailureCount()" + e.getMessage());
			throw new ServiceException(e);
		}
	}
	
}
