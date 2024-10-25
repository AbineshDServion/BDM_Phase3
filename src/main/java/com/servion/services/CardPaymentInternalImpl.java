package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.ibm.icu.text.DecimalFormat;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CCEntityInquiryDAO;
import com.servion.dao.CCGroupInquiryDAO;
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
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.creditCardBalance.CreditCardGroupInq_HostRes;
import com.servion.model.creditCardPayment.CCP_CCEntityFields;
import com.servion.model.creditCardPayment.CreditCardDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class CardPaymentInternalImpl implements ICardPaymentInternal {

	private static Logger logger = LoggerObject.getLogger();


	private CCEntityInquiryDAO ccEntityInquiryDAO;
	private CCGroupInquiryDAO ccGroupInquiryDAO; 

	public CCEntityInquiryDAO getCcEntityInquiryDAO() {
		return ccEntityInquiryDAO;
	}

	public void setCcEntityInquiryDAO(CCEntityInquiryDAO ccEntityInquiryDAO) {
		this.ccEntityInquiryDAO = ccEntityInquiryDAO;
	}
	
	public CCGroupInquiryDAO getCcGroupInquiryDAO() {
		return ccGroupInquiryDAO;
	}

	public void setCcGroupInquiryDAO(CCGroupInquiryDAO ccGroupInquiryDAO) {
		this.ccGroupInquiryDAO = ccGroupInquiryDAO;
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
	public String getCCPAmtMenuPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentInternalImpl.getCCPAmtMenuPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			ArrayList<String> ccpInternalPaymentTypeList = null;
			ccpInternalPaymentTypeList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPInternalPaymentType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The CCP Internal Payment Type List retrieved is :" + ccpInternalPaymentTypeList);}


			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			/**
			 * Note temp_Str is nothing but the product name.  The wave file also should recorded in the same product name
			 * 
			 * eg FULL --> Savings.wav
			 * 
			 */
			String creditCardNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Credit card number is " + util.maskCardOrAccountNumber(creditCardNumber));}

			if(util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes()) || util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Caller Identification host response bean object is null or empty");}
				throw new ServiceException("Caller Identification host response bean object is null or empty");
			}

			CreditCardDetails_HostRes creditCardDetails_HostRes = (CreditCardDetails_HostRes)callInfo.getCreditCardDetails_HostRes();

			HashMap<String,CardAcctDtl> cardAcctDtlMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
			String acctNumber = cardAcctDtlMap.get(creditCardNumber).getCardAccountNumber();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit card's account number is "  +util.maskCardOrAccountNumber(acctNumber));}



			if(util.isNullOrEmpty(callInfo.getCreditCardDetails_HostRes())
					|| util.isNullOrEmpty(creditCardNumber)
					|| util.isNullOrEmpty(callInfo.getCreditCardDetails_HostRes().getAcctNo_AccountDetailMap())
					|| util.isNullOrEmpty(callInfo.getCreditCardDetails_HostRes().getAcctNo_AccountDetailMap().get(acctNumber))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardDetails_HostRes Object of CCEntity service is null / empty");}
				throw new ServiceException("CreditCardDetails_HostRes Object is null or empty");
			}


			/**
			 * 
			 */

			CCP_CCEntityFields ccEntityFields = (CCP_CCEntityFields)creditCardDetails_HostRes.getAcctNo_AccountDetailMap().get(acctNumber);

			String fullDueAmt = Constants.EMPTY_STRING;
			String miniDueAmt = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(ccEntityFields)){
				//Changed as on 23-08-2015
				/***/
				//fullDueAmt = ccEntityFields.getOverDueAmt();
				fullDueAmt = ccEntityFields.getBalance();
				/***/
				miniDueAmt = ccEntityFields.getStmtMinDue();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Full Due amt & Minimum due (Stmt mini due) amount is "+fullDueAmt+","+miniDueAmt);}


			double int_fullDue = util.isNullOrEmpty(fullDueAmt)?Constants.GL_ZERO : Double.parseDouble(fullDueAmt);
			double int_miniDueAmt = util.isNullOrEmpty(miniDueAmt)?Constants.GL_ZERO : Double.parseDouble(miniDueAmt);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Converted fulldue and minidue to double variables " + int_fullDue + int_miniDueAmt);}

			int temp_MoreCount = int_moreCount - 1;
			int temp_count = Constants.GL_ZERO;

			for(int count=Constants.GL_ZERO;count<ccpInternalPaymentTypeList.size();count++){
				temp_Str = ccpInternalPaymentTypeList.get(count);
				//Changed on 17-08-2015
				//if(Constants.CCP_PAYMENTTYPE_FULL_DUE.equalsIgnoreCase(temp_Str) && int_fullDue <= Constants.GL_ZERO){
				if(Constants.CCP_PAYMENTTYPE_FULL_DUE.equalsIgnoreCase(temp_Str) && int_fullDue < Constants.GL_ZERO){
					dynamicValueArray.add(DynaPhraseConstants.Card_Payment_1001);
					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					temp_count++;
					//Changed on 17-08-2015
				//}else if(Constants.CCP_PAYMENTTYPE_MINI_DUE.equalsIgnoreCase(temp_Str) && int_miniDueAmt <= Constants.GL_ZERO){
				}else if(Constants.CCP_PAYMENTTYPE_MINI_DUE.equalsIgnoreCase(temp_Str) && int_miniDueAmt < Constants.GL_ZERO){
					dynamicValueArray.add(DynaPhraseConstants.Card_Payment_1002);

					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					temp_count++;
				}else if(Constants.CCP_PAYMENTTYPE_OTHER_AMT.equalsIgnoreCase(temp_Str)){
					dynamicValueArray.add(DynaPhraseConstants.Card_Payment_1003);

					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					temp_count++;
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added CCP Internal Payment type "+temp_Str);}

				//				if(util.isNullOrEmpty(grammar)){
				//					grammar = temp_Str;
				//				}else{
				//					grammar = grammar + Constants.COMMA + temp_Str;
				//				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CREDITCARD_PAYMENT_DUE_AMOUNT");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Credit_Card_Payment_Internal");
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
			if(temp_count > Constants.GL_ZERO){
				totalPrompt = Constants.GL_TWO * temp_count;
			}

			//To have the property file grammar, need to call that util method here
			//No Need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe seperator sign
			//No Need

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentInternalImpl.getCCPAmtMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentInternalImpl.getCCPAmtMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getCCPGroupInquiry(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER:  CardPaymentInternalImpl.getCCPGroupInquiry()");}
		String code = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			//			String SelectedCardOrAcctNo = (String)callInfo.getField(Field.DESTNO);
			//			if(util.isNullOrEmpty(SelectedCardOrAcctNo)){
			//				throw new ServiceException("Selected Card OR Acct No is empty or null");
			//			}
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting selected card or acct no is" + util.getSubstring(SelectedCardOrAcctNo, Constants.GL_FOUR));}
			//			
			//			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			//			ArrayList<String> customerIDList = new ArrayList<String>();
			//			customerIDList.add(customerID);
			//			
			//			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData)callInfo.getICEFeatureData();
			//			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
			//				throw new ServiceException("ivr_ICEFeatureData object is null");
			//			}
			////			
			//			String entyityInquiryType = Constants.EMPTY_STRING;
			//			entyityInquiryType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CREDITCARD_ENTITYINQ_TYPE);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type from the entityInquiryType " + entyityInquiryType);}
			//			
			//			HashMap<String, CardAcctDtl> cardDetailMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
			//			if(cardDetailMap!=null && cardDetailMap.containsKey(SelectedCardOrAcctNo)){
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type is Card");}
			//				entyityInquiryType = Constants.HOST_REQUEST_ENTITYINQTYPE_CARD;
			//			}else{
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type is Customer");}
			//				entyityInquiryType = Constants.HOST_REQUEST_ENTITYINQTYPE_CUSTOMER;
			//			}
			//			
			/***
			 * 
			 */

			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("iceGlobalConfig object is null");
			}

			ArrayList<String> creditCardList = new ArrayList<>();
			String creditCardNum = (String)callInfo.getField(Field.DESTNO);
			if(util.isNullOrEmpty(creditCardNum)){
				throw new ServiceException("Selected Credit Card Number is empty or null");
			}
			
			
			
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes()) && !util.isNullOrEmpty(creditCardNum)){
				CardAcctDtl cardAcctDtl = null;
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
					HashMap<String, CardAcctDtl>cardDetailsMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained the Card Detail Map object from caller identification");}
				
				if(cardDetailsMap.containsKey(creditCardNum)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The CardDetailsMap object contains the selected credit card number " + util.maskCardOrAccountNumber(creditCardNum));}
					cardAcctDtl = cardDetailsMap.get(creditCardNum);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The obtained cardAcctDtls object is " + cardAcctDtl);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The account number for the selected credit card number is " + util.maskCardOrAccountNumber(cardAcctDtl.getCardAccountNumber()));}
				
					creditCardList.add(cardAcctDtl.getCardAccountNumber());
				
				}
					
				}
			}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Credit Card no  is" + util.getSubstring(creditCardNum, Constants.GL_FOUR));}

			String entyityInquiryType = Constants.EMPTY_STRING;
			entyityInquiryType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CREDITCARD_ENTITYINQ_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type from the entityInquiryType " + entyityInquiryType);}


			String inquiryReference = Constants.EMPTY_STRING;
			inquiryReference = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CREDITCARD_INQUIRYREFERENCE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The inquiryReference is " + inquiryReference);}

			ArrayList<String>numberList = new ArrayList<>();

			if(!util.isNullOrEmpty(creditCardList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit card list size is "+ creditCardList.size());}
				numberList.addAll(creditCardList);
			}

			String returnReplacedCards = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_RETURNED_REPLACED_CARD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Returned Replacec card value from the Configurator is " +returnReplacedCards );}		


			String entitySize = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_CCENTITY_ENQUIRY_SIZE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Return Value of entity enquiry size is " +entitySize );}


			/***
			 * 
			 */
			//			ArrayList<String> creditCardNumList = new ArrayList<String>();
			//			creditCardNumList.add(SelectedCardOrAcctNo);
			//			
			//			
			//			ArrayList<String>cardAccountNumList = new ArrayList<String>();
			//			String cardAccountNum = null;
			//			
			//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
			//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
			//					//TODO - CC
			//					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getFirstCCAccountNo())){
			//						cardAccountNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getFirstCCAccountNo();
			//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Account Number is" + util.getSubstring(cardAccountNum, Constants.GL_FOUR));}
			//						
			//					}
			//				}
			//			}
			//			cardAccountNumList.add(cardAccountNum);
			//			
			/**
			 * Setting the InteralCustomerID and nationalID as null value
			 */

			//			String inquiryReference = Constants.EMPTY_STRING;
			//			inquiryReference = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CREDITCARD_INQUIRYREFERENCE);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The inquiryReference is " + inquiryReference);}
			//			
			//			
			//			ArrayList<String>numberList = new ArrayList<>();
			//			
			//			if(!util.isNullOrEmpty(creditCardNumList)){
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit card list is "+ creditCardNumList);}
			//				numberList.addAll(creditCardNumList);
			////				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as C");}
			////				inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_C;
			//			}else if(!util.isNullOrEmpty(cardAccountNum)){
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card account number list is "+ cardAccountNum);}
			//				numberList.addAll(cardAccountNumList);
			////				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as A");}
			////				inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_A;
			//			}else{
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID list is "+ customerIDList);}
			//				numberList.addAll(customerIDList);
			////				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as U");}
			////				inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_U;
			//			}

			/**
			 * END
			 */



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

			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_CARDNUMBER + Constants.EQUALTO + creditCardNum
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCENTITYINQUIRY);
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

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_REQUEST_TYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCPAYMENT_REQUEST_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			//As per the updated wsdl we need to pass the following value to the host

			CreditCardDetails_HostRes creditCardDetails_HostRes = ccEntityInquiryDAO.getCCPaymentIntraCardDetailHostRes(callInfo, entyityInquiryType, inquiryReference, numberList, returnReplacedCards, entitySize, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "creditCardDetails_HostRes Object is :"+ creditCardDetails_HostRes);}
			callInfo.setCreditCardDetails_HostRes(creditCardDetails_HostRes);

			code = creditCardDetails_HostRes.getErrorCode();
			/*
			 * For Reporting Start
			 */

			String hostEndTime = creditCardDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = creditCardDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_CARDNUMBER + Constants.EQUALTO + creditCardNum
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(creditCardDetails_HostRes.getErrorDesc()) ?"NA" :creditCardDetails_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for CCEntityInquiry host service");}
				//Removed this condition on 18-03-2015
				/*if(util.isNullOrEmpty(callInfo.getCreditCardGroupInq_HostRes()) 
						&& util.isNullOrEmpty(callInfo.getCreditCardGroupInq_HostRes().getCreditCardGrpInfoDetailMap())
						){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardGroupInq_HostRes Object  is null / empty");}
					String cardBalanceCode = "";
					CardBalanceImpl cardBalance = new CardBalanceImpl();
					cardBalanceCode = cardBalance.getCreditCardBalance(callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardGroupInq_HostRes cardBalanceCode value is:"+cardBalanceCode);}
					if(!(cardBalanceCode.equalsIgnoreCase(Constants.ZERO))){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardGroupInq_HostRes cardBalanceCode value not equal to zero");}
						throw new ServiceException("CreditCardGroupInq_HostRes:cardBalanceCode not equal to zero");
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardGroupInq_HostRes Object  is not null / empty");}
				}*/
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, " Calling  ccGroupInquiryDAO.getCCAvailableBalanceHostRes host service");}
				/***Changed on 18-03-2015 based on kaarthik/Faisal Comment***/
				
				/**
				 * For Reporting Purpose
				 */
				hostReportDetails = new HostReportDetails();

				featureId = (String)callInfo.getField(Field.FEATUREID);
				hostReportDetails.setHostActiveMenu(featureId);
				//hostReportDetails.setHostCounter(hostCounter);
				//hostReportDetails.setHostEndTime(hostEndTime);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
				
				/*String cardEmbossNum = null;
				cardEmbossNum = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Selected Credit card number is " + util.maskCardOrAccountNumber(cardEmbossNum));}*/
				
				String creditCardNumber = (String)callInfo.getField(Field.DESTNO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Selected/Requested Credit card number is " + util.maskCardOrAccountNumber(creditCardNumber));}
				
				/**
				 * Following are the field are mapped as per the new WSDL V2.13
				 */
				String reference = Constants.EMPTY_STRING;
				String extraOption = Constants.EMPTY_STRING;
				String manNoAuth = Constants.EMPTY_STRING;

				//As this is yet to be confirmed by the host team,  assigning value 
				reference = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_REFERENCETYPE);
				extraOption = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_EXTRAOPTION);
				manNoAuth = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_MAXNOAUTH);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The reference value for CCBalance is " + reference);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The extraOption value for CCBalance is " + extraOption);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The maxNoAuth value for CCBalance is " + manNoAuth);}
				
				
				strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_CARDEMBOSSNUMBER + Constants.EQUALTO + creditCardNumber
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(creditCardNumber)
				
				
//				Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + cardEmbossNum
//				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + callInfo.getField(Field.SRCTYPE)
//				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
//				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime ;
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCGROUPINQUIRY);
				//hostReportDetails.setHostOutParams(hostOutParams);
				hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
				
				
				String startTime = util.getCurrentDateTime();
				hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			
				hostReportDetails.setHostStartTime(startTime); //It should be in the format of 31/07/2013 18:11:11
				hostReportDetails.setHostType(Constants.HOST_TYPE_DEBITCARDS);
				//End Reporting

				
				/*
				 *  Setting NA values
				 */
				hostReportDetails.setHostEndTime(Constants.NA);
				hostReportDetails.setHostOutParams(Constants.NA);
				hostReportDetails.setHostResponse(Constants.NA);
				
				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
				callInfo.insertHostDetails(ivrdata);
				
				/* END */

				iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				
				if(util.isNullOrEmpty(iceFeatureData)){
					throw new ServiceException("iceFeatureData object is null or empty");
				}
				
				requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CCGROUPDETAILSINQUIRY_REQUESTTYPE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCGROUPDETAILSINQUIRY_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

				
				CreditCardGroupInq_HostRes creditCardGroupInq_HostRes = ccGroupInquiryDAO.getCCAvailableBalanceHostRes(callInfo, creditCardNumber, reference, extraOption, manNoAuth, requestType);


				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "creditCardGroupInq_HostRes Object is :"+ creditCardGroupInq_HostRes);}
				callInfo.setCreditCardGroupInq_HostRes(creditCardGroupInq_HostRes);

				code = creditCardGroupInq_HostRes.getErrorCode();

				/*
				 * For Reporting Start
				 */
				
				hostEndTime = creditCardGroupInq_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}


				strHostInParam = 	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(creditCardNumber)
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);
						
				
				hostResCode = creditCardGroupInq_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);

				responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
		
				hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(creditCardGroupInq_HostRes.getErrorDesc()) ?"NA" :creditCardGroupInq_HostRes.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				//End Reporting
				
				/************************************************************/
				
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for CardGroupInquiry Service");}
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Credit Group Inquiry host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + creditCardGroupInq_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCGROUPINQUIRY, creditCardGroupInq_HostRes.getHostResponseCode());
					
					
					//Need to check with vinod 18-03-2015
					/**
					 * Following will be called only if there occured account selection before this host access
					 *//*
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo,hostResCode);*/
				}
				
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for CCEntityInquiry host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + creditCardDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCENTITYINQUIRY, creditCardDetails_HostRes.getHostResponseCode());
			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT:  CardPaymentInternalImpl.getCCPGroupInquiry()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentInternalImpl.getCCPGroupInquiry "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getCCPInternalDueAmtPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentInternalImpl.getCCPInternalDueAmtPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			ArrayList<String>CCPDueAmtFieldsAndOrder = new ArrayList<String>();

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPInternalBalanceFieldsAndOrder))){
				CCPDueAmtFieldsAndOrder = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCPInternalBalanceFieldsAndOrder);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Bank Balance Due fields and Order in the a local variable");}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CCP  Due Fields and Order size is" + CCPDueAmtFieldsAndOrder.size());}
				throw new ServiceException("CCP Due amount fields order is not configured in UI");
			}

			String creditCardNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Credit card number is " + util.maskCardOrAccountNumber(creditCardNumber));}

			if(util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes()) || util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Caller Identification host response bean object is null or empty");}
				throw new ServiceException("Caller Identification host response bean object is null or empty");
			}

			CreditCardDetails_HostRes creditCardDetails_HostRes = (CreditCardDetails_HostRes)callInfo.getCreditCardDetails_HostRes();

			HashMap<String,CardAcctDtl> cardAcctDtlMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
			String acctNumber = cardAcctDtlMap.get(creditCardNumber).getCardAccountNumber();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit card's account number is "  + util.maskCardOrAccountNumber(acctNumber));}



			if(util.isNullOrEmpty(callInfo.getCreditCardDetails_HostRes())
					|| util.isNullOrEmpty(creditCardNumber)
					|| util.isNullOrEmpty(callInfo.getCreditCardDetails_HostRes().getAcctNo_AccountDetailMap())
					|| util.isNullOrEmpty(callInfo.getCreditCardDetails_HostRes().getAcctNo_AccountDetailMap().get(acctNumber))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardDetails_HostRes Object of CCEntity service is null / empty");}
				throw new ServiceException("CreditCardDetails_HostRes Object is null or empty");
			}


			/**
			 * 
			 */

			CCP_CCEntityFields ccEntityFields = (CCP_CCEntityFields)creditCardDetails_HostRes.getAcctNo_AccountDetailMap().get(acctNumber);

			String fullDueAmt = Constants.EMPTY_STRING;
			String miniDueAmt = Constants.EMPTY_STRING;
			String availableBalance = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(ccEntityFields)){
				//Changed as on 23-08-2015
				/*******/
				//fullDueAmt = ccEntityFields.getOverDueAmt();
				fullDueAmt = ccEntityFields.getBalance();
				/*******/
				miniDueAmt = ccEntityFields.getStmtMinDue();
				availableBalance = ccEntityFields.getBalance();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CCP_CCEntityFields Full Due amt & Minimum due (Stmt mini due) amount is "+fullDueAmt+","+miniDueAmt);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CreditCardGroupInfoDetails Balance amount is "+availableBalance);}

			if(util.isNullOrEmpty(fullDueAmt))
				fullDueAmt = Constants.ZERO;

			if(util.isNullOrEmpty(miniDueAmt))
				miniDueAmt = Constants.ZERO;
			
			if(util.isNullOrEmpty(availableBalance))
				availableBalance = Constants.ZERO;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Full Due amount is " + fullDueAmt);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Minimum due / Stmt mini due amount is "+miniDueAmt);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available balance amount is "+availableBalance);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			/**
			 * Following are the changes done on 15-Dec-2014 for the new business requirement to play full due and min due if it is zero
			 */
			
//			/**
//			 * iF BOTH THE FULL DUE AND THE MIN DUE ARE ZERO NEED TO SKIP THIS ANNOUNCEMENTS
//			 */
//			if(Double.parseDouble(fullDueAmt) == 0 && Double.parseDouble(miniDueAmt) == 0){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Minimum due / Stmt mini due amount is null / empty, return with silence phrase");}
//				return DynaPhraseConstants.SILENCE_PHRASE;
//			}
//			//END - Vinoth

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			for(int count=0; count<CCPDueAmtFieldsAndOrder.size(); count++){
				temp_Str = CCPDueAmtFieldsAndOrder.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CCPDueAmtFieldsAndOrder for announcement is"+ temp_Str);}

				switch(temp_Str)
				{
				case Constants.BALANCE_TYPE_FULL_DUE:

					if(Double.parseDouble(fullDueAmt)<=Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "fullDueAmt is < 0");}
						if(fullDueAmt.contains(Constants.MINUS) && fullDueAmt.length() > Constants.GL_ONE){
							fullDueAmt = fullDueAmt.substring(Constants.GL_ONE, fullDueAmt.length());
							callInfo.setField(Field.FULLDUEAMT, fullDueAmt);
							dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
							//dynamicValueArray.add(fullDueAmt); changed on 23-02-2014
							//Changed on 17-08-2015
							//dynamicValueArray.add(availableBalance);
							dynamicValueArray.add(fullDueAmt);
						}else if(Double.parseDouble(fullDueAmt)==Constants.GL_ZERO){
							callInfo.setField(Field.FULLDUEAMT, fullDueAmt);
							dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
							//dynamicValueArray.add(fullDueAmt); changed on 23-02-2014
							//Changed on 17-08-2015
							//dynamicValueArray.add(availableBalance);
							dynamicValueArray.add(fullDueAmt);
						}else{
							dynamicValueArray.add(Constants.NA);
							dynamicValueArray.add(Constants.NA);
						}
					}else if(Double.parseDouble(fullDueAmt) > Constants.GL_ZERO){
						callInfo.setField(Field.FULLDUEAMT, fullDueAmt);
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
						//dynamicValueArray.add(fullDueAmt); changed on 23-02-2014
						//Changed on 17-08-2015
						//dynamicValueArray.add(availableBalance);
						dynamicValueArray.add(Constants.MINUS+fullDueAmt);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					break;
				case Constants.BALANCE_TYPE_MINI_AMT:
					callInfo.setField(Field.MINIDUEAMT, miniDueAmt);

					if(Double.parseDouble(miniDueAmt)<=Constants.GL_ZERO){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "miniDueAmt balance is < 0");}
						if(miniDueAmt.contains(Constants.MINUS) && miniDueAmt.length() > Constants.GL_ONE){
							miniDueAmt = miniDueAmt.substring(Constants.GL_ONE, miniDueAmt.length());
							callInfo.setField(Field.MINIDUEAMT, miniDueAmt);
							dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1004);
							dynamicValueArray.add(miniDueAmt);
						}else if(Double.parseDouble(miniDueAmt)==Constants.GL_ZERO){
							callInfo.setField(Field.MINIDUEAMT, miniDueAmt);
							dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1004);
							dynamicValueArray.add(miniDueAmt);
						}
						else{
							dynamicValueArray.add(Constants.NA);
							dynamicValueArray.add(Constants.NA);
						}
					}
					else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					break;
				}
			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Due_Amount_Message");
			String featureID = FeatureIDMap.getFeatureID("Credit_Card_Payment_Internal");
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
			//No Need

			//To have the property file grammar, need to call that util method here
			//No need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe seperator sign
			//No Need
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentInternalImpl.getCCPInternalDueAmtPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentInternalImpl.getCCPInternalDueAmtPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}
	/*
	 * public static void main(String s[]){ String s1 = "110.2795"; String s2 =
	 * ".2790";String s3 = "110.2";String s4 = "0"; DecimalFormat format = new
	 * DecimalFormat("0.###");
	 * System.out.println(format.format(Double.valueOf(s1)));
	 * System.out.println(format.format(Double.valueOf(s2)));
	 * System.out.println(format.format(Double.valueOf(s3)));
	 * System.out.println(format.format(Double.valueOf(s4))); }
	 */
}
