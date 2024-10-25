package com.servion.services;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.FetchCardServiceHistoryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.fetchCardServiceHistory.FetchCardServiceHistory_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class FetchCardServiceHistoryImpl implements IFetchCardServiceHistory{

	private static Logger logger = LoggerObject.getLogger();
	
	private FetchCardServiceHistoryDAO fetchCardServiceHistoryDAO; 
	private MessageSource messageSource;
	
	public FetchCardServiceHistoryDAO getFetchCardServiceHistoryDAO() {
		return fetchCardServiceHistoryDAO;
	}

	public void setFetchCardServiceHistoryDAO(
			FetchCardServiceHistoryDAO fetchCardServiceHistoryDAO) {
		this.fetchCardServiceHistoryDAO = fetchCardServiceHistoryDAO;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getCardEmbossingDate(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FetchCardServiceHistoryImpl.getCreditCardBalance()");}
		String code = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			String cardNo = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
			if(util.isNullOrEmpty(cardNo)){
				throw new ServiceException("Selected Card OR Acct No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Selected Credit card number is " + util.maskCardOrAccountNumber(cardNo));}
			

			String entity = Constants.EMPTY_STRING;
			String serviceType = Constants.EMPTY_STRING;
			boolean activateServices = false;

			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("iceGlobalConfig object is null");
			}
			
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			entity = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FetchCardServiceHistory_Entity);
			serviceType = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FetchCardServiceHistory_ServiceType);
			Object temp = ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FetchCardServiceHistory_ActivateServices);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The activateServices object value for FetchCardServiceHistory is " + temp);}
			if(temp instanceof Boolean){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Boolean instance ");}
				activateServices = (Boolean)temp;
			}else{
				activateServices = Boolean.valueOf((String)temp);
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entity value for FetchCardServiceHistory is " + entity);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The serviceType value for FetchCardServiceHistory is " + serviceType);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The activateServices value for FetchCardServiceHistory is " + activateServices);}

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
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_CARDEMBOSSNUMBER + Constants.EQUALTO + cardNo
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(cardNo)
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_FETCHCARDSERVICEHISTORY);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
			
			
			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
		
			hostReportDetails.setHostStartTime(startTime); //It should be in the format of 31/07/2013 18:11:11
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

			String requestType = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FetchCardServiceHistory_RequestType ))? null : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FetchCardServiceHistory_RequestType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			FetchCardServiceHistory_HostRes fetchCardServiceHistory_HostRes = fetchCardServiceHistoryDAO.getFetchCardServiceHistoryHostRes(callInfo, entity, cardNo, serviceType, activateServices, requestType);


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "fetchCardServiceHistory_HostRes Object is :"+ fetchCardServiceHistory_HostRes);}
			callInfo.setFetchCardServiceHistory_HostRes(fetchCardServiceHistory_HostRes);

			code = fetchCardServiceHistory_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = fetchCardServiceHistory_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}


			strHostInParam = 	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(cardNo)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
					
			
			String hostResCode = fetchCardServiceHistory_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
	
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(fetchCardServiceHistory_HostRes.getErrorDesc()) ?"NA" :fetchCardServiceHistory_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for FetchCardServiceHistory Service");}

			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Credit Group Inquiry host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + fetchCardServiceHistory_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_FETCHCARDSERVICEHISTORY, fetchCardServiceHistory_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo,hostResCode);
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FetchCardServiceHistoryImpl.getCreditCardBalance() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public boolean isCardEmbossingDateEligibleForActivation(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FetchCardServiceHistoryImpl.isCardEmbossingDateEligibleForActivation()");}
		boolean returnValue = false;
		String cardEmbDateEligibleForActivation = Constants.EMPTY_STRING;
		String cardEmbDate = Constants.EMPTY_STRING;
		XMLGregorianCalendar cardEmbDateEligibleForActivationXmlGreg = null;
		XMLGregorianCalendar cardEmbDateXmlGreg = null; 
		try{
			
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			cardEmbDateEligibleForActivation = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CardEmbDate_EligibleForActivation);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CardEmbDateEligibleForActivation value is:"+cardEmbDateEligibleForActivation);}
			FetchCardServiceHistory_HostRes fetchCardServiceHistory_HostRes = callInfo.getFetchCardServiceHistory_HostRes();
			if(fetchCardServiceHistory_HostRes!=null && fetchCardServiceHistory_HostRes.getLastActionDateTime()!=null
					&& !(fetchCardServiceHistory_HostRes.getLastActionDateTime().trim().isEmpty())){
			cardEmbDate = callInfo.getFetchCardServiceHistory_HostRes().getLastActionDateTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "cardEmbDate value is:"+cardEmbDate);}
			cardEmbDateEligibleForActivationXmlGreg = util.convertDateStringtoXMLGregCalendar(cardEmbDateEligibleForActivation, Constants.DATEFORMAT_yyyy_MM_ddHH_mm_ss);
			cardEmbDateXmlGreg = util.convertDateStringtoXMLGregCalendar(cardEmbDate, Constants.DATEFORMAT_yyyy_MM_ddTHH_mm_ss);
			int date_diff = cardEmbDateXmlGreg.toGregorianCalendar().compareTo(cardEmbDateEligibleForActivationXmlGreg.toGregorianCalendar());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Date Diff value is:"+date_diff);}
			if(date_diff > 0){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Embossing date is greater than the configured date");}
				returnValue = true;
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Embossing date is lesser/equal to the configured date");}
				returnValue = false;
			}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Embossing date or fetchCardServiceHistory_HostRes is null/empty");}
				returnValue = false;
			}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FetchCardServiceHistoryImpl.isCardEmbossingDateEligibleForActivation() "+ e.getMessage());}
			throw new ServiceException(e);
		}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FetchCardServiceHistoryImpl.isCardEmbossingDateEligibleForActivation return Value:"+returnValue);}
		return returnValue;
	}
		
}
