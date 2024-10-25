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
import com.servion.dao.TelecomPrepaidNumberValDAO;
import com.servion.dao.TelecomSubscriberInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.billPayment.TelecomPrepaidNumberVal_HostRes;
import com.servion.model.billPayment.TelecomSubcriberInfoBasicDetails;
import com.servion.model.billPayment.TelecomSubscriberInfo_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class UtilityBillPaymentTopUpImpl implements IUtilityBillPaymentTopUp{

	private static Logger logger = LoggerObject.getLogger();

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	
	private TelecomSubscriberInfoDAO telecomSubscriberInfoDAO;
	/**
	 * TODO - CR
	*/ 
	private TelecomPrepaidNumberValDAO telecomPrepaidNumberValDAO;
	public TelecomPrepaidNumberValDAO getTelecomPrepaidNumberValDAO() {
		return telecomPrepaidNumberValDAO;
	}

	public void setTelecomPrepaidNumberValDAO(
			TelecomPrepaidNumberValDAO telecomPrepaidNumberValDAO) {
		this.telecomPrepaidNumberValDAO = telecomPrepaidNumberValDAO;
	}
	
	public TelecomSubscriberInfoDAO getTelecomSubscriberInfoDAO() {
		return telecomSubscriberInfoDAO;
	}

	public void setTelecomSubscriberInfoDAO(
			TelecomSubscriberInfoDAO telecomSubscriberInfoDAO) {
		this.telecomSubscriberInfoDAO = telecomSubscriberInfoDAO;
	}

	@Override
	public String getTopUpServiceProviderPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentTopUpImpl.getTopUpServiceProviderPhrases()");}
		String str_GetMessage, finalResult;

		try{ 
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();




			/**
			 * Need Vijay confirmation to enable below since we need to look the configured utilty bill and then look for service provider code here
			 */

			//			
			//			
			//			ArrayList<String> serviceProviderCodeList = null;
			//			//Need to get the FeatureConfig Data
			//			
			//			ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
			//			if(util.isNullOrEmpty(iceFeatureData)){
			//				throw new ServiceException("ICEFeature Date object is null / Empty");
			//			}
			//			
			//			String hostUtilityCodeList = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_APPLICABLE_UTILITY_CODE);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Utility Code list retrieved from UI is "  + hostUtilityCodeList);}
			//			
			//			if(util.isNullOrEmpty(hostUtilityCodeList)){
			//				throw new ServiceException("Utility Code List is not configured in the UI");
			//			}
			//			
			//			if(util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes()) || util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList())){
			//				throw new ServiceException("Utilit code from list beneficiary host is null or empty");
			//			}
			//			
			//			ArrayList<String>utilityCodeListFrmHost = callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList();
			//			String[] strHostArrayList = hostUtilityCodeList.split(Constants.COMMA);
			//			String[] serviceProviderArr = null;
			//			String strServiceProviderList = Constants.EMPTY_STRING;
			//			String strUtilityCode = Constants.EMPTY_STRING;
			//			String strServiceCode = Constants.EMPTY_STRING;
			//			for(int i=0; i<strHostArrayList.length;i++){
			//				strUtilityCode = strHostArrayList[i];
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code is "  + strUtilityCode);}
			//				
			//				if(utilityCodeListFrmHost.contains(strUtilityCode)){
			//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The list beneficiary utlity code is applicable"  + strUtilityCode);}
			//					strServiceProviderList = (String)iceFeatureData.getConfig().getParamValue(strUtilityCode);
			//					serviceProviderArr = strServiceProviderList.split(Constants.COMMA);
			//					
			//					for(int j=0; j<serviceProviderArr.length;j++){
			//						strServiceCode = serviceProviderArr[j];
			//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The service provider code for the above utility code is"  + strServiceCode);}
			//						serviceProviderCodeList.add(strServiceCode);
			//					}
			//				}
			//			}
			//			
			//			
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final service provider list going to read to customer is "  + serviceProviderCodeList);}
			//			


			//Need to get the FeatureConfig Data
			ArrayList<String> providerList = null;
			providerList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_SERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service provider list retrieved is :" + providerList);}


			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			/**
			 * Note temp_Str is nothing but the provider name.  The wave file also should recorded in the same provider name
			 * 
			 * eg NAVRAS --> NAVARS.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;

			for(int count=Constants.GL_ZERO;count<providerList.size();count++){
				temp_Str = providerList.get(count);
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

			String menuID = MenuIDMap.getMenuID("TOPUP_SERVIONPROVIDER_SELECTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Topup");
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
			if(providerList.size()>int_moreCount){
				totalPrompt = Constants.GL_THREE * providerList.size();
				totalPrompt = totalPrompt + Constants.GL_ONE;
				
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = providerList.size() / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//				int temp2 =  providerList.size() % int_moreCount;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//				if(temp2 > 0){
//					temp1++;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
			}
			else{
				totalPrompt = Constants.GL_THREE * providerList.size();
				totalPrompt = totalPrompt + Constants.GL_ONE;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			//To have the property file grammar, need to call that util method here
			//No Need here
			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe seperator sign
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentTopUpImpl.getTopUpServiceProviderPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentTopUpImpl.getTopUpServiceProviderPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTopUpMobileNumberPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentTopUpImpl.getTopUpMobileNumberPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			//			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			//	

			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "*************Rule Engine Object is null or Empty*************" + ruleParamObj);}
			}

			HashMap<String, String>lastEnteredMobileNumberMap = null;

			lastEnteredMobileNumberMap = util.isNullOrEmpty(callInfo.getField(Field.LASTENTEREDMOBILENOMAP)) ? new HashMap<String, String>() : (HashMap<String, String>) callInfo.getField(Field.LASTENTEREDMOBILENOMAP);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last EnteredMobile Number Mobile number map retrieved is "+ lastEnteredMobileNumberMap);}

			String lastSelectedServiceProviderCode = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Selected service provider code is "+ lastSelectedServiceProviderCode);}


			String lastEnteredMobNo = null;

			if(!util.isNullOrEmpty(lastSelectedServiceProviderCode) && !util.isNullOrEmpty(lastEnteredMobileNumberMap)){
				lastEnteredMobNo = lastEnteredMobileNumberMap.get(lastSelectedServiceProviderCode);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Selected service provider code is "+ lastSelectedServiceProviderCode);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last entered mobile number is "+ lastEnteredMobNo);}
			}


			//			String lastEnteredMobNo = (String)callInfo.getField(Field.LASTSELECTEDMOBILENUMBER);
			//			ArrayList<String> lastSelMobileNumberList = (ArrayList<String>)ruleParamObj.getParam(Constants.RULE_ENGINE_TOP_UP_MOBILE_NO)!= null ? (ArrayList<String>)ruleParamObj.getParam(Constants.RULE_ENGINE_TOP_UP_MOBILE_NO) : null;
			//			if(!util.isNullOrEmpty(lastSelMobileNumberList)){
			//				lastEnteredMobNo = lastSelMobileNumberList.get(Constants.GL_ZERO);
			//			}

			//Need to get the FeatureConfig Data
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Selected Mobile number is"+lastEnteredMobNo);}
			callInfo.setField(Field.LASTSELECTEDMOBILENUMBER, lastEnteredMobNo);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			if(!util.isNullOrEmpty(lastEnteredMobNo)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding phrase for top up already entered mobile number ");}
				dynamicValueArray.add(DynaPhraseConstants.Top_Up_1001);
				dynamicValueArray.add(lastEnteredMobNo);
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
				dynamicValueArray.add(DynaPhraseConstants.Lost_Card_1006);
				grammar = Constants.ONE;
			}

			dynamicValueArray.add(DynaPhraseConstants.Top_Up_1002);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated Grammar list is"+grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("TOPUP_WITH_SAME_NUMBER");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Topup");
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
			if(!util.isNullOrEmpty(lastEnteredMobNo)){
				totalPrompt = Constants.GL_FIVE;
			}
			else{
				totalPrompt = Constants.GL_ONE;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			//To have the property file grammar, need to call that util method here
			//No Need to have grammar here

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey,
					dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe Seperator sign
			//No Need here
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentTopUpImpl.getTopUpMobileNumberPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentTopUpImpl.getTopUpMobileNumberPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String isAValidMobileNumber(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentTopUpImpl.isAValidMobileNumber()");}
		String code = Constants.ONE;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Global Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String MSISDN = util.isNullOrEmpty(callInfo.getField(Field.DESTNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.DESTNO);
			if(util.isNullOrEmpty(MSISDN)){
				throw new ServiceException("enteredMobileNumber Object is null / EMpty");
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "enteredMobileNumber" + MSISDN);}

			String serviceProviderCode = util.isNullOrEmpty(callInfo.getField(Field.SELECTEDSERVICEPROVIDER))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected service provider code is " + serviceProviderCode);}

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			//TODO - CR
			//Following variable handled for the new CR of omantel
			boolean isForOmantelServ = false;
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMSUBSCRIBERINFO_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMSUBSCRIBERINFO_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			String providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMSUBSCRIBERINFO_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMSUBSCRIBERINFO_PROVIDERTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Provider type configured is " + providerType);}
			
			String omatelProviderList = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_OMANTEL_PROVIDERS))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_OMANTEL_PROVIDERS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Omantel Provider type configured is " + omatelProviderList);}
			
			String nawrasProviderList = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_NAWRAS_PROVIDERS))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_NAWRAS_PROVIDERS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Nawras provider configured is " + nawrasProviderList);}
			
			if(util.isCodePresentInTheConfigurationList(serviceProviderCode, omatelProviderList)){
				//TODO - CR
				isForOmantelServ = true;
				providerType = Constants.TELECOMSERVICEINFO_OMANTEL;
			}else{
				providerType = Constants.TELECOMSERVICEINFO_NAWRAS;
			}
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_MOBILE_NO + Constants.EQUALTO + MSISDN
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO);
			//TODO - CR
			
			if(isForOmantelServ){
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMPREPAIDNUMBERVAL);
			}else{
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO);
			}
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
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
			
//			TelecomSubscriberInfo_HostRes telecomSubscriberInfo_HostRes = telecomSubscriberInfoDAO.getTelecomSubscriberInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, MSISDN);
//			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomSubscriberInfo_HostRes);}
//			callInfo.setTelecomSubscriberInfo_HostRes(telecomSubscriberInfo_HostRes);
//
//			code = telecomSubscriberInfo_HostRes.getErrorCode();
//
//			/*
//			 * For Reporting Start
//			 */
//			
//			String hostEndTime = telecomSubscriberInfo_HostRes.getHostEndTime();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
//			hostReportDetails.setHostEndTime(hostEndTime);
//
//			String hostResCode = telecomSubscriberInfo_HostRes.getHostResponseCode();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
//			hostReportDetails.setHostResponse(hostResCode);
//
//			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
//		
//			String responseDesc = Constants.HOST_FAILURE;
//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				responseDesc = Constants.HOST_SUCCESS;
//			}
//			
//			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
//					+ Constants.EQUALTO + hostResCode
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(telecomSubscriberInfo_HostRes.getErrorDesc()) ?"NA" :telecomSubscriberInfo_HostRes.getErrorDesc());
//			hostReportDetails.setHostOutParams(hostOutputParam);
//
//			callInfo.setHostReportDetails(hostReportDetails);
//			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
//
//			callInfo.updateHostDetails(ivrdata);
//			//End Reporting
//
//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
//				
//			}else{
//
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Telecomsubscriber host service");}
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + telecomSubscriberInfo_HostRes.getHostResponseCode());}
//
//				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO, telecomSubscriberInfo_HostRes.getHostResponseCode());
//
//				/**
//				 * Following will be called only if there occured account selection before this host access
//				 */
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
//				util.setEligibleAccountCounts(callInfo, hostResCode);
//			}
//		}catch(Exception e){
//			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  UtilityBillPaymentTopUpImpl.isAValidMobileNumber() "+ e.getMessage());
//			throw new ServiceException(e);
//		}
//		return code;
//
//	}
//			//TODO - CR Comment above from line 528 and following need to be uncommented for enabling the CR
//			
			if(isForOmantelServ){
				
				//Since it is a top up feature and the utility code value is optional as per host spec document.  So setting the empty value
				String utilityCode = Constants.EMPTY;

				requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

				providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_PROVIDERTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Provider type configured is " + providerType);}
				
				
				TelecomPrepaidNumberVal_HostRes telecomPrepaidNumberVal_HostRes = telecomPrepaidNumberValDAO.getTelecomPrepaidNumberVal_HostRes(callInfo, requestType, providerType, utilityCode, serviceProviderCode, MSISDN);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomPrepaidNumberVal_HostRes);}
				callInfo.setTelecomPrepaidNumberVal_HostRes(telecomPrepaidNumberVal_HostRes);

				code = telecomPrepaidNumberVal_HostRes.getErrorCode();

				/*
				 * For Reporting Start
				 */
				
				String hostEndTime = telecomPrepaidNumberVal_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String hostResCode = telecomPrepaidNumberVal_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);

				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
			
				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				/****Duplicate RRN Fix 25012016 *****/
				strHostInParam = Constants.HOST_INPUT_PARAM_MOBILE_NO + Constants.EQUALTO + MSISDN
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);
				/************************************/
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(telecomPrepaidNumberVal_HostRes.getErrorDesc()) ?"NA" :telecomPrepaidNumberVal_HostRes.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				//End Reporting

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomPrepaidNumberVal_HostRes");}
					
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for telecomPrepaidNumberVal_HostRes host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + telecomPrepaidNumberVal_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMPREPAIDNUMBERVAL, telecomPrepaidNumberVal_HostRes.getHostResponseCode());

					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);
				}
				
				
				
			}else{

				TelecomSubscriberInfo_HostRes telecomSubscriberInfo_HostRes = telecomSubscriberInfoDAO.getTelecomSubscriberInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, MSISDN);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomSubscriberInfo_HostRes);}
				callInfo.setTelecomSubscriberInfo_HostRes(telecomSubscriberInfo_HostRes);

			code = telecomSubscriberInfo_HostRes.getErrorCode();
			/**
			 * Added on 18-Apr-2017 for Ooredoo change
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes response code:" + code);}
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
				code = Constants.WS_FAILURE_CODE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to verify the prepaid number");}
				ArrayList<TelecomSubcriberInfoBasicDetails> telecomSubscriberInfoBasicDetailsList = telecomSubscriberInfo_HostRes.getTelecomSubcriberInfoBasicDetails();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails value:" + telecomSubscriberInfoBasicDetailsList);}
				if(telecomSubscriberInfoBasicDetailsList != null){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails list size:" + telecomSubscriberInfoBasicDetailsList.size());}
					for(TelecomSubcriberInfoBasicDetails telecomSubcriberInfoBasicDetails : telecomSubscriberInfoBasicDetailsList){
						if(telecomSubcriberInfoBasicDetails != null && telecomSubcriberInfoBasicDetails.getMSISDNStatus().equalsIgnoreCase(Constants.Ooredoo_MSISDNStatus_Active)
								&& (telecomSubcriberInfoBasicDetails.getAccctType().equalsIgnoreCase(Constants.Ooredoo_AccType_GSM_PREPAID) || telecomSubcriberInfoBasicDetails.getAccctType().equalsIgnoreCase(Constants.Ooredoo_AccType_GSM_FIXED_PREPAID))){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails Ooredo_MSISDNStatus_Active:" + telecomSubcriberInfoBasicDetails.getMSISDNStatus());}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails Ooredo_AccountType:" + telecomSubcriberInfoBasicDetails.getAccctType());}
							code = Constants.WS_SUCCESS_CODE;
							break;
						}
					}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomSubscriberInfo_HostRes);}
			}
			/***Ooredo change END****/
			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = telecomSubscriberInfo_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = telecomSubscriberInfo_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
		
			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_MOBILE_NO + Constants.EQUALTO + MSISDN
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(telecomSubscriberInfo_HostRes.getErrorDesc()) ?"NA" :telecomSubscriberInfo_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
				
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Telecomsubscriber host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + telecomSubscriberInfo_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO, telecomSubscriberInfo_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
				}
			}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  UtilityBillPaymentTopUpImpl.isAValidMobileNumber() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}
			//TODO - CR Comment above from line 528 and following need to be uncommented for enabling the CR
			
//			if(isForOmantelServ){
//				
//				//Since it is a top up feature and the utility code value is optional as per host spec document.  So setting the empty value
//				String utilityCode = Constants.EMPTY;
//
//				requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_REQUESTTYPE);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}
//
//				providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPREPAIDNUMBERVALIDATION_PROVIDERTYPE);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Provider type configured is " + providerType);}
//				
//				
//				TelecomPrepaidNumberVal_HostRes telecomPrepaidNumberVal_HostRes = telecomPrepaidNumberValDAO.getTelecomPrepaidNumberVal_HostRes(callInfo, requestType, providerType, utilityCode, serviceProviderCode, MSISDN);
//				
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomPrepaidNumberVal_HostRes);}
//				callInfo.setTelecomPrepaidNumberVal_HostRes(telecomPrepaidNumberVal_HostRes);
//
//				code = telecomPrepaidNumberVal_HostRes.getErrorCode();
//
//				/*
//				 * For Reporting Start
//				 */
//				
//				String hostEndTime = telecomPrepaidNumberVal_HostRes.getHostEndTime();
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
//				hostReportDetails.setHostEndTime(hostEndTime);
//
//				String hostResCode = telecomPrepaidNumberVal_HostRes.getHostResponseCode();
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
//				hostReportDetails.setHostResponse(hostResCode);
//
//				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
//			
//				String responseDesc = Constants.HOST_FAILURE;
//				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//					responseDesc = Constants.HOST_SUCCESS;
//				}
//				
//				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
//						+ Constants.EQUALTO + hostResCode
//				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(telecomPrepaidNumberVal_HostRes.getErrorDesc()) ?"NA" :telecomPrepaidNumberVal_HostRes.getErrorDesc());
//				hostReportDetails.setHostOutParams(hostOutputParam);
//
//				callInfo.setHostReportDetails(hostReportDetails);
//				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
//
//				callInfo.updateHostDetails(ivrdata);
//				//End Reporting
//
//				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomPrepaidNumberVal_HostRes");}
//					
//				}else{
//
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for telecomPrepaidNumberVal_HostRes host service");}
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + telecomPrepaidNumberVal_HostRes.getHostResponseCode());}
//
//					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMPREPAIDNUMBERVAL, telecomPrepaidNumberVal_HostRes.getHostResponseCode());
//
//					/**
//					 * Following will be called only if there occured account selection before this host access
//					 */
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
//					util.setEligibleAccountCounts(callInfo, hostResCode);
//				}
//				
//				
//				
//			}else{
//
//				TelecomSubscriberInfo_HostRes telecomSubscriberInfo_HostRes = telecomSubscriberInfoDAO.getTelecomSubscriberInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, MSISDN);
//				
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomSubscriberInfo_HostRes);}
//				callInfo.setTelecomSubscriberInfo_HostRes(telecomSubscriberInfo_HostRes);
//
//				code = telecomSubscriberInfo_HostRes.getErrorCode();
//
//				/*
//				 * For Reporting Start
//				 */
//				
//				String hostEndTime = telecomSubscriberInfo_HostRes.getHostEndTime();
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
//				hostReportDetails.setHostEndTime(hostEndTime);
//
//				String hostResCode = telecomSubscriberInfo_HostRes.getHostResponseCode();
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
//				hostReportDetails.setHostResponse(hostResCode);
//
//				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}
//			
//				String responseDesc = Constants.HOST_FAILURE;
//				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//					responseDesc = Constants.HOST_SUCCESS;
//				}
//				
//				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
//						+ Constants.EQUALTO + hostResCode
//				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(telecomSubscriberInfo_HostRes.getErrorDesc()) ?"NA" :telecomSubscriberInfo_HostRes.getErrorDesc());
//				hostReportDetails.setHostOutParams(hostOutputParam);
//
//				callInfo.setHostReportDetails(hostReportDetails);
//				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
//
//				callInfo.updateHostDetails(ivrdata);
//				//End Reporting
//
//				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
//					
//				}else{
//
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Telecomsubscriber host service");}
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + telecomSubscriberInfo_HostRes.getHostResponseCode());}
//
//					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO, telecomSubscriberInfo_HostRes.getHostResponseCode());
//
//					/**
//					 * Following will be called only if there occured account selection before this host access
//					 */
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
//					util.setEligibleAccountCounts(callInfo, hostResCode);
//				}
//			}
//		}catch(Exception e){
//			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  UtilityBillPaymentTopUpImpl.isAValidMobileNumber() "+ e.getMessage());
//			throw new ServiceException(e);
//		}
//		return code;
//
//	}

}

