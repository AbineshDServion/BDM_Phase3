package com.servion.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.ExchngRateInqDAO;
import com.servion.dao.FundTransferIntraDAO;
import com.servion.dao.FundsTransferRemittDAO;
import com.servion.dao.UtilityBillPaymentDAO;
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
import com.servion.model.billPayment.UpdatePaymentDetails_HostRes;
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.fundsTransfer.FT_BeneficiaryDetails;
import com.servion.model.fundsTransfer.FT_ExchangeRateDetails;
import com.servion.model.fundsTransfer.FT_ExchangeRateDetails_HostRes;
import com.servion.model.fundsTransfer.UpdateFTIntraPayment_HostRes;
import com.servion.model.fundsTransfer.UpdateFTRemittPayment_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class FundsTransferConfirmationImpl implements IFundsTransferConfirmation{

	private static Logger logger = LoggerObject.getLogger();

	private ExchngRateInqDAO exchngRateInqDAO;
	private FundsTransferRemittDAO fundsTransferRemittDAO;
	private FundTransferIntraDAO fundTransferIntraDAO;
	private UtilityBillPaymentDAO utilityBillPaymentDAO;


	public UtilityBillPaymentDAO getUtilityBillPaymentDAO() {
		return utilityBillPaymentDAO;
	}

	public void setUtilityBillPaymentDAO(UtilityBillPaymentDAO utilityBillPaymentDAO) {
		this.utilityBillPaymentDAO = utilityBillPaymentDAO;
	}
	public ExchngRateInqDAO getExchngRateInqDAO() {
		return exchngRateInqDAO;
	}

	public void setExchngRateInqDAO(ExchngRateInqDAO exchngRateInqDAO) {
		this.exchngRateInqDAO = exchngRateInqDAO;
	}

	public FundsTransferRemittDAO getFundsTransferRemittDAO() {
		return fundsTransferRemittDAO;
	}

	public void setFundsTransferRemittDAO(
			FundsTransferRemittDAO fundsTransferRemittDAO) {
		this.fundsTransferRemittDAO = fundsTransferRemittDAO;
	}

	public FundTransferIntraDAO getFundTransferIntraDAO() {
		return fundTransferIntraDAO;
	}

	public void setFundTransferIntraDAO(FundTransferIntraDAO fundTransferIntraDAO) {
		this.fundTransferIntraDAO = fundTransferIntraDAO;
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
	public String getFTPaymentConfirmationPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferConfirmationImpl.getFTPaymentConfirmationPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			String lastNDigits = (String)callInfo.getField(Field.LastNDigits);
			int int_LastNDigit = util.isNullOrEmpty(lastNDigits)?Constants.GL_ZERO:Integer.parseInt(lastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured last N digit length for announcement is "+ int_LastNDigit);}

			String amount = (String)callInfo.getField(Field.AMOUNT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected amount is "+ amount);}

			String destNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected destination number ending with "+  util.getSubstring(destNumber, int_LastNDigit));}

			String sourceNumber = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Source number ending with "+ util.getSubstring(sourceNumber, int_LastNDigit));}

			
			/**
			 * Getting the customer category type from the account detail map object
			 */
			String customerCategoryType = Constants.DEFAULT;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getCallerIdentification_HostRes is not empty or null ");}
				
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail Map object is not null or empty");}
					
					HashMap<String, AcctInfo>acctDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
					if(!util.isNullOrEmpty(sourceNumber) && !util.isNullOrEmpty(acctDetailMap.get(sourceNumber))){
						
						customerCategoryType = acctDetailMap.get(sourceNumber).getCategory();
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Funds Transaction fee amount is "+transactionFee);}
			
			callInfo.setField(Field.TransactionFee, transactionFee);

			double double_TransFee = util.isNullOrEmpty(transactionFee)?Constants.GL_ZERO : Double.parseDouble(transactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee after converting to double is "+double_TransFee);}
			
			String featureName = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+featureName);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = util.isNullOrEmpty(sourceNumber)?Constants.EMPTY_STRING: util.getSubstring(sourceNumber, int_LastNDigit);

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(amount);
			dynamicValueArray.add(temp_Str);

			if(Constants.FEATURENAME_FUNDSTRANSFERINTERNAL.equalsIgnoreCase(featureName)){
				dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1008);
				temp_Str = util.isNullOrEmpty(sourceNumber)?Constants.EMPTY_STRING: util.getSubstring(destNumber, int_LastNDigit);
				dynamicValueArray.add(temp_Str);

				//Handle for cross currency condition
				if(callInfo.getCallerIdentification_HostRes()!=null){
					if(callInfo.getCallerIdentification_HostRes().getAccountDetailMap()!=null){
						HashMap<String, AcctInfo> accountDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
						AcctInfo srcAcctInfo = accountDetailMap.get(sourceNumber);
						if(srcAcctInfo != null){
							AcctInfo destAcctInfo = accountDetailMap.get(destNumber);
							if(destAcctInfo != null){
								if(!srcAcctInfo.getAcctCurr().equalsIgnoreCase(destAcctInfo.getAcctCurr())){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its a cross Currency transfer"+featureName);}
									dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1012);

									if(callInfo.getFT_ExchangeRateDetails_HostRes() != null){
										if(callInfo.getFT_ExchangeRateDetails_HostRes().getExchangeRateCurrMap() != null){
											FT_ExchangeRateDetails exchangeRateInquiryDtls = callInfo.getFT_ExchangeRateDetails_HostRes().getExchangeRateCurrMap().get(destAcctInfo.getAcctCurr());
											String currencyRate = exchangeRateInquiryDtls.getSellRate();
											dynamicValueArray.add(currencyRate);

											//setting the value in the callinfo Field
											callInfo.setField(Field.EXCHANGE_RATES_VALUE, currencyRate);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Currency rate value is "+currencyRate);}

											dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1014);
											dynamicValueArray.add(amount);
										}
									}
								}
							}
						}
					}
				}

			}else if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM.equalsIgnoreCase(featureName)
					|| Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM_2.equalsIgnoreCase(featureName)){
				dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1009);
				temp_Str = util.isNullOrEmpty(sourceNumber)?Constants.EMPTY_STRING: util.getSubstring(destNumber, int_LastNDigit);
				dynamicValueArray.add(temp_Str);
				//Handle for cross currency condition
				if(callInfo.getCallerIdentification_HostRes()!=null){
					if(callInfo.getCallerIdentification_HostRes().getAccountDetailMap()!=null){
						HashMap<String, AcctInfo> accountDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
						AcctInfo srcAcctInfo = accountDetailMap.get(sourceNumber);
						if(srcAcctInfo != null){
							AcctInfo destAcctInfo = accountDetailMap.get(destNumber);
							if(destAcctInfo != null){
								if(!srcAcctInfo.getAcctCurr().equalsIgnoreCase(destAcctInfo.getAcctCurr())){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its a cross Currency transfer"+featureName);}
									dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1012);

									if(callInfo.getFT_ExchangeRateDetails_HostRes() != null){
										if(callInfo.getFT_ExchangeRateDetails_HostRes().getExchangeRateCurrMap() != null){
											FT_ExchangeRateDetails exchangeRateInquiryDtls = callInfo.getFT_ExchangeRateDetails_HostRes().getExchangeRateCurrMap().get(destAcctInfo.getAcctCurr());
											String currencyRate = exchangeRateInquiryDtls.getSellRate();
											dynamicValueArray.add(currencyRate);

											//setting the value in the callinfo Field
											callInfo.setField(Field.EXCHANGE_RATES_VALUE, currencyRate);
											if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Currency rate value is "+currencyRate);}

											dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1014);
											dynamicValueArray.add(amount);
										}
									}
								}
							}
						}
					}
				}

			}else if(Constants.FEATURENAME_FUNDSTRANSFERCHARITY.equalsIgnoreCase(featureName)){
				String selectedCharity = (String)callInfo.getField(Field.SELECTEDCHARITYTYPE);
				selectedCharity = util.isNullOrEmpty(selectedCharity)?Constants.EMPTY_STRING : selectedCharity;
				dynamicValueArray.add((selectedCharity + Constants.WAV_EXTENSION).trim());
				//dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);
			}else if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM.equalsIgnoreCase(featureName) || 
					Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYOUTSIDEBM.equalsIgnoreCase(featureName)
					|| Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM_2.equalsIgnoreCase(featureName)){
				dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1009);
				temp_Str = util.isNullOrEmpty(sourceNumber)?Constants.EMPTY_STRING: util.getSubstring(destNumber, int_LastNDigit);
				dynamicValueArray.add(temp_Str);
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);

			}



			if(!util.isNullOrEmpty(double_TransFee) && double_TransFee > Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "including transaction fee also for confirmation announcements");}
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_CHARGING_TRANSFEE); 
				dynamicValueArray.add(transactionFee); 
			}else{
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("FUNDTRANSFER_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("FundsTransfer_Confirmation");
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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ProductInformationImpl.getProductInformationMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at ReportLossCardImpl.getReportLostCardMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getForgeinExchangeRate(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferConfirmationImpl.getForgeinExchangeRate()");}   
		String code = Constants.EMPTY_STRING;
		//getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String selectedAcctNo = (String)callInfo.getField(Field.DESTNO);

			if(util.isNullOrEmpty(selectedAcctNo)){
				throw new ServiceException("Selected Account No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting selected Account no as entered cin" + util.getSubstring(selectedAcctNo, Constants.GL_FOUR));}



			/**
			 * If the currency code value is empty the we will receive all the exchange rate of all currency
			 */

			//Modified by Vinoth on 05 - Mar -2013 for currency code
			String currencyCode = Constants.EMPTY_STRING;
			HashMap<String, String> accountCurrMap = (HashMap<String, String>)callInfo.getField(Field.ACCTCURRMAP );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The retrived accountCurrMap" + accountCurrMap);}
			currencyCode = accountCurrMap.get(selectedAcctNo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The source account currency is " + currencyCode);}

			//			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
			//				HashMap<String, AcctInfo> cardDetailMap = (HashMap<String, AcctInfo>)callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
			//				AcctInfo acctInfo = cardDetailMap.get(selectedAcctNo);
			//				if(util.isNullOrEmpty(acctInfo)){
			//					currencyCode = acctInfo.getAcctCurr();
			//				}
			//			}


			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The customer id is " + customerID);}

			String ccyMarket = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EXCHANGERATE_CCYMARKET);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CCY Market value is " + ccyMarket);}
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
			
			String strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + currencyCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_EXCHNGRATEINQ);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

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

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_EXCHANGERATEINQ_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_EXCHANGERATEINQ_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}



			FT_ExchangeRateDetails_HostRes ft_ExchangeRateInquiry_HostRes = exchngRateInqDAO.getFTExchangeRateHostRes(callInfo, currencyCode, customerID, ccyMarket, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ft_ExchangeRateInquiry_HostRes Object is :"+ ft_ExchangeRateInquiry_HostRes);}
			callInfo.setFT_ExchangeRateDetails_HostRes(ft_ExchangeRateInquiry_HostRes);

			code = ft_ExchangeRateInquiry_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = ft_ExchangeRateInquiry_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = ft_ExchangeRateInquiry_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + currencyCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(ft_ExchangeRateInquiry_HostRes.getErrorDesc()) ?"NA" :ft_ExchangeRateInquiry_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			
			

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service exchangeRateInquiry");}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for ExchangeInq host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + ft_ExchangeRateInquiry_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_EXCHNGRATEINQ, ft_ExchangeRateInquiry_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occurred account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  FundsTransferConfirmationImpl.getForgeinExchangeRate() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String isCrossCurrencyTransferEnable(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			/*
			 * ExchangeRateDetails should be reseted here to avoid using the same value again
			 */
			callInfo.setFT_ExchangeRateDetails_HostRes(null);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferConfirmationImpl.isCrossCurrencyTransferEnable()");}
			String lastNDigits = (String)callInfo.getField(Field.LastNDigits);
			int int_LastNDigit = util.isNullOrEmpty(lastNDigits)?Constants.GL_ZERO:Integer.parseInt(lastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured last N digit length for announcement is "+ int_LastNDigit);}


			String destNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected destination number ending with "+  util.getSubstring(destNumber, int_LastNDigit));}

			String sourceNumber = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Source number ending with "+ util.getSubstring(sourceNumber, int_LastNDigit));}

			//Handle for cross currency condition
			if(callInfo.getCallerIdentification_HostRes()!=null){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Successfully retrieved the calleridentification host");}

				if(callInfo.getCallerIdentification_HostRes().getAccountDetailMap()!=null){
					HashMap<String, AcctInfo> accountDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "retrieval accountDetailMap" + accountDetailMap);}

					AcctInfo srcAcctInfo = accountDetailMap.get(sourceNumber);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "retrieval srcAcctInfo" + srcAcctInfo);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "retrieval srcAcctInfo.getAcctCurr" + srcAcctInfo.getAcctCurr());}
					if(srcAcctInfo != null){
						AcctInfo destAcctInfo = accountDetailMap.get(destNumber);
						if(destAcctInfo != null){
							if(!srcAcctInfo.getAcctCurr().equalsIgnoreCase(destAcctInfo.getAcctCurr())){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "retrieval destAcctInfo" + destAcctInfo);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "retrieval destAcctInfo.getAcctCurr" + destAcctInfo.getAcctCurr());}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its a cross currency transaction");}
								return "true";
							}
						}
					}
				}
			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FundsTransferConfirmationImpl.isCrossCurrencyTransferEnable()");}
		}catch(Exception e){
			WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FundsTransferConfirmationImpl.isCrossCurrencyTransferEnable() "	+ e.getMessage());
			throw new ServiceException(e);
		}
		return "false";
	}

	@Override
	public String updateFTPayment(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferConfirmationImpl.updateFTPayment()");}
		String code = Constants.EMPTY_STRING;

		try{

			/**
			 * Checking the condition for calling list beneficiary and beneficiary details host access 
			 */
			boolean isOTPCalledAfterDisconnected = util.isNullOrEmpty(callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT)) ? false : (boolean)callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is OTP Called After Disconnected " +isOTPCalledAfterDisconnected );}
			if(isOTPCalledAfterDisconnected){
				String returnCode = Constants.ONE;
				if(Constants.FEATURENAME_FUNDSTRANSFERCHARITY.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
					if(!util.isNullOrEmpty(Context.getiFundsTransferCharity())){
						returnCode = Context.getiFundsTransferCharity().getFTCharityBeneficiaryList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiFundsTransferCharity().getFTCharityBeneficiaryDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferCharity() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}
				}else if(Constants.FEATURENAME_FUNDSTRANSFERINTERNAL.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){

					/**
					 * No need to call any list or beneficiary details host call for internal third party account
					 */
				}else if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYOUTSIDEBM.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){

					if(!util.isNullOrEmpty(Context.getiFundsTransferThirdPartyOutsideBM())){
						returnCode = Context.getiFundsTransferThirdPartyOutsideBM().getFTOutsideBMBeneficiaryList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiFundsTransferThirdPartyOutsideBM().getFTOutsideBMBeneficiaryDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferThirdPartyOutsideBM() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferThirdPartyOutsideBM() is null or empty");
					}

				}else if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))
						|| Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM_2.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){

					if(!util.isNullOrEmpty(Context.getiFundsTransferThirdPartyWithinBM())){
						returnCode = Context.getiFundsTransferThirdPartyWithinBM().getFTWithinBMBeneficiaryList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiFundsTransferThirdPartyWithinBM().getFTWithinBMBeneficiaryDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getiFundsTransferThirdPartyWithinBM() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}

				}
			}

			//END - FundsTransferConfirmation
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			ICEFeatureData ivr_FeatureData  = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_FeatureData)){
				throw new ServiceException("ivr_FeatureData object is null");
			}

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
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + callInfo.getField(Field.DESTCURR)
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_EXCHANGE_RATE + Constants.EQUALTO + callInfo.getField(Field.EXCHANGE_RATES_VALUE)
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			}catch(Exception e){}
			
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_FUNDTRANSFERINTRA);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

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
			

			String hostEndTime = Constants.EMPTY_STRING;
			String hostResCode = Constants.EMPTY_STRING;


			String requestType = (String)ivr_FeatureData.getConfig().getParamValue(Constants.CUI_FUNDSTRANSFER_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Funds Transfer / Utility (Charity) service Request type is "+ requestType);}


			if(Constants.FEATURENAME_FUNDSTRANSFERCHARITY.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){

				hostReportDetails.setHostMethod(Constants.HOST_METHOD_UTILITYBILLPAYMENTBANK);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
							
				strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + callInfo.getField(Field.DESTNO)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.DESTNO)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_SERVICE_PROVIDER_CODE + Constants.EQUALTO + callInfo.getField(Field.SELECTEDSERVICEPROVIDER)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.UTILITYCODE)
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);

				String benfAcctID = (String)callInfo.getField(Field.DESTNO);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Selected charity code is " + util.maskCardOrAccountNumber(benfAcctID));}

				FT_BeneficiaryDetails ft_BeneficiaryDetails = null;

				if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes())){
					if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes().getFT_BeneficiaryCharityDetailsMap())){
						ft_BeneficiaryDetails = callInfo.getFT_BenfPayeeDetails_HostRes().getFT_BeneficiaryCharityDetailsMap().get(benfAcctID);
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Received Charity Beneficiary Details " + ft_BeneficiaryDetails);}

					}
				}

				String serviceProviderCode  = util.isNullOrEmpty(ft_BeneficiaryDetails)? Constants.EMPTY_STRING : ft_BeneficiaryDetails.getServiceProviderCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Service provider code is "+ serviceProviderCode);}

				String benfCode = util.isNullOrEmpty(ft_BeneficiaryDetails)? Constants.EMPTY_STRING : ft_BeneficiaryDetails.getBeneficiaryID();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary code is "+ benfCode);}

				String contractNo = util.isNullOrEmpty(ft_BeneficiaryDetails)? Constants.EMPTY_STRING : ft_BeneficiaryDetails.getBenefContractNo();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary Contract number is "+ contractNo);}

				String billNo = util.isNullOrEmpty(ft_BeneficiaryDetails)? Constants.EMPTY_STRING : ft_BeneficiaryDetails.getBenefBillNo();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary bill number is "+ billNo);}


				int msisdn = Constants.GL_ZERO;
				if(ft_BeneficiaryDetails.getBenefGSMNo() != null && !(ft_BeneficiaryDetails.getBenefGSMNo()).equalsIgnoreCase(Constants.EMPTY_STRING)
						&& !(ft_BeneficiaryDetails.getBenefGSMNo()).equalsIgnoreCase(Constants.NULL) && !(ft_BeneficiaryDetails.getBenefGSMNo()).equalsIgnoreCase(Constants.NA)){

					msisdn = Integer.parseInt(ft_BeneficiaryDetails.getBenefGSMNo());
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "MSISDN number is "+ msisdn);}

				String selectedacctNo = (String)callInfo.getField(Field.SRCNO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User selected source account number"+ util.getSubstring(selectedacctNo, Constants.GL_FOUR));}

				String debitAcctID = selectedacctNo;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected account no is "+ util.maskCardOrAccountNumber(debitAcctID));}

				String debitAmt = Constants.ZERO;
				BigDecimal bigDebitAmt = null;		
				if(!util.isNullOrEmpty(callInfo.getField(Field.AMOUNT))){
					debitAmt = (String) callInfo.getField(Field.AMOUNT);
					bigDebitAmt = new BigDecimal(debitAmt);
				}


				String paymentAmt = Constants.ZERO;
				BigDecimal bigPayamt = null;		
				if(!util.isNullOrEmpty(callInfo.getField(Field.AMOUNT))){
					paymentAmt = (String) callInfo.getField(Field.AMOUNT);
					bigPayamt = new BigDecimal(debitAmt);
				}


				XMLGregorianCalendar debitValueDate =  util.getXMLGregorianCalendarNow();

				String bonusRechrgAmt = Constants.ZERO;
				BigDecimal big_BonusRechrgAmt = new BigDecimal(bonusRechrgAmt);

				//Payment type for charity is Charity
				String paymentType = Constants.HOST_FT_PAYMENTTYPE_CHARITY;

				String utilityCode = util.isNullOrEmpty(ft_BeneficiaryDetails)? Constants.EMPTY_STRING : ft_BeneficiaryDetails.getUtilityCode();


				//END

				UpdatePaymentDetails_HostRes updatePaymentDetails_HostRes = utilityBillPaymentDAO.getUtilityBillUpdPaymentHostRes(callInfo, paymentType, utilityCode, 
						serviceProviderCode, null, null, benfCode, contractNo, billNo, Constants.EMPTY_STRING, Constants.EMPTY_STRING, Constants.EMPTY_STRING, msisdn, debitAcctID, bigDebitAmt, debitValueDate, bigPayamt, null, big_BonusRechrgAmt, requestType);
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updatePaymentDetails_HostRes Object is :"+ updatePaymentDetails_HostRes);}

				callInfo.setUpdatePaymentDetails_HostRes(updatePaymentDetails_HostRes);
				code = updatePaymentDetails_HostRes.getErrorCode();


				//Setting transactionRefNo
				callInfo.setField(Field.Transaction_Ref_No, updatePaymentDetails_HostRes.getXferID());

				hostEndTime = updatePaymentDetails_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				hostResCode = updatePaymentDetails_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);

				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				/****Duplicate RRN Fix 25012016 *****/
				strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + callInfo.getField(Field.DESTNO)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.DESTNO)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_SERVICE_PROVIDER_CODE + Constants.EQUALTO + callInfo.getField(Field.SELECTEDSERVICEPROVIDER)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_UTILITY_NUMBER + Constants.EQUALTO + callInfo.getField(Field.UTILITYCODE)
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);
				/************************************/
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO +hostResCode+ hostResCode+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_TRANSREFNO + Constants.EQUALTO +updatePaymentDetails_HostRes.getXferID()
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(updatePaymentDetails_HostRes.getErrorDesc()) ?"NA" :updatePaymentDetails_HostRes.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for UtilityBillPaymentConfirmationImpl.UpdateBillPayment");}
					
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
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Deposit Dtls Inquriry host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updatePaymentDetails_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_UTILITYBILLPAYMENTBANK, updatePaymentDetails_HostRes.getHostResponseCode());
					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);

				}

			}else if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYOUTSIDEBM.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_FUNDTRANSFERREMITT);

				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The Feature name is " + callInfo.getField(Field.FEATURENAME));}
				/**
				 * Following field are not mandatory and we need to pass those value as empty.
				 */
				String benfAcctID = (String)callInfo.getField(Field.DESTNO);
				FT_BeneficiaryDetails ft_BeneficiaryDetails = null;

				if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes())){
					if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes().getFT_BeneficiaryAcctDetailsMap())){
						ft_BeneficiaryDetails = callInfo.getFT_BenfPayeeDetails_HostRes().getFT_BeneficiaryAcctDetailsMap().get(benfAcctID);
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Received Beneficiary Account Details " + ft_BeneficiaryDetails);}
					}
				}

				String xferID = Constants.EMPTY_STRING; //For a new transaction XferID should be empty - mentioned in the specification document
				String benfCode = util.isNullOrEmpty(ft_BeneficiaryDetails)? Constants.EMPTY_STRING : ft_BeneficiaryDetails.getBeneficiaryID();  // This might only applicable for ouside BM / Within BM transactions
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary code is " + benfCode);}


				String accWithBank = Constants.EMPTY_STRING;
				String bankCode = Constants.EMPTY_STRING;

				String customerRate =  Constants.ZERO;
				BigDecimal bigCutomerRate = new BigDecimal(customerRate);

				String paymentDetails = Constants.EMPTY_STRING;
				String purposeCode = Constants.EMPTY_STRING;
				int txnCode = Constants.GL_ZERO;
				String fullName = Constants.EMPTY_STRING;
				String nostroBankName = Constants.EMPTY_STRING;
				String gsmNo = Constants.EMPTY_STRING;
				String benfCustomer = Constants.EMPTY_STRING;
				String purchaseBenfAcctNo =ft_BeneficiaryDetails!=null ? ft_BeneficiaryDetails.getBenefAccountNo() : Constants.EMPTY_STRING;
				String benfLocation = Constants.EMPTY_STRING;
				String benfBranch = Constants.EMPTY_STRING;

				String ccyRate = Constants.ZERO;
				BigDecimal bigCCYRate = new BigDecimal(ccyRate);

				XMLGregorianCalendar debitValueDate = util.getXMLGregorianCalendarNow();

				String debitAmt = util.isNullOrEmpty(callInfo.getField(Field.AMOUNT)) ? Constants.ZERO : (String)callInfo.getField(Field.AMOUNT); //Either we need to set Debit amt or credit amt ..here we are setting debit amount
				BigDecimal big_DebitAmt = new BigDecimal(debitAmt);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Debit Amount is "+ big_DebitAmt);}
				
				String creditAmt =   Constants.ZERO;
				BigDecimal big_CreditAmt = new BigDecimal(creditAmt);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Credit Amount is "+ big_CreditAmt);}
				
				String acctID = (String)callInfo.getField(Field.SRCNO);

				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Source acct ending with " + util.getSubstring(acctID, Constants.GL_FOUR));}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Destination acct ending with "+ util.getSubstring(benfAcctID, Constants.GL_FOUR));}



				//			String fundsOBM_RequestType = Constants.HOST_REQUEST_TYPE_OF_EXTERNALTHIRDPARTYOUTSIDEBM;
				//				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Setted Request type is "+ fundsOBM_RequestType);}
				//				//END


				UpdateFTRemittPayment_HostRes updateFTRemittPayment_HostRes = fundsTransferRemittDAO.getFTTOBMUpdatePaymentOHostRes(callInfo, xferID, big_DebitAmt, big_CreditAmt, debitValueDate, 
						acctID, accWithBank, bankCode, bigCutomerRate, paymentDetails, purposeCode, txnCode, fullName, nostroBankName, gsmNo, benfCode, 
						benfCustomer, purchaseBenfAcctNo, benfLocation, benfBranch, benfAcctID, bigCCYRate, requestType);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updateFTRemittPayment_HostRes Object is :"+ updateFTRemittPayment_HostRes);}

				callInfo.setUpdateFTRemittPayment_HostRes(updateFTRemittPayment_HostRes);

				code = updateFTRemittPayment_HostRes.getErrorCode();

				//Setting transactionRefNo
				callInfo.setField(Field.Transaction_Ref_No, updateFTRemittPayment_HostRes.getXferID());


				hostEndTime = updateFTRemittPayment_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				hostResCode = updateFTRemittPayment_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);
				
				
				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				
				/****Duplicate RRN Fix 25012016 *****/
				try{
					strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + callInfo.getField(Field.DESTCURR)
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_EXCHANGE_RATE + Constants.EQUALTO + callInfo.getField(Field.EXCHANGE_RATES_VALUE)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					}catch(Exception e){}
					
					hostReportDetails.setHostInParams(strHostInParam);
					
				/************************************/
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_TRANSREFNO + Constants.EQUALTO +updateFTRemittPayment_HostRes.getXferID()
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(updateFTRemittPayment_HostRes.getErrorDesc()) ?"NA" :updateFTRemittPayment_HostRes.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				
				
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for UtilityBillPaymentConfirmationImpl.UpdateBillPayment");}
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
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for updateFTIntraPayment_HostRes service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updateFTRemittPayment_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_FUNDTRANSFERINTRA, updateFTRemittPayment_HostRes.getHostResponseCode());
					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);
				}
			}else{
				/**
				 * Following field are not mandatory and we need to pass those value as empty.
				 */

				String xferId = Constants.EMPTY_STRING; //For a new transaction XferID should be empty - mentioned in the specification document
				String benfCode = Constants.EMPTY_STRING;  // This might only applicable for ouside BM / Within BM transactions
				
				String creditAmt = Constants.ZERO;  //Either we need to set Debit amt or credit amt ..here we are setting debit amount
				BigDecimal big_CreditAmt = new BigDecimal(creditAmt);

				String serviceProviderID = Constants.EMPTY_STRING; //Only applicable for Utility Bill Payments
				String utilityID = Constants.EMPTY_STRING; //Only applicable for Utility Bill Payments
				String billID = Constants.EMPTY_STRING; //Only applicable for Utility Bill Payments
				String contractID = Constants.EMPTY_STRING; //Only applicable for Utility Bill Payments


				String debitAmt = (String)callInfo.getField(Field.AMOUNT);
				BigDecimal big_DebitAmt = new BigDecimal(debitAmt);

				String acctID = (String)callInfo.getField(Field.SRCNO);
				String destAcctID = (String)callInfo.getField(Field.DESTNO);

				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Source acct ending with " + util.getSubstring(acctID, Constants.GL_FOUR));}
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Destination acct ending with "+ util.getSubstring(destAcctID, Constants.GL_FOUR));}

				String featureName = util.isNullOrEmpty(callInfo.getField(Field.FEATURENAME))?Constants.NA : (String)callInfo.getField(Field.FEATURENAME);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The Feature name is "+ featureName);}
				
				if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM.equalsIgnoreCase(featureName)
						|| Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYWITHINBM_2.equalsIgnoreCase(featureName)){
					HashMap<String, FT_BeneficiaryDetails> beneficiaryDetailMap = null;
					
					if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes())){
						if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes().getFT_BeneficiaryDetailsMap())){
							beneficiaryDetailMap = callInfo.getFT_BenfPayeeDetails_HostRes().getFT_BeneficiaryDetailsMap();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Beneficiary Detail map retrieved from host is "  + beneficiaryDetailMap);}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of Beneficiary details  available is"  + beneficiaryDetailMap.size());}
						}else{
							throw new ServiceException("beneficiaryDetailMap object is null / Empty");
						}
					}else{
						throw new ServiceException("FT_BenfPayeeDetails_HostRes  object is null / Empty");
					}
					
					
					Iterator iter = beneficiaryDetailMap.keySet().iterator();
					String benefID = Constants.EMPTY_STRING;
					int count = Constants.GL_ZERO;
					String temp_Str = Constants.EMPTY_STRING;
					FT_BeneficiaryDetails temp_benefDetail = null;
					
					while(iter.hasNext()) {    
						benefID = (String)iter.next();    
						temp_benefDetail  = (FT_BeneficiaryDetails)beneficiaryDetailMap.get(benefID); 
						
						if(!util.isNullOrEmpty(temp_benefDetail) && !util.isNullOrEmpty(temp_benefDetail.getBenefAccountNo())){
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" beneficiary account number is " +temp_benefDetail.getBenefAccountNo());}
							temp_Str = temp_benefDetail.getBenefAccountNo();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+ benefID + "account number is"+temp_Str);}
							
							if(temp_Str.equalsIgnoreCase(destAcctID)){
								benfCode = benefID;
								serviceProviderID = temp_benefDetail.getServiceProviderCode();
								utilityID = temp_benefDetail.getUtilityCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the "+ benefID + "for the beneficiary code of the account number "+temp_Str);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the service provider code too as "+ serviceProviderID + "for the beneficiary code of the account number "+temp_Str);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the Utility code too as "+ utilityID + "for the beneficiary code of the account number "+temp_Str);}
								break;
							}
						}
					}
				}
				

				/**
				 * Added Request type for the funds transfer host access
				 */
				//				String fundsWBM_RequestType = Constants.EMPTY_STRING;
				//				if(Constants.FEATURENAME_FUNDSTRANSFERINTERNAL.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
				//					fundsWBM_RequestType = Constants.HOST_REQUEST_TYPE_OF_OWNTRANSFER;
				//				}else{
				//					fundsWBM_RequestType = Constants.HOST_REQUEST_TYPE_OF_INTERNALTHRIDPARTY;
				//				}
				//				
				//				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Setted Request type is "+ fundsWBM_RequestType);}
				//				//END

				UpdateFTIntraPayment_HostRes updateFTIntraPayment_HostRes = fundTransferIntraDAO.getFTInterPaymentUpdHostRes(callInfo, xferId, benfCode, big_DebitAmt, big_CreditAmt, acctID, 
						destAcctID, serviceProviderID, utilityID, billID, contractID,requestType);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updateFTIntraPayment_HostRes Object is :"+ updateFTIntraPayment_HostRes);}

				callInfo.setUpdateFTIntraPayment_HostRes(updateFTIntraPayment_HostRes);

				code = updateFTIntraPayment_HostRes.getErrorCode();


				//Setting transactionRefNo
				callInfo.setField(Field.Transaction_Ref_No, updateFTIntraPayment_HostRes.getXferID());


				hostEndTime = updateFTIntraPayment_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				hostResCode = updateFTIntraPayment_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);
				
				
				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				/****Duplicate RRN Fix 25012016 *****/
				try{
					strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + callInfo.getField(Field.DESTCURR)
							+Constants.COMMA + Constants.HOST_INPUT_PARAM_EXCHANGE_RATE + Constants.EQUALTO + callInfo.getField(Field.EXCHANGE_RATES_VALUE)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					}catch(Exception e){}
				/************************************/
				
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_TRANSREFNO + Constants.EQUALTO +updateFTIntraPayment_HostRes.getXferID()
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(updateFTIntraPayment_HostRes.getErrorDesc()) ?"NA" :updateFTIntraPayment_HostRes.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);

				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				/**
				 * Updating the isOTPCalledAfterDisconnect flag as false
				 */
				callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, false);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updating Is OTP Called After Disconnected " +false );}
				//END
				
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for UtilityBillPaymentConfirmationImpl.UpdateBillPayment");}
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
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for updateFTIntraPayment_HostRes service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + updateFTIntraPayment_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_FUNDTRANSFERINTRA, updateFTIntraPayment_HostRes.getHostResponseCode());
					/**
					 * Following will be called only if there occurred account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);

				}
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferConfirmationImpl.updateFTPayment() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getFTSuccessAnnouncement(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferConfirmationImpl.getFTSuccessAnnouncement()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			String transRefID = Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature name is " + (String)callInfo.getField(Field.FEATURENAME));}

			if(Constants.FEATURENAME_FUNDSTRANSFERCHARITY.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
				if(!util.isNullOrEmpty(callInfo.getUpdatePaymentDetails_HostRes())){
					transRefID = callInfo.getUpdatePaymentDetails_HostRes().getXferID();
				}
			}else if(Constants.FEATURENAME_FUNDSTRANSFERTHIRDPARTYOUTSIDEBM.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
				if(!util.isNullOrEmpty(callInfo.getUpdateFTRemittPayment_HostRes())){
					transRefID = callInfo.getUpdateFTRemittPayment_HostRes().getXferID();
				}
			}else{
				if(!util.isNullOrEmpty(callInfo.getUpdateFTIntraPayment_HostRes())){
					transRefID = callInfo.getUpdateFTIntraPayment_HostRes().getXferID();
				}
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
			String featureID = FeatureIDMap.getFeatureID("FundsTransfer_Confirmation");
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
