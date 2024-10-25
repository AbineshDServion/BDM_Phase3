package com.servion.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.bankmuscat.esb.feedbackmanagementservice.CaseListDtlsType;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.FetchCaseListOrCountDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.complaintAlert.CheckComplaintID_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class ComplaintAlertImpl implements IComplaintAlert{
	
	private static Logger logger = LoggerObject.getLogger();
	
	private FetchCaseListOrCountDAO fetchCaseListOrCountDAO; 
	
	private MessageSource messageSource;

	public FetchCaseListOrCountDAO getFetchCaseListOrCountDAO() {
		return fetchCaseListOrCountDAO;
	}

	public void setFetchCaseListOrCountDAO(
			FetchCaseListOrCountDAO fetchCaseListOrCountDAO) {
		this.fetchCaseListOrCountDAO = fetchCaseListOrCountDAO;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	

	@Override
	public String getComplaintAlertMenuPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ComplaintAlertImpl.getComplaintAlertMenuPhrases()");}
		String str_GetMessage, finalResult, bankingWith, status;
		String code = Constants.EMPTY_STRING;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Rule Object values");}
			//ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			ICERuleParam iceRuleParam = (ICERuleParam)callInfo.getICERuleParam();
			
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			bankingWith = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BANKINGWITH ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BANKINGWITH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "BankingWith configured is " + bankingWith);}

			status = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_STATUS ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_STATUS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Status configured is " + status);}
			
			/***CTI CRM CR***/
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
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_BANKINGWITH + Constants.EQUALTO + bankingWith
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STATUS + Constants.EQUALTO + status
			
			
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE   + iceFeatureData.getConfig().getParamValue(Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE ));}
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			CheckComplaintID_HostRes checkComplaintID_HostRes = fetchCaseListOrCountDAO.getFetchCaseListOrCountHostRes(callInfo, customerIDObbj, bankingWith, status, requestType);
					

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "checkComplaintID_HostRes Object is :"+ checkComplaintID_HostRes);}
			//callInfo.setCreditCardGroupInq_HostRes(creditCardGroupInq_HostRes);

			code = checkComplaintID_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = checkComplaintID_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}


			strHostInParam = 	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_BANKINGWITH + Constants.EQUALTO + bankingWith
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STATUS + Constants.EQUALTO + status
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
					
			
			String hostResCode = checkComplaintID_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
	
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(checkComplaintID_HostRes.getErrorDesc()) ?"NA" :checkComplaintID_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
						
			/***CTI CRM CR***/
			
			//Integer caseCount
			
			List<CaseListDtlsType> caseListDtlsType = checkComplaintID_HostRes.getCaseListDtlsType();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CaseListDtlsType Object is " + caseListDtlsType);}
			
			String strRequestDate = Constants.EMPTY;
			String strRequestType = Constants.EMPTY;
			String strSlaDate = Constants.EMPTY;
			String strtodayDate = Constants.EMPTY;
			boolean isTodaygreaterSLA = false;
			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}

			
			if(!util.isNullOrEmpty(caseListDtlsType) && caseListDtlsType.size() >0){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Case Count is " + caseListDtlsType.size());}
				
				Collections.sort(caseListDtlsType, new Comparator<CaseListDtlsType>(){
		        	public int compare(CaseListDtlsType o1, CaseListDtlsType o2) {
		        		//o1.getCaseAddnlInfo().getDateCreated_0020()
		        		return o1.getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime().compareTo(o2.getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime());
		                //return o1.getProcMtrxDtls().getDateCreated().toGregorianCalendar().getTime().compareTo(o2.getProcMtrxDtls().getDateCreated().toGregorianCalendar().getTime());
		            }
		        });
				
				
				//strRequestDate = caseListDtlsType.get(caseListDtlsType.size()-1).getProcMtrxDtls().getDateCreated().toGregorianCalendar().getTime().toString();
				//strRequestDate = caseListDtlsType.get(caseListDtlsType.size()-1).getCaseAddnlInfo().getDateCreated_0020().toGregorianCalendar().getTime().toString();
				strRequestDate = util.convertXMLCalendarToString(caseListDtlsType.get(caseListDtlsType.size()-1).getCaseAddnlInfo().getDateCreated(), Constants.DATEFORMAT_YYYYMMDD);
				strRequestType = caseListDtlsType.get(caseListDtlsType.size()-1).getProcMtrxDtls().getArea();
				strSlaDate = util.convertXMLCalendarToString(caseListDtlsType.get(caseListDtlsType.size()-1).getCaseAddnlInfo().getSLADate(), Constants.DATEFORMAT_YYYYMMDD);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Date Date :" + strRequestDate);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SLA Date :" + strSlaDate);}
				
				
				
				
				strtodayDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDD);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today Date :" + strtodayDate);}
				
				isTodaygreaterSLA = util.isgreaterDate(strtodayDate, strSlaDate, Constants.DATEFORMAT_YYYYMMDD);
				callInfo.setField(Field.HASCOMPLAINTEXPIRED, isTodaygreaterSLA);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "is Today Greater :" + isTodaygreaterSLA);}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Setting Customer Segment in the Rule Engine " + true);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCOMPLAINTTIMEEXPIRED, "true");
				
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Customer Segment in the Rule Engine " + false);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCOMPLAINTTIMEEXPIRED, "false");
			}
			
						
			//Collected values from Rule 
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Request Date :" + strRequestDate);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Request Type :" + strRequestType);}
			
			
			
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Updating the RuleEngine Object");}
			ruleParamObj.updateIVRFields();
			//END Rule Engine Updation
			
			if(!util.isNullOrEmpty(strRequestDate)  
					//&& !util.isNullOrEmpty(strSlaDate)
					//&& !util.isNullOrEmpty(strRequestType)
					){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entering Complaint Pharse Formation : ");}
				
				//Adding the values to dynamic 
				//dynamicValueArray.add(strRequestType+Constants.WAV_EXTENSION);
				dynamicValueArray.add(strRequestDate);
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
				
				//Getting language code 
				String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
				Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}
				
				//Forming Key combination 
				String anncID = AnncIDMap.getAnncID("Complaint_Message");
				String featureID = FeatureIDMap.getFeatureID("Complaint_Alert");
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
				
				/**
				 * Following changes are done for the new Complaint Alert CR
				 */
				/*if(curr_date.compareTo(sla_Date)>0){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Since the resolving date over exceeding, updating the total pormpt count to 4");}
					totalPrompt = Constants.GL_FOUR;
				}*/
				String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
				String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

				finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			}else{
				finalResult=Constants.EMPTY_STRING;
			}
		   

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ComplaintAlertImpl.getComplaintAlertMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  ComplaintAlertImpl.getComplaintAlertMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}
	
	/**
	 * 
	 * @param callInfo
	 * @return
	 * @throws ServiceException
	 * As per the new complaint alert change, need to false the expired status to continue the IVR
	 */
	@Override
	public void changeComplaintStatus(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ComplaintAlertImpl.changeComplaintStatus()");}
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Rule Object values");}
			
			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Setting Customer complaint expired staus in the Rule Engine " + false);}
			ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCOMPLAINTTIMEEXPIRED, "false");
				
				/**
				 * Following change is to have the correct transfer reason at the CTI screen
				 */
				callInfo.setField(Field.HASCOMPLAINTEXPIRED, Constants.FALSE);
						
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Updating the RuleEngine Object");}
			ruleParamObj.updateIVRFields();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "RuleEngine Object updated");}
			//END Rule Engine Updation
			
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  ComplaintAlertImpl.changeComplaintStatus() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
	
	}
	
	@Override
	public String getComplaintDetailsMenuPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ComplaintAlertImpl.getComplaintDetailsMenuPhrases()");}
		String str_GetMessage, finalResult = Constants.EMPTY_STRING, bankingWith, status, strNoOfOpenComplaintAnnc, strOpenComplaintStatusCode, strClosedComplaintStatusCode, strResolvedComplaintStatusCode;
		String strClosedOrResolvedAnncExpiryDays;
		String strdefaultClosedOrResolvedAnncExpiryDays = "31";
		int intClosedOrResolvedAnncExpiryDays=31;
		String code = Constants.EMPTY_STRING;
		int defaultNoOfOpenComplaintAnnc = 5, noOfOpenComplaintAnnc = 0;

		try{
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Rule Object values");}
			//ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			ICERuleParam iceRuleParam = (ICERuleParam)callInfo.getICERuleParam();
			
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			bankingWith = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BANKINGWITH ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BANKINGWITH);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "BankingWith configured is " + bankingWith);}

			status = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_STATUS ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_STATUS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Status configured is " + status);}
			
			strNoOfOpenComplaintAnnc = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_NO_OF_OPEN_COMPLAINTS_ANNC ))? ""+defaultNoOfOpenComplaintAnnc : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_NO_OF_OPEN_COMPLAINTS_ANNC);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No of Open Complaint Annc configured is " + status);}
			
			strClosedOrResolvedAnncExpiryDays = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CLOSED_RESOLVED_ANNC_EXPIRYDAYS ))? ""+strdefaultClosedOrResolvedAnncExpiryDays : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CLOSED_RESOLVED_ANNC_EXPIRYDAYS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Closed Or Resolved Annc Expiry Days configured is " + strClosedOrResolvedAnncExpiryDays);}
			
			if(!util.isNullOrEmpty(strClosedOrResolvedAnncExpiryDays)) {
				try {
				intClosedOrResolvedAnncExpiryDays = Integer.parseInt(strClosedOrResolvedAnncExpiryDays);
				}catch(Exception e) {
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_, "Exception in converting  strClosedOrResolvedAnncExpiryDays" + e);}
				}
			}
			
			try {
				noOfOpenComplaintAnnc = Integer.parseInt(strNoOfOpenComplaintAnnc);
			}catch(NumberFormatException e) {
				noOfOpenComplaintAnnc = defaultNoOfOpenComplaintAnnc;
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No of Open Complaint to Annc is " + noOfOpenComplaintAnnc);}
			
			strOpenComplaintStatusCode = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_OPEN_COMPLAINT_STATUSCODE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_OPEN_COMPLAINT_STATUSCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Open Complaint status code configured is " + strOpenComplaintStatusCode);}

			strClosedComplaintStatusCode = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_CLOSED_COMPLAINT_STATUSCODE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_CLOSED_COMPLAINT_STATUSCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Closed Complaint status code configured is " + strClosedComplaintStatusCode);}
			
			strResolvedComplaintStatusCode = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_RESOLVED_COMPLAINT_STATUSCODE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_RESOLVED_COMPLAINT_STATUSCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Resolved Complaint status code configured is " + strResolvedComplaintStatusCode);}

			strResolvedComplaintStatusCode = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_RESOLVED_COMPLAINT_STATUSCODE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_RESOLVED_COMPLAINT_STATUSCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Resolved Complaint status code configured is " + strResolvedComplaintStatusCode);}
			
			/***CTI CRM CR***/
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
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_BANKINGWITH + Constants.EQUALTO + bankingWith
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STATUS + Constants.EQUALTO + status
			
			
//			Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + cardEmbossNum
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + callInfo.getField(Field.SRCTYPE)
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
//			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime ;
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_FETCHCASELISTORCOUNT);
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE   + iceFeatureData.getConfig().getParamValue(Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE ));}
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE ))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_FETCHCASELISTORCOUNT_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			CheckComplaintID_HostRes checkComplaintID_HostRes = fetchCaseListOrCountDAO.getFetchCaseListOrCountHostRes(callInfo, customerIDObbj, bankingWith, null, requestType);
					

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "checkComplaintID_HostRes Object is :"+ checkComplaintID_HostRes);}
			//callInfo.setCreditCardGroupInq_HostRes(creditCardGroupInq_HostRes);
			callInfo.setCheckComplaintID_HostRes(checkComplaintID_HostRes);

			code = checkComplaintID_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			
			String hostEndTime = checkComplaintID_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}


			strHostInParam = 	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_BANKINGWITH + Constants.EQUALTO + bankingWith
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_STATUS + Constants.EQUALTO + status
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
					
			
			String hostResCode = checkComplaintID_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
	
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(checkComplaintID_HostRes.getErrorDesc()) ?"NA" :checkComplaintID_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
						
			/***CTI CRM CR***/
			
			//Integer caseCount
			
			List<CaseListDtlsType> caseListDtlsType = checkComplaintID_HostRes.getCaseListDtlsType();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CaseListDtlsType Object is " + caseListDtlsType);}
			List<CaseListDtlsType> caseListDtlsTypeSelected = new ArrayList<CaseListDtlsType>();
			List<CaseListDtlsType> openCaseListDtlsType = new ArrayList<CaseListDtlsType>();
			List<CaseListDtlsType> closedCaseListDtlsType = new ArrayList<CaseListDtlsType>();
			
			
			String strRequestDate = Constants.EMPTY;
			String strSlaDate = Constants.EMPTY;
			String strtodayDate = Constants.EMPTY;
			String strCaseNumber = Constants.EMPTY;
			boolean isTodaygreaterSLA = false;
			boolean isClosedOrResolvedCaseAvailable = false;
			

			
			if(!util.isNullOrEmpty(caseListDtlsType) && caseListDtlsType.size() >0){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Case Count is " + caseListDtlsType.size());}
				for(CaseListDtlsType caseList : caseListDtlsType) {
					if(util.isCodePresentInTheConfigurationList(caseList.getCaseAddnlInfo().getStatus(), strOpenComplaintStatusCode)) {
						openCaseListDtlsType.add(caseList);
					}else if(util.isCodePresentInTheConfigurationList(caseList.getCaseAddnlInfo().getStatus(), strClosedComplaintStatusCode)) {
						closedCaseListDtlsType.add(caseList);
					}else if(util.isCodePresentInTheConfigurationList(caseList.getCaseAddnlInfo().getStatus(), strResolvedComplaintStatusCode)) {
						closedCaseListDtlsType.add(caseList);
					}
				}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Open Case Count is " + openCaseListDtlsType.size());}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Closed/Resolved Case Count is " + closedCaseListDtlsType.size());}
				
				if(openCaseListDtlsType != null && openCaseListDtlsType.size() > 0) {
					Collections.sort(openCaseListDtlsType, new Comparator<CaseListDtlsType>(){
						public int compare(CaseListDtlsType o1, CaseListDtlsType o2) {
							return o1.getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime().compareTo(o2.getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime());
						}
					}.reversed());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Open Case List values before limiting:" + openCaseListDtlsType.size());}
					for(CaseListDtlsType cldt : openCaseListDtlsType) {
						if(logger.isDebugEnabled()){
							WriteLog.write(WriteLog.DEBUG,session_ID_, "CaseNumber:"+cldt.getCaseAddnlInfo().getFeedbackId()+", Status:"+cldt.getCaseAddnlInfo().getStatus()+", Created:"+cldt.getCaseAddnlInfo().getDateCreated()
								+", SLADate:"+cldt.getCaseAddnlInfo().getSLADate());}
					}
					caseListDtlsTypeSelected = openCaseListDtlsType.stream().limit(noOfOpenComplaintAnnc).collect(Collectors.toList());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Open Case List values after limiting:" + caseListDtlsTypeSelected.size());}
					for(CaseListDtlsType cldt : caseListDtlsTypeSelected) {
						if(logger.isDebugEnabled()){
							WriteLog.write(WriteLog.DEBUG,session_ID_, "CaseNumber:"+cldt.getCaseAddnlInfo().getFeedbackId()+", Status:"+cldt.getCaseAddnlInfo().getStatus()+", Created:"+cldt.getCaseAddnlInfo().getDateCreated()
								+", SLADate:"+cldt.getCaseAddnlInfo().getSLADate());}
					}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total caseListDtlsTypeSelected open Count is " + caseListDtlsTypeSelected.size());}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total caseListDtlsTypeSelected open case list: " + caseListDtlsTypeSelected);}
				
				if(closedCaseListDtlsType != null && closedCaseListDtlsType.size() > 0) {
					Collections.sort(closedCaseListDtlsType, new Comparator<CaseListDtlsType>(){
						public int compare(CaseListDtlsType o1, CaseListDtlsType o2) {
							//return o1.getCaseAddnlInfo().getCloseDate().toGregorianCalendar().getTime().compareTo(o2.getCaseAddnlInfo().getCloseDate().toGregorianCalendar().getTime());
							return o1.getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime().compareTo(o2.getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime());
						}
					}.reversed());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Closed/Resolved Case List values :" + closedCaseListDtlsType.size());}
					for(CaseListDtlsType cldt : closedCaseListDtlsType) {
						if(logger.isDebugEnabled()){
							WriteLog.write(WriteLog.DEBUG,session_ID_, "CaseNumber:"+cldt.getCaseAddnlInfo().getFeedbackId()+", Status:"+cldt.getCaseAddnlInfo().getStatus()+", Created:"+cldt.getCaseAddnlInfo().getDateCreated()
								+", SLADate:"+cldt.getCaseAddnlInfo().getSLADate());}
					}
					LocalDate now = LocalDate.now();
					LocalDate closedDate = closedCaseListDtlsType.get(0).getCaseAddnlInfo().getDateCreated().toGregorianCalendar().getTime()
							.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					//LocalDate closedDate = closedCaseListDtlsType.get(closedCaseListDtlsType.size()-1).getCaseAddnlInfo().getCloseDate().toGregorianCalendar().getTime()
					//		.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					long days = ChronoUnit.DAYS.between(now, closedDate);
					//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Complaint ID:"+closedCaseListDtlsType.get(closedCaseListDtlsType.size()-1).getCaseAddnlInfo().getFeedbackId()+", Complaint Closed Date:"+closedDate+", Days:"+days);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Complaint ID:"+closedCaseListDtlsType.get(0).getCaseAddnlInfo().getFeedbackId()+", Complaint Opened Date:"+closedDate+", Days:"+days);}
					if(days > -31) {
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Closed/Resolved available within last 30 days");}
						//caseListDtlsTypeSelected.add(closedCaseListDtlsType.get(closedCaseListDtlsType.size()-1));
						caseListDtlsTypeSelected.add(closedCaseListDtlsType.get(0));
						isClosedOrResolvedCaseAvailable = true;
					}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Open and closed/resolved Case List values :" + caseListDtlsTypeSelected.size());}
				for(CaseListDtlsType cldt : caseListDtlsTypeSelected) {
					if(logger.isDebugEnabled()){
						WriteLog.write(WriteLog.DEBUG,session_ID_, "CaseNumber:"+cldt.getCaseAddnlInfo().getFeedbackId()+", Status:"+cldt.getCaseAddnlInfo().getStatus()+", Created:"+cldt.getCaseAddnlInfo().getDateCreated()
							+", SLADate:"+cldt.getCaseAddnlInfo().getSLADate());}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total caseListDtlsTypeSelected closed Count is " + caseListDtlsTypeSelected.size());}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total caseListDtlsTypeSelected open and closed case list: " + caseListDtlsTypeSelected);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total NUMBER OF CASE TO BE ANNOUNCED: " + caseListDtlsTypeSelected.size());}
				ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
				for(int x=0; x < caseListDtlsTypeSelected.size(); x++) {
					if(!dynamicValueArray.isEmpty()) {
						dynamicValueArray.clear();
					}
					strCaseNumber = caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getFeedbackId();
					strRequestDate = util.convertXMLCalendarToString(caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getDateCreated(), Constants.DATEFORMAT_YYYYMMDD);
					//if(x<caseListDtlsTypeSelected.size()-1) {
					if(x<caseListDtlsTypeSelected.size()) {
						strSlaDate = util.convertXMLCalendarToString(caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getSLADate(), Constants.DATEFORMAT_YYYYMMDD);
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Case Number :" + strCaseNumber);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Created Date :" + strRequestDate);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "SLA Date :" + strSlaDate);}
					
					if(util.isCodePresentInTheConfigurationList(caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getStatus(), strOpenComplaintStatusCode)) {
						
					strtodayDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today Date :" + strtodayDate);}
					
					isTodaygreaterSLA = util.isgreaterDate(strtodayDate, strSlaDate, Constants.DATEFORMAT_YYYYMMDD);
					callInfo.setField(Field.HASCOMPLAINTEXPIRED, isTodaygreaterSLA);
					if(isTodaygreaterSLA) {
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Complaint ID: "+caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getFeedbackId() + " is expired");}
						callInfo.setField(Field.HAS_ANY_COMPLAINT_EXPIRED, isTodaygreaterSLA);
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "is Today Greater :" + isTodaygreaterSLA);}
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entering Existing Complaint announcement Pharse Formation : ");}
					
					dynamicValueArray.add(strCaseNumber);
					dynamicValueArray.add(strRequestDate);
					//if(x<caseListDtlsTypeSelected.size()-1) {
					if(x<caseListDtlsTypeSelected.size()) {
						dynamicValueArray.add(strSlaDate);
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
					
					//Getting language code 
					String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
					Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}
					
					//Forming Key combination 
					String anncID = AnncIDMap.getAnncID("Existing_Complaint");
					String featureID = FeatureIDMap.getFeatureID("Complaint");
					String combinedKey = Constants.EMPTY; 
					if(x==caseListDtlsTypeSelected.size()-1 && isClosedOrResolvedCaseAvailable) {		
						if(util.isCodePresentInTheConfigurationList(caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getStatus(), strClosedComplaintStatusCode)) {
							combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID+Constants.UNDERSCORE+Constants.TWO;
						}else if(util.isCodePresentInTheConfigurationList(caseListDtlsTypeSelected.get(x).getCaseAddnlInfo().getStatus(), strResolvedComplaintStatusCode)) {
							combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID+Constants.UNDERSCORE+Constants.THREE;
						}
					}else {
						combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID+Constants.UNDERSCORE+Constants.ONE;
					}
					
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
					
					if(util.isNullOrEmpty(finalResult)) {
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else {
						finalResult = finalResult + "*" + util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
				}
			}else{
				finalResult=Constants.EMPTY_STRING;
			}
			}else {
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for FetchCaseListOrCount host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + checkComplaintID_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_FETCHCASELISTORCOUNT, checkComplaintID_HostRes.getHostResponseCode());
			}
			callInfo.setField(Field.INITIAL_PROMPT, finalResult);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ComplaintAlertImpl Final Phrase:"+finalResult);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ComplaintAlertImpl.getComplaintDetailsMenuPhrases()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  ComplaintAlertImpl.getComplaintDetailsMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		//return finalResult;
		return code;
	}
	
}
