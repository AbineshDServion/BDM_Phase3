package com.servion.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.GetCCCustDtlsDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.getCCCustDtls.GetCCCustDtls_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class GetCCCustDtlsImpl implements IGetCCCustDtls{

	private static Logger logger = LoggerObject.getLogger();
	
	private GetCCCustDtlsDAO getCCCustDtlsDAO; 
	private MessageSource messageSource;
	
	

	public GetCCCustDtlsDAO getGetCCCustDtlsDAO() {
		return getCCCustDtlsDAO;
	}

	public void setGetCCCustDtlsDAO(GetCCCustDtlsDAO getCCCustDtlsDAO) {
		this.getCCCustDtlsDAO = getCCCustDtlsDAO;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getGetCCCustDtls(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GetCCCustDtlsImpl.getGetCCCustDtls()");}
		String code = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			String cardNo = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
			if(util.isNullOrEmpty(cardNo)){
				throw new ServiceException("Selected Card OR Acct No is empty or null");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Selected Credit/Prepaid card number is " + util.maskCardOrAccountNumber(cardNo));}
			

			ICEGlobalConfig iceGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(iceGlobalConfig)){
				throw new ServiceException("iceGlobalConfig object is null");
			}
			
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
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

			String requestType = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_GetCCCustDtls_REQUESTTYPE ))? null : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_GetCCCustDtls_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			GetCCCustDtls_HostRes getCCCustDtls_HostRes = getCCCustDtlsDAO.getGetCCCustDtlsHostRes(callInfo, cardNo, requestType);


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getCCCustDtls_HostRes Object is :"+ getCCCustDtls_HostRes);}
			callInfo.setCCCustDtls_HostRes(getCCCustDtls_HostRes);

			code = getCCCustDtls_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = getCCCustDtls_HostRes.getHostEndTime();
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
					
			
			String hostResCode = getCCCustDtls_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
	
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(getCCCustDtls_HostRes.getErrorDesc()) ?"NA" :getCCCustDtls_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for FetchCardServiceHistory Service");}

			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Credit Group Inquiry host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + getCCCustDtls_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_GetCCCustDtls, getCCCustDtls_HostRes.getHostResponseCode());

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

			
}
