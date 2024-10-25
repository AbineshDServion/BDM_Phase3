package com.servion.services;

import java.util.ArrayList;
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
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.dao.ListBeneficiaryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.fundsTransfer.FT_BeneficiaryDetailList_HostRes;
import com.servion.model.fundsTransfer.FT_BeneficiaryShortDetails;
import com.servion.model.fundsTransfer.FT_BenfPayeeDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class FundsTransferCharityImpl implements IFundsTransferCharity {

	private static Logger logger = LoggerObject.getLogger();

	private ListBeneficiaryDAO listBeneficiaryDAO;
	private BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO;

	public ListBeneficiaryDAO getListBeneficiaryDAO() {
		return listBeneficiaryDAO;
	}

	public void setListBeneficiaryDAO(ListBeneficiaryDAO listBeneficiaryDAO) {
		this.listBeneficiaryDAO = listBeneficiaryDAO;
	}

	public BeneficiaryDtlsInquiryDAO getBeneficiaryDtlsInquiryDAO() {
		return beneficiaryDtlsInquiryDAO;
	}

	public void setBeneficiaryDtlsInquiryDAO(
			BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO) {
		this.beneficiaryDtlsInquiryDAO = beneficiaryDtlsInquiryDAO;
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
	public String getFTCharityBeneficiaryList(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER:FundsTransferCharityImpl.getFTCharityBeneficiaryList()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String customerId = (String) callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Customer ID is "+customerId);}

			String paymentType = Constants.HOST_FT_PAYMENTTYPE_CHARITY;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested paymentType is "+paymentType);}


			/**
			 * For Reporting Purpose
			 */

			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam =  Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId + Constants.COMMA + Constants.HOST_INPUT_PARAM_PAYMENTTYPE + Constants.EQUALTO + 
					paymentType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_LISTBENEFICIARY);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting


			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}



			FT_BeneficiaryDetailList_HostRes ft_BeneficiaryDetailList_HostRes = listBeneficiaryDAO.getFTTWBMBenfListHostRes(callInfo, customerId, paymentType, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FT_BeneficiaryDetailList_HostRes Object is :"+ ft_BeneficiaryDetailList_HostRes);}
			callInfo.setFT_BeneficiaryDetailList_HostRes(ft_BeneficiaryDetailList_HostRes);

			code = ft_BeneficiaryDetailList_HostRes.getErrorCode();


			/*
			 * For Reporting Start
			 */

			String hostEndTime = ft_BeneficiaryDetailList_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = ft_BeneficiaryDetailList_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam =  Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId + Constants.COMMA + Constants.HOST_INPUT_PARAM_PAYMENTTYPE + Constants.EQUALTO + 
					paymentType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(ft_BeneficiaryDetailList_HostRes.getErrorDesc()) ?"NA" :ft_BeneficiaryDetailList_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.insertHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for list beneficiary");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary id list object is " + ft_BeneficiaryDetailList_HostRes.getBeneficiaryIdList());}
				if(!util.isNullOrEmpty(ft_BeneficiaryDetailList_HostRes.getBeneficiaryIdList()))
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total number of beneficiary id is :" + ft_BeneficiaryDetailList_HostRes.getBeneficiaryIdList().size());}

			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for List Beneficiary host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + ft_BeneficiaryDetailList_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LISTBENEFICIARY, ft_BeneficiaryDetailList_HostRes.getHostResponseCode());

			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT :FundsTransferCharityImpl.getFTCharityBeneficiaryList()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferCharityImpl.getFTCharityBeneficiaryList() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}


	public String getFTCharityBeneficiaryDetails(CallInfo callInfo) throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferCharityImpl.getFTCharityBeneficiaryDetails()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call Beneficiary Detail Enquiry Service");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is "+ customerID);}

			if(util.isNullOrEmpty(customerID)){
				throw new ServiceException("customerID value is null");
			}

			ArrayList<String> beneficiaryIdList = null;

			HashMap<String,FT_BeneficiaryShortDetails> benficiaryIdMap = null;
			FT_BeneficiaryShortDetails ft_BeneficiaryShortDetails = null;
			if(!util.isNullOrEmpty(callInfo.getFT_BeneficiaryDetailList_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getFT_BeneficiaryDetailList_HostRes().getBeneficiaryIdList())
						&& !util.isNullOrEmpty(callInfo.getFT_BeneficiaryDetailList_HostRes().getBenefShortDescDetailMap())){
					beneficiaryIdList = callInfo.getFT_BeneficiaryDetailList_HostRes().getBeneficiaryIdList();
					benficiaryIdMap =  callInfo.getFT_BeneficiaryDetailList_HostRes().getBenefShortDescDetailMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary ID list received is " + beneficiaryIdList);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + benficiaryIdMap.size());}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiaty short detail map object is " + benficiaryIdMap);}

				}
			}

			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ICE Feature data object is null or empty" + ivr_ICEFeatureData);}
				throw new ServiceException("ICE Feature data object is null or empty");
			}

			ArrayList<String> configuredCharityList = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FT_CHARITY_CODE_LIST);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Charity List are"+ configuredCharityList);}

			ArrayList<String> validBeneficiaryIDList = new ArrayList<>();
			if(!util.isNullOrEmpty(beneficiaryIdList) && !util.isNullOrEmpty(benficiaryIdMap)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Beneficiary ID available is " +beneficiaryIdList.size() );}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available Beneficiaries are " +beneficiaryIdList);}

				String charityCode = Constants.EMPTY_STRING;
				Iterator iterator = benficiaryIdMap.entrySet().iterator();
				Map.Entry mapEntry = null;
				if(!util.isNullOrEmpty(configuredCharityList)){
					while (iterator.hasNext()) {
						mapEntry = (Map.Entry) iterator.next();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The key is " +mapEntry.getKey());}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Value is " +mapEntry.getValue());}

						ft_BeneficiaryShortDetails = (FT_BeneficiaryShortDetails) mapEntry.getValue();

						for(int i=0; i< configuredCharityList.size(); i++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The configured utility code from the configured list is " +configuredCharityList.get(i));}
							charityCode = configuredCharityList.get(i);

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code of the Beneficiary id " +ft_BeneficiaryShortDetails.getBeneficiaryId()
									+" is" +ft_BeneficiaryShortDetails.getUtilityCode());}	
							if(charityCode!=null  && charityCode.equalsIgnoreCase(ft_BeneficiaryShortDetails.getUtilityCode())){
								validBeneficiaryIDList.add(ft_BeneficiaryShortDetails.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the beneficiary id " +ft_BeneficiaryShortDetails.getBeneficiaryId() + "into the list");}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The list count is " +ft_BeneficiaryShortDetails.getBeneficiaryId() + "into the list");}
							}
						}

					}
				}


				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final beneficiary id list is " +validBeneficiaryIDList);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final beneficiary id list size is " +validBeneficiaryIDList.size());}

			}

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
			hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY);
			//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
			hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

			hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting

			/*
			 *  Setting NA values
			 */
			hostReportDetailsForSecHost.setHostEndTime(util.getCurrentDateTime());
			hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
			hostReportDetailsForSecHost.setHostResponse(Constants.NA);
			
			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdataForSecHost);
			
			/* END */
			
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			FT_BenfPayeeDetails_HostRes ft_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getFTTWBMBenfDelsHostRes(callInfo, validBeneficiaryIDList, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ft_BenfPayeeDetails_HostRes Object is :"+ ft_BenfPayeeDetails_HostRes);}
			callInfo.setFT_BenfPayeeDetails_HostRes(ft_BenfPayeeDetails_HostRes);
			code = ft_BenfPayeeDetails_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String hostEndTimeForSecHost = ft_BenfPayeeDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
			hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

			String hostResCodeForSecHost = ft_BenfPayeeDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
			hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

			String responseDescForSecHost = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDescForSecHost = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
			/************************************/
			
			String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCodeForSecHost
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(ft_BenfPayeeDetails_HostRes.getErrorDesc()) ?"NA" :ft_BenfPayeeDetails_HostRes.getErrorDesc());
			hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdataForSecHost);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Beneficiary Details");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + ft_BenfPayeeDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY, ft_BenfPayeeDetails_HostRes.getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FundsTransferCharityImpl.getFTCharityBeneficiaryDetails()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferCharityImpl.getFTCharityBeneficiaryDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}
	@Override
	public String getFTCharityPayeeListPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentElectricityImpl.getElectricityBillAccountNumberMenuPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();


			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null / empty");
			}
			//Need to get the FeatureConfig Data
			ArrayList<String> configuredCharityList = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FT_CHARITY_CODE_LIST);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Charity List are"+ configuredCharityList);}
			boolean allowAllCharity = false;

			if(util.isNullOrEmpty(configuredCharityList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Charity code was not configure at the host so going to allow all host return charity codes");}
				allowAllCharity = true;
			}

			ArrayList<String> charityCodeListFromHost = null;

			//Calling the beneficiary Details host service to get all beneficiary details of the available utility type beneficiary ids
			String beneficiaryDetlHostCode = getFTCharityBeneficiaryDetails(callInfo);

			if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
				throw new ServiceException("Utility_BenfPayeeDetails_HostRes object is null / Empty");
			}

			if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getFT_BenfPayeeDetails_HostRes().getCharityCodeList())){
					charityCodeListFromHost = callInfo.getFT_BenfPayeeDetails_HostRes().getCharityCodeList();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The charityCodeListFromHost retrieved from host is "  + charityCodeListFromHost);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of charityCodeListFromHost available is"  + charityCodeListFromHost.size());}
			
					
					if(charityCodeListFromHost.size() == 1 && (callInfo.getFT_BenfPayeeDetails_HostRes().getCharityCodeList().get(0) == null ||  
							callInfo.getFT_BenfPayeeDetails_HostRes().getCharityCodeList().get(0) == Constants.EMPTY)){
						charityCodeListFromHost = null;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The charityCodeListFromHost retrieved from host is "  + charityCodeListFromHost);}
				
					}
				
				}else{
					throw new ServiceException("charityCodeListFromHost object is null / Empty");
				}
			}else{
				throw new ServiceException("getFT_BenfPayeeDetails_HostRes access object is null / Empty");
			}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_CharityCode = null;
			int validPayeeCount = Constants.GL_ZERO;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int temp_MoreCount = int_moreCount - 1;

			for(int count=Constants.GL_ZERO;count<charityCodeListFromHost.size();count++){
				temp_CharityCode = charityCodeListFromHost.get(count);

				if(!allowAllCharity){
					if(!util.isNullOrEmpty(temp_CharityCode) && configuredCharityList.contains(temp_CharityCode)){
						validPayeeCount++;

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Charity code is " + temp_CharityCode);}

						dynamicValueArray.add((temp_CharityCode+Constants.WAV_EXTENSION).trim());

						if(count == temp_MoreCount){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
							moreOption = true;
							callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
						}


						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Charity code in the grammar list "+temp_CharityCode);}

						if(util.isNullOrEmpty(grammar)){
							grammar = temp_CharityCode;
						}else{
							grammar = grammar + Constants.COMMA + temp_CharityCode;
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

					}
				}else{
					if(!util.isNullOrEmpty(temp_CharityCode)){

						validPayeeCount++;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" Charity code is " + temp_CharityCode);}

						dynamicValueArray.add((temp_CharityCode+Constants.WAV_EXTENSION).trim());

						if(count == temp_MoreCount){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
							moreOption = true;
							callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Charity code in the grammar list "+temp_CharityCode);}

						if(util.isNullOrEmpty(grammar)){
							grammar = temp_CharityCode;
						}else{
							grammar = grammar + Constants.COMMA + temp_CharityCode;
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}
					}

				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Valid payee Charity code total count is "+validPayeeCount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			/**
			 * Handled inorder to announce technical difficulties if there is no specific charity there
			 */

			if(validPayeeCount == Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no specific charity code for this customer ");}
				return Constants.ONE;
			}
			//END 

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("FUNDTRANSFER_CHARITY");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Funds_Transfer_Charity");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
			//END - Vinoth
			
			
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
			if(validPayeeCount >int_moreCount){
				totalPrompt = Constants.GL_THREE * validPayeeCount;
				//totalPrompt = totalPrompt + Constants.GL_TWO;
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = validPayeeCount / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

				int temp2 =  validPayeeCount % int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
				if(temp2 == 0){
					temp1--;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient decreased by one "+temp1);}
				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
				
			}
			else{
				totalPrompt = Constants.GL_THREE * validPayeeCount;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

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


			//Need to handle if we want to append pipeseperator sign
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentElectricityImpl.getElectricityBillAccountNumberMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentElectricityImpl.getElectricityBillAccountNumberMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
