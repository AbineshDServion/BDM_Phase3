package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.jce.JCEWrapper;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;

public class OTPValidationImpl implements IOTPValidation{
	private static Logger logger = LoggerObject.getLogger();


	@Override
	public String getValidateOTP(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPValidationImpl. getValidateOTP()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			//			HashMap<String, Object> configMap = new HashMap<String, Object>();

			try {
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the ivr_RuleEngingeData values");}
				ICERuleParam ivr_RuleEngingeData = (ICERuleParam) callInfo.getICERuleParam();

				if(util.isNullOrEmpty(ivr_RuleEngingeData)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Rule Engine object is null or empty, returning One");}
					return Constants.ONE;
				}

				String featureName = (String)callInfo.getField(Field.FEATURENAME);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected feature name is :" + featureName);}



				String customerId = Constants.EMPTY_STRING + callInfo.getField(Field.CUSTOMERID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting  Customer ID in the Rule Engine " + customerId);}
				ivr_RuleEngingeData.setIVRParam(Constants.RULE_ENGINE_CUSTOMERID, customerId);


				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting  Feature name in the Rule Engine " + featureName);}
				ivr_RuleEngingeData.setIVRParam(Constants.RULE_ENGINE_FEATURENAME, featureName);
				ivr_RuleEngingeData.updateIVRFields();


				//			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Customer id is :" + customerID);}

				//			configMap.put(DBConstants.FEATURENAME, featureName);
				//			configMap.put(DBConstants.CUSTOMERID, customerID);
				//			configMap.put(DBConstants.VALIDATED, Constants.N);
				//			
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language" + configMap.get(Constants.PREFERRED_LANGUAGE));}
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI is " + configMap.get(Constants.CLI) );}

				//			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
				//code = dataServices.getOTP(logger,sessionId, configMap);
				//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The DB Method response is " + code );}

				List<String> otpList = (List<String>)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_OTP);

				if(!util.isNullOrEmpty(otpList) && otpList.size() > Constants.GL_ZERO){
					String otp = otpList.get(Constants.GL_ZERO);
					//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP Retrieved from the Rule engine is " + otp);}

					if(!util.isNullOrEmpty(otp)){

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success OTP" );}
						code = Constants.ZERO;

						/**
						 * Following flag value is enabled to handle the flow for the OTP for second time after dis conecting the call
						 */

						callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, true);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "setted the flag value ISOTPCALLEDAFTERDISCONNECT as true" );}
						//END - for setting the otp flag value for disconeected call

						//						List otpList = new ArrayList(1);
						//						String OTP = Constants.EMPTY_STRING;
						//						otpList = (ArrayList)ivr_RuleEngingeData.getConfig().getParamValue(DBConstants.OTP);
						//						OTP = !util.isNullOrEmpty(otpList)? (String)otpList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						//						Setting the return code value here
						//						if(!util.isNullOrEmpty(OTP)){
						//							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the OTP validation return code as zero ");}
						//						}

						//Setting the encrypted OTP in the callInfo field
						callInfo.setField(Field.ENCRYPTEDOTP, otp);
						String decryptedOTP = Constants.EMPTY_STRING;
						String otp_Key = (String)callInfo.getField(Field.OTPKEY);

						if(util.isNullOrEmpty(otp_Key) || util.isNullOrEmpty(otp)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP from Rule Engine  / OTP key from jks file is null or empty"+ otp);}
							throw new ServiceException("OTP or OTP key value is null or empty");
						}

						//TODO NEED TO DECRYPT
						otp_Key = util.convertTo48BitKey(otp_Key);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP key has been converted to 48 bits");}
						JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
						SecretKey sKey = jceWrap.toSecretKey(otp_Key);
						decryptedOTP = jceWrap.decrypt(otp, sKey);
						//						decryptedOTP=otp;
						//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Decrypted OTP value is "+ decryptedOTP);}

						//Setting the OTP value in the callInfo field
						
						String otpLength = util.isNullOrEmpty(callInfo.getField(Field.OTPLENGTH)) ? Constants.FOUR : (String)callInfo.getField(Field.OTPLENGTH);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The OTP Length is " + otpLength);}
						
						if(Constants.THREE.equalsIgnoreCase(otpLength)){
							decryptedOTP = decryptedOTP.substring(Constants.GL_ZERO, Constants.GL_THREE);
							
						}else if(Constants.FOUR.equalsIgnoreCase(otpLength)){
							decryptedOTP = decryptedOTP.substring(Constants.GL_ZERO, Constants.GL_FOUR);
							
						}else if(Constants.FIVE.equalsIgnoreCase(otpLength)){
							decryptedOTP = decryptedOTP.substring(Constants.GL_ZERO, Constants.GL_FIVE);
							
						}else if(Constants.SIX.equalsIgnoreCase(otpLength)){
							decryptedOTP = decryptedOTP.substring(Constants.GL_ZERO, Constants.GL_SIX);
							
						}else{
							decryptedOTP = decryptedOTP.substring(Constants.GL_ZERO, Constants.GL_THREE);
						}
						
						//END -  Vinoth
						
						callInfo.setField(Field.GENERATEDOTP, decryptedOTP);

						//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Encrypted OTP is "+ otp);}
						//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Decrypted OTP is "+ decryptedOTP);}

						//						List charityNameList = new ArrayList(1);
						//						String charityType = Constants.EMPTY_STRING;
						//						charityNameList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_CHARITYNAME);
						//						charityType = !util.isNullOrEmpty(charityNameList)? (String)charityNameList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;
						//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The charity type is " + charityType );}

						List charityNameList = new ArrayList(1);
						String charityName = Constants.EMPTY_STRING;
						charityNameList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_CHARITYNAME);
						charityName = !util.isNullOrEmpty(charityNameList)? (String)charityNameList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.SELECTEDCHARITYTYPE, charityName);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Charity Name is "+ charityName);}

						List destAcctNumberList = new ArrayList(1);
						String destNo = Constants.EMPTY_STRING;
						destAcctNumberList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_DESTACCTNO);
						destNo = !util.isNullOrEmpty(destAcctNumberList)? (String)destAcctNumberList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Before substring process the destination number is "+ destNo);}

						/**
						 * Following are the changed done by Vinoth on 19-Aug-2014 for Thrid party remittance
						 */
						String remitanceDestCurr = Constants.CURR_TYPE_INR;
						if(Constants.FEATURENAME_THIRDPARTYREMITTANCE.equalsIgnoreCase(featureName)){
							String[] strArray = destNo.split(",");
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Splitted array list is "+ strArray);}

							if(!util.isNullOrEmpty(strArray) && strArray.length == Constants.GL_TWO){
								destNo = strArray[0];
								remitanceDestCurr = strArray[1];
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Splitted Dest no is "+ destNo);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Splitted Remittance Dest Curr is "+ remitanceDestCurr);}

							}else if(strArray.length == Constants.GL_ONE){
								destNo = strArray[0];
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Splitted Dest no is "+ destNo);}
							}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After substring process the destination number is "+ destNo);}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After substring process the TPR destination curr value is "+ remitanceDestCurr);}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "TRP destination currency value is "+ remitanceDestCurr);}
							callInfo.setField(Field.TPRSELECTEDCURRTYPE, remitanceDestCurr);
						}
						//END Vinoth

						/**
						 * Following are the condition handling for credit card payment internal and within BM flows
						 */
						if(Constants.FEATURENAME_CREDITCARDPAYMENTINTERNAL.equalsIgnoreCase(featureName)
								|| Constants.FEATURENAME_CREDITCARDPAYMENTTHIRDPARTYWITHINBM.equalsIgnoreCase(featureName)){

							String cardEncryptionKey = Constants.CARD_ENCRYPTION_KEY;
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Card decryption key from the card payment decryption");}

							cardEncryptionKey = util.convertTo48BitKey(cardEncryptionKey);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "card decryption key has been converted to 48 bits");}

							//							JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
							SecretKey sKey1 = jceWrap.toSecretKey(cardEncryptionKey);
							destNo = jceWrap.decrypt(destNo, sKey1);
							//							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received the decrypted key for credit card number is "+ destNo);}
						}
						//END

						callInfo.setField(Field.DESTNO, destNo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DestNo is "+ util.maskCardOrAccountNumber(destNo));}

						List exchangeRateList = new ArrayList(1);
						String exchageRate = Constants.EMPTY_STRING;
						exchangeRateList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_EXCHAGERATE);
						exchageRate = !util.isNullOrEmpty(exchangeRateList)? (String)exchangeRateList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.EXCHANGE_RATES_VALUE, exchageRate);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exchage rate value is "+ exchageRate);}

						List srcNoList = new ArrayList(1);
						String srcNo = Constants.EMPTY_STRING;
						srcNoList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_SRCACCTNO);
						srcNo = !util.isNullOrEmpty(srcNoList)? (String)srcNoList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.SRCNO, srcNo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Source No is "+ util.maskCardOrAccountNumber(srcNo));}

						List transactionFeeList = new ArrayList(1);
						String transactionFee = Constants.EMPTY_STRING;
						transactionFeeList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_TRANSACTIONFEE);
						transactionFee = !util.isNullOrEmpty(transactionFeeList)? (String)transactionFeeList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.TransactionFee, transactionFee);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee is "+ transactionFee);}

						List transactionAmountList = new ArrayList(1);
						String transactionAmount = Constants.EMPTY_STRING;
						transactionAmountList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_TRANSFERAMT);
						transactionAmount = !util.isNullOrEmpty(transactionAmountList)? (String)transactionAmountList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.AMOUNT, transactionAmount);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction Amount is "+ transactionAmount);}

						List serviceProviderList = new ArrayList(1);
						String serviceProviderCode = Constants.EMPTY_STRING;
						serviceProviderList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_SERVICEPROVIDERCODE);
						serviceProviderCode = !util.isNullOrEmpty(serviceProviderList)? (String)serviceProviderList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.SELECTEDSERVICEPROVIDER, serviceProviderCode);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility Name  / serivce provider code is "+ serviceProviderCode);}

						List utilityCodeList = new ArrayList(1);
						String utilityCode = Constants.EMPTY_STRING;
						utilityCodeList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_UTILITYNAME);
						utilityCode = !util.isNullOrEmpty(utilityCodeList)? (String)utilityCodeList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.UTILITYCODE, utilityCode);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility Code  / Name provider code is "+ utilityCode);}

						List otpDateTimeList = new ArrayList(1);
						String otpDateTime = Constants.EMPTY_STRING;
						otpDateTimeList = (ArrayList)ivr_RuleEngingeData.getParam(Constants.RULE_ENGINE_OTPDATETIME);
						otpDateTime = !util.isNullOrEmpty(otpDateTimeList)? (String)otpDateTimeList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;

						callInfo.setField(Field.OTPDATETIME, otpDateTime);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OTP Generated Date time is "+ otpDateTime);}


					}else{

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Since OTP is null or empty returning 1");}
						return Constants.ONE;
					}

				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Since retrieving the OTP from the rule engine is null or empty, returning the value 1 to IVR");}
					return Constants.ONE;
				}

			} catch (Exception e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: OTPValidationImpl.getValidateOTP()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: OTPValidationImpl. getValidateOTP ()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: OTPValidationImpl. getValidateOTP ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}

	@Override
	public String updateOTPValidationFlag(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: OTPValidationImpl.updateOTPValidationFlag()");}
		try {

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			//			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);

			String currentDate = util.isNullOrEmpty(callInfo.getField(Field.OTPDATETIME))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.OTPDATETIME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is "+ currentDate);}
			configMap.put(DBConstants.DATETIME, currentDate);

			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID is "+ callInfo.getField(Field.CUSTOMERID));}

			String featureName = util.isNullOrEmpty(callInfo.getField(Field.FEATURENAME))? Constants.EMPTY : (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}
			configMap.put(DBConstants.FEATURENAME, featureName);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Validated falg "+ Constants.Y);}
			configMap.put(DBConstants.VALIDATED, Constants.Y);

			/**
			 * Following are the fixes done by Vinoth on 13-Aug-2014 for updating OTP flag at Back end
			 */
			String encryptedOTP = util.isNullOrEmpty(callInfo.getField(Field.ENCRYPTEDOTP))? Constants.NULL: (String)callInfo.getField(Field.ENCRYPTEDOTP);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ENcrypted OTP value is "+ callInfo.getField(Field.ENCRYPTEDOTP));}		
			configMap.put(DBConstants.OTP, encryptedOTP);
			//END Vinoth

			String uui = util.isNullOrEmpty(callInfo.getField(Field.UUI))? Constants.EMPTY : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updateOTP(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Insert OTP method result is " + code );}

			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  OTPGenerationImpl. updateOTPValidationFlag ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: OTPGenerationImpl. updateOTPValidationFlag ()");}

			return code;
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at OTPGenerationImpl.isOTPValidationDisabled() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
	}

}
