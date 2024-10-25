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
import com.servion.context.Context;
import com.servion.dao.CCPaymentDAO;
import com.servion.dao.CCtxnPostRqDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.creditCardPayment.UpdCCPaymentDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class CardPaymentConfirmationImpl implements ICardPaymentConfirmation {

	private static Logger logger = LoggerObject.getLogger();

	private CCPaymentDAO ccPaymentDAO;
	private CCtxnPostRqDAO cctxnPostRqDAO;

	public CCPaymentDAO getCcPaymentDAO() {
		return ccPaymentDAO;
	}

	public void setCcPaymentDAO(CCPaymentDAO ccPaymentDAO) {
		this.ccPaymentDAO = ccPaymentDAO;
	}

	public CCtxnPostRqDAO getCctxnPostRqDAO() {
		return cctxnPostRqDAO;
	}

	public void setCctxnPostRqDAO(CCtxnPostRqDAO cctxnPostRqDAO) {
		this.cctxnPostRqDAO = cctxnPostRqDAO;
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
	public String getCCPConfirmationPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentConfirmationImpl.getCCPConfirmationPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data

			String amount = (String)callInfo.getField(Field.AMOUNT);	
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount entered was "+ amount);}

			String featureName = (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}

			String lastNNumDigit = (String)callInfo.getField(Field.LastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last N Num digit is "+ lastNNumDigit);}

			int int_LastNNumDigit = lastNNumDigit!=null? Integer.parseInt(lastNNumDigit): Constants.GL_FOUR;

			String destNo = (String) callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Destination number ending with "+ util.getSubstring(destNo, int_LastNNumDigit));}

			String srcNo = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Source number ending with is "+ util.getSubstring(srcNo, int_LastNNumDigit));}
			srcNo = util.getSubstring(srcNo, int_LastNNumDigit);

			
			
			/**
			 * Getting the customer category type from the account detail map object
			 */
			String customerCategoryType = Constants.DEFAULT;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getCallerIdentification_HostRes is not empty or null ");}
				
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail Map object is null or empty");}
					
					HashMap<String, AcctInfo>acctDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
					if(!util.isNullOrEmpty((String)callInfo.getField(Field.SRCNO)) && !util.isNullOrEmpty(acctDetailMap.get((String)callInfo.getField(Field.SRCNO)))){
						
						customerCategoryType = acctDetailMap.get((String)callInfo.getField(Field.SRCNO)).getCategory();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Category type is "+ customerCategoryType);}
						
						if(util.isNullOrEmpty(customerCategoryType) || util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType))){
							customerCategoryType = Constants.DEFAULT;
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Category type is resetted to Default"+ customerCategoryType);}	
						}
					}
				}
				
			}
			
			
			String transactionFee = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType))?
					Constants.ZERO : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType);
			
			//String transactionFee =  (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee is "+ transactionFee);}
			callInfo.setField(Field.TransactionFee, transactionFee);
			
			double double_TransFee = util.isNullOrEmpty(transactionFee)?Constants.GL_ZERO : Double.parseDouble(transactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee converted to double  is "+double_TransFee);}
	
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(amount);

			if(Constants.FEATURENAME_CREDITCARDPAYMENTINTERNAL.equalsIgnoreCase(featureName)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Called from Credit card payment internal ");}
				dynamicValueArray.add(DynaPhraseConstants.Lost_Card_1003);
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_ENDING_WITH);
				destNo = util.getSubstring(destNo, int_LastNNumDigit);

			}else if(Constants.FEATURENAME_CREDITCARDPAYMENTTHIRDPARTYWITHINBM.equalsIgnoreCase(featureName)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "credit card payment third party within BM");}
				dynamicValueArray.add(DynaPhraseConstants.Card_Payment_1008);
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
			}

			dynamicValueArray.add(destNo);

			dynamicValueArray.add(srcNo);

			if(!util.isNullOrEmpty(double_TransFee) && double_TransFee > Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Will Announce the transactino fee announcement");}
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_CHARGING_TRANSFEE);
				dynamicValueArray.add(transactionFee);
			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CREDITCARD_PAYMENT_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Credit_Card_Payment_Confirmation");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			if(!util.isNullOrEmpty(double_TransFee) && double_TransFee > Constants.GL_ZERO){
				combinedKey = combinedKey +Constants.UNDERSCORE+ Constants.ALPHA_A;
			}

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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentConfirmationImpl.getCCPConfirmationPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at CardPaymentConfirmationImpl.getCCPConfirmationPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String updateCCPayment(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentConfirmationImpl.updateCCPayment()");}
		String code = Constants.EMPTY_STRING;
		//getConfigurationParam(callInfo);

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			

			/**
			 * Checking the condition for calling list beneficiary and beneficiary details host access 
			 */
			boolean isOTPCalledAfterDisconnected = util.isNullOrEmpty(callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT)) ? false : (boolean)callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is OTP Called After Disconnected " +isOTPCalledAfterDisconnected );}
			if(isOTPCalledAfterDisconnected){
				String returnCode = Constants.ONE;
				if(Constants.FEATURENAME_CREDITCARDPAYMENTTHIRDPARTYWITHINBM.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
					if(!util.isNullOrEmpty(Context.getiCardPaymentThirdPartyWithinBM())){
						returnCode = Context.getiCardPaymentThirdPartyWithinBM().getCCPWithinBMBeneficiaryList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service of CCP ThirdParty Within BM is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiCardPaymentThirdPartyWithinBM().getCCPPayeeDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details  CCP ThirdParty Within BM web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access of  CCP ThirdParty Within BM");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiCardPaymentThirdPartyWithinBM() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}
				}
			}

			String debitAcctID  = (String)callInfo.getField(Field.SRCNO);
			if(!util.isNullOrEmpty(debitAcctID)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected source Debit account id ending with : " + util.getSubstring(debitAcctID, Constants.GL_FOUR));}
			}

			String creditCardNum  = (String)callInfo.getField(Field.DESTNO);
			if(!util.isNullOrEmpty(creditCardNum)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected destination creditcard number ending with :" + util.getSubstring(creditCardNum, Constants.GL_FOUR));}
			}


			String amount  = (String)callInfo.getField(Field.AMOUNT);
//			double double_Amt = util.isNullOrEmpty(amount)? Constants.GL_ZERO:Double.parseDouble(amount);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The credit card payment amount is :"+ amount);}


			/**
			 * Note : as per host specification document 9.3 credit card account number is conditional,  If we have that credit card account number then we can pass,
			 * so that the amount will be transfer to that credit card account directly
			 */
			String creditCardAcctNo = null;
			CardAcctDtl cardAcctDtl = null;
			/**
			 * Note : if the ESB team want to send the Credit card account number means kindly uncomments the following lines
			 */

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got the CallerIdentification host response object is " + callInfo.getCallerIdentification_HostRes());}
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The card Account Object retrieved is " + cardAcctDtl);}
					cardAcctDtl = callInfo.getCallerIdentification_HostRes().getCardDetailMap().get(creditCardNum);
					if(!util.isNullOrEmpty(cardAcctDtl)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The card Account Object retrieved is " + cardAcctDtl);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card account number is " + cardAcctDtl.getCardAccountNumber());}
						creditCardAcctNo = cardAcctDtl.getCardAccountNumber();
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The credit card account number from the calleridentification host access is " + util.maskCardOrAccountNumber(creditCardAcctNo));}
				}
			}

			String beneficiaryRegCode  = (String) callInfo.getField(Field.CCPBENEFICIARYID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "For third Party Credit card Payment the selected Beneficiary id is " + beneficiaryRegCode);}

			ICEFeatureData iceFeatureData = (ICEFeatureData)callInfo.getICEFeatureData();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ICE Feature Data object is " + iceFeatureData);}
		
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("ICE Feature Data object is null or empty");
			}
			
			String requestType = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_REQUEST_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CC Payment Request type is " + requestType);}
		
			
			String reasonCode  = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_REASON_CODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CC Payment Reason type is " + reasonCode);}
			
			String text  = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_TEXT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CC Payment Reason type is " + text);}
			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

			String strHostInParam = Constants.NA;
			try{
			strHostInParam =	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
//					+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.UTILITYCODE);
			}catch(Exception e){}
			
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCPAYMENT);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_CREDITCARDS);
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
			
			/**
			 * Following are the changes advised by Sastry to send
			 */
			

			
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetched the Feature Object values " + ivr_ICEFeatureData);}
			
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String currencyType = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_CURRENCYTYPE))?
					Constants.EMPTY_STRING : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_CURRENCYTYPE);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured currency type is " + currencyType);}
			
			
			String postTo = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_POSTTO))?
					Constants.EMPTY_STRING : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_POSTTO);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Post To value is " + postTo);}
			
			
			String transPostType = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_TRANSPOSTTYPE))?
					Constants.EMPTY_STRING : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_TRANSPOSTTYPE);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Post Type value is " + transPostType);}

			
			String paymentReference = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_PAYMENTREFERENCE))?
					Constants.EMPTY_STRING : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_PAYMENTREFERENCE);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Payment Referenc value is " + paymentReference);}
			
			
			String utilityCode = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_UTILITYCODE))?
					Constants.EMPTY_STRING : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_UTILITYCODE);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured utilityCode value is " + utilityCode);}
					
			String serviceProviderCode = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_SERVICEPROVIDECODE))?
					Constants.EMPTY_STRING : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_SERVICEPROVIDECODE);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured serviceProviderCode value is " + serviceProviderCode);}

			UpdCCPaymentDetails_HostRes updCCPaymentDetails_HostRes = ccPaymentDAO.getCCPaymentUpdHostRes(callInfo, debitAcctID, beneficiaryRegCode, 
					creditCardNum, amount, creditCardAcctNo, requestType,reasonCode,text, currencyType, postTo, transPostType, paymentReference,utilityCode, serviceProviderCode);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updCCPaymentDetails_HostRes Object is :"+ updCCPaymentDetails_HostRes);}
			callInfo.setUpdCCPaymentDetails_HostRes(updCCPaymentDetails_HostRes);

			code = updCCPaymentDetails_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = updCCPaymentDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = updCCPaymentDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			/**
			 * Updating the isOTPCalledAfterDisconnect flag as false
			 */
			callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, false);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updating Is OTP Called After Disconnected " +false );}
			//END
			
			
			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam =	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/			
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode+ hostResCode+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_TRANSREFNO +  Constants.EQUALTO+updCCPaymentDetails_HostRes.getDebitRefNo()
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(updCCPaymentDetails_HostRes.getErrorDesc()) ?"NA" :updCCPaymentDetails_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				//code = Constants.WS_FAILURE_CODE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for CardPaymentConfirmationImpl.updateCCPayment");}
				
				
				/**
				 * Following are handled for updating the enter amount after getting success from host
				 */
				try{
				IEnterAmount iEnterAmount = Context.getiEnterAmount();
				String result = iEnterAmount.UpdateEnteredAmount(callInfo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The result of updating the entered amount result is "+ result);}
				}catch(Exception e){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exception occured in while updating the entered amount result is "+ e);}
				}
				//End
				//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call CardPaymentConfirmationImpl.updateCCPayment  CCtxnPostRq service");}
				/*	*//**
				 * Since we are using payment for Credit cards, following request fields are filled with the credit card's cerendials
				 *//*
				String reference = Constants.HOST_CCTXNPOST_REFERENCE_TYPE_CARD;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting Reference type is "+ reference);}

				String postTo = Constants.HOST_CCTXNPOST_POSTTO_TYPE_CARD;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requesting postTo type is "+postTo);}

				String acctId = (String)callInfo.getField(Field.SRCNO);
				if(!util.isNullOrEmpty(acctId)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account number from which we are making payment is " + util.getSubstring(acctId, Constants.GL_FOUR));}
				}
				String cardNo = (String)callInfo.getField(Field.DESTNO);
				if(!util.isNullOrEmpty(cardNo)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit Card number for which we are making payment is " + util.getSubstring(cardNo, Constants.GL_FOUR));}
				}

				XMLGregorianCalendar trxnPostingDate = util.getXMLGregorianCalendarNow();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Thr TrxnPosting Date is " + trxnPostingDate);}

				XMLGregorianCalendar trxnDate =  util.getXMLGregorianCalendarNow();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Thr TrxnDate is "+ trxnDate);}

				String str_OrigCcyAmt = (String)callInfo.getField(Field.AMOUNT);
				BigDecimal origCcyAmt =  util.isNullOrEmpty(str_OrigCcyAmt)?new BigDecimal(Constants.GL_ZERO):new BigDecimal(str_OrigCcyAmt);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Amount to be transfer is " + origCcyAmt);}


				String origCcyCode = "Default";
				if(!util.isNullOrEmpty(acctId)){
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
						if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
							AcctInfo acctInfo = callInfo.getCallerIdentification_HostRes().getAccountDetailMap().get(acctId);
							origCcyCode = acctInfo.getAcctCurr();
						}
					}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Currency of the Account is " + origCcyCode);}

				String description = Constants.HOST_CCTXNPOST_REQUEST_DESCRIPTION;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requsting Description for credit Card paymentis " + description);}


				  *//**
				  * For Reporting Purpose
				  *//*
				HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

				String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
				hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
				String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CARDNUMBER + Constants.EQUALTO + cardNo
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + acctId
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
				hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_CCTRANSACTIONPOST);
				//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
				hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
				hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
				hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
				hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

				hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
				hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_CREDITCARDS);
				//End Reporting


				UpdateCreditCardPaymenTxnPosttDetails_HostRes updateCreditCardPaymenTxnPosttDetails_HostRes = cctxnPostRqDAO.getCCPaymentUpdateHostRes(callInfo, reference, postTo, acctId, cardNo, 
						trxnPostingDate, trxnDate, origCcyAmt, origCcyCode, description);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updateCreditCardPaymenTxnPosttDetails_HostRes Object is :"+ updateCreditCardPaymenTxnPosttDetails_HostRes);}
				callInfo.setUpdateCreditCardPaymenTxnPosttDetails_HostRes(updateCreditCardPaymenTxnPosttDetails_HostRes);

				code = updateCreditCardPaymenTxnPosttDetails_HostRes.getErrorCode();


				   * For Reporting Start

				String hostEndTimeForSecHost = updateCreditCardPaymenTxnPosttDetails_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
				hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

				String hostResCodeForSecHost = updateCreditCardPaymenTxnPosttDetails_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
				hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

				String responseDescForSecHost = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCodeForSecHost;

				hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

				callInfo.setHostReportDetails(hostReportDetailsForSecHost);
				IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.insertHostDetails(ivrdataForSecHost);
				//End Reporting

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for CCTransactionPost service");}
				}
				else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for CCTxnPostDetails host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updateCreditCardPaymenTxnPosttDetails_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCTRANSACTIONPOST, updateCreditCardPaymenTxnPosttDetails_HostRes.getHostResponseCode());
				   *//**
				   * Following will be called only if there occured account selection before this host access
				   *//*
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo);
				}*/
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for CCPayment host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updCCPaymentDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCPAYMENT, updCCPaymentDetails_HostRes.getHostResponseCode());
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentConfirmationImpl.updateCCPayment() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;	

	}

	@Override
	public String getCCPSuccessAnnouncement(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentConfirmationImpl.getCCPSuccessAnnouncement()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			String transRefID = Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature name is " + (String)callInfo.getField(Field.FEATURENAME));}

			if(!util.isNullOrEmpty(callInfo.getUpdCCPaymentDetails_HostRes())){
				transRefID = callInfo.getUpdCCPaymentDetails_HostRes().getDebitRefNo();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Transfer ID is " + transRefID);}
			//Setting this reference id in the callinfo
			callInfo.setField(Field.Transaction_Ref_No, transRefID);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			//Need to handle the Dynamic phrase list and Mannual Grammar portions

			dynamicValueArray.add(DynaPhraseConstants.PHRASE_TRANS_REF_NO);	
			dynamicValueArray.add(transRefID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Transaction_Success_Message");
			String featureID = FeatureIDMap.getFeatureID("Credit_Card_Payment_Confirmation");
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

			int totalPrompt = 0;
			if(!util.isNullOrEmpty(transRefID)){
				totalPrompt = util.getTotalPromptCount(str_GetMessage);
			}else{
				totalPrompt =Constants.GL_ONE;
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file

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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FundsTransferConfirmationImpl.getFTSuccessAnnouncement()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FundsTransferConfirmationImpl.getFTSuccessAnnouncement() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
