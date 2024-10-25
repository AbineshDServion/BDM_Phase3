package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.SecretKey;

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
import com.servion.jce.JCEWrapper;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;

public class OTPGenerationImpl implements IOTPGeneration{

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
	public String getOTPGenerationPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPGenerationImpl.getOTPGenerationPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			//Need to get the FeatureConfig Data
			generateOTP(callInfo);

			String otpRefNo = Constants.EMPTY_STRING+callInfo.getField(Field.GENERATEDOTPREFNO);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The OTP Reference number is "+ otpRefNo);}

			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The Feature name is "+ featureName);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(otpRefNo);
			dynamicValueArray.add((featureName+Constants.WAV_EXTENSION).trim());

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("OTP_Sent_Message");
			String anncID = AnncIDMap.getAnncID("OTP_Sent_Message");
			String featureID = FeatureIDMap.getFeatureID("OTP_Validation");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;

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
			//no need

			//To have the property file grammar, need to call that util method here
			//no need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign
			//no need
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: OTPGenerationImpl.getOTPGenerationPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at OTPGenerationImpl.getOTPGenerationPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getOTPLogGeneration(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPGenerationImpl. getOTPLogGeneration()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is "+ currentDate);}
			
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentDate);
			
			String charityType = (String)callInfo.getField(Field.SELECTEDCHARITYTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Charity type is "+ charityType);}
			
			configMap.put(DBConstants.CHARITYNAME, charityType);
			
			
			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}
			
			configMap.put(DBConstants.FEATURENAME, featureName);
			
			String destNo = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Destination Account number ending with "+ util.getSubstring(destNo, Constants.GL_FOUR));}
			
			/**
			 * Following are the condition handling for credit card payment internal and within BM flows
			 */
			if(Constants.FEATURENAME_CREDITCARDPAYMENTINTERNAL.equalsIgnoreCase(featureName)
					|| Constants.FEATURENAME_CREDITCARDPAYMENTTHIRDPARTYWITHINBM.equalsIgnoreCase(featureName)){
				
				String cardEncryptionKey = Constants.CARD_ENCRYPTION_KEY;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Card encryption key from the card payment encryption");}

				cardEncryptionKey = util.convertTo48BitKey(cardEncryptionKey);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "card encryptiong key has been converted to 48 bits");}
				
				JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
				SecretKey sKey = jceWrap.toSecretKey(cardEncryptionKey);
				destNo = jceWrap.encrypt(destNo, sKey);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received the encrypted key for credit card number is "+ destNo);}
			}
			//END
			
			
			/**
			 * Following are the changes done for Third Party Remittance destination currency type announcements
			 */
			if(Constants.FEATURENAME_THIRDPARTYREMITTANCE.equalsIgnoreCase(featureName)){
				
				String descCurr = (String)callInfo.getField(Field.TPRSELECTEDCURRTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Third Party Remittance Desctination currency type is "+  descCurr);}

				destNo = destNo + Constants.COMMA + descCurr;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After concatenation the destination currency type the final value is "+  destNo);}
			}
			//END  vinoth
			configMap.put(DBConstants.DESTACCOUNTNUMBER, destNo);
			
			String exchangeRates = (String)callInfo.getField(Field.EXCHANGE_RATES_VALUE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exchange rate value is "+ exchangeRates);}
			
			configMap.put(DBConstants.EXCHANGERATE, exchangeRates);

			
			String encryptedOTP = (String)callInfo.getField(Field.ENCRYPTEDOTP);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Encrypted OTP is "+ encryptedOTP);}
			
			configMap.put(DBConstants.OTP, encryptedOTP);
			
			String srcNo = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "source account or card number is "+ util.maskCardOrAccountNumber(srcNo));}
			
			configMap.put(DBConstants.SRCACCOUNTNUMBER, srcNo);
			
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
//			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			String transactionFee = (String)callInfo.getField(Field.TransactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee for this feature is "+ transactionFee);}
			
			configMap.put(DBConstants.TRANSACTIONFEE, transactionFee);
			
			String amount = (String)callInfo.getField(Field.AMOUNT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction amount for this feature is "+ amount);}
			
			configMap.put(DBConstants.TRANSFERAMOUNT, amount);
			
			String serviceProviderCode = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, " The Service Provider code for this feature is "+ serviceProviderCode);}
			
			configMap.put(DBConstants.SERVICEPROVIDERCODE, serviceProviderCode);
			
			String utilityCode = (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, " The utility name for this feature is "+ utilityCode);}
			
			configMap.put(DBConstants.UTILITYNAME, utilityCode);
			
			String validated = Constants.N;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Making the validated flag as "+ validated);}
			
			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			
			configMap.put(DBConstants.VALIDATED, validated);

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.insertOTP(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Insert OTP method result is " + code );}

			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  OTPGenerationImpl. getOTPLogGeneration ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: OTPGenerationImpl. getOTPLogGeneration ()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  OTPGenerationImpl. getOTPLogGeneration ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}

	@Override
	public boolean isMultipleOTPEnabled(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPGenerationImpl.isMultipleOTPEnabled()");}
		boolean finalResult = true;
		String strFinalResult =Constants.EMPTY_STRING;

		try {
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

		
			strFinalResult=(String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableOneOTPForMultiTrans);
			finalResult = Boolean.parseBoolean(strFinalResult);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is Multiple OTP for this feature is enabled ?" + finalResult);}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: OTPGenerationImpl.isMultipleOTPEnabled()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at OTPGenerationImpl.isMultipleOTPEnabled() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

		return finalResult;
	}

	@Override
	public boolean isOTPValidationEnabled(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPGenerationImpl.isOTPValidationDisabled()");}
		boolean finalResult = false;
		String strFinalResult = Constants.EMPTY_STRING;

		try {
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			strFinalResult = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_IsOTPRequire);
			if(util.isNullOrEmpty(strFinalResult)){
				finalResult=false;
			}else{
				finalResult = Boolean.parseBoolean(strFinalResult);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is OTP required for this feature ?" + finalResult);}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: OTPGenerationImpl.isOTPValidationDisabled()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at OTPGenerationImpl.isOTPValidationDisabled() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

		return finalResult;
	}

	public void generateOTP(CallInfo callInfo)throws ServiceException{

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPGenerationImpl. generateOTP()");}

			
			/**
			 * Following changes are done based on Making of OTP Length as configurable
			 */
			
			String otpLength = util.isNullOrEmpty(callInfo.getField(Field.OTPLENGTH)) ? Constants.FOUR : (String)callInfo.getField(Field.OTPLENGTH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The OTP Length is " + otpLength);}
			
			
			long generatedOTPNo = Constants.GL_ZERO;
			String strOTP = Constants.EMPTY;

			if(Constants.THREE.equalsIgnoreCase(otpLength)){
				generatedOTPNo = util.getRandomNumber(100, 999);
				strOTP = Constants.EMPTY_STRING + generatedOTPNo;

				strOTP = strOTP + Constants.OTP_CHECKDIGITS_THREE;
				strOTP = strOTP.trim();

			}else if(Constants.FOUR.equalsIgnoreCase(otpLength)){
				generatedOTPNo = util.getRandomNumber(1000, 9999);
				strOTP = Constants.EMPTY_STRING + generatedOTPNo;

				strOTP = strOTP + Constants.OTP_CHECKDIGITS_FOUR;
				strOTP = strOTP.trim();


			}else if(Constants.FIVE.equalsIgnoreCase(otpLength)){
				generatedOTPNo = util.getRandomNumber(10000, 99999);
				strOTP = Constants.EMPTY_STRING + generatedOTPNo;

				strOTP = strOTP + Constants.OTP_CHECKDIGITS_FIVE;
				strOTP = strOTP.trim();


			}else if(Constants.SIX.equalsIgnoreCase(otpLength)){
				generatedOTPNo = util.getRandomNumber(100000, 999999);
				strOTP = Constants.EMPTY_STRING + generatedOTPNo;

				strOTP = strOTP + Constants.OTP_CHECKDIGITS_SIX;
				strOTP = strOTP.trim();

			}
			//END - Vinoth

			//TODO need to mask the below logging lines
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP Generated successfull");}
//			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "the generated otp is  : " + generatedOTPNo);}
			callInfo.setField(Field.GENERATEDOTP, String.valueOf(generatedOTPNo));
			

			long generatedOTPRefNo = util.getRandomNumber(10000000, 99999999);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP reference number Generated successfull");}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The generated otp reference no is  : " + generatedOTPRefNo);}
			callInfo.setField(Field.GENERATEDOTPREFNO, String.valueOf(generatedOTPRefNo));


		
			/**
			 * Doing triple desk encryption using JCEWrapper code
			 */
			String otpKey = util.isNullOrEmpty(callInfo.getField(Field.OTPKEY)) ? Constants.OTP_KEY : (String)callInfo.getField(Field.OTPKEY);
//			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The OTP key stored is " + otpKey);}

			otpKey = util.convertTo48BitKey(otpKey);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP key has been converted to 48 bits");}

			if(util.isNullOrEmpty(otpKey)){
				throw new ServiceException("otpKey conversion result is null or empty");
			}
			
			JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
			SecretKey sKey = jceWrap.toSecretKey(otpKey);
			String encryptedOTP = jceWrap.encrypt(strOTP, sKey);
//			String encryptedOTP = strOTP;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Encrypted OTP value is "+ encryptedOTP);}
			
			//TODO need to mask the below logging line 
			callInfo.setField(Field.ENCRYPTEDOTP, encryptedOTP);

		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:OTPGenerationImpl. generateOTP()" + e.getMessage());
			throw new ServiceException(e);
		}

	}
		
}
