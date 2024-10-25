package com.servion.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

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
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.creditCardBalance.CCEntityFields;
import com.servion.model.creditCardBalance.CardDetails;
import com.servion.model.creditCardBalance.CreditCardBalanceDetails_HostRes;
import com.servion.model.creditCardBalance.CreditCardGroupInfoDetails;
import com.servion.model.creditCardBalance.CreditCardGroupInq_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class CardBalanceImpl implements ICardBalance{

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
	public String getCreditCardBalance(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardBalanceImpl.getCreditCardBalance()");}
		String code = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
//			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
//
//			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
//				throw new ServiceException("ICEGlobalConfig object is null");
//			}


			String cardEmbossNum = null;
			cardEmbossNum = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Selected Credit card number is " + util.maskCardOrAccountNumber(cardEmbossNum));}

//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetched the caller identification host res object");}
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetched the caller identification host res object's CCEntity Details");}
//					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber())){
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetched the caller identification host res object's CCEntity Detail' Card emboss number");}
//						cardEmbossNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCardEmbossNumber();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card Emboss Number is " + cardEmbossNum);}
//					}
//				}
//
//			}


			/**
			 * Following are the field are mapped as per the new WSDL V2.13
			 */
			String reference = Constants.EMPTY_STRING;
			String extraOption = Constants.EMPTY_STRING;
			String manNoAuth = Constants.EMPTY_STRING;

			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("iceGlobalConfig object is null");
			}
			//As this is yet to be confirmed by the host team,  assigning value 
			reference = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_REFERENCETYPE);
			extraOption = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_EXTRAOPTION);
			manNoAuth = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_MAXNOAUTH);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The reference value for CCBalance is " + reference);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The extraOption value for CCBalance is " + extraOption);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The maxNoAuth value for CCBalance is " + manNoAuth);}

//			ArrayList<String> cardAccountNumList = new ArrayList<>();
//			String cardAccountNum = null;

			
			//TODO
//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())){
//					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getCustomerEntityAccountMap())){
//						cardAccountNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getAccountNumber();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Account Number is" + util.getSubstring(cardAccountNum, Constants.GL_FOUR));}
//
//					}
//				}
//			}
//			
			


			//			HashMap<String, CardAcctDtl> cardDetailMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
			//			if(cardDetailMap!=null && cardDetailMap.containsKey(creditCardNum)){
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type is Card");}
			//				entyityInquiryType = Constants.HOST_REQUEST_ENTITYINQTYPE_CARD;
			//			}else{
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type is Customer");}
			//				entyityInquiryType = Constants.HOST_REQUEST_ENTITYINQTYPE_CUSTOMER;
			//			}


			//			String creditCardNum = Constants.EMPTY_STRING;

			//			String cardAccountNum = Constants.EMPTY_STRING;

			//			String internalCustomerID = Constants.EMPTY_STRING;

			//			String nationalID = Constants.EMPTY_STRING;


			//END - Request field for CCEntityInquiry


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
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_CARDEMBOSSNUMBER + Constants.EQUALTO + cardEmbossNum
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(cardEmbossNum)
			
			
//			Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + cardEmbossNum
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + callInfo.getField(Field.SRCTYPE)
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime ;
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
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);
			
			/* END */

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CCGROUPDETAILSINQUIRY_REQUESTTYPE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCGROUPDETAILSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			CreditCardGroupInq_HostRes creditCardGroupInq_HostRes = ccGroupInquiryDAO.getCCAvailableBalanceHostRes(callInfo, cardEmbossNum, reference, extraOption, manNoAuth, requestType);


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "creditCardGroupInq_HostRes Object is :"+ creditCardGroupInq_HostRes);}
			callInfo.setCreditCardGroupInq_HostRes(creditCardGroupInq_HostRes);

			code = creditCardGroupInq_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = creditCardGroupInq_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}


			strHostInParam = 	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(cardEmbossNum)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
					
			
			String hostResCode = creditCardGroupInq_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
	
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(creditCardGroupInq_HostRes.getErrorDesc()) ?"NA" :creditCardGroupInq_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for CardGroupInquiry Service");}

				boolean isCalledFromFaxModule = callInfo.getField(Field.ISCALLEDFROMFAXMODULE)!= null ? (boolean)callInfo.getField(Field.ISCALLEDFROMFAXMODULE) : false;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is Card balance is called from Fax module ? " + isCalledFromFaxModule);}

				if(!util.isNullOrEmpty(isCalledFromFaxModule) && !isCalledFromFaxModule){


					code = Constants.WS_FAILURE_CODE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call callerIdentification_HostRes.CCENTITYINQUIRY");}

					/**
					 * For Reporting Purpose
					 */
					HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

					String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
					hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
					String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CARDNUMBER + Constants.EQUALTO + cardEmbossNum
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
					hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_CCENTITYINQUIRY);
					//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
					hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
					hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
					hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
					hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

					String startTimeSec = util.getCurrentDateTime();
					hostReportDetailsForSecHost.setHostStartTime(startTimeSec); //It should be in the formate of 31/07/2013 18:11:11
				
					hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
					//End Reporting
					
					/*
					 *  Setting NA values
					 */
					hostReportDetailsForSecHost.setHostEndTime(Constants.NA);
					hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
					hostReportDetailsForSecHost.setHostResponse(Constants.NA);
					
					callInfo.setHostReportDetails(hostReportDetailsForSecHost);
					IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);
					callInfo.insertHostDetails(ivrdataForSecHost);
					
					/* END */
								
					/**
					 * For Request field of CCEntityInquriy Service
					 */

					ArrayList<String> creditCardList = new ArrayList<>();
					String creditCardNum = (String)callInfo.getField(Field.SRCNO);
					
					if(util.isNullOrEmpty(creditCardNum)){
						throw new ServiceException("Selected Credit Card Number is empty or null");
					}
					
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes()) && !util.isNullOrEmpty(creditCardNum)){
						CardAcctDtl cardAcctDtl = null;
						if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
							HashMap<String, CardAcctDtl>cardDetailsMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained the Card Detail Map object from caller identification" + cardDetailsMap);}

							if(cardDetailsMap.containsKey(creditCardNum)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The CardDetailsMap object contains the selected credit card number " + util.maskCardOrAccountNumber(creditCardNum));}
								cardAcctDtl = cardDetailsMap.get(creditCardNum);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The obtained cardAcctDtls object is " + cardAcctDtl);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The account number for the selected credit card number is " + util.maskCardOrAccountNumber(cardAcctDtl.getCardAccountNumber()));}

								creditCardList.add(cardAcctDtl.getCardAccountNumber());
							}
						}
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Credit Card's Account no  is" + util.getSubstring(creditCardNum, Constants.GL_FOUR));}
					
//					ArrayList<String> ccList = (ArrayList<String>)callInfo.getField(Field.SRCNO);
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit Card List are " + ccList );}
//					if(!util.isNullOrEmpty(ccList)){
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "And the CC list count is " + ccList.size());}
//					}
//					cardAccountNumList.addAll(ccList);

//					String customerID = (String)callInfo.getField(Field.CUSTOMERID);
//					ArrayList<String> customerIDList = new ArrayList<String>();
//					customerIDList.add(customerID);

					String entyityInquiryType = Constants.EMPTY_STRING;
					entyityInquiryType = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CREDITCARD_ENTITYINQ_TYPE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type from the entityInquiryType " + entyityInquiryType);}
					

					/**
					 * Setting the InteralCustomerID and nationalID as null value
					 */

					String inquiryReference = Constants.EMPTY_STRING;
					inquiryReference = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CREDITCARD_INQUIRYREFERENCE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The inquiryReference is " + inquiryReference);}

					ArrayList<String>numberList = new ArrayList<>();

					if(!util.isNullOrEmpty(creditCardList)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit card list size is "+ creditCardList.size());}
						numberList.addAll(creditCardList);
						//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as C");}
						//					inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_C;
					}//else if(!util.isNullOrEmpty(cardAccountNumList)){
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card account number list is "+ cardAccountNumList);}
//						numberList.addAll(cardAccountNumList);
//						//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as A");}
//						//					inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_A;
//					}else{
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID list is "+ customerIDList);}
//						numberList.addAll(customerIDList);
//						//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as U");}
//						//					inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_U;
//					}

					String returnReplacedCards = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_CCBALANCE_RETURNED_REPLACED_CARD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Returned Replacec card value from the Configurator is " +returnReplacedCards );}		

					
					String entitySize = (String)iceGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_CCENTITY_ENQUIRY_SIZE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Return Value of entity enquiry size is " +entitySize );}
					/**
					 * END
					 */
					
					requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CCENTITYINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CCENTITYINQUIRY_REQUESTTYPE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

					
					CreditCardBalanceDetails_HostRes creditCardBalanceDetails_HostRes = ccEntityInquiryDAO.getCCBalanceHostRes(callInfo, entyityInquiryType, inquiryReference, numberList, returnReplacedCards, entitySize,requestType);

					callInfo.setCreditCardBalanceDetails_HostRes(creditCardBalanceDetails_HostRes);
					code = creditCardBalanceDetails_HostRes.getErrorCode();

					/*
					 * For Reporting Start
					 */
					String hostEndTimeForSecHost = creditCardBalanceDetails_HostRes.getHostEndTime();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
					hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

					String hostResCodeForSecHost = creditCardBalanceDetails_HostRes.getHostResponseCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
					hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

					
					String durationTimeSec = util.hostServiceTimeDuration(startTimeSec, hostEndTimeForSecHost, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTimeSec);}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

					strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(numberList.get(Constants.GL_ZERO))
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					
					hostReportDetailsForSecHost.setHostInParams(strHostInParam);
					
					
					String responseDescForSecHost = Constants.HOST_FAILURE;
					if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
						responseDescForSecHost = Constants.HOST_SUCCESS;
					}
					String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
							+ Constants.EQUALTO + hostResCodeForSecHost
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(creditCardBalanceDetails_HostRes.getErrorDesc()) ?"NA" :creditCardBalanceDetails_HostRes.getErrorDesc());
					hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

					callInfo.setHostReportDetails(hostReportDetailsForSecHost);
					ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

					callInfo.updateHostDetails(ivrdataForSecHost);
					//End Reporting


					if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for customer profile aggregate");}
					}else{

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for CCEntity host service");}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + creditCardBalanceDetails_HostRes.getHostResponseCode());}

						util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCENTITYINQUIRY, creditCardBalanceDetails_HostRes.getHostResponseCode());

						/**
						 * following will be called only if there occured account selection before this host access
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
						util.setEligibleAccountCounts(callInfo,hostResCodeForSecHost);
					}
				}
			}else{


				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Credit Group Inquiry host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + creditCardGroupInq_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCGROUPINQUIRY, creditCardGroupInq_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo,hostResCode);
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardBalanceImpl.getCreditCardBalance() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getCreditCardBalancePhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage = Constants.EMPTY_STRING;
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardBalanceImpl.getCreditCardBalancePhrases()");}

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String>CCBalanceFieldsAndOrder = new ArrayList<String>();

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCBalanceFieldsAndOrder))){
				CCBalanceFieldsAndOrder = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CCBalanceFieldsAndOrder);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting card Account Balance fields and Order in the a local variable");}
			}

			if(!util.isNullOrEmpty(CCBalanceFieldsAndOrder)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CC Balance Fields and Order size is" + CCBalanceFieldsAndOrder.size());}
			}

			callInfo.setField(Field.CCBalanceFieldsAndOrder, CCBalanceFieldsAndOrder);


			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			CreditCardBalanceDetails_HostRes creditCardBalanceDetails_HostRes = callInfo.getCreditCardBalanceDetails_HostRes();
			ArrayList<String> balanceListForAnnc = (ArrayList<String>)callInfo.getField(Field.CCBalanceFieldsAndOrder);


			if(util.isNullOrEmpty(creditCardBalanceDetails_HostRes)){
				throw new ServiceException("Card balance host response object bean is null");
			}

			if(util.isNullOrEmpty(balanceListForAnnc)){
				throw new ServiceException("User doesn't configure the balance types in UI");
			}

			String availableBalance = Constants.EMPTY_STRING;
			String outstandingAmount = Constants.EMPTY_STRING;
			String MinDueAmount = Constants.EMPTY_STRING;
			String DueDate = Constants.EMPTY_STRING;
			String creditLimit = Constants.EMPTY_STRING;
			String lastPaymentAmount = Constants.EMPTY_STRING;
			String lastPaymentDate = Constants.EMPTY_STRING;
			String last4Digit = Constants.EMPTY_STRING;
			ArrayList<Double> availBalList = new ArrayList<Double>();
			ArrayList<String> creditLimitList = new ArrayList<String>();
			Iterator iterator = null;
			//			HashMap<String, ArrayList<String>> cutomerID_AccountNumberMap = null;
			//			ArrayList<String> accountNumberList = null;
			HashMap<String, CCEntityFields> acctNo_AccountDetailMap =  null;
			CCEntityFields ccEntityFields = null;

			String selectedCC = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Credit Card number is "+util.maskCardOrAccountNumber(selectedCC));}
			
			HashMap<String, ArrayList<CreditCardGroupInfoDetails>> ccGrpInfoMap = null;
			ArrayList<CreditCardGroupInfoDetails> ccGrpInfoList = null;
			
			if(!util.isNullOrEmpty(callInfo.getCreditCardGroupInq_HostRes()) && !util.isNullOrEmpty(callInfo.getCreditCardGroupInq_HostRes().getCreditCardGrpInfoDetailMap())){
				ccGrpInfoMap = callInfo.getCreditCardGroupInq_HostRes().getCreditCardGrpInfoDetailMap();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Retrieved CCGrpInfoDetail Map is "+ ccGrpInfoMap);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected card number is "+ ccGrpInfoMap);}
				
				ccGrpInfoList = ccGrpInfoMap.get(selectedCC);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Credti card group info list is "+ ccGrpInfoList);}
				if(!util.isNullOrEmpty(ccGrpInfoList)){
					String groupNo = Constants.EMPTY;
					
					for(int i = 0; i <ccGrpInfoList.size(); i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+i +" index of the Credit card Group info list is "+ ccGrpInfoList.get(i));}	
						groupNo = ccGrpInfoList.get(i).getGroupnumber();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected card's group number is "+ groupNo);}
						
						
						/**
						 * following changes like if the group number having "1512" was confirmed by Faraz, on 18-June-2014
						 * Confirming that we should announce the Group1 card details irrespective of primary or secondary card
						 */
						last4Digit = groupNo.substring(groupNo.length()-4, groupNo.length());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "last 4 digit of the selected card's group number is "+ last4Digit);}
						
						if(Constants.GROUP_ONE_CARD_ID.equalsIgnoreCase(last4Digit)){
							creditLimit = ccGrpInfoList.get(i).getCreditLimit();
						//TODO: Prime R4 CR Changes 22-08-2015
							availableBalance = ccGrpInfoList.get(i).getTotalOTB();
							if(util.isNullOrEmpty(availableBalance))
								availableBalance = Constants.EMPTY_STRING;
							else{
								try{
									availBalList.add(Double.parseDouble(availableBalance));
									creditLimitList.add(ccGrpInfoList.get(i).getCreditLimit());
								}catch(Exception e){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_, "Available Balance Double data type conversion failing at index["+i+"] value["+ availableBalance+"]");}
								}
							}
								
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit limit is "+ creditLimit);}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available Balance is "+ availableBalance);}
						}
						
					}
					
					//To get minimum available balance from the list
					int index_Cnt =  availBalList.indexOf(Collections.min(availBalList));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Prime Index Count:"+ index_Cnt);}
					availableBalance = Double.toString(availBalList.get(index_Cnt)); 
					if(creditLimitList != null && !creditLimitList.isEmpty() && creditLimitList.size() > index_Cnt){
						creditLimit = ccGrpInfoList.get(index_Cnt).getCreditLimit();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Prime Final Credit limit is "+ creditLimit);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Prime Final Available Balance is :"+ availableBalance);}
					//Prime R4 Changes End
				}
				
			}
			
			if(!util.isNullOrEmpty(creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap())  && 
					creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap().size() > Constants.GL_ZERO){

				//				cutomerID_AccountNumberMap = creditCardBalanceDetails_HostRes.getCutomerID_AccountNumberMap();
				//				iterator = cutomerID_AccountNumberMap.entrySet().iterator();

				//				Map.Entry mapEntry = (Map.Entry) iterator.next();
				//				String accountNumber = (String) mapEntry.getKey();
				//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer number retrieved from the host is "+ accountNumber);}

				//				accountNumberList = (ArrayList<String>)mapEntry.getValue();
				//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the account list from the CreditCard Balance details map"+ accountNumberList);}

				acctNo_AccountDetailMap = creditCardBalanceDetails_HostRes.getAcctNo_AccountDetailMap();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail object from the host is "+ acctNo_AccountDetailMap);}

				if(!util.isNullOrEmpty(acctNo_AccountDetailMap) && acctNo_AccountDetailMap.size() > Constants.GL_ZERO){

					iterator = acctNo_AccountDetailMap.entrySet().iterator();
					Map.Entry mapEntry = (Map.Entry) iterator.next();

					String accountNumber = (String) mapEntry.getKey();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account number retrieved from the host is "+ accountNumber);}

					ccEntityFields = (CCEntityFields) mapEntry.getValue();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved the CCEntityField object "+ ccEntityFields);}

					if(!util.isNullOrEmpty(ccEntityFields)){
//						availableBalance = ccEntityFields.getBalance();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The available balance received from the host is "+ availableBalance);}

						//MinDueAmount = ccEntityFields.getCurrencyMinAmount(); //changes as per faisal comment on 25-02-2014
						MinDueAmount = ccEntityFields.getStmtMinDue();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Minimum amount is "+ MinDueAmount);}
						
						outstandingAmount = ccEntityFields.getBalance();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Outstanding Balance Amount is "+ outstandingAmount);}
						
						DueDate = ccEntityFields.getStmtDueDate();
						DueDate = util.convertDateStringFormat(DueDate, Constants.DATEFORMAT_YYYYMMDD, "yyyyMMdd");
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyyMMdd" + DueDate );}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Due date is "+ DueDate);}

//						creditLimit = ccEntityFields.getCreditLimit();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit Limit is "+ creditLimit);}

						lastPaymentAmount = ccEntityFields.getPaymentAmount();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last payment amount is "+ lastPaymentAmount);}

						lastPaymentDate = ccEntityFields.getPaymentDate();
						lastPaymentDate = util.convertDateStringFormat(lastPaymentDate, Constants.DATEFORMAT_YYYYMMDD	, "yyyyMMdd");
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyyMMdd" + lastPaymentDate );}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last payment Date is "+ lastPaymentDate);}

//						HashMap<String, CardDetails> cardDetailsMap = (HashMap<String, CardDetails>) ccEntityFields.getCardDetailsMap();
//						CardDetails cardDetails = null;
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Card Details Map object retrieved is "+ cardDetailsMap);}
//						if(!util.isNullOrEmpty(cardDetailsMap) && cardDetailsMap.size() > Constants.GL_ZERO){
//
//							iterator = cardDetailsMap.entrySet().iterator();
//							mapEntry = (Map.Entry) iterator.next();
//
//							accountNumber = (String) mapEntry.getKey();
//							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card number retrieved from the host is "+ accountNumber);}
//
//							cardDetails = (CardDetails) mapEntry.getValue();
//							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card detail object retrieved from the host is "+ cardDetails);}
//
//							if(!util.isNullOrEmpty(cardDetails)){
//								outstandingAmount = cardDetails.getOutstandingAmt();
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "OutStanding amount is "+ outstandingAmount);}
//							}
//						}
					}
				}
			}



			if(util.isNullOrEmpty(availableBalance))
				availableBalance = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(outstandingAmount))
				outstandingAmount = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(MinDueAmount))
				MinDueAmount = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(DueDate))
				DueDate = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(creditLimit))
				creditLimit = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(lastPaymentAmount))
				lastPaymentAmount = Constants.EMPTY_STRING;

			if(util.isNullOrEmpty(lastPaymentDate))
				lastPaymentDate = Constants.EMPTY_STRING;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available balance for the credit card is"+ availableBalance);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Outstanding Amount for the credit card is"+ outstandingAmount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Minimum Due Amount for the credit card is"+ MinDueAmount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Due Date for the credit card is"+ DueDate);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Credit Limit for the credit card is"+ creditLimit);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Payment Amount for the credit card is"+ lastPaymentAmount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last Payment Date for the credit card is"+ lastPaymentDate);}

			
			/**
			 * Following are the parameter declared to make the prompt announcement / balance announcement as dynamic
			 */
			String languageKey = Constants.EMPTY_STRING;
			Locale locale = null;
			String anncID = Constants.EMPTY_STRING;
			String featureID = Constants.EMPTY_STRING;
			String combinedKey = Constants.EMPTY_STRING;
			Object[] object = null;
			int totalPrompt = Constants.GL_ZERO;
			String dynamicPhraseKey = Constants.EMPTY_STRING;
			String dynamicMessageValue = Constants.EMPTY_STRING;
			String grammar = Constants.EMPTY_STRING;
			
			languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("REPORT_LOSS_CARD_CONFIRMATION");
			anncID = AnncIDMap.getAnncID("CC_Balance_Message");
			featureID = FeatureIDMap.getFeatureID("Credit_Card_Balance");
			combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
			//END 
			
			
			String temp_Str;
			for(int count=0; count<balanceListForAnnc.size(); count++){
				temp_Str = balanceListForAnnc.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Balance type for announcement is"+ temp_Str);}

				switch(temp_Str)
				{
				case Constants.BALANCE_TYPE_AVAIL_BALANE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(availableBalance)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1000);
						dynamicValueArray.add(availableBalance);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_AVAIL_BALANE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_OUTSTANDING_AMT:
					dynamicValueArray = new ArrayList<Object>();
					//Changed as on 23-08-2015
					/*****/
					/*if(!util.isNullOrEmpty(outstandingAmount)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
						dynamicValueArray.add(outstandingAmount);
					}*/
					if(!util.isNullOrEmpty(outstandingAmount)){
						if(Double.parseDouble(outstandingAmount)<=Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "outstandingAmount is < 0");}
							if(outstandingAmount.contains(Constants.MINUS) && outstandingAmount.length() > Constants.GL_ONE){
								outstandingAmount = outstandingAmount.substring(Constants.GL_ONE, outstandingAmount.length());
								dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
								dynamicValueArray.add(outstandingAmount);
							}else if(Double.parseDouble(outstandingAmount)==Constants.GL_ZERO){
								dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
								dynamicValueArray.add(outstandingAmount);
							}else{
								dynamicValueArray.add(Constants.NA);
								dynamicValueArray.add(Constants.NA);
							}
						}else if(Double.parseDouble(outstandingAmount) > Constants.GL_ZERO){
							dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1001);
							dynamicValueArray.add(Constants.MINUS+outstandingAmount);
						}else{
							dynamicValueArray.add(Constants.NA);
							dynamicValueArray.add(Constants.NA);
						}
					}
					else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					/*****/
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_OUTSTANDING_AMT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_MIN_DUE_AMOUNT:
					dynamicValueArray = new ArrayList<Object>();
					
					//Changed as on 23-08-2015
					/*******/
					/*if(!util.isNullOrEmpty(MinDueAmount)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1004);
						dynamicValueArray.add(MinDueAmount);
					}*/
					if(!util.isNullOrEmpty(MinDueAmount)){
						if(Double.parseDouble(MinDueAmount)<=Constants.GL_ZERO){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "MinDueAmount is < 0");}
							if(MinDueAmount.contains(Constants.MINUS) && MinDueAmount.length() > Constants.GL_ONE){
								MinDueAmount = MinDueAmount.substring(Constants.GL_ONE, MinDueAmount.length());
								dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1004);
								dynamicValueArray.add(MinDueAmount);
							}else if(Double.parseDouble(MinDueAmount)==Constants.GL_ZERO){
								dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1004);
								dynamicValueArray.add(MinDueAmount);
							}else{
								dynamicValueArray.add(Constants.NA);
								dynamicValueArray.add(Constants.NA);
							}
						}else if(Double.parseDouble(MinDueAmount) > Constants.GL_ZERO){
							dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1004);
							dynamicValueArray.add(Constants.MINUS+MinDueAmount);
						}else{
							dynamicValueArray.add(Constants.NA);
							dynamicValueArray.add(Constants.NA);
						}
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					/******/
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_MIN_DUE_AMOUNT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					
					break;
				case Constants.BALANCE_TYPE_DUEDATE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(DueDate)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1005);
						dynamicValueArray.add(DueDate);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_DUEDATE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					
					break;
				case Constants.BALANCE_TYPE_CREDIT_LIMIT:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(creditLimit)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1006);
						dynamicValueArray.add(creditLimit);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_CREDIT_LIMIT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_LAST_PAYMENT_AMOUNT:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(lastPaymentAmount)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1007);
						dynamicValueArray.add(lastPaymentAmount);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_LAST_PAYMENT_AMOUNT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_LAST_PAYMENT_DATE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(lastPaymentDate)){
						dynamicValueArray.add(DynaPhraseConstants.Card_Balance_1008);
						dynamicValueArray.add(lastPaymentDate);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_LAST_PAYMENT_DATE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				}
			}

			/*
			 * Handling Grammar and MoreOptions for OD Use
			 */
			grammar = Constants.NA;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Grammar value is"+grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting moreoption as false");}
			callInfo.setField(Field.MOREOPTION, false);
			//End

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardBalanceImpl.getCreditCardBalancePhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardBalanceImpl.getCreditCardBalancePhrases() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}
}
