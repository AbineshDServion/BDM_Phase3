package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
import com.servion.dao.DebitCardServiceRequestDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.debitCardActivation.ActivateCard_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class DebitCardActivationImpl implements IDebitCardActivation {

	private static Logger logger = LoggerObject.getLogger();

	private DebitCardServiceRequestDAO debitCardServiceRequestDAO;
	public DebitCardServiceRequestDAO getDebitCardServiceRequestDAO() {
		return debitCardServiceRequestDAO;
	}

	public void setDebitCardServiceRequestDAO(
			DebitCardServiceRequestDAO debitCardServiceRequestDAO) {
		this.debitCardServiceRequestDAO = debitCardServiceRequestDAO;
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
	public String getCardActivation(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: DebitCardActivationImpl.getCardActivation()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String requestType = Constants.HOST_REQUESTTYPE_ACTIVATECARD;
			String primaryAccountNum = Constants.EMPTY_STRING;

			String isafterMainMenu=(String)callInfo.getField(Field.isAfterMainMenu);
			if(Constants.TRUE.equalsIgnoreCase(isafterMainMenu)){
				primaryAccountNum = (String) callInfo.getField(Field.SRCNO);
			}else{
				primaryAccountNum = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Primary card / source account number is  " + util.maskCardOrAccountNumber(primaryAccountNum));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Request type is " + requestType);}



			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)+Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO+ Constants.EQUALTO +util.maskCardOrAccountNumber(primaryAccountNum) + Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + 
					requestType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_DEBITCARDSERVICEREQUEST);
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
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);
			
			/* END */

			String processingCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_ProcessingCode);
			//			int intProcessingCode = util.isNullOrEmpty(processingCode)?Constants.GL_ZERO:Integer.parseInt(processingCode);

			String amtTransaction = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_AmtTransaction);

			String transactionDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);
			//(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_TransmissionDate);

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

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
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


			String sysTraceAuditNumber = strRefNumberOne;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "System Trace audit number " + sysTraceAuditNumber);}
			//					util.getRandomNumber(999999) + Constants.EMPTY_STRING; 
			//			int intSysTraceAuditNumber = util.isNullOrEmpty(sysTraceAuditNumber)?Constants.GL_ZERO:Integer.parseInt(sysTraceAuditNumber);

			String localTime = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
			String localDate = (String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDD);
			String expirationDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_ExpirationDate);
			String settlementDate = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_SettlementDate);

			String merchantType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_MerchantType);
			//			int intMerchantType = util.isNullOrEmpty(merchantType)?Constants.GL_ZERO:Integer.parseInt(merchantType);

			String serviceEntryMode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_ServiceEntryMode);
			//			int intServiceEntryMode = util.isNullOrEmpty(serviceEntryMode)?Constants.GL_ZERO:Integer.parseInt(serviceEntryMode);

			String cardSequenceNum = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_CardSeqNum);
			//			int intCardSequenceNum = util.isNullOrEmpty(cardSequenceNum)?Constants.GL_ZERO:Integer.parseInt(cardSequenceNum);

			String serviceCondCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_ServCondCode);
			//			int intServiceCondCode = util.isNullOrEmpty(serviceCondCode)?Constants.GL_ZERO:Integer.parseInt(serviceCondCode);

			String serviceCaptureCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_ServCaptureCode);
			//			int intServiceCaptureCode = util.isNullOrEmpty(serviceCaptureCode)?Constants.GL_ZERO:Integer.parseInt(serviceCaptureCode);


			String transCurrCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_CurrCode);

			String extendedTransType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_DebitCardServiceRequest_ExtendedTransactionType);

			
			/**
			 * Following are the modification done on 01-Sep-2014 for the handling of dynamic Debit card length (15 to 19)
			 */
			int panLength = util.isNullOrEmpty(primaryAccountNum)?Constants.GL_SIXTEEN : primaryAccountNum.length();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length is "+ panLength);}
			
			String panLengthKey = Constants.UNDERSCORE + panLength;
			panLengthKey = panLengthKey.trim();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length key is "+ panLengthKey);}
			
			panLengthKey = Constants.CUI_UI_DebitCardServiceRequest_RequestStructData_Activation+panLengthKey;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The new Request Structure Data key is "+ panLengthKey);}
			//END 
			
			
			
			String structureData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(panLengthKey);
			structureData = (structureData!=null)? structureData.replace(Constants.STRUCTURED_DATA_PAN_KEY, primaryAccountNum): null;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Finalized Structured data is "+ structureData);}

			String fwdInstitutionIdnefCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_FwdInstitutionIDCode);
			String recvInstitutionIdnefCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_RecvInstitutionIDCode);
			String trackTwoData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_TrackTwoData);
			String cardAcceptorTerminalID = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_CardAccpTerminalID); 
			String cardAcceptorIDCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_CardAccpIDCode);
			String cardAcceptorName = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_CardAccpName); 
			String pin = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_Pin); 
			String securityControlInfo = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_SecurityContInfo); 
			String mesgReasonCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_SecurityContInfo); 
			String originalDataElement = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_OriginalDataElement); 
			String accountIdentification = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_AcctIdenf); 
			String posDataCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_PosDataCode); 
			String bitMap = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_BitMap); 
			String terminalOwner = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_TerminalOwner);
			String posGeographicData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_PosGeographicData);
			String sponsorBank = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_SponsorBank);
			String iccData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_IccData);
			String originalNode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_OrigData); 
			String mesgAuthCode =  (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_MacField);
			String addrVerfData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_AddrVerfData);

			ActivateCard_HostRes activateCard_HostRes = debitCardServiceRequestDAO.getDebitCardActivationHostRes(callInfo, requestType, primaryAccountNum, processingCode, amtTransaction, transactionDate, 
					sysTraceAuditNumber, localTime, localDate, expirationDate, settlementDate, merchantType, serviceEntryMode, cardSequenceNum, serviceCondCode, serviceCaptureCode, 
					fwdInstitutionIdnefCode, trackTwoData, cardAcceptorTerminalID, cardAcceptorIDCode, cardAcceptorName, pin, securityControlInfo, mesgReasonCode, originalDataElement, 
					recvInstitutionIdnefCode, accountIdentification, posDataCode, bitMap, terminalOwner, posGeographicData, sponsorBank, addrVerfData, iccData, originalNode, mesgAuthCode, transCurrCode, structureData, extendedTransType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ActivateCard_HostRes Object is :"+ activateCard_HostRes);}
			callInfo.setActivateCard_HostRes(activateCard_HostRes);

			code = activateCard_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String hostEndTime = activateCard_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = activateCard_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)+Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO+ Constants.EQUALTO +util.maskCardOrAccountNumber(primaryAccountNum) + Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO + 
					requestType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(activateCard_HostRes.getErrorDesc()) ?"NA" :activateCard_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
//				code = Constants.WS_FAILURE_CODE;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit Card is activated successfully ");}


				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit card which is activated is " + primaryAccountNum);}


				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){


					ArrayList<String>inActiveDbtCrd = callInfo.getField(Field.DRVISACARDLISTINACTIVE)!=null?(ArrayList<String>)callInfo.getField(Field.DRVISACARDLISTINACTIVE) : new ArrayList<String>();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Inactive debit card list and count is " + inActiveDbtCrd.size());}	
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Inactive debit card list is " + inActiveDbtCrd + " and count is " + inActiveDbtCrd.size());}
					
					ArrayList<String>activeDbtCrd = callInfo.getField(Field.DRVISACARDLIST)!=null?(ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST) :  new ArrayList<String>();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current active debit card list and count is " + activeDbtCrd.size());}
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current active debit card list is " +activeDbtCrd + " and count is " + activeDbtCrd.size());}
					
					String inActiveDbtVISACrdCount = util.isNullOrEmpty(callInfo.getField(Field.NO_OF_DR_VISA_CARDS_INACTVE))?Constants.ZERO : Constants.EMPTY + callInfo.getField(Field.NO_OF_DR_VISA_CARDS_INACTVE);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Inactive debit card VISA count is " + inActiveDbtVISACrdCount);}	
					int int_InActiveVisa = Integer.parseInt(inActiveDbtVISACrdCount);

					//				String activeDbtCrdVISACount = callInfo.getField(Field.NO_OF_DR_VISA_CARDS)!=null?(String)callInfo.getField(Field.NO_OF_DR_VISA_CARDS) :  Constants.ZERO;
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current active debit card VISA card count is " + activeDbtCrdVISACount);}
					//				int int_ActiveVisa = Integer.parseInt(activeDbtCrdVISACount);

					String inActiveDbtMasterCrdCount = !util.isNullOrEmpty(Field.NO_OF_DR_MASTER_CARDS_INACTIVE)?Constants.EMPTY + callInfo.getField(Field.NO_OF_DR_MASTER_CARDS_INACTIVE) : Constants.ZERO;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Inactive debit card Master count is " + inActiveDbtMasterCrdCount);}	
					int int_InActiveMaster = Integer.parseInt(inActiveDbtMasterCrdCount);

					//				String activeDbtCrdMasterCount = callInfo.getField(Field.NO_OF_DR_MASTER_CARDS)!=null?(String)callInfo.getField(Field.NO_OF_DR_MASTER_CARDS) :  Constants.ZERO;
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current active debit card Master card count is " + activeDbtCrdMasterCount);}
					//				int int_ActiveMaster = Integer.parseInt(activeDbtCrdMasterCount);

					String inActiveDbtAmexCrdCount = !util.isNullOrEmpty(Field.NO_OF_DR_AMEX_CARDS_INACTIVE)? Constants.EMPTY+callInfo.getField(Field.NO_OF_DR_AMEX_CARDS_INACTIVE) : Constants.ZERO;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Inactive debit card Amex count is " + inActiveDbtAmexCrdCount);}	
					int int_inActiveAmex = Integer.parseInt(inActiveDbtAmexCrdCount);


					//				String activeDbtCrdAmexCount = callInfo.getField(Field.NO_OF_DR_AMEX_CARDS)!=null?(String)callInfo.getField(Field.NO_OF_DR_AMEX_CARDS) :  Constants.ZERO;
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current active debit card Amex card count is " + activeDbtCrdAmexCount);}
					//				int int_ActiveAmex =  Integer.parseInt(activeDbtCrdAmexCount);

					ICEGlobalConfig ivr_ICEGlobalObject = (ICEGlobalConfig)callInfo.getICEGlobalConfig();
					if(util.isNullOrEmpty(ivr_ICEGlobalObject)){
						throw new ServiceException("ivr_ICEGlobalObject object is null");
					}

					String debitCardVisaCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_VISA_DEBITCARD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Debit card Visa BIN type is "+ debitCardVisaCodeList);}

					String debitCardMasterCodeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_MASTER_DEBITCARD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Debit card Master BIN type is "+ debitCardMasterCodeList);}

					String debitCardAmexCodeeList = (String)ivr_ICEGlobalObject.getConfig().getParamValue(Constants.CUI_BIN_FOR_AMEX_DEBITCARD);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Configured Debit card Amex BIN type is "+ debitCardAmexCodeeList);}


					//Manipulating
					//				activeDbtCrd.add(primaryAccountNum);
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Lis of Active debit card are " + activeDbtCrd);}

					if(inActiveDbtCrd.contains(primaryAccountNum)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "List of In Active debit card is " + inActiveDbtCrd.size());}
						inActiveDbtCrd.remove(primaryAccountNum);
					}

					//				callInfo.setField(Field.DRVISACARDLIST, activeDbtCrd);
					callInfo.setField(Field.DRVISACARDLISTINACTIVE, inActiveDbtCrd);

					//					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Caller identification object ");}
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Card detail map object ");}


						if(util.isBinNoEligible(primaryAccountNum, debitCardVisaCodeList)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Activated is a Debit VISA card");}
							//							int_ActiveVisa++;
							int_InActiveVisa--;
						}

						if(util.isBinNoEligible(primaryAccountNum, debitCardMasterCodeList)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Activated is a Debit MASTER card");}
							//							int_ActiveMaster++;
							int_InActiveMaster--;
						}

						if(util.isBinNoEligible(primaryAccountNum, debitCardAmexCodeeList)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Activated is a Debit AMEX card");}
							//							int_ActiveAmex++;
							int_inActiveAmex--;
						}

					}
					//					}

					//				callInfo.setField(Field.NO_OF_DR_VISA_CARDS, int_ActiveVisa);
					callInfo.setField(Field.NO_OF_DR_VISA_CARDS_INACTVE, int_InActiveVisa);


					//				callInfo.setField(Field.NO_OF_DR_MASTER_CARDS, int_ActiveMaster);
					callInfo.setField(Field.NO_OF_DR_MASTER_CARDS_INACTIVE, int_InActiveMaster);


					//				callInfo.setField(Field.NO_OF_DR_AMEX_CARDS, int_ActiveAmex);
					callInfo.setField(Field.NO_OF_DR_AMEX_CARDS_INACTIVE, int_inActiveAmex);


					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Activated is a In Active Debit VISA card" + int_InActiveVisa);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Activated is a In Active Debit Master card" + int_InActiveMaster);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Activated is a In Active Debit Amex card" + int_inActiveAmex);}

					/**
					 * Rule engine update
					 */
					ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

					if(util.isNullOrEmpty(ruleParamObj)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
					}

					//Initialized the Rule Engine Object

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Total No Of Inactive cards " + inActiveDbtCrd);}

					if(!util.isNullOrEmpty(inActiveDbtCrd)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Total No Of Inactive Debit Cards in the Rule Engine " + inActiveDbtCrd);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDINACTIVECOUNT, inActiveDbtCrd + Constants.EMPTY);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Total No Of available Debit Cards in the Rule Engine " + inActiveDbtCrd);}
						ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDCOUNT, inActiveDbtCrd + Constants.EMPTY);

						ruleParamObj.updateIVRFields();
					}
				}



			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for DebitCardActivation host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + activateCard_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_DEBITCARDSERVICEREQUEST, activateCard_HostRes.getHostResponseCode());
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: DebitCardActivationImpl.getCardActivation()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  DebitCardActivationImpl.getCardActivation() "+ e.getMessage());
//			e.printStackTrace();
			throw new ServiceException(e);
		}
		return code;


	}

	@Override
	public String getDebitCardActivationConfirmPhrases(CallInfo callInfo)

			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: DebitCardActivationImpl.getDebitCardActivationConfirmPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			String primaryAccountNum = Constants.EMPTY_STRING;
			String isAfterMainMenu=(String)callInfo.getField(Field.isAfterMainMenu);
			//TODO
			if(Constants.TRUE.equalsIgnoreCase(isAfterMainMenu)){
				primaryAccountNum = (String) callInfo.getField(Field.SRCNO);
			}else{
				primaryAccountNum = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + util.maskCardOrAccountNumber(primaryAccountNum));}

			String lastNDigit = (String)callInfo.getField(Field.LastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last n digit value is " + lastNDigit);}
			int int_LastNDigit = util.isNullOrEmpty(lastNDigit)? Constants.GL_FOUR : Integer.parseInt(lastNDigit);

			String anntAnnc = util.getSubstring(primaryAccountNum, int_LastNDigit);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Announcing account ending digit is " + anntAnnc);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			//Need to handle the Dynamic phrase list and s Grammar portions
			dynamicValueArray.add(anntAnnc);


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CARD_ACTIVATION_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Card_Activation");
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
			//No Need

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


			//Need to handle if we want to append pipeseperator sign
			//No Need

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: DebitCardActivationImpl.getDebitCardActivationConfirmPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at DebitCardActivationImpl.getDebitCardActivationConfirmPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;

	}


	public boolean isDebitCardActive(CallInfo callInfo)throws ServiceException{

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: DebitCardActivationImpl.isDebitCardActive()");}

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			String acctNumber = Constants.EMPTY_STRING;

			String isAfterMainMenu=(String)callInfo.getField(Field.isAfterMainMenu);

			//TODO
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String cardActiveStatus = null;
			boolean isCardActive = false;

			if(Constants.TRUE.equalsIgnoreCase(isAfterMainMenu)){
				acctNumber = (String) callInfo.getField(Field.SRCNO);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + util.maskCardOrAccountNumber(acctNumber));}

				String lastNDigit = (String)callInfo.getField(Field.LastNDigits);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last n digit value is " + lastNDigit);}
				int int_LastNDigit = util.isNullOrEmpty(lastNDigit)? Constants.GL_FOUR : Integer.parseInt(lastNDigit);

				CardAcctDtl acctInfo = null;
				String status = Constants.EMPTY_STRING;
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Detail Map :"+callInfo.getCallerIdentification_HostRes().getCardDetailMap());}
						acctInfo = callInfo.getCallerIdentification_HostRes().getCardDetailMap().get(acctNumber);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "acctInfo :" + acctInfo);}
						status = acctInfo.getCardStatus();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The status of the Card is " + status);}
					}
				}


				cardActiveStatus = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CARD_ACTIVE_STATUS);
				isCardActive = util.isCodePresentInTheConfigurationList(status, cardActiveStatus);

			}else{
				acctNumber = (String) callInfo.getField(Field.ENTEREDCINNUMBER);

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + util.maskCardOrAccountNumber(acctNumber));}

				String lastNDigit = (String)callInfo.getField(Field.LastNDigits);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Last n digit value is " + lastNDigit);}
				int int_LastNDigit = util.isNullOrEmpty(lastNDigit)? Constants.GL_FOUR : Integer.parseInt(lastNDigit);

				CardAcctDtl acctInfo = null;
				String status = Constants.EMPTY_STRING;
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
					if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCardDetailMap())){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Detail Map :"+callInfo.getCallerIdentification_HostRes().getCardDetailMap());}
						acctInfo = callInfo.getCallerIdentification_HostRes().getCardDetailMap().get(acctNumber);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "acctInfo :" + acctInfo);}
						status = acctInfo.getCardStatus();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The status of the Card is " + status);}
					}
				}


				cardActiveStatus = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CARD_ACTIVE_STATUS);
				isCardActive = util.isCodePresentInTheConfigurationList(status, cardActiveStatus);

			}


			if(isCardActive){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Returning card is active" );}
				return true;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Its an inactive account" );}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: DebitCardActivationImpl.isDebitCardActive()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at DebitCardActivationImpl.getDebitCardActivationConfirmPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return false;
	}
}
