package com.servion.services;

import java.math.BigDecimal;
import java.util.HashMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.CustDtlsDAO;
import com.servion.dao.CustomerProfileAggregateDAO;
import com.servion.dao.GetDebitCardDetailsDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.CustDtls.CustDtls_HostRes;
import com.servion.model.callerIdentification.CallerIdenf_DebitCardDetails;
import com.servion.model.callerIdentification.CallerIdentification_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class CallerIdentificationImpl implements ICallerIdentification {

	private static Logger logger = LoggerObject.getLogger();

	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	private GetDebitCardDetailsDAO getDebitCardDetailsDAO;
	private CustomerProfileAggregateDAO customerProfileAggregateDAO;
	private CustDtlsDAO custDtlsDAO;


	public GetDebitCardDetailsDAO getGetDebitCardDetailsDAO() {
		return getDebitCardDetailsDAO;
	}

	public void setGetDebitCardDetailsDAO(
			GetDebitCardDetailsDAO getDebitCardDetailsDAO) {
		this.getDebitCardDetailsDAO = getDebitCardDetailsDAO;
	}

	public CustomerProfileAggregateDAO getCustomerProfileAggregateDAO() {
		return customerProfileAggregateDAO;
	}

	public void setCustomerProfileAggregateDAO(
			CustomerProfileAggregateDAO customerProfileAggregateDAO) {
		this.customerProfileAggregateDAO = customerProfileAggregateDAO;
	}
	
	public CustDtlsDAO getCustDtlsDAO() {
		return custDtlsDAO;
	}

	public void setCustDtlsDAO(CustDtlsDAO custDtlsDAO) {
		this.custDtlsDAO = custDtlsDAO;
	}

	@Override
	public String getAccountType(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.getDebitCardLength()");}
		String returnValue = Constants.EMPTY_STRING;
		try{
			getConfigurationParam(callInfo);
			//returnValue = (String)callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getAccountProduct();
			returnValue = (String)callInfo.getField(Field.ENTEREDCINTYPE);

			if(util.isNullOrEmpty(returnValue))
			{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entered CIN number Field value is null or Empty"+ returnValue);}
				return Constants.EMPTY_STRING;
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The caller Product type is "+ returnValue);}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.isCallerIdentified() "+ e.getMessage());}
			throw new ServiceException(e);
		}

		return returnValue;
	}
	
	public String getCallerProfileDetails(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.getCallerIdentification()");}
		String code = Constants.EMPTY_STRING;
		String customerID = Constants.EMPTY_STRING;
		try{
			
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the IVRData for reporting " + ivrdata);}
			
			String uui = util.isNullOrEmpty(callInfo.getField(Field.UUI))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained UUI is " + uui);}

			String connectBackCustomerID = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Connect back Customer ID is " + connectBackCustomerID);}
			
		customerID = (String) callInfo.getField(Field.CUSTOMERID);
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Retrieved Customer ID:"+customerID);}	
		/**
		 * Rule engine update
		 */
		ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

		if(util.isNullOrEmpty(ruleParamObj)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
		}

		ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERID, customerID);
		ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCALLERVALIDATED, Constants.TRUE);
		ruleParamObj.updateIVRFields();

		//Setting the below rule engine for Promotion count update
		//ruleParamObj.getParam(Constants.RULE_ENGINE_PROMOTION_COUNT);
		
		
		
		/**
		 * For Reporting Purpose
		 */
		HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

		String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
		hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
		String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
		hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_CUSTOMERPROFILEAGGREGATE);
		//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
		hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
		hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
		hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
		hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

		hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
		hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
		//End Reporting
		
		
		/*
		 *  Setting NA values
		 */
		hostReportDetailsForSecHost.setHostEndTime(Constants.NA);
		hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
		hostReportDetailsForSecHost.setHostResponse(Constants.NA);
		
		callInfo.setHostReportDetails(hostReportDetailsForSecHost);
		ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
		callInfo.insertHostDetails(ivrdata);
		
		/* END */
		
		
		ICEFeatureData iceFeatureData_Obj = callInfo.getICEFeatureData();
		
		if(util.isNullOrEmpty(iceFeatureData_Obj)){
			throw new ServiceException("ICE Feature Object is null");
		}
		
		String requestType_Obj = util.isNullOrEmpty(iceFeatureData_Obj.getConfig().getParamValue(Constants.CUI_CUSTOMERPROFILE_REQUESTTYPE))? null : (String)iceFeatureData_Obj.getConfig().getParamValue(Constants.CUI_CUSTOMERPROFILE_REQUESTTYPE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType_Obj);}

		
		CallerIdentification_HostRes callerIdentification_HostRes = getCustomerProfileAggregateDAO().getCallerIdentificationHostRes(callInfo, customerID, requestType_Obj);

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "callerIdentification_HostRes Object is :"+ callerIdentification_HostRes);}
		callInfo.setCallerIdentification_HostRes(callerIdentification_HostRes);
		code = callerIdentification_HostRes.getErrorCode();


		/**
		 * For handling caller identification error response code
		 */
		callInfo.setField(Field.CallerIdenfErrorCode, callerIdentification_HostRes.getErrorCode());
		callInfo.setField(Field.CallerIdentfHostResponseCode, callerIdentification_HostRes.getHostResponseCode());
		//END

		/*
		 * For Reporting Start
		 */
		
		/****Duplicate RRN Fix 25012016 *****/
		strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
		/************************************/
		
		
		
		String hostEndTimeForSecHost = callerIdentification_HostRes.getHostEndTime();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
		hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

		String hostResCodeForSecHost = callerIdentification_HostRes.getHostResponseCode();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
		hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

		String responseDescForSecHost = Constants.HOST_FAILURE;
		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			responseDescForSecHost = Constants.HOST_SUCCESS;
		}
		String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
				+ Constants.EQUALTO + hostResCodeForSecHost 			
		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(callerIdentification_HostRes.getErrorDesc()) ?"NA" :callerIdentification_HostRes.getErrorDesc());
		hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

		callInfo.setHostReportDetails(hostReportDetailsForSecHost);
		IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

		callInfo.updateHostDetails(ivrdataForSecHost);
		//End Reporting

		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for customer profile aggregate");}
			callInfo.setField(Field.ISCALLERIDENTIFIED, true);

			
			
			/**
			 * Calling the below method to set the LastSelectedMobileNumberMap
			 * 
			 */
			IGlobal iglobal = Context.getIglobal();
			HashMap<String, String>lastSelectedMobMap = null;
			if(!util.isNullOrEmpty(iglobal)){
				lastSelectedMobMap = iglobal.getLastEnteredMobileNoMap(callInfo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The last selected mobile number map is " + lastSelectedMobMap);}
			}
			//END Vinoth
			
//			String custType = callerIdentification_HostRes.getCustomerShortDetails().getCustType();
//
//			if(custType.equalsIgnoreCase(Constants.ALPHA_P)){
//				callInfo.setField(Field.ISPRIORITYCUSTOMER, true);
//			}
//
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the Card is a Primary Card "+ callInfo.getField(Field.ISPRIORITYCUSTOMER));}

			String acctType = Constants.EMPTY_STRING;
			String accountOpeningDate = Constants.EMPTY_STRING;
			String accountNumber = Constants.EMPTY_STRING;

			if(!util.isNullOrEmpty(callerIdentification_HostRes) && !util.isNullOrEmpty(callerIdentification_HostRes.getAcctInfoList())){
				if(!util.isNullOrEmpty(callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO))){
					acctType = callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO).getAcctType();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account type is "+ acctType);}
				}
			}
			
			
			if(!util.isNullOrEmpty(callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO))){
				accountNumber = callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO).getAcctID();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first account ID number of the retrieved account type is :" + accountNumber);}

				if(!util.isNullOrEmpty(callerIdentification_HostRes.getAccountDetailMap().get(accountNumber))){
					accountOpeningDate = (callerIdentification_HostRes.getAccountDetailMap().get(accountNumber)).getAcctStartDate();

					//Adding in the CallInfo Field
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date set into  is callInfo field :" + accountOpeningDate);}
					callInfo.setField(Field.AccountOpeningDate, accountOpeningDate);

					/**
					 * Rule engine update
					 */
					if(util.isNullOrEmpty(ruleParamObj)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
					}

					accountOpeningDate = util.convertDateStringFormat(accountOpeningDate, Constants.DATEFORMAT_YYYY_MM_DD, Constants.DATEFORMAT_YYYYMMDDHHMMSS);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Converted account opening date format is " + accountOpeningDate);}
					
					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ACCOUNTOPENINGDATE, accountOpeningDate);
					//END Rule Engine Updation
					

				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Opening Date is "+ accountOpeningDate);}

			
			
			
			
			/**
			 * 
			 * Following are the DB Data service method used to update the Customer details in the FEED table
			 * 
			 */
			
			//For Updating CLI A Mobile no field
			boolean isCLIAMobNo = false;
			boolean isCLIARegMobileno = false;
			if(!util.isNullOrEmpty( Context.getIglobal())){
				isCLIAMobNo = Context.getIglobal().isANIAMobNo(callInfo);
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Is CLI a mobile number " + isCLIAMobNo);}
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ANI is  " + callInfo.getField(Field.ANI));}
			
			String regMobileNo = util.isNullOrEmpty(callInfo.getField(Field.REG_MOBILENO)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.REG_MOBILENO);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Registered Mobile number is " + regMobileNo);}
			
			String ani = util.isNullOrEmpty(callInfo.getField(Field.ANI)) ? Constants.NA : (String)callInfo.getField(Field.ANI);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ANI is " + ani);}
			
			if(isCLIAMobNo){
				if(regMobileNo.equalsIgnoreCase(ani)){
					callInfo.setField(Field.ISCLIAREGISTEREDMOBILENO, Constants.Y);
					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIAREGISTEREDMOBILENO, Constants.Y);
					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLINOTREGMOBILENUMBER, Constants.FALSE);
					isCLIARegMobileno = true;
				}
				else{
					callInfo.setField(Field.ISCLIAREGISTEREDMOBILENO, Constants.N);
					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIAREGISTEREDMOBILENO, Constants.N);
					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLINOTREGMOBILENUMBER, Constants.TRUE);
				}
			}else{
				callInfo.setField(Field.ISCLIAREGISTEREDMOBILENO, Constants.N);
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIAREGISTEREDMOBILENO, Constants.N);
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLINOTREGMOBILENUMBER, Constants.TRUE);
			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Is CLI a Mobile number " + callInfo.getField(Field.ISCLIAREGISTEREDMOBILENO));}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Updating Rule engine");}
			ruleParamObj.updateIVRFields();
			
			if(isCLIARegMobileno){
				try {
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: callerIdenticiationImpl.calling insertcustdetail()");}
					
					String dbcode = Constants.ONE;
					String sessionId_Obj = (String)callInfo.getField(Field.SESSIONID);
					
					String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID)) ?Constants.EMPTY_STRING : (String)(callInfo.getField(Field.CUSTOMERID));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is " + customerId);}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
					HashMap<String, Object>configMap_Obj = new HashMap<String, Object>();
					
					String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date is "+ accountOpeningDate);}
					
					configMap_Obj.put(DBConstants.CUSTOMERID, customerID);
					configMap_Obj.put(DBConstants.DATETIME, currentDate);
					configMap_Obj.put(DBConstants.PREFERRED_LANGUAGE, callInfo.getField(Field.LANGUAGE));
					configMap_Obj.put(DBConstants.CLI, callInfo.getField(Field.ANI));
					configMap_Obj.put(DBConstants.ACCOUNT_OPENING_DATE, accountOpeningDate);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is " + configMap_Obj.get(Constants.DATETIME) );}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language" + configMap_Obj.get(Constants.PREFERRED_LANGUAGE));}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI is " + configMap_Obj.get(Constants.CLI) );}
					
					uui = (String)callInfo.getField(Field.UUI);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
					
					DataServices dataServices_Obj = VRUDBDataServicesInstance.getInstance();
					if(util.isNullOrEmpty(dataServices_Obj)){
						throw new ServiceException("Data Service object is null or empty");
					}
					try {
						dbcode = dataServices_Obj.insertCustDetails(logger, sessionId_Obj, uui, configMap_Obj);
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Inserterd the Cust Details successfully");}
						
					} catch (com.db.exception.ServiceException e) {
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: callerIdenticiationImpl.calling insertcustdetail()");}
						dbcode = Constants.ONE;
						//e.printStackTrace();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + dbcode );}
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: callerIdenticiationImpl.calling insertcustdetail()");}
					
				} catch (Exception e) {
					WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  callerIdenticiationImpl.calling insertcustdetail()" + e.getMessage());
					throw new ServiceException(e);
				}
			}
			
			
			

			/**
			 * END - Updating the DB and Rule engine
			 */
			
			callInfo.setField(Field.ACCOUNTTYPE, acctType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Type is "+ acctType);}

			String cardType = Constants.EMPTY_STRING;
			if(callerIdentification_HostRes.getCardAcctDtlList()!=null && callerIdentification_HostRes.getCardAcctDtlList().size() > Constants.GL_ZERO){
				if(!util.isNullOrEmpty(callerIdentification_HostRes.getCardAcctDtlList().get(Constants.GL_ZERO))){
					cardType = callerIdentification_HostRes.getCardAcctDtlList().get(Constants.GL_ZERO).getCardType();
				}
			}

			callInfo.setField(Field.CREDITCARDTYPE, cardType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Type is "+ cardType);}

			String cin = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered CIN is "+ util.maskCardOrAccountNumber(cin));}

			/**
			 * APIN Status has been handled at APIN Validation module
			 */
			
//			String apinStatus = Constants.EMPTY_STRING;
//
//			if(callerIdentification_HostRes.getCardDetailMap()!=null && cin!=null && callerIdentification_HostRes.getCardDetailMap().size() > Constants.GL_ZERO){
//				if(!util.isNullOrEmpty(callerIdentification_HostRes.getCardDetailMap().get(cin))){
//
//					apinStatus = callerIdentification_HostRes.getCardDetailMap().get(cin).getCardStatus();
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The CIN card APIN status is "+ apinStatus);}
//				}
//			}
//
//			callInfo.setField(Field.APIN_STATUS, apinStatus);

		}else{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + callerIdentification_HostRes.getHostResponseCode());}

			util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CUSTOMERPROFILEAGGREGATE, callerIdentification_HostRes.getHostResponseCode());
		}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CallerIdentificationImpl.getCallerProfileDetails()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.getCallerProfileDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getCallerIdentification(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.getCallerIdentification()");}
		String code = Constants.EMPTY_STRING;
		getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			/**
			 * Following changes are made for Connect Back to IVR flow
			 */
			
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the IVRData for reporting " + ivrdata);}
			
			String uui = util.isNullOrEmpty(callInfo.getField(Field.UUI))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained UUI is " + uui);}

			String connectBackCustomerID = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Connect back Customer ID is " + connectBackCustomerID);}

			String connectBackFeatureID = util.isNullOrEmpty(callInfo.getField(Field.DNIS_Type))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.DNIS_Type);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Connect back DNIS_Type is " + connectBackFeatureID);}
			
			String SelectedCardOrAcctNo = Constants.EMPTY_STRING;
			
			//Connect back another card entry fix 03-May-2021
			/*
			 * if(util.isNullOrEmpty(uui) ||
			 * Constants.EMPTY_STRING.equalsIgnoreCase(connectBackCustomerID) ||
			 * Constants.ConnectBack_CBPinset.equalsIgnoreCase(connectBackFeatureID) ||
			 * Constants.ConnectBack_CBPinreset.equalsIgnoreCase(connectBackFeatureID) ){
			 */	
			//disabled Connect back another card entry fix 03-May-2021 on 07-Nov-2021
			if(util.isNullOrEmpty(uui) ||
					 Constants.EMPTY_STRING.equalsIgnoreCase(connectBackCustomerID) ||
					 Constants.ConnectBack_CBPinset.equalsIgnoreCase(connectBackFeatureID) ||
					 Constants.ConnectBack_CBPinreset.equalsIgnoreCase(connectBackFeatureID) ){
				SelectedCardOrAcctNo = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
				if(util.isNullOrEmpty(SelectedCardOrAcctNo)){
					throw new ServiceException("Selected Card OR Acct No is empty or null");
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting selected card or acct no as entered cin" + util.getSubstring(SelectedCardOrAcctNo, Constants.GL_FOUR));}
				
				//String pan = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_Pan);
				String pan = SelectedCardOrAcctNo;
				
				String processingCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_ProcessingCode);
//				int int_ProcessingCode = util.isNullOrEmpty(processingCode)?Constants.GL_ZERO : Integer.parseInt(processingCode);
				
				String amtTransaction = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AmtTransaction); 
				
				String str_AmtSettlement = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AmtSettlement); 
				BigDecimal amtSettlement =  util.isNullOrEmpty(str_AmtSettlement)?new BigDecimal(Constants.GL_ZERO):new BigDecimal(str_AmtSettlement);
				
				String transmissionDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);
				
				String str_ConversionRate =  (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_ConversionRate); 
				int conversionRate = util.isNullOrEmpty(str_ConversionRate)?Constants.GL_ZERO:Integer.parseInt(str_ConversionRate);
				
				//Following for the sequencial number generation for System trace audit number for S1 systems
				String db_Code = Constants.ONE;
				int codeLength = Constants.GL_ZERO;
				String sessionId = (String) callInfo.getField(Field.SESSIONID);
				
				
				if(util.isNullOrEmpty(sessionId)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Session ID is null or empty");}
					throw new ServiceException("Session id is null or empty");
				}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
				HashMap<String, Object> configMap = new HashMap<String, Object>();
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the input for getSequenceNo");}
				
//				String uui = (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
				
				
				DataServices dataServices = VRUDBDataServicesInstance.getInstance();
				if(util.isNullOrEmpty(dataServices)){
					throw new ServiceException("Data Service object is null or empty");
				}
				String strRefNumberOne = Constants.EMPTY_STRING;
				try {
					db_Code = dataServices.getSequenceNoS1(logger, sessionId, uui, configMap);
					
					if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)){
						strRefNumberOne = (String) configMap.get(DBConstants.SEQUENCENO);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The sequencial number return value " + strRefNumberOne);}
						codeLength = strRefNumberOne.length();
						
						for(int p=codeLength; p < 6; p ++){
							strRefNumberOne = Constants.ZERO + strRefNumberOne;
							strRefNumberOne.trim();
						}
						
					}else{
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Error in the S1 sequencial DB response");}
						throw new ServiceException("Sequencial number DB access throwing error");
					}
				} catch (com.db.exception.ServiceException e) {
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: GlobalImpl.getSequenceNumber()");}
					throw new ServiceException("S1 Sequencial number DB access throwing error");
					//e.printStackTrace();
				}
				
				
				String str_SysTraceAuditNo = strRefNumberOne; 
				//			util.getRandomNumber(999999) + Constants.EMPTY_STRING;
				//			int sysTraceAuditNo = util.isNullOrEmpty(str_SysTraceAuditNo)?Constants.GL_ZERO:Integer.parseInt(str_SysTraceAuditNo); 
				
				String localTransTime = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
				String localTansDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDD); 
				String expirationDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_ExpirationDate); 
				
				String settlementDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_SettlementDate);
				
				//XMLGregorianCalendar xml_DateConversion = (XMLGregorianCalendar)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_DateConversion);
				//String dateConversion =  util.convertXMLCalendarToString(xml_DateConversion, Constants.DATEFORMAT_MMDD);
				
				String dateConversion =  (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_DateConversion);
				XMLGregorianCalendar xml_DateConversion = null;
				if(!util.isNullOrEmpty(dateConversion)){
					xml_DateConversion = util.convertDateStringtoXMLGregCalendar(dateConversion, Constants.DATEFORMAT_MMDD);
				}
				
				String str_MerchantType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_MerchantType);
				//			int merchantType = util.isNullOrEmpty(str_MerchantType)?Constants.GL_ZERO:Integer.parseInt(str_MerchantType);
				
				String str_PointOfServiceMode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_PointOfServiceMode);
				//			int pointOfServiceMode = util.isNullOrEmpty(str_MerchantType)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServiceMode); 
				
				String str_CardSeqNum = (String)util.getSubstring(pan, Constants.GL_THREE);
				//			int cardSeqNum = util.isNullOrEmpty(str_MerchantType)?Constants.GL_ZERO:Integer.parseInt(str_CardSeqNum); 
				
				String str_PointOfServCondCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_PointOfServCondCode);
				//			int pointOfServCondCode = util.isNullOrEmpty(str_MerchantType)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServCondCode);
				
				String str_PointOfServCaptureCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_PointOfServCaptureCode);
				//			int pointOfServCaptureCode = util.isNullOrEmpty(str_MerchantType)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServCaptureCode); 
				
				String str_AuthIDRespLength = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AuthIDRespLength);
				int authIDRespLength = util.isNullOrEmpty(str_AuthIDRespLength)?Constants.GL_ZERO:Integer.parseInt(str_AuthIDRespLength);
				
				String str_AmtSettlementFee = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AmtSettlementFee);
				BigDecimal amtSettlementFee = util.isNullOrEmpty(str_AmtSettlementFee)?new  BigDecimal(Constants.GL_ZERO):new BigDecimal(str_AmtSettlementFee); 
				
				String str_AmtSettlementProcFee = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AmtSettlementProcFee);
				BigDecimal amtSettlementProcFee = util.isNullOrEmpty(str_AmtSettlementProcFee)?new BigDecimal(Constants.GL_ZERO):new BigDecimal(str_AmtSettlementProcFee);
				
				String AcquiringInstitutionID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AcquiringInstitutionID); 
				String trackTwoData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_TrackTwoData);
				String cardAccpTerminalID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_CardAccpTerminalID); 
				String cardAccpIDCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_CardAccpIDCode);
				String cardAccpName = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_CardAccpName); 
				String currCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_CurrCode); 
				String currCodeSettlement = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_CurrCodeSettlement);
				
				
				/**
				 * Doing the process of PIN Blocking and Encryption
				 */
				//			String pin = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_Pin); 
				
				//Setting the User entered APIN value
				String pin = (String)callInfo.getField(Field.APIN);
				//(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Pin);
				
				
				/**
				 * As Per ESB no need to pass pin as a mandatory field for getDebitCardService
				 */
				
				//			String apinKey = (String)callInfo.getField(Field.APINKEY);
				//			if(util.isNullOrEmpty(apinKey)){
				//				throw new ServiceException("Apin Key stored in the callinfo is null / empty");
				//			}
				//			
				//			apinKey = util.getSubstring(apinKey, Constants.GL_THIRTYTWO);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first 32 digit key is "+ apinKey);}
				//
				//			//TODO this should be handled from the JKS file
				//			String masterkeyValue = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_APIN_MASTERKEY);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Obtained Master key from the keystore / Config file");}
				//
				//			masterkeyValue = util.convertTo48BitKey(masterkeyValue);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Master key has been converted to 48 bits");}
				//
				//			
				//			JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
				//			SecretKey sKey = jceWrap.toSecretKey(masterkeyValue);
				//			String clearKey = jceWrap.decrypt(apinKey, sKey);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received the clear key and the clear key is "+ clearKey);}
				//
				//			
				//			String pinBlocking = util.getISOPinBlock(pan, pin, true, false, Constants.GL_THREE, Constants.GL_ONE);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully done the pin blocking"+ pinBlocking);}
				//
				//			clearKey = util.convertTo48BitKey(clearKey);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Clear key is converted to 48 digit and the result is "+ clearKey);}
				//			
				//			String encryptPIN = jceWrap.encrypt(pinBlocking, clearKey);
				//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully done the pin encryption"+ encryptPIN);}
				//			
				
				String encryptPIN = Constants.EMPTY_STRING;
				
				//END - Encrypting the PIN
				
				String securityContInfo = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_SecurityContInfo); 
				String additionalAmt = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AdditionalAmt);
				
				String str_ExtendedPaymentCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_ExtendedPaymentCode); 
				int extendedPaymentCode = util.isNullOrEmpty(str_ExtendedPaymentCode)?Constants.GL_ZERO:Integer.parseInt(str_ExtendedPaymentCode);
				
				String originalDataElement = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_OriginalDataElement); 
				String payee = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_Payee);
				String recvInstIDCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_RecvInstIDCode); 
				String acctIdenfOne = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AcctIdenfOne); 
				String acctIdenfTwo = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AcctIdenfTwo);
				String posDataCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_PosDataCode); 
				String bitMap = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_BitMap); 
				String switchKey = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_SwitchKey);
				String checkData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_CheckData); 
				String terminalOwner = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_TerminalOwner); 
				String posGeographicData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_PosGeographicData);
				String sponsorBank = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_SponsorBank);
				String addrVerfData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_AddrVerfData); 
				String bankDetails = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_BankDetails);
				String payeeName = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_PayeeName); 
				String iccData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_IccData); 
				String origData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_OrigData); 
				String macField =  (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_MacField); 
				
				
				/**
				 * Following are the modification done on 01-Sep-2014 for the handling of dynamic Debit card length (15 to 19)
				 */
				int panLength = util.isNullOrEmpty(pan)?Constants.GL_SIXTEEN : pan.length();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length is "+ panLength);}
				
				String panLengthKey = Constants.UNDERSCORE + panLength;
				panLengthKey = panLengthKey.trim();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length key is "+ panLengthKey);}
				
				panLengthKey = Constants.CUI_UI_GetDebitCardDetails_RequestStructData+panLengthKey;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The new Request Structure Data key is "+ panLengthKey);}
				
				String structureData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(panLengthKey);
				structureData = (structureData!=null)? structureData.replace(Constants.STRUCTURED_DATA_PAN_KEY, pan): null;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Finalized Structured data is "+ structureData);}
				//END 
				String extendedTransType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_ExtendedTransactionType);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The extended Transaction type from CUI is "+ extendedTransType);}
				
//			String requestType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_RequestType);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "GetDebitCardDetails request type is "+ requestType);}
				
				/**
				 * For Reporting Purpose
				 */
				HostReportDetails hostReportDetails = new HostReportDetails();
				
				String featureId = (String)callInfo.getField(Field.FEATUREID);
				hostReportDetails.setHostActiveMenu(featureId);
				//hostReportDetails.setHostCounter(hostCounter);
				//hostReportDetails.setHostEndTime(hostEndTime);
				String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
						Constants.NA +Constants.COMMA +  Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_GETDEBITCARDDETAILS);
				//hostReportDetails.setHostOutParams(hostOutParams);
				hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
				hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
				
				hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
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
				
				
				ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				
				if(util.isNullOrEmpty(iceFeatureData)){
					throw new ServiceException("iceFeatureData object is null or empty");
				}
				
				String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_GETDEBITCARDDETAILS_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_GETDEBITCARDDETAILS_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}
				
				
				
				CallerIdenf_DebitCardDetails callerIdenf_DebitCardDetails = getDebitCardDetailsDAO.getCallerIdenfCustomerIDHostRes(callInfo, pan, 
						processingCode, amtTransaction, amtSettlement, transmissionDate, conversionRate, str_SysTraceAuditNo, localTransTime, 
						localTansDate, expirationDate, settlementDate, xml_DateConversion, str_MerchantType, str_PointOfServiceMode, str_CardSeqNum, str_PointOfServCondCode, 
						str_PointOfServCaptureCode, authIDRespLength, amtSettlementFee, amtSettlementProcFee, AcquiringInstitutionID, trackTwoData, 
						cardAccpTerminalID, cardAccpIDCode, cardAccpName, currCode, currCodeSettlement, encryptPIN, securityContInfo, additionalAmt, extendedPaymentCode, 
						originalDataElement, payee, recvInstIDCode, acctIdenfOne, acctIdenfTwo, posDataCode, bitMap, switchKey, checkData, terminalOwner, 
						posGeographicData, sponsorBank, addrVerfData, bankDetails, payeeName, iccData, origData, macField, structureData, extendedTransType, requestType);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CallerIdenf_DebitCardDetails Object is :"+ callerIdenf_DebitCardDetails);}
				callInfo.setCallerIdenf_DebitCardDetails(callerIdenf_DebitCardDetails);
				
				code = callerIdenf_DebitCardDetails.getErrorCode();
				
				/**
				 * For handling caller identification error response code
				 */
				
				callInfo.setField(Field.CallerIdenfErrorCode, callerIdenf_DebitCardDetails.getErrorCode());
				callInfo.setField(Field.CallerIdentfHostResponseCode, callerIdenf_DebitCardDetails.getHostResponseCode());
				//END
				
				/*
				 * For Reporting Start
				 */
				
				/****Duplicate RRN Fix 25012016 *****/
				strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
						Constants.NA +Constants.COMMA +  Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetails.setHostInParams(strHostInParam);
				/************************************/
				
				String hostEndTime = callerIdenf_DebitCardDetails.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);
				
				String hostResCode = callerIdenf_DebitCardDetails.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);
				
				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				//CallerIdenf_DebitCardDetails callerIdenf_DebitCardDetails_Obj = callInfo.getCallerIdenf_DebitCardDetails();
				if(!util.isNullOrEmpty(callInfo.getCallerIdenf_DebitCardDetails()) && !util.isNullOrEmpty(callInfo.getCallerIdenf_DebitCardDetails().getCustomerID())){
					connectBackCustomerID = callInfo.getCallerIdenf_DebitCardDetails().getCustomerID();
				}
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode + Constants.COMMA + Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + connectBackCustomerID
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(callerIdenf_DebitCardDetails.getErrorDesc()) ?"NA" :callerIdenf_DebitCardDetails.getErrorDesc());
				hostReportDetails.setHostOutParams(hostOutputParam);
				
				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
				
				callInfo.updateHostDetails(ivrdata);
				//End Reporting
			//Connect back another card entry fix 03-May-2021
			//}
			//disabled Connect back another card entry fix 03-May-2021 on 07-Nov-2021
		}
			
			//CallerIdenf_DebitCardDetails callerIdenf_DebitCardDetails_Obj = callInfo.getCallerIdenf_DebitCardDetails();
			if(!util.isNullOrEmpty(callInfo.getCallerIdenf_DebitCardDetails()) && !util.isNullOrEmpty(callInfo.getCallerIdenf_DebitCardDetails().getCustomerID())){
				connectBackCustomerID = callInfo.getCallerIdenf_DebitCardDetails().getCustomerID();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id received from the GetDebitCardDetail services is " + connectBackCustomerID);}
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code) || !util.isNullOrEmpty(uui)){
				code = Constants.WS_FAILURE_CODE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call callerIdentification_HostRes.CustomerProfileAggregate");}

				String customerID = connectBackCustomerID;

				if(util.isNullOrEmpty(customerID)){
					throw new ServiceException("Customer ID from GetDebitCardDetails is null / Empty");
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting customer id in the field");}
				callInfo.setField(Field.CUSTOMERID, customerID);


				/**
				 * Rule engine update
				 */
				ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

				if(util.isNullOrEmpty(ruleParamObj)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
				}

				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERID, customerID);
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCALLERVALIDATED, Constants.TRUE);
				ruleParamObj.updateIVRFields();

				//Setting the below rule engine for Promotion count update
				//ruleParamObj.getParam(Constants.RULE_ENGINE_PROMOTION_COUNT);
				
				
				
				/**
				 * For Reporting Purpose
				 */
				HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

				String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
				hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
				String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
				hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_CUSTOMERPROFILEAGGREGATE);
				//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
				hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
				hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
				hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
				hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

				hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
				hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
				//End Reporting
				
				
				/*
				 *  Setting NA values
				 */
				hostReportDetailsForSecHost.setHostEndTime(Constants.NA);
				hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
				hostReportDetailsForSecHost.setHostResponse(Constants.NA);
				
				callInfo.setHostReportDetails(hostReportDetailsForSecHost);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
				callInfo.insertHostDetails(ivrdata);
				
				/* END */
				
				
				ICEFeatureData iceFeatureData_Obj = callInfo.getICEFeatureData();
				
				if(util.isNullOrEmpty(iceFeatureData_Obj)){
					throw new ServiceException("ICE Feature Object is null");
				}
				
				String requestType_Obj = util.isNullOrEmpty(iceFeatureData_Obj.getConfig().getParamValue(Constants.CUI_CUSTOMERPROFILE_REQUESTTYPE))? null : (String)iceFeatureData_Obj.getConfig().getParamValue(Constants.CUI_CUSTOMERPROFILE_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType_Obj);}

				
				CallerIdentification_HostRes callerIdentification_HostRes = getCustomerProfileAggregateDAO().getCallerIdentificationHostRes(callInfo, customerID, requestType_Obj);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "callerIdentification_HostRes Object is :"+ callerIdentification_HostRes);}
				callInfo.setCallerIdentification_HostRes(callerIdentification_HostRes);
				code = callerIdentification_HostRes.getErrorCode();


				/**
				 * For handling caller identification error response code
				 */
				callInfo.setField(Field.CallerIdenfErrorCode, callerIdentification_HostRes.getErrorCode());
				callInfo.setField(Field.CallerIdentfHostResponseCode, callerIdentification_HostRes.getHostResponseCode());
				//END

				/*
				 * For Reporting Start
				 */
				
				/****Duplicate RRN Fix 25012016 *****/
				strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
				hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
				/************************************/
				
				
				
				String hostEndTimeForSecHost = callerIdentification_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
				hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

				String hostResCodeForSecHost = callerIdentification_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
				hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

				String responseDescForSecHost = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDescForSecHost = Constants.HOST_SUCCESS;
				}
				String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCodeForSecHost 			
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(callerIdentification_HostRes.getErrorDesc()) ?"NA" :callerIdentification_HostRes.getErrorDesc());
				hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

				callInfo.setHostReportDetails(hostReportDetailsForSecHost);
				IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdataForSecHost);
				//End Reporting

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for customer profile aggregate");}
					callInfo.setField(Field.ISCALLERIDENTIFIED, true);

					
					
					/**
					 * Calling the below method to set the LastSelectedMobileNumberMap
					 * 
					 */
					IGlobal iglobal = Context.getIglobal();
					HashMap<String, String>lastSelectedMobMap = null;
					if(!util.isNullOrEmpty(iglobal)){
						lastSelectedMobMap = iglobal.getLastEnteredMobileNoMap(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The last selected mobile number map is " + lastSelectedMobMap);}
					}
					//END Vinoth
					
//					String custType = callerIdentification_HostRes.getCustomerShortDetails().getCustType();
//
//					if(custType.equalsIgnoreCase(Constants.ALPHA_P)){
//						callInfo.setField(Field.ISPRIORITYCUSTOMER, true);
//					}
//
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the Card is a Primary Card "+ callInfo.getField(Field.ISPRIORITYCUSTOMER));}

					String acctType = Constants.EMPTY_STRING;
					String accountOpeningDate = Constants.EMPTY_STRING;
					String accountNumber = Constants.EMPTY_STRING;

					if(!util.isNullOrEmpty(callerIdentification_HostRes) && !util.isNullOrEmpty(callerIdentification_HostRes.getAcctInfoList())){
						if(!util.isNullOrEmpty(callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO))){
							acctType = callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO).getAcctType();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account type is "+ acctType);}
						}
					}
					
					
					if(!util.isNullOrEmpty(callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO))){
						accountNumber = callerIdentification_HostRes.getAcctInfoList().get(Constants.GL_ZERO).getAcctID();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The first account ID number of the retrieved account type is :" + accountNumber);}

						if(!util.isNullOrEmpty(callerIdentification_HostRes.getAccountDetailMap().get(accountNumber))){
							accountOpeningDate = (callerIdentification_HostRes.getAccountDetailMap().get(accountNumber)).getAcctStartDate();

							//Adding in the CallInfo Field
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date set into  is callInfo field :" + accountOpeningDate);}
							callInfo.setField(Field.AccountOpeningDate, accountOpeningDate);

							/**
							 * Rule engine update
							 */
							if(util.isNullOrEmpty(ruleParamObj)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
							}

							accountOpeningDate = util.convertDateStringFormat(accountOpeningDate, Constants.DATEFORMAT_YYYY_MM_DD, Constants.DATEFORMAT_YYYYMMDDHHMMSS);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Converted account opening date format is " + accountOpeningDate);}
							
							ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ACCOUNTOPENINGDATE, accountOpeningDate);
							//END Rule Engine Updation
							

						}
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Opening Date is "+ accountOpeningDate);}

					
					
					
					
					/**
					 * 
					 * Following are the DB Data service method used to update the Customer details in the FEED table
					 * 
					 */
					
					//For Updating CLI A Mobile no field
					boolean isCLIAMobNo = false;
					boolean isCLIARegMobileno = false;
					if(!util.isNullOrEmpty( Context.getIglobal())){
						isCLIAMobNo = Context.getIglobal().isANIAMobNo(callInfo);
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Is CLI a mobile number " + isCLIAMobNo);}
					}
					
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ANI is  " + callInfo.getField(Field.ANI));}
					
					String regMobileNo = util.isNullOrEmpty(callInfo.getField(Field.REG_MOBILENO)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.REG_MOBILENO);
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Registered Mobile number is " + regMobileNo);}
					
					String ani = util.isNullOrEmpty(callInfo.getField(Field.ANI)) ? Constants.NA : (String)callInfo.getField(Field.ANI);
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ANI is " + ani);}
					
					if(isCLIAMobNo){
						if(regMobileNo.equalsIgnoreCase(ani)){
							callInfo.setField(Field.ISCLIAREGISTEREDMOBILENO, Constants.Y);
							ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIAREGISTEREDMOBILENO, Constants.Y);
							isCLIARegMobileno = true;
						}
						else{
							callInfo.setField(Field.ISCLIAREGISTEREDMOBILENO, Constants.N);
							ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIAREGISTEREDMOBILENO, Constants.N);
						}
					}else{
						callInfo.setField(Field.ISCLIAREGISTEREDMOBILENO, Constants.N);
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCLIAREGISTEREDMOBILENO, Constants.N);
					}
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Is CLI a Mobile number " + callInfo.getField(Field.ISCLIAREGISTEREDMOBILENO));}
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Updating Rule engine");}
					ruleParamObj.updateIVRFields();
					
					if(isCLIARegMobileno){
						try {
							if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: callerIdenticiationImpl.calling insertcustdetail()");}
							
							String dbcode = Constants.ONE;
							String sessionId_Obj = (String)callInfo.getField(Field.SESSIONID);
							
							String customerId = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID)) ?Constants.EMPTY_STRING : (String)(callInfo.getField(Field.CUSTOMERID));
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is " + customerId);}
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
							HashMap<String, Object>configMap_Obj = new HashMap<String, Object>();
							
							String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account opening date is "+ accountOpeningDate);}
							
							configMap_Obj.put(DBConstants.CUSTOMERID, customerID);
							configMap_Obj.put(DBConstants.DATETIME, currentDate);
							configMap_Obj.put(DBConstants.PREFERRED_LANGUAGE, callInfo.getField(Field.LANGUAGE));
							configMap_Obj.put(DBConstants.CLI, callInfo.getField(Field.ANI));
							configMap_Obj.put(DBConstants.ACCOUNT_OPENING_DATE, accountOpeningDate);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is " + configMap_Obj.get(Constants.DATETIME) );}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Language" + configMap_Obj.get(Constants.PREFERRED_LANGUAGE));}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI is " + configMap_Obj.get(Constants.CLI) );}
							
							uui = (String)callInfo.getField(Field.UUI);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
							
							DataServices dataServices_Obj = VRUDBDataServicesInstance.getInstance();
							if(util.isNullOrEmpty(dataServices_Obj)){
								throw new ServiceException("Data Service object is null or empty");
							}
							try {
								dbcode = dataServices_Obj.insertCustDetails(logger, sessionId_Obj, uui, configMap_Obj);
								if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Inserterd the Cust Details successfully");}
								
							} catch (com.db.exception.ServiceException e) {
								if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: callerIdenticiationImpl.calling insertcustdetail()");}
								dbcode = Constants.ONE;
								//e.printStackTrace();
							}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + dbcode );}
							if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: callerIdenticiationImpl.calling insertcustdetail()");}
							
						} catch (Exception e) {
							WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  callerIdenticiationImpl.calling insertcustdetail()" + e.getMessage());
							throw new ServiceException(e);
						}
					}
					
					
					

					/**
					 * END - Updating the DB and Rule engine
					 */
					
					callInfo.setField(Field.ACCOUNTTYPE, acctType);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Type is "+ acctType);}

					String cardType = Constants.EMPTY_STRING;
					if(callerIdentification_HostRes.getCardAcctDtlList()!=null && callerIdentification_HostRes.getCardAcctDtlList().size() > Constants.GL_ZERO){
						if(!util.isNullOrEmpty(callerIdentification_HostRes.getCardAcctDtlList().get(Constants.GL_ZERO))){
							cardType = callerIdentification_HostRes.getCardAcctDtlList().get(Constants.GL_ZERO).getCardType();
						}
					}

					callInfo.setField(Field.CREDITCARDTYPE, cardType);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Type is "+ cardType);}

					String cin = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered CIN is "+ util.maskCardOrAccountNumber(cin));}

					/**
					 * APIN Status has been handled at APIN Validation module
					 */
					
//					String apinStatus = Constants.EMPTY_STRING;
//
//					if(callerIdentification_HostRes.getCardDetailMap()!=null && cin!=null && callerIdentification_HostRes.getCardDetailMap().size() > Constants.GL_ZERO){
//						if(!util.isNullOrEmpty(callerIdentification_HostRes.getCardDetailMap().get(cin))){
//
//							apinStatus = callerIdentification_HostRes.getCardDetailMap().get(cin).getCardStatus();
//							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The CIN card APIN status is "+ apinStatus);}
//						}
//					}
//
//					callInfo.setField(Field.APIN_STATUS, apinStatus);

				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + callerIdentification_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CUSTOMERPROFILEAGGREGATE, callerIdentification_HostRes.getHostResponseCode());
				}
				/*********************ISD Fetch Change***************************/
				if(Constants.FEATURENAME_CARDPINSET.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))
						|| Constants.FEATURENAME_CARDPINRESET.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code) || !util.isNullOrEmpty(uui)){
					code = Constants.WS_FAILURE_CODE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call callerIdentification_HostRes.CustomerProfileAggregate");}

					/**
					 * For Reporting Purpose
					 */
					hostReportDetailsForSecHost = new HostReportDetails();

					featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
					hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
					strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
					hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_CustDtls);
					//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
					hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
					hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
					hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
					hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

					hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
					hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
					//End Reporting
					
					
					/*
					 *  Setting NA values
					 */
					hostReportDetailsForSecHost.setHostEndTime(Constants.NA);
					hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
					hostReportDetailsForSecHost.setHostResponse(Constants.NA);
					
					callInfo.setHostReportDetails(hostReportDetailsForSecHost);
					ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
					callInfo.insertHostDetails(ivrdata);
					
					/* END */
					
					
					requestType_Obj = util.isNullOrEmpty(iceFeatureData_Obj.getConfig().getParamValue(Constants.CUI_CustDtls_REQUESTTYPE))? null : (String)iceFeatureData_Obj.getConfig().getParamValue(Constants.CUI_CustDtls_REQUESTTYPE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType_Obj);}

					CustDtls_HostRes custDtls_HostRes = getCustDtlsDAO().getCustDtlsHostRes(callInfo, SelectedCardOrAcctNo, customerID, requestType_Obj);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "custDtls_HostRes Object is :"+ custDtls_HostRes);}
					callInfo.setCustDtls_HostRes(custDtls_HostRes);
					code = custDtls_HostRes.getErrorCode();


					/**
					 * For handling caller identification error response code
					 */
					callInfo.setField(Field.CallerIdenfErrorCode, custDtls_HostRes.getErrorCode());
					callInfo.setField(Field.CallerIdentfHostResponseCode, custDtls_HostRes.getHostResponseCode());
					//END

					/*
					 * For Reporting Start
					 */
					
					/****Duplicate RRN Fix 25012016 *****/
					strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
							+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
					/************************************/
					
					
					
					hostEndTimeForSecHost = custDtls_HostRes.getHostEndTime();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
					hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

					hostResCodeForSecHost = custDtls_HostRes.getHostResponseCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
					hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

					responseDescForSecHost = Constants.HOST_FAILURE;
					if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
						responseDescForSecHost = Constants.HOST_SUCCESS;
					}
					hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
							+ Constants.EQUALTO + hostResCodeForSecHost 			
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(custDtls_HostRes.getErrorDesc()) ?"NA" :custDtls_HostRes.getErrorDesc());
					hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

					callInfo.setHostReportDetails(hostReportDetailsForSecHost);
					ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

					callInfo.updateHostDetails(ivrdataForSecHost);
					//End Reporting

					if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for CustDtls");}
					}
				
				/************************************************/
				}
			}
			}else if(Constants.WS_FAILURE_CODE.equalsIgnoreCase(code) && !util.isNullOrEmpty(callInfo.getCallerIdenf_DebitCardDetails()) &&
					!util.isNullOrEmpty(callInfo.getCallerIdenf_DebitCardDetails().getHostResponseCode())){

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for GetDebitCardDetails host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " +(String) callInfo.getCallerIdenf_DebitCardDetails().getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_GETDEBITCARDDETAILS, (String) callInfo.getCallerIdenf_DebitCardDetails().getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CallerIdentificationImpl.getCallerIdentification()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.getCallerIdentification() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public int getDebitCardLength(CallInfo callInfo) throws ServiceException {
		getConfigurationParam(callInfo);
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.getDebitCardLength()");}
		int returnValue = Constants.GL_SIXTEEN;

		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit card length is"+ callInfo.getField(Field.DebitCardLength));}
			returnValue = Integer.parseInt(callInfo.getField(Field.DebitCardLength).toString());

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.getDebitCardLength() "+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return returnValue;
	}

	//	
	//	public CallerIdentification_HostRes callCallerIdenfHostAccess(CallInfo callInfo){
	//		
	//		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.callCallerIdenfHostAccess()");}
	//		CallerIdentification_HostRes responseValue = null;
	//		
	//		
	//		
	//		return responseValue;
	//	}
	//	


	@Override
	public boolean isCallerIdentified(CallInfo callInfo) throws ServiceException {
		getConfigurationParam(callInfo);
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.isCallerIdentified()");}
		boolean returnValue = false;
		try{

			returnValue = (boolean)callInfo.getField(Field.ISCALLERIDENTIFIED);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the Caller Identified "+ returnValue);}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.isCallerIdentified() "+ e.getMessage());}
			throw new ServiceException(e);
		}

		return returnValue;
	}
	public boolean isCreditResetPINNotAllow(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.isCreditResetPINAllow()");}
		boolean returnValue = false;
		String creditNPBINS = "";
		try {
			getConfigurationParam(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Processing the User Entered card number");}

			String userEnteredCINNumber  = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			if(util.isNullOrEmpty(userEnteredCINNumber)){
				throw new DaoException("User entered CIN number field is null or empty");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User entered CIN number ending with "+util.getSubstring(userEnteredCINNumber, Constants.GL_FOUR));}
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CreditRPNotAllowBIN))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success error code received from the Feature - CreditRP not allow Bins");}
				creditNPBINS = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CreditRPNotAllowBIN);
				if (creditNPBINS.contains(userEnteredCINNumber.substring(0, 6))) {
					returnValue = true;
				}
			}
			else{
				throw new DaoException(" - CreditRP not allow Bins are not configured at feature level");
			}
		} catch (Exception pe) {
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CallerIdentificationImpl.isBINValidated() "	+ pe.getMessage());}
			throw new ServiceException(pe);
		}
		return returnValue;
	}
	public boolean isPrepaidResetPINNotAllow(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.isPrepaidResetPINNotAllow()");}
		boolean returnValue = false;
		String prepaidNPBINS = "";
		try {
			getConfigurationParam(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Processing the User Entered card number");}

			String userEnteredCINNumber  = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			if(util.isNullOrEmpty(userEnteredCINNumber)){
				throw new DaoException("User entered CIN number field is null or empty");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User entered CIN number ending with "+util.getSubstring(userEnteredCINNumber, Constants.GL_FOUR));}
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.PrepaidRPNotAllowBIN))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success error code received from the Feature - PrepaidRP not allow Bins");}
				prepaidNPBINS = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.PrepaidRPNotAllowBIN);
				if (prepaidNPBINS.contains(userEnteredCINNumber.substring(0, 6))) {
					returnValue = true;
				}
			}
			else{
				throw new DaoException(" - CreditRP not allow Bins are not configured at feature level");
			}
		} catch (Exception pe) {
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CallerIdentificationImpl.isPrepaidResetPINNotAllow() "	+ pe.getMessage());}
			throw new ServiceException(pe);
		}
		return returnValue;
	}
	public boolean isBINValidated(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.isBINValidated()");}
		boolean returnValue = false;
		try{
			getConfigurationParam(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Processing the User Entered card number");}

			String userEnteredCINNumber  = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			callInfo.setField(Field.CIN, userEnteredCINNumber);
			if(util.isNullOrEmpty(userEnteredCINNumber)){
				throw new DaoException("User entered CIN number field is null or empty");
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "User entered CIN number ending with "+util.getSubstring(userEnteredCINNumber, Constants.GL_FOUR));}


			String[] dcBINArray = (String[]) callInfo.getField(Field.DCBINNUMBERS);
			String[] ccBINArray = (String[]) callInfo.getField(Field.CCBINNUMBERS);
			String[] islamicBINArray = (String[]) callInfo.getField(Field.ISLAMICBINNUMBERS);
			String[] prepaidBINArray = (String[]) callInfo.getField(Field.PREPAIDBINNUMBERS);
			String cinType = Constants.EMPTY;
			String binType = Constants.EMPTY;

			if(util.isNullOrEmpty(dcBINArray) || util.isNullOrEmpty(ccBINArray) || util.isNullOrEmpty(prepaidBINArray)){
				//throw new DaoException("Credit / Debit card bin number is not set");
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DC & CC && Prepaid Bin numbers are not configured, so considering true");}

				return true;
			}

			for(int pc_Count=0; pc_Count<prepaidBINArray.length; pc_Count++){
				if(prepaidBINArray[pc_Count].equalsIgnoreCase(userEnteredCINNumber.substring(Constants.GL_ZERO, prepaidBINArray[pc_Count].length()))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the CIN type as PREPAID");}
					cinType = Constants.CIN_TYPE_PREPAID;
					callInfo.setField(Field.PREPAIDCARDNUMBER, userEnteredCINNumber);
					returnValue = true;
					break;
				}
			}
			for(int cc_Count=0; cc_Count<ccBINArray.length; cc_Count++){
				if(ccBINArray[cc_Count].equalsIgnoreCase(userEnteredCINNumber.substring(Constants.GL_ZERO, ccBINArray[cc_Count].length()))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the CIN type as CREDIT");}
					cinType = Constants.CIN_TYPE_CREDIT;
					callInfo.setField(Field.CREDITCARDNUMBER, userEnteredCINNumber);
					returnValue = true;
					break;
				}
			}

			for(int dc_Count=0; dc_Count<dcBINArray.length; dc_Count++){
				if(dcBINArray[dc_Count].equalsIgnoreCase(userEnteredCINNumber.substring(Constants.GL_ZERO, dcBINArray[dc_Count].length()))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the CIN type as DEBIT");}
					cinType = Constants.CIN_TYPE_DEBIT;
					callInfo.setField(Field.DEBITCARDNUMBER, userEnteredCINNumber);
					returnValue = true;
					break;
				}
			}
			
			String dnisType = (String) callInfo.getField(Field.DNIS_Type);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Dnis Type is:"+dnisType);}
			if(!dnisType.equalsIgnoreCase(Constants.CUST_SEGMENT_ISLAMIC) || !dnisType.equalsIgnoreCase(Constants.CUST_SEGMENT_ISLAMICPRIORITY)){
				for(int islamic_Count=0; islamic_Count < islamicBINArray.length; islamic_Count++){
					if(islamicBINArray[islamic_Count].equalsIgnoreCase(userEnteredCINNumber.substring(Constants.GL_ZERO, islamicBINArray[islamic_Count].length()))){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the BIN type as ISLAMIC BIN");}
						binType = Constants.BIN_TYPE_ISLAMIC;
						callInfo.setField(Field.ENTEREDBINTYPE, binType);
						callInfo.setField(Field.ISISLAMICBINTYPE, "true");
						break;
					}
				}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the CIN type in Entered CIN Type and CIN Type");}
			callInfo.setField(Field.ENTEREDCINTYPE, cinType);
			callInfo.setField(Field.CIN_TYPE, cinType);

			//			HashMap<String, ArrayList<String>> binTable = new HashMap<String, ArrayList<String>>();
			//			binTable = (HashMap<String, ArrayList<String>>) callInfo.getField(Field.BINAndAccountTypes);
			//			
			//			if(util.isNullOrEmpty(binTable)){
			//				throw new DaoException("BIN Number table value is null / Empty");
			//			}
			//			
			//			String binKey = Constants.EMPTY_STRING;
			//			ArrayList<String> binNoList = new ArrayList<String>();
			//			ArrayList<String> tempList = new ArrayList<>();
			//			
			//			String strBinValue = Constants.EMPTY_STRING;
			//		
			//			int count = Constants.GL_ZERO;
			//			Iterator entries = binTable.entrySet().iterator();
			//			while (entries.hasNext()) {
			//			    Map.Entry entry = (Map.Entry) entries.next();
			//			    binKey = (String)entry.getKey();
			//			    if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+count+ "The bin table product Key is "+ binKey);}
			//			    binNoList = (ArrayList<String>)entry.getValue();
			//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The retireved bin no list for the key"+binKey+" is " +binNoList);}
			//				
			////				for(int index=0; index < binNoList.size(); index++){
			////					strBinValue = binNoList.get(index);
			////					
			////					if(strBinValue.equalsIgnoreCase(userEnteredCINNumber.substring(Constants.GL_ZERO, strBinValue.length()))){
			////						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Its a valid bin number ");}
			////						if(binKey.contains(Constants.BIN_TYPE_DC) || binKey.contains(Constants.BIN_TYPE_DEBIT)){
			////							cinType = Constants.CIN_TYPE_DEBIT;
			////						}else{
			////							cinType = Constants.CIN_TYPE_CREDIT;
			////						}
			////						
			////						returnValue = true;
			////						break;
			////					}
			////				}
			//				
			//				if(returnValue){
			//					break;
			//				}
			//				
			//			}
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Total Number of bin product types presents are "+binTable.size());}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CustomerProfileAggregateDAOImpl.isAValidAcctOrCardNo()");}
			return returnValue;

		}catch(Exception pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CustomerProfileAggregateDAOImpl.isAValidAcctOrCardNo() "	+ pe.getMessage());}
			throw new ServiceException(pe);
		}
	}

	
	@Override
	public boolean isPriorityCustomer(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.getDebitCardLength()");}
		boolean returnValue = false;
		try{
			getConfigurationParam(callInfo);
			
			
			
			String priorityCustomer = Constants.N;
			
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes()) && !util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())
					&& !util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPriorityCustomer())){
				priorityCustomer = callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPriorityCustomer();
			}
			callInfo.setField(Field.ISPRIORITYCUSTOMER, false);
			//TODO - Need to modify the following condition check
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())&& !util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())&& !util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getPriorityCustomer()))
				if(!util.isNullOrEmpty(priorityCustomer) && (priorityCustomer.equalsIgnoreCase(Constants.Y) || priorityCustomer.equalsIgnoreCase("true") || priorityCustomer.equalsIgnoreCase("t") || priorityCustomer.equalsIgnoreCase("yes"))){
					callInfo.setField(Field.ISPRIORITYCUSTOMER, true);
					returnValue = true;
				}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the Customer is a Priority Customer "+ returnValue);}

			ICERuleParam ruleParamObj = (ICERuleParam) callInfo.getICERuleParam();
			if(returnValue && !(util.isNullOrEmpty(ruleParamObj))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting customer segmentations as Priority "+ returnValue);}
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_CUSTOMERSEGMEMTATION, Constants.CUST_SEGMENT_PRIORITY);
				ruleParamObj.updateIVRFields();
			}
			
			
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.isPriorityCustomer() "+ e.getMessage());}
			throw new ServiceException(e);
		}
		return returnValue;
	}

	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		//Since its a setting configuration param to call info session  variable dont throw any new exception
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.getConfigurationParam()");}

		try{
			
			/**
			 * Debit card bins and Credit  card bins should be taked from Global configuration.  hence this has changed accordingly on 04May2014
			 *
			 */

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
//			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			ICEGlobalConfig ivr_ICEGlobalConfig  = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
			
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ivr_ICEGlobalConfig is null /empty ");}
				throw new ServiceException("ivr_ICEGlobalConfig data object is null or empty");
			}
			int DebitCardLength = Constants.GL_SIXTEEN;
			String temp_Str = Constants.EMPTY_STRING;


			temp_Str = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DebitCardLength);

			if(!util.isNullOrEmpty(temp_Str)){
				DebitCardLength = Integer.parseInt(temp_Str);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit or Credit card lenght is "+ DebitCardLength);}

			callInfo.setField(Field.DebitCardLength, DebitCardLength);

			//Bin Number retrieval
			String ccBinNumbers = Constants.EMPTY_STRING;
			String dcBinNumbers = Constants.EMPTY_STRING;
			String islamicBinNumbers = Constants.EMPTY_STRING;
			String prepaidBinNumbers = Constants.EMPTY_STRING;
			String[] ccBinArray = null;
			String[] dcBinArray = null;
			String[] islamicBinArray = null;
			String[] prepaidBinArray = null;

			ccBinNumbers = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CreditCardBINNumbers);
			dcBinNumbers = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_DebitCardBINNumbers);
			islamicBinNumbers = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_IslamicBINNumbers);
			prepaidBinNumbers = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_PrepaidCardBINNumbers);

			if(util.isNullOrEmpty(ccBinNumbers)){
				throw new DaoException("Credit Card bin numbers are not configured at CUI");
			}

			if(util.isNullOrEmpty(dcBinNumbers)){
				throw new DaoException("Debit Card bin numbers are not configured at CUI");
			}
			
			if(util.isNullOrEmpty(prepaidBinNumbers)){
				throw new DaoException("Prepaid Card bin numbers are not configured at CUI");
			}

			ccBinArray = ccBinNumbers.split(Constants.COMMA);
			dcBinArray = dcBinNumbers.split(Constants.COMMA);
			islamicBinArray = islamicBinNumbers.split(Constants.COMMA);	
			prepaidBinArray = prepaidBinNumbers.split(Constants.COMMA);	

			//			Map<String, String> binProductKeyMap  = null;
			//			HashMap<String, ArrayList<String>> binTable = new HashMap<String, ArrayList<String>>();
			//			String binKey = Constants.EMPTY_STRING;
			//			ArrayList<String> binNoList = new ArrayList<String>();
			//			ArrayList<String> tempList = new ArrayList<>();
			//			binProductKeyMap = (Map<String, String>) ivr_ICEFeatureData.getConfig().getAllParamID();
			//			
			//			int count = Constants.GL_ZERO;
			//			Iterator entries = binProductKeyMap.entrySet().iterator();
			//			while (entries.hasNext()) {
			//			    Map.Entry entry = (Map.Entry) entries.next();
			//			    binKey = (String)entry.getKey();
			//			    
			//			    if(Constants.CUI_BIN_KEY.equalsIgnoreCase(binKey.substring(Constants.GL_ZERO,Constants.GL_FOUR))){
			//			    	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The "+count+ "The bin map Key is "+ binKey);}
			//			    	binNoList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(binKey);
			//			    	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The retireved bin no list for the key"+binKey+" is " +binNoList);}
			//			    	
			//			    	if(binTable.containsKey(binKey)){
			//			    		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Bin Number key is already present in the table");}
			//			    		tempList = binTable.get(binKey);
			//			    		binNoList.addAll(tempList);
			//			    		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Appending all the bin no value under one key");}
			//			    	}
			//			    	
			//			    	binTable.put(binKey, binNoList);
			//			    }
			//  			}

			callInfo.setField(Field.CCBINNUMBERS, ccBinArray);
			callInfo.setField(Field.DCBINNUMBERS, dcBinArray);
			callInfo.setField(Field.ISLAMICBINNUMBERS, islamicBinArray);
			callInfo.setField(Field.PREPAIDBINNUMBERS, prepaidBinArray);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Total Number of Credit card bins are "+ccBinArray.length);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Total Number of Debit card bins are "+dcBinArray.length);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Total Number of Islamic card bins are "+islamicBinArray.length);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Total Number of Prepaid card bins are "+prepaidBinArray.length);}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CallerIdentificationImpl.getConfigurationParam()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.getConfigurationParam() "	+ e.getMessage());}
			throw new ServiceException(e);
		}

	}

//	@Override
//	public boolean isBINCreditCard(CallInfo callInfo) throws ServiceException {
//		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
//		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CallerIdentificationImpl.isBINCreditCard()");}
//		boolean finalResult = false;
//		try{
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
//			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
//
//			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ivr_ICEFeatureData is null /empty ");}
//				throw new ServiceException("ICEFeature data object is null or empty");
//			}
//
//			String enteredDebitCardNo = Constants.EMPTY_STRING + callInfo.getField(Field.ENTEREDCINNUMBER);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entered CIN / Debit card number is " + enteredDebitCardNo);}
//
//
//			String ccBinNumbers = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CreditCardBINNumbers);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "The Credit card bin number is "+ccBinNumbers);}
//
//			finalResult = util.isCodePresentInTheConfigurationList(enteredDebitCardNo, ccBinNumbers);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Entered is a credit card number ? "+finalResult);}
//
//			return finalResult;
//
//		}catch(Exception e){
//			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.isBINCreditCard() "	+ e.getMessage());}
//			throw new ServiceException(e);
//		}
//	}

}
