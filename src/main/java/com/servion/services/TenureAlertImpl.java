package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;


public class TenureAlertImpl implements ITenureAlert{

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
	public String getTenureAlertCongratesPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TenureAlertImpl.getTenureAlertCongratesPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Rule Object values");}

//			ICERuleParam iceRuleParam  = (ICERuleParam) callInfo.getICERuleParam();

//			if(util.isNullOrEmpty(iceRuleParam)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "****iceRuleParam object is null or emtpy*****");}
//				return Constants.EMPTY;
//			}

//			String strNoOfYearsOpened = (String)callInfo.getField(Field.NoOfYearsOpened);

//			ArrayList<String> noOfYearsOpenedList = (ArrayList<String>)iceRuleParam.getParam(Constants.RUI_ENGINE_NO_OF_YEARS_OPENED);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No of years opened list :" + noOfYearsOpenedList);}

//			String str_NoOfYearsOpened = Constants.EMPTY;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No of years opened first index string is  :" + str_NoOfYearsOpened);}

//			str_NoOfYearsOpened = noOfYearsOpenedList!= null ? noOfYearsOpenedList.get(Constants.GL_ONE) : Constants.EMPTY_STRING;

//			if(!util.isNullOrEmpty(noOfYearsOpenedList)){
				
//				Calendar cal1 = Calendar.getInstance();
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's Date is " + cal1.getTime());}
//				
//				cal1.add(Calendar.DATE, -Integer.parseInt(str_NoOfYearsOpened));
//				int acctOpenDays= cal1.get(Calendar.YEAR);
//				int currentDays = Calendar.getInstance().get(Calendar.YEAR);
//				int resultDays = currentDays - acctOpenDays;
//				
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of Years" + resultDays);}
				
				
				ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
				
				if(util.isNullOrEmpty(iceGlobalConfig)){
					throw new ServiceException("ICEGlobal config object is null or empty");
				}
				
				String acctOpeningLimit = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNT_COMPLETION_YEAR_LIMIT);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Opening limit is "+ acctOpeningLimit);}
				
				//Adding the values to dynamic 
				dynamicValueArray.add(acctOpeningLimit);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

				//Getting language code 
				String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
				Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

				//Forming Key combination 
				String anncID = AnncIDMap.getAnncID("Tenure_Message");
				String featureID = FeatureIDMap.getFeatureID("Tenure_Alert");
				String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

				//Adding to object value
				Object[] object = new Object[dynamicValueArray.size()];
				for(int count=0; count<dynamicValueArray.size();count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
					object[count] = dynamicValueArray.get(count);
				}

				//Initial prompt is formed based from
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

				String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
				String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

				finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
//			}else{
//				finalResult=Constants.EMPTY_STRING;
//			}


			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ComplaintAlertImpl.getComplaintAlertMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  ChequeBookRequestImpl.getChequeBookRequestSuccPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public boolean isAccountOpenedForXYears(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TenureAlertImpl. isAccountOpenedForXYears()");}
			//			ArrayList<String> acctOpeningDateList = new ArrayList<String>();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the ivr_RuleEngingeData values");}

			String accountOpeningDate = Constants.EMPTY_STRING;

			//TODO : Need to convert it as Rule :
			//ArrayList<String> acctOpeningDateList = (ArrayList<String>)ivr_RuleEngingeData.getParam(Constants.RUI_ENGINE_OPENING_DATE);

			accountOpeningDate = callInfo.getField(Field.AccountOpeningDate) != null ? (String)callInfo.getField(Field.AccountOpeningDate) : Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date from callInfo field is " + accountOpeningDate);}

			XMLGregorianCalendar todayCalender = util.getXMLGregorianCalendarNow();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's Calendar year is "+ todayCalender);}

			XMLGregorianCalendar acctOpeningDate = util.convertDateStringtoXMLGregCalendar(accountOpeningDate, Constants.DATEFORMAT_YYYYMMDD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening calendar object is "+ acctOpeningDate);}

			int acctOpeningYear = acctOpeningDate.getYear();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Account opening year is "+ acctOpeningYear);}

			int currentYear = todayCalender.getYear();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Totay's year is "+ currentYear);}

			int diffInYears = currentYear - acctOpeningYear;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Difference in the years is "+ diffInYears);}


			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			
			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("ICEGlobal config object is null or empty");
			}
			
			String acctOpeningLimit = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_ACCOUNT_COMPLETION_YEAR_LIMIT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Opening limit is "+ acctOpeningLimit);}

//			String str_acctOpeningDate = callInfo.getField(Field.AccountOpeningDate) != null ? (String)callInfo.getField(Field.AccountOpeningDate) : Constants.EMPTY_STRING;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening year is "+ str_acctOpeningDate);}

			int int_AcctOpeningLimit = Integer.parseInt(acctOpeningLimit);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date integer format is "+ int_AcctOpeningLimit);}


			if(int_AcctOpeningLimit == diffInYears){
				callInfo.setField(Field.NoOfYearsOpened,acctOpeningLimit);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening year and the Configured year are same, the configured year is  "+ int_AcctOpeningLimit);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Returning the method value as true"+ int_AcctOpeningLimit);}
				return true;
			}

			//			GregorianCalendar calAcctOpeningDate = !util.isNullOrEmpty(acctOpeningDate)? acctOpeningDate.toGregorianCalendar() : null;
			//			GregorianCalendar calendartoday = todayCalender.toGregorianCalendar();
			//
			//			long miliSecondForDate1 = calAcctOpeningDate.getTimeInMillis();
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The DOB Millisec"+ miliSecondForDate1);}
			//
			//			long miliSecondForDate2 = calendartoday.getTimeInMillis();
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The today;s Millisec"+ miliSecondForDate2);}
			//
			//			// Calculate the difference in millisecond between two dates
			//			long diffInMilis = miliSecondForDate1 - miliSecondForDate2;
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The difference between dates in milli seconds "+ diffInMilis);}
			//
			//			long diffInDays = diffInMilis / (24 * 60 * 60 * 1000);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The difference between date is "+ diffInDays);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT : TenureAlertImpl. isAccountOpenedForXYears() ");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  TenureAlertImpl. isAccountOpenedForXYears() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return false;
	}

	@Override
	//TODO need to continue with this service
	public boolean isNewlyOpenedAccount(CallInfo callInfo) throws ServiceException {
		boolean finalResult = false;
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}

		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TenureAlertImpl. isNewlyOpenedAccount()");}

			ArrayList<String> acctOpeningDateList = new ArrayList<String>();
			Calendar todayCalender = Calendar.getInstance(),calAccountOpenedDate=Calendar.getInstance();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the ivr_RuleEngingeData values");}
			ICERuleParam ivr_RuleEngingeData = (ICERuleParam) callInfo.getICERuleParam();
			
			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the iceGlobalConfig values");}
			
			if(util.isNullOrEmpty(iceGlobalConfig)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "iceGlobalConfig object is null or empty");}
				throw new ServiceException("iceGlobalConfig object is null or empty");
			}

			//			String accountOpeningDate = Constants.EMPTY_STRING;
			String strNewAccountLimit = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_NEW_ACCOUNT_LIMIT);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Till "+todayCalender+"this account is new. ");}
			//TODO : Need to convert it as Rule :
			//			acctOpeningDateList = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NEW_ACCOUNT_LIMIT);
			//			
			//			acctOpeningDateList = (ArrayList<String>)ivr_RuleEngingeData.getparam(Constants.RUI_ACCOUNT_OPENING_DATE);
			//			if(!util.isNullOrEmpty(acctOpeningDateList) && !util.isNullOrEmpty(acctOpeningDateList.get(Constants.GL_ZERO))){
			//				accountOpeningDate = acctOpeningDateList.get(Constants.GL_ZERO);
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Account opening date is " + accountOpeningDate);}
			//			}

			String accountOpeningDate = callInfo.getField(Field.AccountOpeningDate) != null ? (String) callInfo.getField(Field.AccountOpeningDate) : Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date received from the callInfo field is "+ accountOpeningDate);}

			SimpleDateFormat spdf = new SimpleDateFormat(Constants.DATEFORMAT_YYYYMMDD);
			Date dateAccountOpening = spdf.parse(accountOpeningDate);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening calendar object is "+ accountOpeningDate);}
			calAccountOpenedDate.setTime(dateAccountOpening);

			if(!util.isNullOrEmpty(strNewAccountLimit)){
				int intNewAccountLimit = Integer.parseInt(strNewAccountLimit);
				calAccountOpenedDate.add(Calendar.DATE, intNewAccountLimit);
			}

			if(calAccountOpenedDate.after(todayCalender)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its a newly opened Account");}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TenureAlertImpl. isNewlyOpenedAccount()");}
				return true;

			}else{				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its not newly opened account");}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: TenureAlertImpl. isNewlyOpenedAccount()");}
				return false;
			}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ProductInformationImpl.isNewlyOpenedAccount() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
	}

	@Override
	public String updateTenureAlreadyPlayed(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TenureAlertImpl. updateTenureAlreadyPlayed()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentTime = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date time is "+ currentTime);}

			String mobileNo = util.isNullOrEmpty(callInfo.getField(Field.REG_MOBILENO)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.REG_MOBILENO); 
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Registered mobile number is "+mobileNo);}
			
			configMap.put(DBConstants.MOBILE_NO,mobileNo);
//			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Cutomer id is " + callInfo.getField(Field.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date time is " + currentTime );}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updateTenureAnnounced(logger, sessionId, uui, configMap);

			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:TenureAlertImpl. updateTenureAlreadyPlayed ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:TenureAlertImpl. updateTenureAlreadyPlayed ()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: TenureAlertImpl. updateTenureAlreadyPlayed ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}

}
