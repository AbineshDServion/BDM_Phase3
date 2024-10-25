package com.servion.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICECallData;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;

public class GlobalImpl implements IGlobal {

	private Logger logger = (Logger)LoggerObject.getLogger();

	@Override
	public String getANIRemovePrefix(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String finalANI = Constants.EMPTY_STRING;
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getANIRemovePrefix()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Object value from ICEGlobalConfig");}
			}

			ArrayList<String> aniPrefix = (ArrayList<String>)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_ANIRemovePrefix);

			String strANI = (String)callInfo.getField(Field.ANI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The ANI value from call info is " +strANI);}
			finalANI = strANI;


			if(util.isNullOrEmpty(finalANI)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Incoming ANI is Null / EMpty" +strANI);}
				return finalANI;
			}
			if(finalANI.length()>8){
			String strPrefixIndexVal = Constants.EMPTY_STRING;
			String strANIPrefix = Constants.EMPTY_STRING;

			for(int index=0; index<aniPrefix.size(); index++){
				strPrefixIndexVal = aniPrefix.get(index);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The" + index+" index value of ani prefix is " +strPrefixIndexVal);}

				strANIPrefix = strANI.substring(Constants.GL_ZERO, strPrefixIndexVal.length());
				if(strANIPrefix.equalsIgnoreCase(strPrefixIndexVal)){
					finalANI = strANI.substring(strPrefixIndexVal.length());
					break;
				}
			}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PREFIX removed ANI value is  " +finalANI);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,"EXIT: GlobalImpl.getANIRemovePrefix()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.getANIRemovePrefix() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

		return finalANI;
	}

	@Override
	public int getMaxTries(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		int finalMaxTries = Constants.GL_THREE;
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getMaxTries()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Object value from ICEGlobalConfig");}
				String str_MaxTries = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MaxTries);
				if(!util.isNullOrEmpty(str_MaxTries)){
					finalMaxTries = Integer.parseInt(str_MaxTries);
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Max tries value is "+finalMaxTries);}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,"EXIT: GlobalImpl.getMaxTries()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.getMaxTries() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalMaxTries;
	}

	@Override
	public boolean isANIAMobNo(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		boolean isANIAMobNo = false;
		String final_ANI = Constants.EMPTY_STRING;
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isANIAMobNo()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Object value from ICEGlobalConfig");}
			}

			ArrayList<String> mobilePrefix = (ArrayList<String>)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MobileStartDigit);

			String strANI = (String)callInfo.getField(Field.ANI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The ANI value from call info is " +strANI);}

			final_ANI = getANIRemovePrefix(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After removing the ANI's prefixes the final ANI value is" +final_ANI);}

			String strPrefixIndexVal = Constants.EMPTY_STRING;
			String strMobPrefix = Constants.EMPTY_STRING;

			String mobNoLength = (String)callInfo.getField(Field.MOBILENOLENGTH);
			int int_MobNoLength = util.isNullOrEmpty(mobNoLength) ? Constants.GL_NINE : Integer.parseInt(mobNoLength);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile number length configured is " +int_MobNoLength);}


			if(int_MobNoLength == strANI.length()){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Mobile number length is " +mobNoLength);}
				for(int index=0; index<mobilePrefix.size(); index++){
					strPrefixIndexVal = mobilePrefix.get(index);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The" + index+" index value of Mobile prefix is " +strPrefixIndexVal);}

					strMobPrefix = final_ANI.substring(Constants.GL_ZERO, strPrefixIndexVal.length());
					if(strMobPrefix.equalsIgnoreCase(strPrefixIndexVal)){
						isANIAMobNo = true;
						break;
					}
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the called number is a mobile number " +isANIAMobNo);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,"EXIT: GlobalImpl.isANIAMobNo()");}




		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.isANIAMobNo() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		
		String comingFrom = (String) callInfo.getField(Field.COMING_FROM);
		if(!util.isNullOrEmpty(comingFrom) &&
				comingFrom.equalsIgnoreCase(Constants.PROCESSINFOSMS)){
			return isANIAMobNo;
		}else{
			//TODO Hard COded value
			return true;
		}

	}
	
	@Override
	public boolean isAValidMobNo(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		boolean isANIAMobNo = false;
		//String final_ANI = Constants.EMPTY_STRING;
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isAValidMobNo()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Object value from ICEGlobalConfig");}
			}

			ArrayList<String> mobilePrefix = (ArrayList<String>)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MobileStartDigit);

			String mobileNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The mobileNumber value from call info is " +mobileNumber);}

			//final_ANI = getANIRemovePrefix(callInfo);
			//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After removing the ANI's prefixes the final ANI value is" +final_ANI);}

			String strPrefixIndexVal = Constants.EMPTY_STRING;
			String strMobPrefix = Constants.EMPTY_STRING;

			String mobNoLength = (String)callInfo.getField(Field.MOBILENOLENGTH);
			int int_MobNoLength = util.isNullOrEmpty(mobNoLength) ? Constants.GL_NINE : Integer.parseInt(mobNoLength);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile number length configured is " +int_MobNoLength);}


			if(int_MobNoLength == mobileNumber.length()){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Mobile number length is " +mobNoLength);}
				for(int index=0; index<mobilePrefix.size(); index++){
					strPrefixIndexVal = mobilePrefix.get(index);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The" + index+" index value of Mobile prefix is " +strPrefixIndexVal);}

					strMobPrefix = mobileNumber.substring(Constants.GL_ZERO, strPrefixIndexVal.length());
					if(strMobPrefix.equalsIgnoreCase(strPrefixIndexVal)){
						isANIAMobNo = true;
						break;
					}
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the called number is a mobile number " +isANIAMobNo);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,"EXIT: GlobalImpl.isAValidMobNo()");}




		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.isAValidMobNo() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

		//		return isANIAMobNo;

		//TODO Hard COded value
		return true;
	}

	
	@Override
	public void setAdditionalGlobalParam(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***The OD logger object value is ***" + LoggerObject.getLogger());}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***The OD logger object value is  is debut enabled***" + LoggerObject.getLogger().isDebugEnabled());}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***The CallInfo logger object value is ***" + callInfo.getField(Field.LOGGER));}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***The CallInfo logger object value is ***" + (Logger)callInfo.getField(Field.LOGGER));}

		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.setAdditionalGlobalParam()");}

			//TODO
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***The OD logger object value is ***" + LoggerObject.getLogger());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			ICECallData ivr_ICECallData = (ICECallData)callInfo.getICECallData();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Object value for ICEGlobalConfig");}
			}


			if(util.isNullOrEmpty(ivr_ICECallData)){
				throw new ServiceException("ivr_ICECallData object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Object value for ivr_ICECallData");}
			}

			String ani = ivr_ICECallData.getAni();
			String dnis = ivr_ICECallData.getDnis();
			String ucid = ivr_ICECallData.getUcid();
			//Date startTime = ivr_ICECallData.getStartTime();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ANI is " + ani);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DNIS is " + dnis);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ucid is " + ucid);}
			//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Call Start Time is " + startTime);}

			callInfo.setField(Field.ANI, ani);
			callInfo.setField(Field.DNIS, dnis);
			callInfo.setField(Field.UCID, ucid);
			//callInfo.setField(Field.CALLSTARTTIME, startTime);

			String custSegmentDnisKey = Constants.DNIS_ + dnis;
			String customerSegment = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(custSegmentDnisKey);

			String globalPropFile = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_GLOBAL+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_English = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_English+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_Arabic = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Arabic+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_Hindi = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Hindi+ Constants.UNDERSCORE + customerSegment);

			String lang = Constants.EMPTY_STRING + callInfo.getField(Field.LANGUAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language is " + lang);}

			if(!util.isNullOrEmpty(lang)){

				if(Constants.Hindi.equalsIgnoreCase(lang)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Hindi" + lang);}
					dmPropLocation =  dmPropLocation_Hindi;
				}else if(Constants.Arabic.equalsIgnoreCase(lang)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Arabic" + lang);}
					dmPropLocation =  dmPropLocation_Arabic;
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as English" + lang);}
					dmPropLocation =  dmPropLocation_English;
				}
			}


			String fieldsNotToBeLogged = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FIELDS_NEEDTOBE_SKIPPED_FROM_LOG))? "APIN"  : (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FIELDS_NEEDTOBE_SKIPPED_FROM_LOG);
			String moreCount = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MORECOUNT))? Constants.FIVE  : (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MORECOUNT);
			String BEFOREMAINMENUCHECKFEATURE = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_BEFOREMAINMENUCHECKFEATURE);
			
			
			/**
			 * Following changes are done for the secific vdn for islamic priority vdn
			 */
			
			String defaultVDN = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DefaultVDN);
			
			if(Constants.CUST_SEGMENT_ISLAMICPRIORITY.equalsIgnoreCase(customerSegment)){
				defaultVDN = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DefaultVDN_IslamicPriority);
			}
			
			//END Vinoth
			
			String cardOrAcctSuffLength = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_LastNDigits);
			String defaultErrorCodePhrase = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DFFAULTERRORCODEPHRASE);
			String apinExpiryErrorCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_APINExpiryErrorCode);
			String faxToRegNumber = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FaxToRegNumber);
			String bankAcctLength = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_BankAccountLength);
			String cardLength = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CardAccountLength);
			String faxLength = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FaxLength);
			String apinBlockCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_APINBlock_Code);
			String apinInvalidCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_APINInvalid);
			String apinInactiveCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_APINInactiveCode);
			String faxFileLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FAX_FILE_LOCATION);
			String emailFileLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_FILE_LOCATION);
			String mergingFileLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MERGING_FAX_FILE_LOCATION);
			String faxTemplatePath = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_FAX_TEMPLATE_PATH);
			String mobileNoLength = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_MOBILE_LENGTH);
			String noOfTransPerPage = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_NO_OF_TRANSACTION_PER_PAGE);
			String otp_Validty_Time = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_OTP_VALIDATY_TIME);
			/**
			 * As per the confirmation, we are going to keep the OTP key in the code, so here we are taking the otp key from the constant file
			 */
			String otpKey = Constants.OTP_KEY;
			String listBenefNoRecCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_LISTBENEFICIARY_NORECORDFOUND_CODE);

			String enableAccountErrorCodes = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_ENABLEACCOUNTERRORCODE);

			String emailHostName = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_HOSTNAME)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_HOSTNAME);

			String emailUsername = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_USERNAME)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_USERNAME);

			String emailPassword = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_PASSWORD)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_PASSWORD);

			String emailAuth = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_AUTH)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_AUTH);

			String emailFromEmailId =  util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_FROMEMAILID)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_FROMEMAILID);

			String emailSubjectLine =  util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_SUBJECTLINE)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_SUBJECTLINE);

			
			String formEmailSubjectLine =  util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL__FORM_SUBJECTLINE)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL__FORM_SUBJECTLINE);

			
			String emailBodyText =  util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_MAILBODYTEXT)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_MAILBODYTEXT);

			String emailPort =  util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_MAILPORT)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_MAILPORT);
			
			String emailGifLocaiton =  util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_GIF_LOCATION)) ? Constants.EMPTY_STRING : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EMAIL_GIF_LOCATION);

			String generateHostXml = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_GENERATEHOSTXML)) ? Constants.N : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_GENERATEHOSTXML);

			
			String otpLength = util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_OTP_LENGTH)) ? Constants.FOUR : (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_OTP_LENGTH);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Gloabl property file is" + globalPropFile);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DM Property file path Location is " + dmPropLocation);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DM Property English file path Location is " + dmPropLocation_English);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DM Property Arabic file path Location is " + dmPropLocation_Arabic);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DM Property Hindi file path Location is " + dmPropLocation_Hindi);}

			moreCount = util.isNullOrEmpty(moreCount)? Constants.FIVE : moreCount;
			int int_MoreCount = Integer.parseInt(moreCount);
			if(int_MoreCount > Constants.GL_FIVE){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Resetting the moreCount value as 5 , since it is greater than 5" + int_MoreCount);}
				moreCount = int_MoreCount + Constants.EMPTY_STRING;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Global More Count value is" + moreCount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "BEFOREMAINMENU CHECKFEATURE" + BEFOREMAINMENUCHECKFEATURE);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Default VDN before Language selection" + defaultVDN);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cards / Accts Suffix announcement length is " + cardOrAcctSuffLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Default Error Code Phrase is " + defaultErrorCodePhrase);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured APIN Expiry Error Code is " + apinExpiryErrorCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is to fax the pdf to register number ?" + faxToRegNumber);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bank Acct Length" + bankAcctLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Acct Length" + cardLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FAX Length" + faxLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured APIN Block Code " + apinBlockCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured APIN Invalid Code " + apinInvalidCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured APIN Inactive Code " + apinInactiveCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Customer segment type based on DNIS is  " + customerSegment);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured FAX file location is  " + faxFileLocation);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured FAX file Merging location is  " + mergingFileLocation);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email file location is  " + emailFileLocation);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured OTP Validity Time is  " + otp_Validty_Time);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Mobile Number length is  " + mobileNoLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Fax template path  is  " + faxTemplatePath);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured No of Transaction per page value is  " + noOfTransPerPage);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained OTP Key is  " );}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured List Beneficiary No Recode error code is  " + listBenefNoRecCode);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email Host Name is  " + emailHostName);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email User Name is  " + emailUsername);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email Password is obtained");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email  Auth  is  " + emailAuth);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email From Email Id is  " + emailFromEmailId);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email Subject Line is  " + emailSubjectLine);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Form Email Subject Line is  " + formEmailSubjectLine);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email Body Text is  " + emailBodyText);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email Port is  " + emailPort);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Email Gif Location is  " + emailGifLocaiton);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured EnableAccountErrorCodes is  " + enableAccountErrorCodes);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fields Not to be logged " + fieldsNotToBeLogged);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is to Generate XML files for host ? " + generateHostXml);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The OTP Length is " + otpLength);}
			
			callInfo.setField(Field.GLOBALPROPERTYFILE, globalPropFile);
			callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropLocation);
			callInfo.setField(Field.DMPROPERTIESLOCATIONENGLISH, dmPropLocation_English);
			callInfo.setField(Field.DMPROPERTIESLOCATIONARABIC, dmPropLocation_Arabic);
			callInfo.setField(Field.DMPROPERTIESLOCATIONHINDI, dmPropLocation_Hindi);

			callInfo.setField(Field.MORECOUNT, moreCount);

			callInfo.setField(Field.BEFOREMAINMENUCHECKFEATURE, BEFOREMAINMENUCHECKFEATURE);
			callInfo.setField(Field.DEFAULTVDN, defaultVDN);
			callInfo.setField(Field.LastNDigits, cardOrAcctSuffLength);

			//Need to set
			if(!util.isNullOrEmpty(defaultErrorCodePhrase)){
				callInfo.setField(Field.DEFAULTERRORCODEPHRASE,defaultErrorCodePhrase);
			}

			callInfo.setField(Field.FIELDSNOTTOLOG, fieldsNotToBeLogged);
			callInfo.setField(Field.APINExpiryErrorCode,apinExpiryErrorCode);
			callInfo.setField(Field.FAXTOREGNUMBER, faxToRegNumber);
			callInfo.setField(Field.BANKACCTLENGTH, bankAcctLength);
			callInfo.setField(Field.CARDACCTLENGTH, cardLength);
			callInfo.setField(Field.FAXLENGTH, faxLength);
			callInfo.setField(Field.APINBlockCode, apinBlockCode);
			callInfo.setField(Field.APINInvalidCode, apinInvalidCode);
			callInfo.setField(Field.APINInactiveCode, apinInactiveCode);
			callInfo.setField(Field.CUST_SEGMENT_TYPE, customerSegment);
			callInfo.setField(Field.DNIS_Type, customerSegment);
			callInfo.setField(Field.OTPValidityTime, otp_Validty_Time);
			callInfo.setField(Field.MOBILENOLENGTH, mobileNoLength);
			callInfo.setField(Field.MERGINGFAXLOCATION, mergingFileLocation);
			callInfo.setField(Field.FAXFILELOCATION, faxFileLocation);
			callInfo.setField(Field.EMAILFILELOCATION, emailFileLocation);
			callInfo.setField(Field.FAXTEMPLATEPATH, faxTemplatePath);
			callInfo.setField(Field.NOOFTRANSPERPAGE, noOfTransPerPage);
			callInfo.setField(Field.OTPKEY, otpKey);
			callInfo.setField(Field.ListBENF_NO_REC_FOUND_ERR_CODE, listBenefNoRecCode);
			callInfo.setField(Field.EMAILAUTH, emailAuth);
			callInfo.setField(Field.EMAILBODYTEXT, emailBodyText);
			callInfo.setField(Field.EMAILFILELOCATION, emailFileLocation);
			callInfo.setField(Field.EMAILFROMEMAILID, emailFromEmailId);
			callInfo.setField(Field.EMAILHOSTNAME, emailHostName);
			callInfo.setField(Field.EMAILPASSWORD, emailPassword);
			callInfo.setField(Field.EMAILPORT, emailPort);
			callInfo.setField(Field.EMAILSUBJECTLINE, emailSubjectLine);
			callInfo.setField(Field.EMAILFORMSUBJECTLINE, formEmailSubjectLine);
			callInfo.setField(Field.EMAILGIFLOCATION, emailGifLocaiton);
			
			
			callInfo.setField(Field.EMAILUSERNAME, emailUsername);
			callInfo.setField(Field.ENABLEDACCOUNTERRORCODES, enableAccountErrorCodes);
			callInfo.setField(Field.GENERATEHOSTXML, generateHostXml);
			callInfo.setField(Field.OTPLENGTH, otpLength);
			
			/**
			 * Rule engine update
			 */

			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("ICEGlobal config object is null or empty");
			}

			String acctOpeningLimit = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNT_COMPLETION_YEAR_LIMIT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Opening limit is "+ acctOpeningLimit);}

			String strNewAccountLimit = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_NEW_ACCOUNT_LIMIT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cutomer new account limit is "+ strNewAccountLimit);}
			//END Rule Engine Updation


			/**
			 * Rule engine update
			 */


			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}

			//			ArrayList<String>lastSelMobnNoList = new ArrayList<>();
			//			
			//			lastSelMobnNoList = iceGlobalConfig.getConfig().getParamValue(Constants.RULE_ENGINE_TOP_UP_MOBILE_NO)!= null ? (String)iceGlobalConfig.getConfig().getParamValue(Constants.RULE_ENGINE_TOP_UP_MOBILE_NO) : Constants.ZERO;

			if(!util.isNullOrEmpty(acctOpeningLimit)){

				Calendar cal1 = Calendar.getInstance();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's Date is " + cal1.getTime());}

				cal1.add(Calendar.YEAR, -Integer.parseInt(acctOpeningLimit));
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After neglecting the acct opening limit is " + cal1.getTime());}

				Date todayDate = Calendar.getInstance().getTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's Date is  " + todayDate);}
				Date manipulatedDate = cal1.getTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Manipulated Date is  " + manipulatedDate);}

				long compare= todayDate.getTime() - manipulatedDate.getTime();
				compare=compare/(24*60*60*1000);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Compare result of two days are " + compare);}

				//				int acctOpenDays= cal1.get(Calendar.YEAR);
				//				int currentDays = Calendar.getInstance().get(Calendar.YEAR);
				//				int resultDays = currentDays - acctOpenDays;
				//				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of Days" + compare);}
				ruleParamObj.setIVRParam(Constants.RULE_ACCOUNT_TENUREDATE_ANNC_RANGE, compare+Constants.EMPTY);

			}

			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_NEW_ACCOUNT_LIMIT, strNewAccountLimit);


			if(!util.isNullOrEmpty(customerSegment)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer Segment in the Rule Engine " + customerSegment);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERSEGMEMTATION, customerSegment);
			}


			if(!util.isNullOrEmpty(otp_Validty_Time)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting OTP Interval in the Rule Engine " + otp_Validty_Time);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_OTPINTERVAL, otp_Validty_Time);
			}


			String customerID = Constants.EMPTY + callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is " + customerID);}

			if(!util.isNullOrEmpty(customerID)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer id in the Rule Engine " + customerID);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERID, customerID);

			}
			//END Rule Engine Updation


			/**
			 * Rule engine update
			 */

			if(!util.isNullOrEmpty(ani)){

				String prefixRemovedAni = getANIRemovePrefix(callInfo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Prefix removed ANI / CLI is " + prefixRemovedAni);}
				//
				//				boolean isANIAMobileNo = isANIAMobNo(callInfo);
				//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is ANI a Mobile number " + isANIAMobileNo);}
				//
				//				if(isANIAMobileNo){
				//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer isCLIMobile flag Y value in the Rule Engine " + isANIAMobileNo);}
				//					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIMOBILENUMBER, Constants.Y);
				//				}else{
				//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer isCLIMobile flag N value in the Rule Engine " + isANIAMobileNo);}
				//					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIMOBILENUMBER, Constants.N);
				//				}

				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ANI, prefixRemovedAni);



			}



			try {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isLastCallAbandon()");}

				String code = Constants.ONE;

				String sessionId = util.isNullOrEmpty(callInfo.getField(Field.SESSIONID)) ? Constants.EMPTY_STRING :  (String)callInfo.getField(Field.SESSIONID);
				String cli = util.isNullOrEmpty(callInfo.getField(Field.CLI)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.CLI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI "+ cli);}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}

				HashMap<String, Object> configMap = new HashMap<String, Object>();
				configMap.put(DBConstants.CLI,cli);

				String uui = (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


				DataServices dataServices = VRUDBDataServicesInstance.getInstance();
				try {
					code = dataServices.isLastCallAbandon(logger, sessionId, uui, configMap);

					if(Constants.ZERO.equalsIgnoreCase(code)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer isCallAbandon flag Y value in the Rule Engine ");}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCALLABANDON, Constants.Y);
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer isCallAbandon flag N value in the Rule Engine ");}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCALLABANDON, Constants.N);
					}

				} catch (com.db.exception.ServiceException e) {
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: GlobalImpl.isCallOrAbandon()");}
					code = Constants.ONE;
					//e.printStackTrace();
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:  CallerTransferOnAbandonImpl.updateCallerPresentedRule()");}

			} catch (Exception e) {
				WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  TransferToAgentImpl.insertCallAbandon()" + e.getMessage());
				throw new ServiceException(e);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Updating the RuleEngine Object");}
			ruleParamObj.updateIVRFields();
			//END Rule Engine Updation


		}
		catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.setAdditionalGlobalParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
	}    


	@Override
	public void setDefaultVDN(CallInfo callInfo) throws ServiceException {
		//set field DEFAULTVDN based on the language choosen.
		//ice global config, DEFAULTVDN_ENGLISH,DEFAULT_ARABIC,DEFAULT_HINNDI
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String language = (String)callInfo.getField(Field.LANGUAGE);
		String defaultVDNKey = Constants.EMPTY_STRING;
		if(Constants.English.equalsIgnoreCase(language) || Constants.Eng.equalsIgnoreCase(language) || Constants.ALPHA_E.equalsIgnoreCase(language)){
			defaultVDNKey = Constants.CUI_DefaultVDN+Constants.ENGLISH;
		}else if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
			defaultVDNKey = Constants.CUI_DefaultVDN+Constants.ARABIC;
		}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
			defaultVDNKey = Constants.CUI_DefaultVDN+Constants.HINDI;
		}else{
			defaultVDNKey = Constants.CUI_DefaultVDN+Constants.URUDU;
		}

		String defaultLangVDN = Constants.EMPTY_STRING;
		ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getField(Field.ICEGlobalConfig);

		if(!util.isNullOrEmpty(iceGlobalConfig)){
			defaultLangVDN = (String)iceGlobalConfig.getConfig().getParamValue(defaultVDNKey);
		}

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Language specific Default VDN is" + defaultLangVDN);}

		callInfo.setField(Field.DEFAULTVDN, defaultLangVDN);
	}


	/**
	 * Setting the Last entered Mobile number entry map to the Data base
	 */
	@Override
	public String setLastEnteredMobileNoMap (CallInfo callInfo)throws ServiceException{
		String dbcode = Constants.ONE;
		try{
			String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.setLastEnteredMobileNoMap()");}

//			String isCLIARegMobileNo = util.isNullOrEmpty(callInfo.getField(Field.ISCLIAREGISTEREDMOBILENO)) ? Constants.N : (String)callInfo.getField(Field.ISCLIAREGISTEREDMOBILENO);
//			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Is CLI A Registered Mobile no ? "  + isCLIARegMobileNo );}

//			if(Constants.N.equalsIgnoreCase(isCLIARegMobileNo)){

//				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Going to start updating the cust insert data base table"  + isCLIARegMobileNo );}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: callerIdenticiationImpl.calling insertcustdetail()");}


				
				/**
				 * Modified by Vinoth on 23-June-2014 for Last mobile number update of Utility bill payment module for successfull topup
				 */
//				String regMobileNo = util.isNullOrEmpty(callInfo.getField(Field.REG_MOBILENO)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.REG_MOBILENO);
//				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Registered Mobile number is " + regMobileNo);}
				
				String customerID = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.CUSTOMERID);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Cutomer ID is " + customerID);}

				HashMap<String, String> lastSelectedMobileNoEntry = util.isNullOrEmpty(callInfo.getField(Field.LASTENTEREDMOBILENOMAP)) ? new HashMap<String, String>() : (HashMap<String, String>) callInfo.getField(Field.LASTENTEREDMOBILENOMAP);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The last selected Mobile number entry to be updated in the data base is " + lastSelectedMobileNoEntry);}

				String finalString = Constants.EMPTY_STRING;
				if(!util.isNullOrEmpty(lastSelectedMobileNoEntry)){

					Iterator iterator = lastSelectedMobileNoEntry.entrySet().iterator();
					Map.Entry mapEntry = null;
					while (iterator.hasNext()) {
						mapEntry = (Map.Entry) iterator.next();

						if(util.isNullOrEmpty(finalString)){
							finalString = mapEntry.getKey() + Constants.MINUS + mapEntry.getValue();
						}else{
							finalString = finalString + "|" + mapEntry.getKey() + Constants.MINUS + mapEntry.getValue();
						}
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "After inserting the key value pair of last entered mobile and service provider code is " + finalString);}
					}

				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
				HashMap<String, Object>configMap_Obj = new HashMap<String, Object>();

				configMap_Obj.put(DBConstants.CUSTOMERID, customerID);
				configMap_Obj.put(DBConstants.TOPUP_MOBILE_NUMBER, finalString);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is " + customerID );}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Top Up Mobile Number entry is " + finalString);}

				String uui = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.EMPTY : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}

				String sessionId_Obj = (String)callInfo.getField(Field.SESSIONID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session id is " + sessionId_Obj);}

				DataServices dataServices_Obj = VRUDBDataServicesInstance.getInstance();
				if(util.isNullOrEmpty(dataServices_Obj)){
					throw new ServiceException("Data Service object is null or empty");
				}
				try {
					dbcode = dataServices_Obj.insertCustDetails(logger, sessionId_Obj, uui, configMap_Obj);
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Inserterd the Cust Details successfully");}

				} catch (com.db.exception.ServiceException e) {
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  GlobalImpl.setLastEnteredMobileNoMap()");}
					dbcode = Constants.ONE;
					//e.printStackTrace();
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + dbcode );}
//			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: GlobalImpl.setLastEnteredMobileNoMap()");}
		}catch(ServiceException e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.setAdditionalGlobalParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

		return dbcode;
	}


	/**
	 * Getting Transfer VDN
	 */
	@Override
	public String getTransferVDN(CallInfo callInfo)throws ServiceException{
		String transferVDN = null;
		try{

			String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getTransferVDN()");}

			//Need to get that feature specific transfer VDN from feature config parameter.
			ICEFeatureData ivr_ICEFeatureConfig = (ICEFeatureData) callInfo.getICEFeatureData();
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getTransferVDN()"+ ivr_ICEFeatureConfig);}
			
			

			
			/**
			 * Following are the fix done on 19-01-2014 for the transfer VDN issue, if we called from landline instead of mobile number
			 */
//			String preferredLanguage = util.isNullOrEmpty(callInfo.getField(Field.PREFERREDLANG))?Constants.English : (String)callInfo.getField(Field.PREFERREDLANG);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Selected Preferred Language is " + preferredLanguage);}	

			String preferredLanguageFromRuleEng = util.isNullOrEmpty(callInfo.getField(Field.PREFERREDLANG))?null : (String)callInfo.getField(Field.PREFERREDLANG);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Selected Preferred Language is " + preferredLanguageFromRuleEng);}	

			String selectedLang = util.isNullOrEmpty(callInfo.getField(Field.LANGUAGE))?Constants.English : (String)callInfo.getField(Field.LANGUAGE);
			String preferredLanguage = Constants.English;
			if(util.isNullOrEmpty(preferredLanguageFromRuleEng)){
				preferredLanguage = selectedLang;
			}else{
				preferredLanguage = preferredLanguageFromRuleEng;
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final language for forming the agent vdn key is" + preferredLanguage);}
			//END Vinoth
			
			/**
			 * Following are the changes done for Agent transfer VDN based on the customer segment type
			 */
			 
			String custSegment  = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE))?Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Selected Customer segment type is" + custSegment);}
			
			boolean isIslamicBIN = Boolean.parseBoolean((String)callInfo.getField(Field.ISISLAMICBINTYPE));
			if(isIslamicBIN){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Customer Entered bin type is islamic bin. So customer segment assigned as :"+custSegment);}
				custSegment = Constants.CUST_SEGMENT_ISLAMIC;
			}
			
			/********************Issue No: 5426 Priority transfer fix ***************/
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Before Priortiy check Customer segment type is" + custSegment);}
			boolean isPriorityCaller  = util.isNullOrEmpty(callInfo.getField(Field.ISPRIORITYCUSTOMER))? false : (Boolean) callInfo.getField(Field.ISPRIORITYCUSTOMER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"isPriorityCaller: " + isPriorityCaller);}
			
			if(isPriorityCaller){
				if(custSegment.equalsIgnoreCase(Constants.CUST_SEGMENT_RETAIL)){
					custSegment = Constants.CUST_SEGMENT_PRIORITY;
				}else if(custSegment.equalsIgnoreCase(Constants.CUST_SEGMENT_ISLAMIC)){
					custSegment = Constants.CUST_SEGMENT_ISLAMICPRIORITY;
				}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"After Priortiy check Customer segment type is" + custSegment);}
			/************************************************************************/
			
			String key = Constants.EMPTY_STRING;
			if(Constants.English.equalsIgnoreCase(preferredLanguage) || Constants.Eng.equalsIgnoreCase(preferredLanguage) || Constants.ALPHA_E.equalsIgnoreCase(preferredLanguage)
					|| Constants.english.equalsIgnoreCase(preferredLanguage)){
				key = Constants.UNDERSCORE+custSegment+Constants.UNDERSCORE+Constants.English;
			}else if(Constants.Arabic.equalsIgnoreCase(preferredLanguage) || Constants.Arb.equalsIgnoreCase(preferredLanguage) || Constants.ALPHA_A.equalsIgnoreCase(preferredLanguage)
					|| Constants.arabic.equalsIgnoreCase(preferredLanguage)){
				key = Constants.UNDERSCORE+custSegment+Constants.UNDERSCORE+Constants.Arabic;
			}else if(Constants.Hindi.equalsIgnoreCase(preferredLanguage) || Constants.Hin.equalsIgnoreCase(preferredLanguage) || Constants.ALPHA_H.equalsIgnoreCase(preferredLanguage)
					|| Constants.hindi.equalsIgnoreCase(preferredLanguage)){
				key = Constants.UNDERSCORE+custSegment+Constants.UNDERSCORE+Constants.Hindi;
			}else{
				key = Constants.UNDERSCORE+custSegment+Constants.UNDERSCORE+Constants.Urudu;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The second part of transfer vdn key is " + key);}	
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				//throw new ServiceException("ivr_ICEFeatureData object is null");
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"ICEFeatureData obj is null or empty:  " + ivr_ICEFeatureData);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"ICEFeatureData is null or empty, getting the default vdn from global");}
				transferVDN = (String)callInfo.getField(Field.DEFAULTVDN);
			}else {
				transferVDN = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransferVDN + key);
				if(util.isNullOrEmpty(transferVDN)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Transfer vdn at FEATURE Config is null or empty, getting the default vdn from global");}
					transferVDN = (String)callInfo.getField(Field.DEFAULTVDN);

				}
			}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Final Transfer VDN is "+ transferVDN);}

			if(util.isNullOrEmpty(transferVDN)){
				throw new ServiceException("transfer VDN is null or empty");
			}
		}catch(ServiceException e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.setAdditionalGlobalParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

		return transferVDN;
	}

	@Override
	public boolean isEmailIDAvailable(CallInfo callInfo)throws ServiceException{

		try{
			String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isEmailIDAvailable()");}
			CallerIdentification_HostRes CallerIdentification_HostRes = callInfo.getCallerIdentification_HostRes();

			if(util.isNullOrEmpty(CallerIdentification_HostRes)){
				throw new ServiceException("Caller Identificatin host request is null / empty");
			}
			String emaiID = Constants.EMPTY_STRING;
			if(!util.isNullOrEmpty(CallerIdentification_HostRes.getCustomerShortDetails())){
				emaiID = CallerIdentification_HostRes.getCustomerShortDetails().getEMAIL();

				if(!util.isNullOrEmpty(emaiID)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Email id is "  +emaiID);}
					return true;
				}

			}else{
				throw new ServiceException("Caller Identificatin host request is null / empty");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Email id is not available");}
		}catch(ServiceException e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.setAdditionalGlobalParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return false;
	}

	@Override
	public boolean isRegMobileNoAvailable(CallInfo callInfo)throws ServiceException{
		String regmobileNo = Constants.EMPTY_STRING;
		String binType = Constants.EMPTY_STRING;
		try{
			String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isRegMobileNoAvailable()");}
			
			String featureId = (String)callInfo.getField(Field.FEATUREID);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isRegMobileNoAvailable() featureId:"+featureId);}
			
			binType = (String)callInfo.getField(Field.ENTEREDCINTYPE);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Bin Type:"+binType);}
						
			if((Constants.FEATURENAME_CARDPINSET.equalsIgnoreCase(featureId) || Constants.FEATURENAME_CARDPINRESET.equalsIgnoreCase(featureId))
					&& (binType.equalsIgnoreCase(Constants.CREDIT)||binType.equalsIgnoreCase(Constants.CIN_TYPE_PREPAID))){
				regmobileNo = (String) callInfo.getField(Field.REG_MOBILENO);
				if(!util.isNullOrEmpty(regmobileNo)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"regmobileNo is "  +regmobileNo);}
					return true;
				}
			}
			
			
			CallerIdentification_HostRes CallerIdentification_HostRes = callInfo.getCallerIdentification_HostRes();

			if(util.isNullOrEmpty(CallerIdentification_HostRes)){
				
				throw new ServiceException("Caller Identificatin host request is null / empty");
			}else{
			
			if(!util.isNullOrEmpty(CallerIdentification_HostRes.getCustomerShortDetails())){
				regmobileNo = CallerIdentification_HostRes.getCustomerShortDetails().getGSM();

				if(!util.isNullOrEmpty(regmobileNo)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"regmobileNo is "  +regmobileNo);}
					return true;
				}
			}else{
				throw new ServiceException("Caller Identificatin host request is null / empty");
			}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Registered Mobile Number is not available");}
		}catch(ServiceException e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.setAdditionalGlobalParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return false;
	}

	public boolean isRegFaxNumberAvailable(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.isRegFaxNumberAvailable()");}
			CallerIdentification_HostRes CallerIdentification_HostRes = callInfo.getCallerIdentification_HostRes();

			if(util.isNullOrEmpty(CallerIdentification_HostRes)){
				throw new ServiceException("Caller Identificatin host request is null / empty");
			}
			String faxNumber = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(CallerIdentification_HostRes.getCustomerShortDetails())){
				faxNumber = CallerIdentification_HostRes.getCustomerShortDetails().getFAX();

				if(!util.isNullOrEmpty(faxNumber)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Fax Number is "  +faxNumber);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting the fax number to last selected fax number"  +faxNumber);}

					callInfo.setField(Field.LASTSELECTEDFAXNO, faxNumber);

					return true;
				}
			}else{
				throw new ServiceException("Caller Identificatin host request is null / empty");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Email id is not available");}
		}catch(ServiceException e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.setAdditionalGlobalParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return false;
	}



	public String getSequenceNumber(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getSequenceNumber()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the input for getSequenceNo");}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			String returnValue = Constants.EMPTY_STRING;
			try {
				code = dataServices.getSequenceNo(logger, sessionId, uui, configMap);

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					returnValue = (String) configMap.get(DBConstants.SEQUENCENO);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The sequencial number return value " + returnValue);}

				}else{
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Error in the sequencial DB response");}
					throw new ServiceException("Sequencial number DB access throwing error");
				}
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: GlobalImpl.getSequenceNumber()");}
				throw new ServiceException("Sequencial number DB access throwing error");
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + returnValue );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: GlobalImpl.getSequenceNumber()");}

			return returnValue;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  LanguageSelectionImpl.updatePreferredLanguage()" + e.getMessage());
			throw new ServiceException(e);
		}
	}


	@Override
	public void getRetailConfiguration(CallInfo callInfo)throws ServiceException {
		// TODO Auto-generated method stub
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getRetailConfiguration()");}
		try{

			/**
			 * For setting the Retail flow DM Property location path and values
			 */

			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getField(Field.ICEGlobalConfig);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ivr_ICEGlobalConfig Object is " + ivr_ICEGlobalConfig);}

			String customerSegment = Constants.CUST_SEGMENT_RETAIL;

			String globalPropFile = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_GLOBAL+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_English = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_English+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_Arabic = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Arabic+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_Hindi = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Hindi+ Constants.UNDERSCORE + customerSegment);

			String lang = Constants.EMPTY_STRING + callInfo.getField(Field.LANGUAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language is " + lang);}

			if(!util.isNullOrEmpty(lang)){
				if(Constants.Hindi.equalsIgnoreCase(lang)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Hindi" + lang);}
					dmPropLocation =  dmPropLocation_Hindi;
				}else if(Constants.Arabic.equalsIgnoreCase(lang)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Arabic" + lang);}
					dmPropLocation =  dmPropLocation_Arabic;
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as English" + lang);}
					dmPropLocation =  dmPropLocation_English;
				}
			}

			callInfo.setField(Field.GLOBALPROPERTYFILE, globalPropFile);
			callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropLocation);
			callInfo.setField(Field.DMPROPERTIESLOCATIONENGLISH, dmPropLocation_English);
			callInfo.setField(Field.DMPROPERTIESLOCATIONARABIC, dmPropLocation_Arabic);
			callInfo.setField(Field.DMPROPERTIESLOCATIONHINDI, dmPropLocation_Hindi);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from the Retail flow");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Processing the Account details of  flow");}
			ArrayList<String>creditCardList = new ArrayList<String>();
			ArrayList<String>savingAcctList = new ArrayList<String>();
			ArrayList<String>loanAcctList = new ArrayList<String>();
			ArrayList<String>currentAcctList = new ArrayList<String>();
			ArrayList<String>fdAcctList = new ArrayList<String>();
			ArrayList<String>crVISACardList = new ArrayList<String>();
			ArrayList<String>crMasterCardList = new ArrayList<String>();
			ArrayList<String>crAmexCardList = new ArrayList<String>();
			ArrayList<String>retailFDAcctList = new ArrayList<String>();
			ArrayList<String>retailCurrentAcctList = new ArrayList<String>();
			ArrayList<String>retailLoanAcctList = new ArrayList<String>();
			ArrayList<String>retailSavingsAcctList = new ArrayList<String>();
			ArrayList<String>retailCreditCardList = new ArrayList<String>();
			ArrayList<String>retailCRVISACardList = new ArrayList<String>();
			ArrayList<String>retailCRMasterCardList = new ArrayList<String>();
			ArrayList<String>retailCRAmexCardList = new ArrayList<String>();
			
			
			ArrayList<String>retailDRVISACardList = new ArrayList<String>();
			ArrayList<String>retailDRMasterCardList = new ArrayList<String>();
			ArrayList<String>retailDRAmexCardList = new ArrayList<String>();
			ArrayList<String>retailInactiveDRVISACardList = new ArrayList<String>();
			ArrayList<String>retailInactiveDRMasterCardList = new ArrayList<String>();
			ArrayList<String>retailInactiveDRAmexCardList = new ArrayList<String>();
			
			ArrayList<String>drVISACardList = new ArrayList<String>();
			ArrayList<String>drMasterCardList = new ArrayList<String>();
			ArrayList<String>drAmexCardList = new ArrayList<String>();
			ArrayList<String>inactiveDRVISACardList = new ArrayList<String>();
			ArrayList<String>inactiveDRMasterCardList = new ArrayList<String>();
			ArrayList<String>inactiveDRAmexCardList = new ArrayList<String>();
			
			

			creditCardList = !util.isNullOrEmpty(callInfo.getField(Field.CREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.CREDITCARDLIST) : new ArrayList<String>();
			savingAcctList = !util.isNullOrEmpty(callInfo.getField(Field.SAVINGSACCTLIST))? (ArrayList<String>)callInfo.getField(Field.SAVINGSACCTLIST) : new ArrayList<String>();
			loanAcctList = !util.isNullOrEmpty(callInfo.getField(Field.LOANACCTLIST))? (ArrayList<String>)callInfo.getField(Field.LOANACCTLIST) : new ArrayList<String>();
			currentAcctList = !util.isNullOrEmpty(callInfo.getField(Field.CURRENTACCTLIST))? (ArrayList<String>)callInfo.getField(Field.CURRENTACCTLIST) : new ArrayList<String>();
			fdAcctList = !util.isNullOrEmpty(callInfo.getField(Field.FDACCTLIST))? (ArrayList<String>)callInfo.getField(Field.FDACCTLIST) : new ArrayList<String>();
			crVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.CRVISACARDLIST))? (ArrayList<String>)callInfo.getField(Field.CRVISACARDLIST) : new ArrayList<String>();
			crMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.CRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.CRMASTERCARDLIST) : new ArrayList<String>();
			crAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.CRAMEXCARDLIST))? (ArrayList<String>)callInfo.getField(Field.CRAMEXCARDLIST) : new ArrayList<String>();
			retailFDAcctList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILFDACCTLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILFDACCTLIST) : new ArrayList<String>();
			retailCurrentAcctList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILCURRENTACCTLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILCURRENTACCTLIST) : new ArrayList<String>();
			retailLoanAcctList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILLOANACCTLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILLOANACCTLIST) : new ArrayList<String>();
			retailSavingsAcctList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILSAVINGSACCTLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILSAVINGSACCTLIST) : new ArrayList<String>();
			retailCreditCardList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILCREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILCREDITCARDLIST) : new ArrayList<String>();
			retailCRVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILVISACREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILVISACREDITCARDLIST) : new ArrayList<String>();
			retailCRMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILMASTERCREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILMASTERCREDITCARDLIST) : new ArrayList<String>();
			retailCRAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.RETAILAMEXCREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILAMEXCREDITCARDLIST) : new ArrayList<String>();

			
			retailDRVISACardList =  !util.isNullOrEmpty(callInfo.getField(Field.RETAILDRVISACARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILDRVISACARDLIST) : new ArrayList<String>();
			retailDRMasterCardList =  !util.isNullOrEmpty(callInfo.getField(Field.RETAILDRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILDRMASTERCARDLIST) : new ArrayList<String>();
			retailDRAmexCardList =  !util.isNullOrEmpty(callInfo.getField(Field.RETAILDRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.RETAILDRMASTERCARDLIST) : new ArrayList<String>();
			retailInactiveDRVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.DRRETAILVISACARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRRETAILVISACARDLISTINACTIVE) : new ArrayList<String>();
			retailInactiveDRMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRRETAILMASTERCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRRETAILMASTERCARDLISTINACTIVE) : new ArrayList<String>();
			retailInactiveDRAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRRETAILAMEXCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRRETAILAMEXCARDLISTINACTIVE) : new ArrayList<String>();
			
			drVISACardList =  !util.isNullOrEmpty(callInfo.getField(Field.DRVISACARDLIST))? (ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST) : new ArrayList<String>();
			drMasterCardList =  !util.isNullOrEmpty(callInfo.getField(Field.DRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST) : new ArrayList<String>();
			drAmexCardList =  !util.isNullOrEmpty(callInfo.getField(Field.DRAMEXCARDLIST))? (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST) : new ArrayList<String>();
			inactiveDRVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.DRVISACARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE) : new ArrayList<String>();
			inactiveDRMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRMASTERCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLISTINACTIVE) : new ArrayList<String>();
			inactiveDRAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRAMEXCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLISTINACTIVE) : new ArrayList<String>();
			
			
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "********************Before Processing the RETAIL account list********************");}

//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current credit card list is " + creditCardList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Saving account list is " + savingAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current loan account list is " + loanAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Current account list is " + currentAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current FD account list is " + fdAcctList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit VISA card list is " + crVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Creidit MASTER card list is " + crMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit AMEX card list is " + crAmexCardList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail FD Acct list is " + retailFDAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Current Account list is " + retailCurrentAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Loan Account list is " + retailLoanAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Savings Account list is " + retailSavingsAcctList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Credit Card list is " + retailCreditCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Credit VISA list is " + retailCRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Credit Master list is " + retailCRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Credit Amex list is " + retailCRAmexCardList);}

			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Debit Visa list is " + retailDRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Debit Master list is " + retailDRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Debit Amex list is " + retailDRAmexCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Debit Inactive Visa list is " + retailInactiveDRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Debit Inactive Master list is " + retailInactiveDRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Retail Credit Inactive Amex is " + retailInactiveDRAmexCardList);}
			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Visa list is " + drVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Master list is " + drMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Amex list is " + drAmexCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive Visa list is " + inactiveDRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive Master list is " + inactiveDRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Credit Inactive Amex is " + inactiveDRAmexCardList);}

			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current credit card list count is " + creditCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Saving account list count is " + savingAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current loan account list count is " + loanAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Current account list count is " + currentAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current FD account list count is " + fdAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit VISA card list count is " + crVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Creidit MASTER card list count is " + crMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit AMEX card list count is " + crAmexCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic FD Acct list count is " + retailFDAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Current Account list count is " + retailCurrentAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Loan Account list count is " + retailLoanAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Savings Account list count is " + retailSavingsAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Card list count is " + retailCreditCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit VISA list count is " + retailCRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Master list count is " + retailCRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Amex list count is " + retailCRAmexCardList.size());}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  retail Debit Visa list count is " + retailDRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  retail Debit Master list count is " + retailDRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  retail Debit Amex list count is " + retailDRAmexCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  retail Debit Inactive VISA list count is " + retailInactiveDRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  retail Debit Inactive master list count is " + retailInactiveDRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  retail Debit Inactive Amex list count is " + retailInactiveDRAmexCardList.size());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Visa list count is " + drVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Master list count is " + drMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Amex list count is " + drAmexCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive VISA list count is " + inactiveDRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive master list count is " + inactiveDRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive Amex list count is " + inactiveDRAmexCardList.size());}
			

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "**********Resetting the RETAIL acccount / card list to the generic variables ");}


			savingAcctList = retailSavingsAcctList;
			loanAcctList = retailLoanAcctList;
			currentAcctList = retailCurrentAcctList;
			fdAcctList = retailFDAcctList;

			creditCardList = retailCreditCardList;
			crVISACardList = retailCRVISACardList;
			crMasterCardList = retailCRMasterCardList;
			crAmexCardList = retailCRAmexCardList;
			
			
			drVISACardList =retailDRVISACardList;
			drMasterCardList =retailDRMasterCardList;
			drAmexCardList =retailDRAmexCardList;
			inactiveDRVISACardList =retailInactiveDRVISACardList;
			inactiveDRMasterCardList =retailInactiveDRMasterCardList;
			inactiveDRAmexCardList =retailInactiveDRAmexCardList;
			

			callInfo.setField(Field.CREDITCARDLIST, creditCardList);
			callInfo.setField(Field.SAVINGSACCTLIST, savingAcctList);
			callInfo.setField(Field.LOANACCTLIST, loanAcctList);
			callInfo.setField(Field.CURRENTACCTLIST, currentAcctList);
			callInfo.setField(Field.FDACCTLIST, fdAcctList);
			callInfo.setField(Field.CRVISACARDLIST, crVISACardList);
			callInfo.setField(Field.CRMASTERCARDLIST, crMasterCardList);
			callInfo.setField(Field.CRAMEXCARDLIST, crAmexCardList);
			callInfo.setField(Field.RETAILFDACCTLIST, retailFDAcctList);
			callInfo.setField(Field.RETAILCURRENTACCTLIST, retailCurrentAcctList);
			callInfo.setField(Field.RETAILLOANACCTLIST, retailLoanAcctList);
			callInfo.setField(Field.RETAILSAVINGSACCTLIST, retailSavingsAcctList);
			callInfo.setField(Field.RETAILVISACREDITCARDLIST, retailCRVISACardList);
			callInfo.setField(Field.RETAILMASTERCREDITCARDLIST, retailCRMasterCardList);
			callInfo.setField(Field.RETAILAMEXCREDITCARDLIST, retailCRAmexCardList);
			callInfo.setField(Field.RETAILCREDITCARDLIST, retailCreditCardList);

			
			
			callInfo.setField(Field.DRRETAILVISACARDLISTINACTIVE, retailInactiveDRVISACardList);
			callInfo.setField(Field.DRRETAILMASTERCARDLISTINACTIVE, retailInactiveDRMasterCardList);
			callInfo.setField(Field.DRRETAILAMEXCARDLISTINACTIVE, retailInactiveDRAmexCardList);
			callInfo.setField(Field.DRVISACARDLISTINACTIVE, inactiveDRVISACardList);
			callInfo.setField(Field.DRMASTERCARDLISTINACTIVE, inactiveDRMasterCardList);
			callInfo.setField(Field.DRAMEXCARDLISTINACTIVE, inactiveDRAmexCardList);
			
			callInfo.setField(Field.ISLAMICDRVISACARDLIST, retailDRVISACardList);
			callInfo.setField(Field.ISLAMICDRMASTERCARDLIST, retailDRMasterCardList);
			callInfo.setField(Field.ISLAMICDRAMEXCARDLIST, retailDRAmexCardList);
			callInfo.setField(Field.DRVISACARDLIST, drVISACardList);
			callInfo.setField(Field.DRMASTERCARDLIST, drMasterCardList);
			callInfo.setField(Field.DRMASTERCARDLIST, drAmexCardList);
			
			
			ArrayList<String>debitCardList = new ArrayList<String>();
			debitCardList.addAll(drVISACardList);
			debitCardList.addAll(drMasterCardList);
			debitCardList.addAll(drAmexCardList);
			
			callInfo.setField(Field.DEBITCARDLIST, debitCardList);
			
			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}

			//Initialized the Rule Engine Object

			int creditCardCount = util.isNullOrEmpty(creditCardList)? Constants.GL_ZERO : creditCardList.size();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Credit cards : "+creditCardCount );}
			callInfo.setField(Field.NO_OF_CREDIT_CARDS, creditCardCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Credit card count in the Rule Engine " + creditCardCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CREDITCARDCOUNT, (creditCardCount+Constants.EMPTY));
			//END Rule Engine Updation


			int savingAccountCount = util.isNullOrEmpty(savingAcctList)? Constants.GL_ZERO : savingAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Saving accounts : "+savingAccountCount);}
			callInfo.setField(Field.NO_OF_SAVINGS_ACCTS, savingAccountCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Saving Account count in the Rule Engine " + savingAccountCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_SAVINGACCTCOUNT, (savingAccountCount+Constants.EMPTY));
			//END Rule Engine Updation


			int loanAcctCount = util.isNullOrEmpty(loanAcctList)? Constants.GL_ZERO : loanAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Loan accounts : "+loanAcctCount);}
			callInfo.setField(Field.NO_OF_LOAN_ACCTS, loanAcctCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Loan Account count in the Rule Engine " + loanAcctCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_LOANACCTCOUNT, (loanAcctCount+Constants.EMPTY));
			//END Rule Engine Updation


			int currentAcctCount = util.isNullOrEmpty(currentAcctList)?Constants.GL_ZERO : currentAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Current accounts : "+currentAcctCount);}
			callInfo.setField(Field.NO_OF_CURRENT_ACCTS, currentAcctCount);


			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Current Account count in the Rule Engine " + currentAcctCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CURRENTACCTCOUNT, (currentAcctCount+Constants.EMPTY));
			//END Rule Engine Updation

			int fdAcctCount = util.isNullOrEmpty(fdAcctList)? Constants.GL_ZERO : fdAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of FD accounts : "+fdAcctCount);}
			callInfo.setField(Field.NO_OF_FD_ACCTS, fdAcctCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Deposit Account count in the Rule Engine " + fdAcctCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEPOSITACCTCOUNT, (fdAcctCount+Constants.EMPTY));
			//END Rule Engine Updation


			ruleParamObj.updateIVRFields();
			//End Rule Engine Update

			/**
			 * Fixes done by Vinoth on 16-03-2014 to add the no of credit card VISA / Master / AMEX && same for Debit card details
			 */


			int crVisaCardCount = util.isNullOrEmpty(crVISACardList)? Constants.GL_ZERO : crVISACardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of CR VISA Cards are : "+crVisaCardCount);}
			callInfo.setField(Field.NO_OF_CR_VISA_CARDS, crVisaCardCount);


			int crMasterCardCount = util.isNullOrEmpty(crMasterCardList)? Constants.GL_ZERO : crMasterCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of CR Master Cards are : "+crMasterCardCount);}
			callInfo.setField(Field.NO_OF_CR_MASTER_CARDS, crMasterCardCount);


			int crAmexCardCount = util.isNullOrEmpty(crAmexCardList)?Constants.GL_ZERO : crAmexCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of CR Amex Cards are : "+crAmexCardCount);}
			callInfo.setField(Field.NO_OF_CR_AMEX_CARDS, crAmexCardCount);

			
			
			
			

			int drVISACardCount = util.isNullOrEmpty(drVISACardList)? Constants.GL_ZERO : drVISACardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of DR VISA Cards are : "+drVISACardCount);}
			callInfo.setField(Field.NO_OF_DR_VISA_CARDS, drVISACardCount);


			int drMasterCardCount = util.isNullOrEmpty(drMasterCardList)? Constants.GL_ZERO : drMasterCardList.size(); 

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of DR Master Cards are : "+drMasterCardCount);}
			callInfo.setField(Field.NO_OF_DR_MASTER_CARDS, drMasterCardCount);


			int drAmexCardCount = util.isNullOrEmpty(drAmexCardList)? Constants.GL_ZERO : drAmexCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of DR Amex Cards are : "+drAmexCardCount);}
			callInfo.setField(Field.NO_OF_DR_AMEX_CARDS, drAmexCardCount);


			int drVisaCardInactiveCount = util.isNullOrEmpty(inactiveDRVISACardList)? Constants.GL_ZERO : inactiveDRVISACardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Inactive DR VISA Cards are : "+drVisaCardInactiveCount);}
			callInfo.setField(Field.NO_OF_DR_VISA_CARDS_INACTVE, drVisaCardInactiveCount);


			int drMasterCardInActiveCount = util.isNullOrEmpty(inactiveDRMasterCardList)? Constants.GL_ZERO : inactiveDRMasterCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Inactive DR Master Cards are : "+drMasterCardInActiveCount);}
			callInfo.setField(Field.NO_OF_DR_MASTER_CARDS_INACTIVE, drMasterCardInActiveCount);


			int drAmexCardInActiveCount = util.isNullOrEmpty(inactiveDRAmexCardList)? Constants.GL_ZERO : inactiveDRAmexCardList.size(); 

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Inactive DR Amex Cards are : "+drAmexCardInActiveCount);}
			callInfo.setField(Field.NO_OF_DR_AMEX_CARDS_INACTIVE, drAmexCardInActiveCount);

			/**
			 * Rule engine update
			 */
			int totalInActiveDbtCards = Constants.GL_ZERO;

			if(!util.isNullOrEmpty(drVisaCardInactiveCount)){
				totalInActiveDbtCards = totalInActiveDbtCards + drVisaCardInactiveCount;
			}

			if(!util.isNullOrEmpty(drMasterCardInActiveCount)){
				totalInActiveDbtCards = totalInActiveDbtCards + drMasterCardInActiveCount;
			}

			if(!util.isNullOrEmpty(drAmexCardInActiveCount)){
				totalInActiveDbtCards = totalInActiveDbtCards + drAmexCardInActiveCount;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total No Of Inactive cards " + totalInActiveDbtCards);}

			if(!util.isNullOrEmpty(totalInActiveDbtCards)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Total No Of Inactive Debit Cards in the Rule Engine " + totalInActiveDbtCards);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDINACTIVECOUNT, totalInActiveDbtCards + Constants.EMPTY);
				ruleParamObj.updateIVRFields();
			}
			//END Rule Engine Updation
			

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.getIslamicConfiguration() "	+ e.getMessage());}
//			e.printStackTrace();

			throw new ServiceException(e);
		}
	}
	@Override
	public void getIslamicConfiguration(CallInfo callInfo) throws ServiceException {
		// TODO Auto-generated method stub
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getIslamicConfiguration()");}
		try{


			/**
			 * For setting the Islamic flow DM Property location path and values
			 */

			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getField(Field.ICEGlobalConfig);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ivr_ICEGlobalConfig Object is " + ivr_ICEGlobalConfig);}

			String customerSegment = Constants.CUST_SEGMENT_ISLAMIC;

			String globalPropFile = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_GLOBAL+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_English = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_English+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_Arabic = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Arabic+ Constants.UNDERSCORE + customerSegment);
			String dmPropLocation_Hindi = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Hindi+ Constants.UNDERSCORE + customerSegment);

			String lang = Constants.EMPTY_STRING + callInfo.getField(Field.LANGUAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language is " + lang);}

			if(!util.isNullOrEmpty(lang)){
				if(Constants.Hindi.equalsIgnoreCase(lang)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Hindi" + lang);}
					dmPropLocation =  dmPropLocation_Hindi;
				}else if(Constants.Arabic.equalsIgnoreCase(lang)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Arabic" + lang);}
					dmPropLocation =  dmPropLocation_Arabic;
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as English" + lang);}
					dmPropLocation =  dmPropLocation_English;
				}
			}

			callInfo.setField(Field.GLOBALPROPERTYFILE, globalPropFile);
			callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropLocation);
			callInfo.setField(Field.DMPROPERTIESLOCATIONENGLISH, dmPropLocation_English);
			callInfo.setField(Field.DMPROPERTIESLOCATIONARABIC, dmPropLocation_Arabic);
			callInfo.setField(Field.DMPROPERTIESLOCATIONHINDI, dmPropLocation_Hindi);



			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from the Islamic Meethac flow");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Processing the Account details of Islamic Meethac flow");}
			ArrayList<String>creditCardList = new ArrayList<String>();
			ArrayList<String>savingAcctList = new ArrayList<String>();
			ArrayList<String>loanAcctList = new ArrayList<String>();
			ArrayList<String>currentAcctList = new ArrayList<String>();
			ArrayList<String>fdAcctList = new ArrayList<String>();
			ArrayList<String>crVISACardList = new ArrayList<String>();
			ArrayList<String>crMasterCardList = new ArrayList<String>();
			ArrayList<String>crAmexCardList = new ArrayList<String>();
			ArrayList<String>islamicFDAcctList = new ArrayList<String>();
			ArrayList<String>islamicCurrentAcctList = new ArrayList<String>();
			ArrayList<String>islamicLoanAcctList = new ArrayList<String>();
			ArrayList<String>islamicSavingsAcctList = new ArrayList<String>();
			ArrayList<String>islamicCreditCardList = new ArrayList<String>();
			ArrayList<String>islamicCRVISACardList = new ArrayList<String>();
			ArrayList<String>islamicCRMasterCardList = new ArrayList<String>();
			ArrayList<String>islamicCRAmexCardList = new ArrayList<String>();
			
			
			ArrayList<String>islamicDRVISACardList = new ArrayList<String>();
			ArrayList<String>islamicDRMasterCardList = new ArrayList<String>();
			ArrayList<String>islamicDRAmexCardList = new ArrayList<String>();
			ArrayList<String>islamicInactiveDRVISACardList = new ArrayList<String>();
			ArrayList<String>islamicInactiveDRMasterCardList = new ArrayList<String>();
			ArrayList<String>islamicInactiveDRAmexCardList = new ArrayList<String>();
			
			ArrayList<String>drVISACardList = new ArrayList<String>();
			ArrayList<String>drMasterCardList = new ArrayList<String>();
			ArrayList<String>drAmexCardList = new ArrayList<String>();
			ArrayList<String>inactiveDRVISACardList = new ArrayList<String>();
			ArrayList<String>inactiveDRMasterCardList = new ArrayList<String>();
			ArrayList<String>inactiveDRAmexCardList = new ArrayList<String>();
			
			creditCardList = !util.isNullOrEmpty(callInfo.getField(Field.CREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.CREDITCARDLIST) : new ArrayList<String>();
			savingAcctList = !util.isNullOrEmpty(callInfo.getField(Field.SAVINGSACCTLIST))? (ArrayList<String>)callInfo.getField(Field.SAVINGSACCTLIST) : new ArrayList<String>();
			loanAcctList = !util.isNullOrEmpty(callInfo.getField(Field.LOANACCTLIST))? (ArrayList<String>)callInfo.getField(Field.LOANACCTLIST) : new ArrayList<String>();
			currentAcctList = !util.isNullOrEmpty(callInfo.getField(Field.CURRENTACCTLIST))? (ArrayList<String>)callInfo.getField(Field.CURRENTACCTLIST) : new ArrayList<String>();
			fdAcctList = !util.isNullOrEmpty(callInfo.getField(Field.FDACCTLIST))? (ArrayList<String>)callInfo.getField(Field.FDACCTLIST) : new ArrayList<String>();
			crVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.CRVISACARDLIST))? (ArrayList<String>)callInfo.getField(Field.CRVISACARDLIST) : new ArrayList<String>();
			crMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.CRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.CRMASTERCARDLIST) : new ArrayList<String>();
			crAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.CRAMEXCARDLIST))? (ArrayList<String>)callInfo.getField(Field.CRAMEXCARDLIST) : new ArrayList<String>();
			islamicFDAcctList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICFDACCTLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICFDACCTLIST) : new ArrayList<String>();
			islamicCurrentAcctList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICCURRENTACCTLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICCURRENTACCTLIST) : new ArrayList<String>();
			islamicLoanAcctList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICLOANACCTLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICLOANACCTLIST) : new ArrayList<String>();
			islamicSavingsAcctList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICSAVINGSACCTLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICSAVINGSACCTLIST) : new ArrayList<String>();
			islamicCreditCardList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICCREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICCREDITCARDLIST) : new ArrayList<String>();
			islamicCRVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICVISACREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICVISACREDITCARDLIST) : new ArrayList<String>();
			islamicCRMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICMASTERCREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICMASTERCREDITCARDLIST) : new ArrayList<String>();
			islamicCRAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICAMEXCREDITCARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICAMEXCREDITCARDLIST) : new ArrayList<String>();


			islamicDRVISACardList =  !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICDRVISACARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICDRVISACARDLIST) : new ArrayList<String>();
			islamicDRMasterCardList =  !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICDRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICDRMASTERCARDLIST) : new ArrayList<String>();
			islamicDRAmexCardList =  !util.isNullOrEmpty(callInfo.getField(Field.ISLAMICDRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.ISLAMICDRMASTERCARDLIST) : new ArrayList<String>();
			islamicInactiveDRVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.DRISLAMICVISACARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRISLAMICVISACARDLISTINACTIVE) : new ArrayList<String>();
			islamicInactiveDRMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRISLAMICMASTERCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRISLAMICMASTERCARDLISTINACTIVE) : new ArrayList<String>();
			islamicInactiveDRAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRISLAMICAMEXCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRISLAMICAMEXCARDLISTINACTIVE) : new ArrayList<String>();
			
			drVISACardList =  !util.isNullOrEmpty(callInfo.getField(Field.DRVISACARDLIST))? (ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST) : new ArrayList<String>();
			drMasterCardList =  !util.isNullOrEmpty(callInfo.getField(Field.DRMASTERCARDLIST))? (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST) : new ArrayList<String>();
			drAmexCardList =  !util.isNullOrEmpty(callInfo.getField(Field.DRAMEXCARDLIST))? (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST) : new ArrayList<String>();
			inactiveDRVISACardList = !util.isNullOrEmpty(callInfo.getField(Field.DRVISACARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE) : new ArrayList<String>();
			inactiveDRMasterCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRMASTERCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLISTINACTIVE) : new ArrayList<String>();
			inactiveDRAmexCardList = !util.isNullOrEmpty(callInfo.getField(Field.DRAMEXCARDLISTINACTIVE))? (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLISTINACTIVE) : new ArrayList<String>();
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "********************Before Processing the islamic Meethac account list********************");}

//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current credit card list is " + creditCardList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Saving account list is " + savingAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current loan account list is " + loanAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Current account list is " + currentAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current FD account list is " + fdAcctList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit VISA card list is " + crVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Creidit MASTER card list is " + crMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit AMEX card list is " + crAmexCardList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic FD Acct list is " + islamicFDAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Current Account list is " + islamicCurrentAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Loan Account list is " + islamicLoanAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Savings Account list is " + islamicSavingsAcctList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Card list is " + islamicCreditCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit VISA list is " + islamicCRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Master list is " + islamicCRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Amex list is " + islamicCRAmexCardList);}

//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Visa list is " + islamicDRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Master list is " + islamicDRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Amex list is " + islamicDRAmexCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Inactive Visa list is " + islamicInactiveDRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Inactive Master list is " + islamicInactiveDRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Inactive Amex is " + islamicInactiveDRAmexCardList);}
			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Visa list is " + drVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Master list is " + drMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Amex list is " + drAmexCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive Visa list is " + inactiveDRVISACardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive Master list is " + inactiveDRMasterCardList);}
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Credit Inactive Amex is " + inactiveDRAmexCardList);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current credit card list count is " + creditCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Saving account list count is " + savingAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current loan account list count is " + loanAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Current account list count is " + currentAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current FD account list count is " + fdAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit VISA card list count is " + crVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Creidit MASTER card list count is " + crMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Credit AMEX card list count is " + crAmexCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic FD Acct list count is " + islamicFDAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Current Account list count is " + islamicCurrentAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Loan Account list count is " + islamicLoanAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Savings Account list count is " + islamicSavingsAcctList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Card list count is " + islamicCreditCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit VISA list count is " + islamicCRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Master list count is " + islamicCRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Credit Amex list count is " + islamicCRAmexCardList.size());}

			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Visa list count is " + islamicDRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Master list count is " + islamicDRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Amex list count is " + islamicDRAmexCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Inactive VISA list count is " + islamicInactiveDRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Inactive master list count is " + islamicInactiveDRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Islamic Debit Inactive Amex list count is " + islamicInactiveDRAmexCardList.size());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Visa list count is " + drVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Master list count is " + drMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Amex list count is " + drAmexCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive VISA list count is " + inactiveDRVISACardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive master list count is " + inactiveDRMasterCardList.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current  Debit Inactive Amex list count is " + inactiveDRAmexCardList.size());}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "**********Resetting the Islamic acccount / card list to the generic variables ");}


			savingAcctList = islamicSavingsAcctList;
			loanAcctList = islamicLoanAcctList;
			currentAcctList = islamicCurrentAcctList;
			fdAcctList = islamicFDAcctList;

			creditCardList = islamicCreditCardList;
			crVISACardList = islamicCRVISACardList;
			crMasterCardList = islamicCRMasterCardList;
			crAmexCardList = islamicCRAmexCardList;

			
			drVISACardList =islamicDRVISACardList;
			drMasterCardList =islamicDRMasterCardList;
			drAmexCardList =islamicDRAmexCardList;
			inactiveDRVISACardList =islamicInactiveDRVISACardList;
			inactiveDRMasterCardList =islamicInactiveDRMasterCardList;
			inactiveDRAmexCardList =islamicInactiveDRAmexCardList;
			
			
			callInfo.setField(Field.CREDITCARDLIST, creditCardList);
			callInfo.setField(Field.SAVINGSACCTLIST, savingAcctList);
			callInfo.setField(Field.LOANACCTLIST, loanAcctList);
			callInfo.setField(Field.CURRENTACCTLIST, currentAcctList);
			callInfo.setField(Field.FDACCTLIST, fdAcctList);
			callInfo.setField(Field.CRVISACARDLIST, crVISACardList);
			callInfo.setField(Field.CRMASTERCARDLIST, crMasterCardList);
			callInfo.setField(Field.CRAMEXCARDLIST, crAmexCardList);
			callInfo.setField(Field.ISLAMICFDACCTLIST, islamicFDAcctList);
			callInfo.setField(Field.ISLAMICCURRENTACCTLIST, islamicCurrentAcctList);
			callInfo.setField(Field.ISLAMICLOANACCTLIST, islamicLoanAcctList);
			callInfo.setField(Field.ISLAMICSAVINGSACCTLIST, islamicSavingsAcctList);
			callInfo.setField(Field.ISLAMICVISACREDITCARDLIST, islamicCRVISACardList);
			callInfo.setField(Field.ISLAMICMASTERCREDITCARDLIST, islamicCRMasterCardList);
			callInfo.setField(Field.ISLAMICAMEXCREDITCARDLIST, islamicCRAmexCardList);
			callInfo.setField(Field.ISLAMICCREDITCARDLIST, islamicCreditCardList);

			callInfo.setField(Field.DRISLAMICVISACARDLISTINACTIVE, islamicInactiveDRVISACardList);
			callInfo.setField(Field.DRISLAMICMASTERCARDLISTINACTIVE, islamicInactiveDRMasterCardList);
			callInfo.setField(Field.DRISLAMICAMEXCARDLISTINACTIVE, islamicInactiveDRAmexCardList);
			callInfo.setField(Field.DRVISACARDLISTINACTIVE, inactiveDRVISACardList);
			callInfo.setField(Field.DRMASTERCARDLISTINACTIVE, inactiveDRMasterCardList);
			callInfo.setField(Field.DRAMEXCARDLISTINACTIVE, inactiveDRAmexCardList);
			
			callInfo.setField(Field.ISLAMICDRVISACARDLIST, islamicDRVISACardList);
			callInfo.setField(Field.ISLAMICDRMASTERCARDLIST, islamicDRMasterCardList);
			callInfo.setField(Field.ISLAMICDRAMEXCARDLIST, islamicDRAmexCardList);
			callInfo.setField(Field.DRVISACARDLIST, drVISACardList);
			callInfo.setField(Field.DRMASTERCARDLIST, drMasterCardList);
			callInfo.setField(Field.DRMASTERCARDLIST, drAmexCardList);
			
			
			
			ArrayList<String>debitCardList = new ArrayList<String>();
			debitCardList.addAll(drVISACardList);
			debitCardList.addAll(drMasterCardList);
			debitCardList.addAll(drAmexCardList);
			
			callInfo.setField(Field.DEBITCARDLIST, debitCardList);
			
			
			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}

			//Initialized the Rule Engine Object

			int creditCardCount = util.isNullOrEmpty(creditCardList)? Constants.GL_ZERO : creditCardList.size();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Credit cards : "+creditCardCount );}
			callInfo.setField(Field.NO_OF_CREDIT_CARDS, creditCardCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Credit card count in the Rule Engine " + creditCardCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CREDITCARDCOUNT, (creditCardCount+Constants.EMPTY));
			//END Rule Engine Updation


			int savingAccountCount = util.isNullOrEmpty(savingAcctList)? Constants.GL_ZERO : savingAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Saving accounts : "+savingAccountCount);}
			callInfo.setField(Field.NO_OF_SAVINGS_ACCTS, savingAccountCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Saving Account count in the Rule Engine " + savingAccountCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_SAVINGACCTCOUNT, (savingAccountCount+Constants.EMPTY));
			//END Rule Engine Updation


			int loanAcctCount = util.isNullOrEmpty(loanAcctList)? Constants.GL_ZERO : loanAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Loan accounts : "+loanAcctCount);}
			callInfo.setField(Field.NO_OF_LOAN_ACCTS, loanAcctCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Loan Account count in the Rule Engine " + loanAcctCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_LOANACCTCOUNT, (loanAcctCount+Constants.EMPTY));
			//END Rule Engine Updation


			int currentAcctCount = util.isNullOrEmpty(currentAcctList)?Constants.GL_ZERO : currentAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Current accounts : "+currentAcctCount);}
			callInfo.setField(Field.NO_OF_CURRENT_ACCTS, currentAcctCount);


			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Current Account count in the Rule Engine " + currentAcctCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CURRENTACCTCOUNT, (currentAcctCount+Constants.EMPTY));
			//END Rule Engine Updation

			int fdAcctCount = util.isNullOrEmpty(fdAcctList)? Constants.GL_ZERO : fdAcctList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of FD accounts : "+fdAcctCount);}
			callInfo.setField(Field.NO_OF_FD_ACCTS, fdAcctCount);

			/**
			 * Rule engine update
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Deposit Account count in the Rule Engine " + fdAcctCount);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEPOSITACCTCOUNT, (fdAcctCount+Constants.EMPTY));
			//END Rule Engine Updation


			ruleParamObj.updateIVRFields();
			//End Rule Engine Update

			/**
			 * Fixes done by Vinoth on 16-03-2014 to add the no of credit card VISA / Master / AMEX && same for Debit card details
			 */


			int crVisaCardCount = util.isNullOrEmpty(crVISACardList)? Constants.GL_ZERO : crVISACardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of CR VISA Cards are : "+crVisaCardCount);}
			callInfo.setField(Field.NO_OF_CR_VISA_CARDS, crVisaCardCount);


			int crMasterCardCount = util.isNullOrEmpty(crMasterCardList)? Constants.GL_ZERO : crMasterCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of CR Master Cards are : "+crMasterCardCount);}
			callInfo.setField(Field.NO_OF_CR_MASTER_CARDS, crMasterCardCount);


			int crAmexCardCount = util.isNullOrEmpty(crAmexCardList)?Constants.GL_ZERO : crAmexCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of CR Amex Cards are : "+crAmexCardCount);}
			callInfo.setField(Field.NO_OF_CR_AMEX_CARDS, crAmexCardCount);

			
			
			int drVISACardCount = util.isNullOrEmpty(drVISACardList)? Constants.GL_ZERO : drVISACardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of DR VISA Cards are : "+drVISACardCount);}
			callInfo.setField(Field.NO_OF_DR_VISA_CARDS, drVISACardCount);


			int drMasterCardCount = util.isNullOrEmpty(drMasterCardList)? Constants.GL_ZERO : drMasterCardList.size(); 

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of DR Master Cards are : "+drMasterCardCount);}
			callInfo.setField(Field.NO_OF_DR_MASTER_CARDS, drMasterCardCount);


			int drAmexCardCount = util.isNullOrEmpty(drAmexCardList)? Constants.GL_ZERO : drAmexCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of DR Amex Cards are : "+drAmexCardCount);}
			callInfo.setField(Field.NO_OF_DR_AMEX_CARDS, drAmexCardCount);


			int drVisaCardInactiveCount = util.isNullOrEmpty(inactiveDRVISACardList)? Constants.GL_ZERO : inactiveDRVISACardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Inactive DR VISA Cards are : "+drVisaCardInactiveCount);}
			callInfo.setField(Field.NO_OF_DR_VISA_CARDS_INACTVE, drVisaCardInactiveCount);


			int drMasterCardInActiveCount = util.isNullOrEmpty(inactiveDRMasterCardList)? Constants.GL_ZERO : inactiveDRMasterCardList.size();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Inactive DR Master Cards are : "+drMasterCardInActiveCount);}
			callInfo.setField(Field.NO_OF_DR_MASTER_CARDS_INACTIVE, drMasterCardInActiveCount);


			int drAmexCardInActiveCount = util.isNullOrEmpty(inactiveDRAmexCardList)? Constants.GL_ZERO : inactiveDRAmexCardList.size(); 

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Inactive DR Amex Cards are : "+drAmexCardInActiveCount);}
			callInfo.setField(Field.NO_OF_DR_AMEX_CARDS_INACTIVE, drAmexCardInActiveCount);

			
			
			/**
			 * Rule engine update
			 */
			int totalInActiveDbtCards = Constants.GL_ZERO;

			if(!util.isNullOrEmpty(drVisaCardInactiveCount)){
				totalInActiveDbtCards = totalInActiveDbtCards + drVisaCardInactiveCount;
			}

			if(!util.isNullOrEmpty(drMasterCardInActiveCount)){
				totalInActiveDbtCards = totalInActiveDbtCards + drMasterCardInActiveCount;
			}

			if(!util.isNullOrEmpty(drAmexCardInActiveCount)){
				totalInActiveDbtCards = totalInActiveDbtCards + drAmexCardInActiveCount;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total No Of Inactive cards " + totalInActiveDbtCards);}

			if(!util.isNullOrEmpty(totalInActiveDbtCards)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Total No Of Inactive Debit Cards in the Rule Engine " + totalInActiveDbtCards);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDINACTIVECOUNT, totalInActiveDbtCards + Constants.EMPTY);
				ruleParamObj.updateIVRFields();
			}
			//END Rule Engine Updation

			
			
			

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GlobalImpl.getIslamicConfiguration() "	+ e.getMessage());}
//			e.printStackTrace();

			throw new ServiceException(e);
		}


	}

	@Override
	public HashMap<String, String> getLastEnteredMobileNoMap(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GlobalImpl.getLastEnteredMobileNoMap()");}
		
		HashMap<String, String>lastEnteredMobileNoMap = new HashMap<String, String>();
		try{
			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}
			
			List<String> lastEnteredMobNoMapList = (List<String>)ruleParamObj.getParam(Constants.RULE_ENGINE_LAST_ENTERED_MOBILENO_MAP);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last entered Mobile number map from the Feed table is "+ lastEnteredMobNoMapList);}

			if(!util.isNullOrEmpty(lastEnteredMobNoMapList)){

				String strLastSelectedMobNoMap = lastEnteredMobNoMapList.get(Constants.GL_ZERO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The last selected mobile number map entry is "+ strLastSelectedMobNoMap);}

				if(!util.isNullOrEmpty(strLastSelectedMobNoMap)){
					String[] strArray = strLastSelectedMobNoMap.split(Constants.PIPESEPERATOR);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Pipe seperating the last selected mobile entry the value is "+ strArray);}

					if(!util.isNullOrEmpty(strArray)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The size of the Selected mobile Number Map entry is "+ strArray.length);}

						String tempString = Constants.EMPTY_STRING;
						String[] tempArray = null;
						

						for(int i=0; i<strArray.length; i++){
							if(!util.isNullOrEmpty(strArray[i])){
								tempString = strArray[i].toString();
								if(!util.isNullOrEmpty(tempString)){
									tempArray = tempString.split(Constants.MINUS);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After splitting the each entry of the map the key and value pair entries are"+ tempArray);}

									if(!util.isNullOrEmpty(tempArray) && tempArray.length == Constants.GL_TWO ){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The key value pair of the last entered mobile number size is "+ tempArray.length);}
										lastEnteredMobileNoMap.put(tempArray[0], tempArray[1]);

										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The hash map entries are "+ lastEnteredMobileNoMap);}
									}
								}
							}
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Final hash map entries to be inserted to the callinfo field is "+ lastEnteredMobileNoMap);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Final hash map size to be inserted to the callinfo field is "+ lastEnteredMobileNoMap.size());}

						callInfo.setField(Field.LASTENTEREDMOBILENOMAP, lastEnteredMobileNoMap);
					}
				}
			}
		}catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e, "EXCEPTION at:  Processing the last entered mobile number map method" + e.getMessage());
			throw new ServiceException(e);
		}

		return lastEnteredMobileNoMap;
	}

}
