package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.db.beans.TblIVRData;
import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.GetCallDataDAO;
import com.servion.dao.SetCallDataDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;

public class RapCTIImpl implements IRapCTI {
	private static Logger logger = LoggerObject.getLogger();

	private GetCallDataDAO getCallDataDAO;
	private SetCallDataDAO setCallDataDAO;

	public GetCallDataDAO getGetCallDataDAO() {
		return getCallDataDAO;
	}

	public void setGetCallDataDAO(GetCallDataDAO getCallDataDAO) {
		this.getCallDataDAO = getCallDataDAO;
	}

	public SetCallDataDAO getSetCallDataDAO() {
		return setCallDataDAO;
	}

	public void setSetCallDataDAO(SetCallDataDAO setCallDataDAO) {
		this.setCallDataDAO = setCallDataDAO;
	}

	@Override
	public String SetCallData(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String code = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RapCTIImpl.SetCallData()");}

		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the SETCALLDATA DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the Call Data for CTI Pop UP screen");}

			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionId)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "session id is null or empty");}
				throw new ServiceException("session id is null or empty");
			}

			int id;
			String callUniqueID;
			String callTime;
			String cli;
			String dnis;
			String callType;
			String cif;
			String apinStatus;
			boolean apinVerified;
			String callTransferReason;
			String cardNumber;
			String mobileNumber;
			String lang;
			String dateTime;
			String apinTries;

			callUniqueID = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  callUnique ID is " + callUniqueID );}

			TblIVRData tblIVRData = (TblIVRData)callInfo.getField(Field.TblIVRData);

			if(util.isNullOrEmpty(tblIVRData)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "#########  This is a NEW CALL #########" + callUniqueID );}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "TblIVRData object from the Call Info is null or empty so creating new instance");}
				tblIVRData = new TblIVRData();
				tblIVRData.setCallUniqueID(callUniqueID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  ID will be set by DB ");}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "********* This is the call being connected back from Agent ********");}
				id = tblIVRData.getId();
				callUniqueID = tblIVRData.getCallUniqueID();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  ID is " + id);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Call Unique ID retrieved from TBLIvrData object is ");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  callUnique ID is " + callUniqueID );}
			}


			callTime = util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Currenty Time is " + callTime);}

			dateTime = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Currenty Date is " + dateTime);}

			cli =  util.isNullOrEmpty(callInfo.getField(Field.CLI))? Constants.NA : (String)callInfo.getField(Field.CLI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  CLI is " + cli);}

			dnis = util.isNullOrEmpty(callInfo.getField(Field.DNIS))? Constants.NA : (String)callInfo.getField(Field.DNIS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  DNIS is " + dnis);}

			callType = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE))? Constants.NA :(String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA  FOR RAP CTI###  callType / Customer Segment type is " + callType);}

			cif = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))? Constants.NA :(String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Customer id is " + cif);}

			apinStatus = util.isNullOrEmpty(callInfo.getField(Field.APIN_STATUS))? Constants.NA : (String)callInfo.getField(Field.APIN_STATUS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  APIN Status is " + apinStatus);}

			String str_APINVerf = Constants.N;
			if(callInfo.getField(Field.APIN_VALIDATED)!=null){
				apinVerified = (boolean) callInfo.getField(Field.APIN_VALIDATED);
				if(apinVerified){
					str_APINVerf = Constants.Y;
				}else{
					str_APINVerf = Constants.N;
				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  APIN Verified is " + str_APINVerf);}

			lang = (String)callInfo.getField(Field.LANGUAGE);
			lang = util.getLanguageKey(lang);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Language is " + lang);}

			cardNumber = util.isNullOrEmpty(callInfo.getField(Field.CIN))? Constants.NA : (String)callInfo.getField(Field.CIN);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA  FOR RAP CTI###  Card Number ending with is " + util.getSubstring(cardNumber, Constants.GL_FOUR));}

			mobileNumber =util.isNullOrEmpty(callInfo.getField(Field.REG_MOBILENO))? Constants.NA : (String)callInfo.getField(Field.REG_MOBILENO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Mobile number is " + mobileNumber);}

			
			/**
			 * Following are the condition handling for call Transfer Reason setting
			 */
			String callDisposition = util.isNullOrEmpty(callInfo.getField(Field.CALL_DISPOSITION))?Constants.THREE : (String)callInfo.getField(Field.CALL_DISPOSITION);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### The Call Disposition value is " + callDisposition);}
			
			if(Constants.ZERO.equalsIgnoreCase(callDisposition)){
				callTransferReason = Constants.TRANSFER_REASON_USER_OPTED;
			}else if(Constants.ONE.equalsIgnoreCase(callDisposition)){
				callTransferReason = Constants.TRANSFER_REASON_TRIES_EXCEEDED;
			}else if(Constants.TWO.equalsIgnoreCase(callDisposition)){
				callTransferReason = Constants.TRANSFER_REASON_HOST_ERROR;
			}else {
				callTransferReason = Constants.TRANSFER_REASON_USER_OPTED;
			}
			
			/**
			 * Following reason is for complaint alert changes
			 */
			Object obj = callInfo.getField(Field.HASCOMPLAINTEXPIRED);
			//String hasComplaintExpired = Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the complaint expired Object:" + obj);}
			if(obj instanceof Boolean){
				boolean hasComplaintExpiredBool = obj != null? (Boolean)obj : false;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the complaint expired boolean:" + hasComplaintExpiredBool);}
				
				if(hasComplaintExpiredBool){
					callTransferReason = Constants.TRANSFER_REASON_COMPLAINTALERT;
				}
				
			}else{
				String hasComplaintExpired = callInfo.getField(Field.HASCOMPLAINTEXPIRED) != null? (String)callInfo.getField(Field.HASCOMPLAINTEXPIRED) : Constants.FALSE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Has the complaint expired " + hasComplaintExpired);}
				
				if(Constants.TRUE.equalsIgnoreCase(hasComplaintExpired)){
					callTransferReason = Constants.TRANSFER_REASON_COMPLAINTALERT;
				}
			}
	
			//END - Vinoth
			
			//Complaint Changes
			if(!util.isNullOrEmpty(callInfo.getField(Field.CallTransferReason))) {
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Assigning call transfer reason for Complaint: " + (String) callInfo.getField(Field.CallTransferReason));}
				callTransferReason = (String) callInfo.getField(Field.CallTransferReason);
			}
			
			//callTransferReason =util.isNullOrEmpty(callInfo.getField(Field.CallTransferReason))?Constants.NA : (String)callInfo.getField(Field.CallTransferReason);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Call Transfer Reason is " + callTransferReason);}

			/*
			 * For APIN Tries Count
			 */
			apinTries = util.isNullOrEmpty(callInfo.getField(Field.APIN_TRIES))? Constants.ZERO : (String)callInfo.getField(Field.APIN_TRIES);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  APIN Tries count is " + apinTries);}
			//END
			
			/*
			 * For Session ID
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "### INPUT SETCALLDATA FOR RAP CTI###  Session ID " + sessionId);}

			tblIVRData.setCallTime(callTime);
			tblIVRData.setCli(cli);
			tblIVRData.setDnis(dnis);
			tblIVRData.setCallType(callType);
			tblIVRData.setCif(cif);
			tblIVRData.setaPINStatus(apinStatus);
			tblIVRData.setaPINVerified(str_APINVerf);
			tblIVRData.setLang(lang);
			tblIVRData.setCardNumber(cardNumber);
			tblIVRData.setMobileNumber(mobileNumber);
			tblIVRData.setaPINTries(apinTries);
			tblIVRData.setSessionID(sessionId);

			SimpleDateFormat dateformatter = new SimpleDateFormat(Constants.DATEFORMAT_YYYYMMDD);
			Date dateString = dateformatter.parse(dateTime);
			tblIVRData.setCallDateTime(dateString);

			tblIVRData.setCallTransferReason(callTransferReason);

			String menuPath = Constants.EMPTY_STRING;

			IvrData ivrdata = util.isNullOrEmpty(callInfo.getField(Field.IVRDATA))? null : (IvrData)callInfo.getField(Field.IVRDATA);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained IVR Data" + ivrdata);}
			
			tblIVRData.setLastMenu1("null");
			tblIVRData.setLastMenu2("null");
			tblIVRData.setLastMenu3("null");

			String lastMenu1 = Constants.EMPTY_STRING;
			String lastMenu2 = Constants.EMPTY_STRING;
			String lastMenu3 = Constants.EMPTY_STRING;
			
			if(ivrdata!=null && ivrdata.getCallInfo() != null &&  ivrdata.getCallInfo().getMenuPath() != null){

				menuPath = ivrdata.getCallInfo().getMenuPath();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Menu Path " + menuPath);}


//				if(menuPath != null){
//					String[] menuArray = menuPath.split(Constants.PIPESEPERATOR);
//
//					if(!util.isNullOrEmpty(menuArray) && !util.isNullOrEmpty(menuArray.length)){
//						if(menuArray.length >= Constants.GL_ONE && !util.isNullOrEmpty(menuArray[menuArray.length-1])){
//							lastMenu1 = menuArray[menuArray.length-1];
//						}
//						if(menuArray.length >= Constants.GL_TWO && !util.isNullOrEmpty(menuArray[menuArray.length-2])){
//							lastMenu2 = menuArray[(menuArray.length)-2];
//						}
//						if(menuArray.length >= Constants.GL_THREE && !util.isNullOrEmpty(menuArray[menuArray.length-3])){
//							lastMenu3 = menuArray[(menuArray.length)- 3];
//						}
//					}
//				}
				
				
				/**
				 * Modified code inorder to avoid the last menu values as null or empty
				 */
				
				if(!util.isNullOrEmpty(menuPath)){
					String[] menuArray = menuPath.split(Constants.PIPESEPERATOR);
					
					String[] lastMenuArray = new String[5];
					
					lastMenuArray[0] = Constants.EMPTY;
					lastMenuArray[1] = Constants.EMPTY;
					lastMenuArray[2] = Constants.EMPTY;
					lastMenuArray[3] = Constants.EMPTY;
					lastMenuArray[4] = Constants.EMPTY;
					
					int i; int k = 0; int j = 0;
					
					if(!util.isNullOrEmpty(menuArray)){

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Menu Array after pipe seperator is  " + menuArray);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Menu Array lenght is  " + menuArray.length);}

						for(i=0;i<menuArray.length;i++){
							j++;

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "J value is :"+j);}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "k value is :" +k);}

							lastMenuArray[k] = menuArray[menuArray.length - j];

							if(!util.isNullOrEmpty(lastMenuArray[k])){
								k++;
								if(!util.isNullOrEmpty(lastMenuArray[0]) && !util.isNullOrEmpty(lastMenuArray[1]) && !util.isNullOrEmpty(lastMenuArray[2])){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to Break the loop");}
									break;
								}
							}else{
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Continueing the loop");}
								continue;
							}
						}
					}
					
					lastMenu1 = lastMenuArray[0];
					lastMenu2 = lastMenuArray[1];
					lastMenu3 = lastMenuArray[2];
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Last Menu 1 is " + lastMenu1);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Last Menu 2 is " + lastMenu2);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Last Menu 3 is " + lastMenu3);}
			
			/**
			 * Following are the code to handle the feature appending at during transferring
			 */
			String lastAccessedFeatureName = (String)callInfo.getField(Field.FEATURENAMEBEFTRANSFER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature Before Transferring the call is " + lastAccessedFeatureName);}
			
			lastMenu1 = lastMenu1 + Constants.MINUS +lastAccessedFeatureName;
			lastMenu1 =lastMenu1.trim();
			//END Vinoth
			tblIVRData.setLastMenu1(lastMenu1);
			tblIVRData.setLastMenu2(lastMenu2);
			tblIVRData.setLastMenu3(lastMenu3);

			configMap.put(DBConstants.IVRDATA, tblIVRData);

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			code = dataServices.insertIVRData(logger, sessionId,callUniqueID, configMap);

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, " Successfully inserting the RAP CTI call data into DB" );}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, " inserting th RAP CTI call data into DB got failed" );}
			}


		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at RapCTIImpl.SetCallData() "+ e.getMessage());
			throw new ServiceException(e);
		}


		/**
		 * Calling the insert Call On Abandon method
		 */

		if(!util.isNullOrEmpty(Context.getiTransferToAgent())){

			try {
				String dbReturnCode = Context.getiTransferToAgent().insertCallAbandon(callInfo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Insert Call On Abandon DB method" + dbReturnCode);}

			} catch (ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  RapCTIImpl.SetCallData()");}
				//e.printStackTrace();
			}
		}
		
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: RapCTIImpl.SetCallData()");}
		//ENd Calling of insert call on abandon method
		return code;


		//		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RapCTIImpl.SetCallData()");}
		//		return Constants.ZERO;
		//		logger = (Logger)callInfo.getField(Field.LOGGER);
		//		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RapCTIImpl.SetCallData()");}
		//		String code = Constants.EMPTY_STRING;
		//
		//		try{
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
		//			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
		//
		//			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
		//				throw new ServiceException("ICEGlobalConfig object is null");
		//			}
		//
		//			
		//			/**
		//			 * For Reporting Purpose
		//			 */
		//			HostReportDetails hostReportDetails = new HostReportDetails();
		//			
		//			String featureId = (String)callInfo.getField(Field.FEATUREID);
		//			hostReportDetails.setHostActiveMenu(featureId);
		//			//hostReportDetails.setHostCounter(hostCounter);
		//			//hostReportDetails.setHostEndTime(hostEndTime);
		//			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
		//					callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + callInfo.getField(Field.ACCOUNTNUMBER);
		//			hostReportDetails.setHostInParams(strHostInParam);
		//			hostReportDetails.setHostMethod(Constants.HOST_METHOD_RAPCTI_SETDATA);
		//			//hostReportDetails.setHostOutParams(hostOutParams);
		//			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
		//			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
		//			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
		//			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
		//			
		//			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
		//			hostReportDetails.setHostType(Constants.HOST_TYPE_RAP);
		//			//End Reporting
		//			
		//			String str_deviceID = (String)callInfo.getField(Field.DEVICE_ID);
		//			int deviceID = util.isNullOrEmpty(str_deviceID)?Constants.GL_ZERO:Integer.parseInt(str_deviceID);
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested deviceID "+ deviceID);}
		//			
		//			String str_callId = (String)callInfo.getField(Field.CALL_ID);
		//			int callId = util.isNullOrEmpty(str_callId)?Constants.GL_ZERO:Integer.parseInt(str_callId);
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Callid "+ callId);}
		//			
		//			String str_mode = (String)callInfo.getField(Field.MODE);
		//			int mode = util.isNullOrEmpty(str_mode)?Constants.GL_ZERO:Integer.parseInt(str_mode);
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Mode "+ mode);}
		//			
		//			String str_data = (String)callInfo.getField(Field.DATA);
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Data "+ str_data);}
		//			
		//			SetData_HostRes setData_HostRes = setCallDataDAO.getCTISetCallDataHostRes(callInfo, callId, deviceID, mode, str_data);
		//
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "GetData_HostRes Object is :"+ setData_HostRes);}
		//			code = setData_HostRes.getErrorCode();
		//			
		//			
		//			/*
		//			 * For Reporting Start
		//			 */
		//			
		//			String hostEndTime = setData_HostRes.getHostEndTime();
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
		//			hostReportDetails.setHostEndTime(hostEndTime);
		//			
		//			String hostResCode = setData_HostRes.getHostResponseCode();
		//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
		//			hostReportDetails.setHostResponse(hostResCode);
		//			
		//			String responseDesc = Constants.HOST_FAILURE;
		//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
		//				responseDesc = Constants.HOST_SUCCESS;
		//			}
		//			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
		//					+ Constants.EQUALTO + hostResCode;
		//				
		//			hostReportDetails.setHostOutParams(hostOutputParam);
		//			
		//			callInfo.setHostReportDetails(hostReportDetails);
		//			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
		//			
		//			callInfo.insertMenuDetails(ivrdata);
		//			//End Reporting
		//			
		//			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
		//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for RAPCTI SETDATA request service");}
		//			}else{
		//
		//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Update setData_HostRes host service");}
		//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + setData_HostRes.getHostResponseCode());}
		//
		//				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_RAPCTI_SETDATA, setData_HostRes.getHostResponseCode());
		//			
		//			
		//			}
		//			
		//			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: RapCTIImpl.SetCallData()");}
		//			
		//		}catch(Exception e){
		//			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at RapCTIImpl.SetCallData() "+ e.getMessage());
		//			throw new ServiceException(e);
		//		}
		//		return code;
	}

	@Override
	public String getCallData(CallInfo callInfo) throws ServiceException {

		// logger = (Logger)callInfo.getField(Field.LOGGER);
		// if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,
		// "ENTER: RapCTIImpl.getCallData()");}
		// callInfo.setField(Field.CALL_ID, "1234567890");
		// return Constants.ZERO;

		// Need to do XML Parsing for getCallData response value
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: RapCTIImpl.getCallData()");
		}
		String code = Constants.EMPTY_STRING;

		try {

			//Setting callInfo field fromAgent as True
			callInfo.setField(Field.FromAgent, Constants.Y);


			/**
			 * Setting Rule Engine values
			 */

			ICERuleParam iceRuleParam = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(iceRuleParam)){
				if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"ICERuleParam object is null or empty" + iceRuleParam);}
				throw new ServiceException("ICERuleParam is null or empty");
			}

			iceRuleParam.setIVRParam(Constants.RULE_ENGINE_FROMAGENT, Constants.Y);

			//END for ICERuleParam value

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG,session_ID_,
						"Calling the GETCALLDATA DB Method ");
			}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG,session_ID_,
						"Setting the Call Data for CTI Pop UP screen");
			}

			String sessionId = (String) callInfo.getField(Field.SESSIONID);

			if (util.isNullOrEmpty(sessionId)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG,session_ID_,
							"session id is null or empty");
				}
				throw new ServiceException("session id is null or empty");
			}

			String callUniqueID = (String) callInfo.getField(Field.UUI);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG,session_ID_,
						"### INPUT FOR GETCALLDATA RAP CTI###  callUnique ID is "
								+ callUniqueID);
			}

			TblIVRData tblIVRData = new TblIVRData();
			tblIVRData.setCallUniqueID(callUniqueID);

			configMap.put(DBConstants.IVRDATA, tblIVRData);

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			code = dataServices.getIVRData(logger, sessionId, callUniqueID,
					configMap);


			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_," Successfully retrieved the RAP CTI call data from DB");
				}

				tblIVRData = (TblIVRData) configMap.get(DBConstants.IVRDATA);
				callInfo.setField(Field.TblIVRData, tblIVRData);
				if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Retrieved TblIVRData value is " + tblIVRData);}

				if(tblIVRData!=null){

					String customerID = (String)tblIVRData.getCif();
					if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Customer ID retrieved from CTI is " + customerID);}

					callInfo.setField(Field.CUSTOMERID, customerID);

					String language = (String)tblIVRData.getLang();
					if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Language retrieved from CTI is " + language);}

					callInfo.setField(Field.LANGUAGE, language);

					
					/**
					 * Following are the handling done for diconnecting the ivr while making conferencing calls
					 */
					String conferenceFlag = util.isNullOrEmpty(tblIVRData.getCbIvrFlag())?Constants.ALPHA_T : (String)tblIVRData.getCbIvrFlag();
					if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Conference flag for connect back to ivr is " + conferenceFlag);}
					callInfo.setField(Field.CONFERENCEFLAG, conferenceFlag);
					//END
					
					
					if(!Constants.ALPHA_T.equalsIgnoreCase(conferenceFlag)){
						callInfo.setField(Field.ISCONNECTBACKCALL, "true");
					}else{
						callInfo.setField(Field.ISCONNECTBACKCALL, "false");
					}
					if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Is need to plan tone masking while getting APIN input" + callInfo.getField(Field.ISCONNECTBACKCALL));}
					
					/**
					 * For setting the Islamic flow DM Property location path and values
					 */

					ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getField(Field.ICEGlobalConfig);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ivr_ICEGlobalConfig Object is " + ivr_ICEGlobalConfig);}

					/**
					 * Following are the changes done on 10-09-2014 for customer segment check
					 */
					
					String customerSegment = util.isNullOrEmpty(callInfo.getField(Field.CUST_SEGMENT_TYPE))?Constants.CUST_SEGMENT_RETAIL : (String)callInfo.getField(Field.CUST_SEGMENT_TYPE);
					if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"The DM property Customer segment is " + customerSegment);}
					//END - Vinoth
					
					String globalPropFile = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_GLOBAL+ Constants.UNDERSCORE + customerSegment);
					String dmPropLocation = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location+ Constants.UNDERSCORE + customerSegment);
					String dmPropLocation_English = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_English+ Constants.UNDERSCORE + customerSegment);
					String dmPropLocation_Arabic = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Arabic+ Constants.UNDERSCORE + customerSegment);
					String dmPropLocation_Hindi = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DM_Properties_Location_Hindi+ Constants.UNDERSCORE + customerSegment);

					String lang = Constants.EMPTY_STRING + callInfo.getField(Field.LANGUAGE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language is " + lang);}
					
					lang = util.getLanguageKey(lang);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "the util Language key for DM property value setting is " + lang);}
					
					if(!util.isNullOrEmpty(lang)){
						if(Constants.ALPHA_H.equalsIgnoreCase(lang) || Constants.Hindi.equalsIgnoreCase(lang) || Constants.Hin.equalsIgnoreCase(lang)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Hindi" + lang);}
							dmPropLocation =  dmPropLocation_Hindi;
						}else if(Constants.ALPHA_A.equalsIgnoreCase(lang)|| Constants.Arabic.equalsIgnoreCase(lang) || Constants.Arb.equalsIgnoreCase(lang)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Arabic" + lang);}
							dmPropLocation =  dmPropLocation_Arabic;
						}
						else if(Constants.ALPHA_U.equalsIgnoreCase(lang)|| Constants.Urudu.equalsIgnoreCase(lang) || Constants.Uru.equalsIgnoreCase(lang)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as Hindi" + lang);}
							dmPropLocation =  dmPropLocation_Hindi;
						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the language as English" + lang);}
							dmPropLocation =  dmPropLocation_English;
						}
					}

					callInfo.setField(Field.GLOBALPROPERTYFILE, globalPropFile);
					callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropLocation);
					callInfo.setField(Field.DMPROPERTIESLOCATIONENGLISH, dmPropLocation_English);
					callInfo.setField(Field.DMPROPERTIESLOCATIONARABIC, dmPropLocation_Arabic);
					callInfo.setField(Field.DMPROPERTIESLOCATIONHINDI, dmPropLocation_Hindi);
					
					String dmPropertyLocEnglish = (String)callInfo.getField(Field.DMPROPERTIESLOCATIONENGLISH);
					String dmPropertyLocArabic = (String)callInfo.getField(Field.DMPROPERTIESLOCATIONARABIC);
					String dmPropertyLocHindi = (String)callInfo.getField(Field.DMPROPERTIESLOCATIONHINDI);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Returing English Language Locale");}

					if(Constants.English.equalsIgnoreCase(language) || Constants.Eng.equalsIgnoreCase(language) || Constants.ALPHA_E.equalsIgnoreCase(language)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"setting English language ");}
						callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocEnglish);
					}else if(Constants.Arabic.equalsIgnoreCase(language) || Constants.Arb.equalsIgnoreCase(language) || Constants.ALPHA_A.equalsIgnoreCase(language)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Arabic Language Key");}
						callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocArabic);
					}else if(Constants.Hindi.equalsIgnoreCase(language) || Constants.Hin.equalsIgnoreCase(language) || Constants.ALPHA_H.equalsIgnoreCase(language)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Hindi Language Key");}
						callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocHindi);
					}else if(Constants.Urudu.equalsIgnoreCase(language) || Constants.Uru.equalsIgnoreCase(language) || Constants.ALPHA_U.equalsIgnoreCase(language)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Reuring Urudu Language Key");}
						callInfo.setField(Field.DMPROPERTIESLOCATION, dmPropertyLocHindi);
					}
					
					// END
					
					String apinVerified = (String)tblIVRData.getaPINVerified();
					if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"apinVerified value retrieved from CTI is " + apinVerified);}

					if(!util.isNullOrEmpty(apinVerified) && Constants.Y.equalsIgnoreCase(apinVerified)){
						callInfo.setField(Field.APIN_VALIDATED, true);
						if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Updating APIN validation field value as true");}
					}else{
						callInfo.setField(Field.APIN_VALIDATED, false);
						if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Updating APIN validation field value as false");}
					}

					iceRuleParam.setIVRParam(Constants.RULE_ENGINE_APINVERIFIED, apinVerified);
					iceRuleParam.setIVRParam(Constants.RULE_ENGINE_CUSTOMERID, customerID);
					
					//Connect Back to IVR -> APIN Verified -> ConnectBack to PINset & Reset issue handling on 09-Nov-2021
					String connectBackFeatureID = util.isNullOrEmpty(callInfo.getField(Field.DNIS_Type))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.DNIS_Type);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Connect back DNIS_Type is " + connectBackFeatureID);}

					if(!util.isNullOrEmpty(customerID) && !util.isNullOrEmpty(apinVerified) && Constants.Y.equalsIgnoreCase(apinVerified)
							&& !(Constants.ConnectBack_CBPinset.equalsIgnoreCase(connectBackFeatureID) ||
									 Constants.ConnectBack_CBPinreset.equalsIgnoreCase(connectBackFeatureID) )
							){

						ICallerIdentification iCallerIdentification = Context.getiCallerIdentification();
						code = iCallerIdentification.getCallerIdentification(callInfo);
						if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"IcallerIdentification host service return value is " + code);}

						if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
							if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Successfully called the caller Identification method" + code);}
						}else{
							if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_," Failure at the caller identification method call" + code);}
						}
					}

				}

			} else {
				if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_," Retrieving th RAP CTI call data from DB got failed");	}
				if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"Since its a DB service failure considering as success");	}
				code = Constants.WS_SUCCESS_CODE;

				if (logger.isDebugEnabled()) {WriteLog.write(WriteLog.DEBUG,session_ID_,"The callFlow will proceed as normal flow");	}

			}

		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR,	e,"There was an error at RapCTIImpl.SetCallData() "	+ e.getMessage());

			throw new ServiceException(e);
		}
		return code;

		// try{
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "Fetching the Feature Object values");}
		// ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)
		// callInfo.getICEGlobalConfig();
		//
		// if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
		// throw new ServiceException("ICEGlobalConfig object is null");
		// }
		//
		// /**
		// * For Reporting Purpose
		// */
		// HostReportDetails hostReportDetails = new HostReportDetails();
		//
		// String featureId = (String)callInfo.getField(Field.FEATUREID);
		// hostReportDetails.setHostActiveMenu(featureId);
		// //hostReportDetails.setHostCounter(hostCounter);
		// //hostReportDetails.setHostEndTime(hostEndTime);
		// String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID +
		// Constants.EQUALTO +
		// callInfo.getField(Field.CUSTOMERID) + Constants.COMMA +
		// Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO +
		// callInfo.getField(Field.ACCOUNTNUMBER);
		// hostReportDetails.setHostInParams(strHostInParam);
		// hostReportDetails.setHostMethod(Constants.HOST_METHOD_RAPCTI_GETDATA);
		// //hostReportDetails.setHostOutParams(hostOutParams);
		// hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
		// hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
		// hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
		// hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
		//
		// hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It
		// should be in the formate of 31/07/2013 18:11:11
		// hostReportDetails.setHostType(Constants.HOST_TYPE_RAP);
		// //End Reporting
		// String rep_DeviceID = (String)callInfo.getField(Field.DEVICE_ID);
		// int deviceID =
		// util.isNullOrEmpty(rep_DeviceID)?Constants.GL_ZERO:Integer.parseInt(rep_DeviceID);
		// GetData_HostRes getData_HostRes =
		// getCallDataDAO.getCTIGetCallDataHostRes(callInfo, deviceID);
		//
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "GetData_HostRes Object is :"+ getData_HostRes);}
		// code = getData_HostRes.getErrorCode();
		//
		// /*
		// * For Reporting Start
		// */
		// String hostEndTime = getData_HostRes.getHostEndTime();
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "Actual Host End time is is " + hostEndTime);}
		// hostReportDetails.setHostEndTime(hostEndTime);
		//
		// String hostResCode = getData_HostRes.getHostResponseCode();
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "Actual Host response code is " + hostResCode);}
		// hostReportDetails.setHostResponse(hostResCode);
		//
		// String responseDesc = Constants.HOST_FAILURE;
		// if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
		// responseDesc = Constants.HOST_SUCCESS;
		// }
		// String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC +
		// Constants.EQUALTO + responseDesc +Constants.COMMA +
		// Constants.HOST_OUTPUT_PARAM_RESPONSECODE
		// + Constants.EQUALTO + hostResCode;
		//
		// hostReportDetails.setHostOutParams(hostOutputParam);
		//
		// callInfo.setHostReportDetails(hostReportDetails);
		// IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
		//
		// callInfo.insertHostDetails(ivrdata);
		// //End Reporting
		// if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "Got success response for RAPCTI GETDATA request service");}
		// }else{
		//
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "Got failure response for Update getData host service");}
		// if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
		// "The original response code of host access is " +
		// getData_HostRes.getHostResponseCode());}
		//
		// util.setHostErrorCodePhrase(callInfo,
		// Constants.HOST_METHOD_RAPCTI_SETDATA,
		// getData_HostRes.getHostResponseCode());
		// }
		// if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,
		// "EXIT: RapCTIImpl.GetCallData()");}
		//
		// }catch(Exception e){
		// WriteLog.writeError(WriteLog.ERROR, e,
		// "There was an error at RapCTIImpl.getCallData() "+ e.getMessage());
		// throw new ServiceException(e);
		// }
		// return code;
	}

}
