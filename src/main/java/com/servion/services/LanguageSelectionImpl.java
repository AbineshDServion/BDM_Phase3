package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.Field;

public class LanguageSelectionImpl implements ILanguageSelection{

	private static Logger logger = LoggerObject.getLogger();
	private boolean isConfigMethodCalled = false;

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	@Override
	public String getLanguagePhrases(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.getLanguagePhrases()");}

		try{

			getConfigurationParam(callInfo);

			ArrayList<String> phraseList = (ArrayList<String>) callInfo.getField(Field.LanguagePhraseList);

			if(!util.isNullOrEmpty(phraseList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language phraseList size is "+ phraseList.size());}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting silence phrase in the phrase list");}
				phraseList.add(DynaPhraseConstants.SILENCE_PHRASE);
			}
			finalResult = util.getCUIPhraseString(phraseList);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Phrase String is"+ finalResult);}

			//Setting the grammar value
			ArrayList<String> dynamicValueArray = (ArrayList<String>)callInfo.getField(Field.LanguageList);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UI language list"+ dynamicValueArray);}

			String str_Grammar = Constants.EMPTY_STRING;
			if(!util.isNullOrEmpty(dynamicValueArray)){
				for(int count=0; count<dynamicValueArray.size();count++){
					if(!util.isNullOrEmpty(str_Grammar)){
						str_Grammar = str_Grammar+Constants.COMMA+dynamicValueArray.get(count);
					}else{
						str_Grammar = dynamicValueArray.get(count);
					}
				}
			}

			callInfo.setField(Field.DYNAMICLIST, str_Grammar);

			callInfo.setField(Field.MOREOPTION, false);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final language grammar"+ str_Grammar);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Dynamic List Grammar count  is"+ dynamicValueArray.size());}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "MoreOption  is false");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LanguageSelectionImpl.getLanguagePhrases() "+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}

	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable dont throw any new exception, throw if it is mandatory
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.getConfigurationParam()");}
			if(!isConfigMethodCalled){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
				ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

				ArrayList<String> languagePhraseList = new ArrayList<String>();
				int LanguageSelectionMaxTries = Constants.GL_ZERO;
				ArrayList<String>  availableLanguages = new ArrayList<String>();

				languagePhraseList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LanguagePhrases);
				availableLanguages = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LanguageList);

				String temp_Str = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_LanguageSelectionMaxTries);
				LanguageSelectionMaxTries = util.isNullOrEmpty(temp_Str)?Constants.GL_ONE:Integer.parseInt(temp_Str);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Language Selection maximum try count is"+LanguageSelectionMaxTries);}

				if(!util.isNullOrEmpty(languagePhraseList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Account phrase list size is"+languagePhraseList.size());}
				}

				if(!util.isNullOrEmpty(availableLanguages)){
					for(int count=0; count<availableLanguages.size(); count++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The available "+count+" languages is "+availableLanguages.get(count));}
					}
					callInfo.setField(Field.AVAILABLELANGCOUNT, availableLanguages.size());
				}

				callInfo.setField(Field.LanguageList, availableLanguages);
				callInfo.setField(Field.LanguagePhraseList, languagePhraseList);
				callInfo.setField(Field.LanguageSelectionMaxTries, LanguageSelectionMaxTries);

				isConfigMethodCalled = true;
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CUI config parameters has already been setted");}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: LanguageSelectionImpl.getConfigurationParam()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LanguageSelectionImpl.getConfigurationParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
	}

	@Override
	public int getLanguageSelectionMaxTries(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.getLanguageSelectionMaxTries()");}
		int returnValue = Constants.GL_ONE;

		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The language selection maximum try count is"+ callInfo.getField(Field.LanguageSelectionMaxTries));}
			returnValue = Integer.parseInt(callInfo.getField(Field.LanguageSelectionMaxTries).toString());

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LanguageSelectionImpl.getLanguageSelectionMaxTries() "+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return returnValue;
	}

	@Override
	public String getPreferredLanguage(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String preferredLanguage = Constants.EMPTY_STRING;
		String code = Constants.ONE;
		try{
			getConfigurationParam(callInfo);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.getPreferredLanguage()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			
			
			/**
			 * Rule engine update
			 */
			
			
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}

			
			List<String> preferredLangList = util.isNullOrEmpty(ruleParamObj.getParam(Constants.RULE_ENGINE_PREFERRED_LANGUAGE))? null : ruleParamObj.getParam(Constants.RULE_ENGINE_PREFERRED_LANGUAGE);
			preferredLanguage = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(preferredLangList)){
				preferredLanguage = preferredLangList.get(Constants.GL_ZERO);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Preferred Language from Rule engine is " + preferredLanguage);}
				//Language retrieved from table - issue for connect back to IVR calls 20-Aug-2019
				if(preferredLanguage.equalsIgnoreCase(Constants.ALPHA_A)){
					preferredLanguage = Constants.arabic;
				}else if(preferredLanguage.equalsIgnoreCase(Constants.ALPHA_E)){
					preferredLanguage = Constants.english;
				}else if(preferredLanguage.equalsIgnoreCase(Constants.ALPHA_H)){
					preferredLanguage = Constants.hindi;
				}else if(preferredLanguage.equalsIgnoreCase(Constants.ALPHA_U)){
					preferredLanguage = Constants.urudu;
				}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Preferred Language after generalized " + preferredLanguage);}
				if(!util.isNullOrEmpty(preferredLanguage)){
					callInfo.setField(Field.PREFERREDLANG, preferredLanguage);
					callInfo.setField(Field.LANGUAGE, preferredLanguage);
				}
				
				code = Constants.ZERO;
				
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Resultant code is " + code);}
			/**
			 * Get ICE IVR Field
			 */
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "##########  Getting the ICE IVR FIELD : ##########" );}

			//TODO need to update in Rule Engine
			//preferredLanguage = ICE Field;
			//ivr_ICEFeatureData.getConfig().putParam(key, dataType, value);

//			callInfo.setField(Field.PREFERREDLANG , preferredLanguage);

//			if(!util.isNullOrEmpty(preferredLanguage)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Preferred language is "+preferredLanguage);}
//			}
//
//			callInfo.setField(Field.LANGUAGE, preferredLanguage);
//			callInfo.setField(Field.PREFERREDLANG, preferredLanguage);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: LanguageSelectionImpl.getPreferredLanguage()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LanguageSelectionImpl.getPreferredLanguage()"+ e.getMessage());}
			throw new ServiceException(e);
		}


		return code;
	}

	@Override
	public boolean isMobileNumber(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		boolean isANIAMobNo = false;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.isMobileNumber()");}
		try{
			IGlobal globalService = Context.getIglobal();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling IGlobal service object");}
			isANIAMobNo = globalService.isANIAMobNo(callInfo);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the ANI is a mobile number "+ isANIAMobNo);}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LanguageSelectionImpl.isMobileNumber()"+ e.getMessage());}
			throw new ServiceException(e);
		}

		return isANIAMobNo;
	}

	@Override
	public boolean isMoreThanOneLangAvail(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		boolean isMoreLangAvail = false;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.isMoreThanOneLangAvail()");}

		try{

			getConfigurationParam(callInfo);
			ArrayList<String> LanguageList = (ArrayList<String>) callInfo.getField(Field.LanguageList);

			if(!util.isNullOrEmpty(LanguageList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The size of available language phrase size is "+ LanguageList.size());}
				if(LanguageList.size()>Constants.GL_ONE){
					isMoreLangAvail = true;
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language are not setted at the ICE FetureConfig level");}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is this callflow has more than one language options"+ isMoreLangAvail);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: LanguageSelectionImpl.isMoreThanOneLangAvail()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LanguageSelectionImpl.isMoreThanOneLangAvail()"+ e.getMessage());}
			throw new ServiceException(e);
		}

		return isMoreLangAvail;
	}

	@Override
	public String updatePreferredLanguage(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: LanguageSelectionImpl.updatePreferredLanguage()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}
			configMap.put(DBConstants.DATETIME, currentDate);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is " + configMap.get(Constants.DATETIME) );}
			
			
			configMap.put(DBConstants.PREFERRED_LANGUAGE, callInfo.getField(Field.LANGUAGE));
			configMap.put(DBConstants.CLI, callInfo.getField(Field.ANI));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language" + configMap.get(Constants.PREFERRED_LANGUAGE));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI is " + configMap.get(Constants.CLI) );}
			
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updatePreferredLanguage(logger, sessionId, uui, configMap);
				
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: LanguageSelectionImpl.updatePreferredLanguage()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: LanguageSelectionImpl.updatePreferredLanguage()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  LanguageSelectionImpl.updatePreferredLanguage()" + e.getMessage());
			throw new ServiceException(e);
		}

	}


	public static String getLanguageLocale(CallInfo callInfo){
		String returnStr = Constants.EMPTY;
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			String language = (String) callInfo.getField(Field.LANGUAGE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"User selected language is :" + language);}

			if(util.isNullOrEmpty(language)){
				throw new ServiceException("Language value for getLanguageLocale is null or empty");
			}


			String dmPropertyLocEnglish = (String)callInfo.getField(Field.DMPROPERTIESLOCATIONENGLISH);
			String dmPropertyLocArabic = (String)callInfo.getField(Field.DMPROPERTIESLOCATIONARABIC);
			String dmPropertyLocHindi = (String)callInfo.getField(Field.DMPROPERTIESLOCATIONHINDI);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Returing English Language Locale");}



			if(Constants.English.equalsIgnoreCase(language) || Constants.Eng.equalsIgnoreCase(language) || Constants.ALPHA_E.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring English Language Locale");}
				callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocEnglish);
				returnStr =  Constants.Locale_English;
			}else if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Arabic Language Key");}
				callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocArabic);
				returnStr =  Constants.Locale_Arabic; 
			}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Hindi Language Key");}
				callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocHindi);
				returnStr =  Constants.Locale_Hindi;
			}else if(Constants.Urudu.equalsIgnoreCase(language) || Constants.Uru.equalsIgnoreCase(language) || Constants.ALPHA_U.equalsIgnoreCase(language)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Urudu Language Key");}
				callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocHindi);
				returnStr =  Constants.Locale_Urudu;
			}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: Util.getLanguageLocale()" + e.getMessage());
		}

		return returnStr;
	}
}
