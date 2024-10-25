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
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.callerIdentification.CardAcctDtl;
import com.servion.model.reportLostCard.LostStolenCard_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class ReportLossCardImpl implements IReportLossCard {
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
	public String getDebitCardBlock(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ReportLossCardImpl.getDebitCardBlock()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String requestType = Constants.HOST_REQUESTTYPE_BLOCKCARD;
			String primaryAccountNum = Constants.EMPTY_STRING;


			//TODO
			String isAfterMainMenu=(String)callInfo.getField(Field.isAfterMainMenu);
			if(Constants.TRUE.equalsIgnoreCase(isAfterMainMenu)){
				primaryAccountNum = (String) callInfo.getField(Field.SRCNO);
			}else{
				primaryAccountNum = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Primary card number is " + util.maskCardOrAccountNumber(primaryAccountNum));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Request Type is " + requestType);}
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)+Constants.COMMA+Constants.HOST_INPUT_PARAM_SOURCE_NO+ Constants.EQUALTO + util.maskCardOrAccountNumber(primaryAccountNum) + Constants.COMMA +Constants.HOST_INPUT_PARAM_REQUEST_TYPE+ Constants.EQUALTO + requestType
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

			String transactionDate =(String)util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);
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
				db_Code = dataServices.getSequenceNoS1(logger, sessionId, uui,  configMap);

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

			String sysTraceAuditNumber =strRefNumberOne;
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
			String transCurrCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_CurrCode);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Trans Curr Code value is "+ transCurrCode);}

			String extendedTransType = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_DebitCardServiceRequest_ExtendedTransactionType);

			
			/**
			 * Following are the modification done on 01-Sep-2014 for the handling of dynamic Debit card length (15 to 19)
			 */
			int panLength = util.isNullOrEmpty(primaryAccountNum)?Constants.GL_SIXTEEN : primaryAccountNum.length();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length is "+ panLength);}
			
			String panLengthKey = Constants.UNDERSCORE + panLength;
			panLengthKey = panLengthKey.trim();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The PAN length key is "+ panLengthKey);}
			
			panLengthKey = Constants.CUI_UI_DebitCardServiceRequest_RequestStructData_Block+panLengthKey;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The new Request Structure Data key is "+ panLengthKey);}
			//END 
			
			
			
			String structureData = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(panLengthKey);
			structureData = (structureData!=null)? structureData.replace(Constants.STRUCTURED_DATA_PAN_KEY, primaryAccountNum): null;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Finalized Structured data is "+ structureData);}

			LostStolenCard_HostRes lostStolenCard_HostRes = debitCardServiceRequestDAO.getReportLostCardHostRes(callInfo, requestType, primaryAccountNum, processingCode, amtTransaction, transactionDate, 
					sysTraceAuditNumber, localTime, localDate, expirationDate, settlementDate, merchantType, serviceEntryMode, cardSequenceNum, serviceCondCode, serviceCaptureCode, 
					fwdInstitutionIdnefCode, trackTwoData, cardAcceptorTerminalID, cardAcceptorIDCode, cardAcceptorName, pin, securityControlInfo, mesgReasonCode, originalDataElement, 
					recvInstitutionIdnefCode, accountIdentification, posDataCode, bitMap, terminalOwner, posGeographicData, sponsorBank, addrVerfData, iccData, originalNode, mesgAuthCode, transCurrCode, structureData, extendedTransType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ActivateCard_HostRes Object is :"+ lostStolenCard_HostRes);}
			callInfo.setLostStolenCard_HostRes(lostStolenCard_HostRes);

			code = lostStolenCard_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = lostStolenCard_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = lostStolenCard_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + callInfo.getField(Field.CUSTOMERID)+Constants.COMMA+Constants.HOST_INPUT_PARAM_SOURCE_NO+ Constants.EQUALTO + util.maskCardOrAccountNumber(primaryAccountNum) + Constants.COMMA +Constants.HOST_INPUT_PARAM_REQUEST_TYPE+ Constants.EQUALTO + requestType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(lostStolenCard_HostRes.getErrorDesc()) ?"NA" :lostStolenCard_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit Card is Blocked successfully ");}



				/**
				 * Following are the fixes done by Vinoth on 02-May-2014 to remove the debit card number from the debit card block list
				 */

				ArrayList<String> debitCardList = util.isNullOrEmpty(callInfo.getField(Field.DEBITCARDLIST))? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DEBITCARDLIST);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit card list size retrieved from the caller identification host is " + debitCardList.size());}

				if(!util.isNullOrEmpty(debitCardList) && !util.isNullOrEmpty(primaryAccountNum)){

					boolean isAcctPresentInDebitCardList = debitCardList.contains(primaryAccountNum);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the bloced debit card number present in the debit card list " +isAcctPresentInDebitCardList );}

					if(isAcctPresentInDebitCardList){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to remove the blocked debit card from the debit card list");}

						debitCardList.remove(primaryAccountNum);
						debitCardList = util.isNullOrEmpty(debitCardList)? new ArrayList<String>() : debitCardList;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Debit card list size is " + debitCardList.size());}
						callInfo.setField(Field.DEBITCARDLIST, debitCardList);
					}

				}

				ArrayList<String> debitCardVisaList = util.isNullOrEmpty(callInfo.getField(Field.DRVISACARDLIST))? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRVISACARDLIST);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit VISA card list retrieved from the caller identification host size is " + debitCardVisaList.size());}

				ArrayList<String> debitCardMasterList = util.isNullOrEmpty(callInfo.getField(Field.DRAMEXCARDLIST))? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRAMEXCARDLIST);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit Amex card list retrieved from the caller identification host size is " + debitCardMasterList.size());}

				ArrayList<String> debitCardAmexList = util.isNullOrEmpty(callInfo.getField(Field.DRMASTERCARDLIST))? new ArrayList<String>() : (ArrayList<String>)callInfo.getField(Field.DRMASTERCARDLIST);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Debit Master card list retrieved from the caller identification host size is " + debitCardAmexList.size());}


				if((!util.isNullOrEmpty(debitCardVisaList) || !util.isNullOrEmpty(debitCardMasterList) || !util.isNullOrEmpty(debitCardAmexList)) && !util.isNullOrEmpty(primaryAccountNum)){

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the bloced debit card number present in the debit VISA card list " +debitCardVisaList.contains(primaryAccountNum) );}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the bloced debit card number present in the debit MASTER card list " +debitCardMasterList.contains(primaryAccountNum) );}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is the bloced debit card number present in the debit AMEX card list " +debitCardAmexList.contains(primaryAccountNum) );}

					if(debitCardVisaList.contains(primaryAccountNum)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to remove the blocked debit card from the debit card VISA list");}

						debitCardVisaList.remove(primaryAccountNum);
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Debit card Visa list is " + debitCardVisaList);}
						callInfo.setField(Field.DRVISACARDLIST, debitCardVisaList);
					}
					else if(debitCardMasterList.contains(primaryAccountNum)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to remove the blocked debit card from the debit card Master list");}

						debitCardMasterList.remove(primaryAccountNum);
//						debitCardMasterList = util.isNullOrEmpty(debitCardMasterList)?new ArrayList<String>():debitCardMasterList;
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Debit card Master list size is " + debitCardMasterList.size());}
						callInfo.setField(Field.DRMASTERCARDLIST, debitCardMasterList);
					}
					else if(debitCardAmexList.contains(primaryAccountNum)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to remove the blocked debit card from the debit card Amex list");}

						debitCardAmexList.remove(primaryAccountNum);
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Debit card Amex list is " + debitCardAmexList);}
						callInfo.setField(Field.DRAMEXCARDLIST, debitCardAmexList);
					}
				}


				ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

				if(util.isNullOrEmpty(ruleParamObj)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
				}


				if(!util.isNullOrEmpty(debitCardList)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no of Debit Cards: "+debitCardList.size() );}
					callInfo.setField(Field.NO_OF_DEBIT_CARDS, debitCardList.size());

					/**
					 * Rule engine update
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Debit card count in the Rule Engine " + debitCardList.size());}
					ruleParamObj.setIVRParam(Constants.RULE_ENGINE_DEBITCARDCOUNT, (debitCardList.size()+Constants.EMPTY));
					//END Rule Engine Updation

				}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for  Debit card Details Service host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + lostStolenCard_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_DEBITCARDSERVICEREQUEST, lostStolenCard_HostRes.getHostResponseCode());

			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ReportLossCardImpl.getDebitCardBlock()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  ReportLossCardImpl.getDebitCardBlock() "+ e.getMessage());
//			e.printStackTrace();
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getReportLostCardMenuPhrases(CallInfo callInfo)throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage = Constants.EMPTY_STRING;
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ReportLossCardImpl.getReportLostCardMenuPhrases()");}

		try{

			getConfigurationParam(callInfo);

			String cardSuffixAnncLength = (String) callInfo.getField(Field.LastNDigits);
			String selectedCardOrAcctNo = Constants.EMPTY;

			String isAfterMainMenu=(String)callInfo.getField(Field.isAfterMainMenu);
			if(Constants.TRUE.equalsIgnoreCase(isAfterMainMenu)){
				selectedCardOrAcctNo = (String) callInfo.getField(Field.SRCNO);
			}else{
				selectedCardOrAcctNo = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			}


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Suffix Announcement length is : "+ cardSuffixAnncLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Debit card number is "+ util.maskCardOrAccountNumber(selectedCardOrAcctNo));}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}


			String menuID = MenuIDMap.getMenuID("REPORT_LOSS_CARD_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID(code)
			String featureID = FeatureIDMap.getFeatureID("Report_Lost_Card");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			if(util.isNullOrEmpty(selectedCardOrAcctNo)){
				throw new ServiceException("There is no valid Debit card number");
			}

			//Setting the selected debit card number as a default debit card number henceforth
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the selected debit card numbe as default debit card number henceforth");}
			callInfo.setField(Field.DEBITCARDNUMBER, selectedCardOrAcctNo);


			if(util.isNullOrEmpty(cardSuffixAnncLength)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Assinging card suffix length as Zero");}
				cardSuffixAnncLength = Constants.ZERO;
			}


			String debitCardSuff = util.getSubstring(selectedCardOrAcctNo, Integer.parseInt(cardSuffixAnncLength));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The debit card suffix length is" +debitCardSuff );}


			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			dynamicValueArray.add(debitCardSuff);

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			/*
			 * Handling Grammar and MoreOptions for OD Use
			 */
			String grammar = util.getGrammar(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Grammar value is"+grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting moreoption as false");}
			callInfo.setField(Field.MOREOPTION, false);
			//End

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ReportLossCardImpl.getBankAccountBalancePhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ReportLossCardImpl.getReportLostCardMenuPhrases() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}


	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable dont throw any new exception
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ReportLossCardImpl.getConfigurationParam()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			String ReportLossOfCardsHandling = Constants.EMPTY_STRING;
			String tempStr = Constants.EMPTY_STRING;
			String DebitOrCreditCardLength = Constants.EMPTY_STRING;
			HashMap<String, ArrayList<String>> BINAndAccountTypes = new HashMap<String, ArrayList<String>>();

			ReportLossOfCardsHandling = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ReportLossOfCardsHandling);

			//			tempStr = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CardSuffixAnncLength);
			//			if(!util.isNullOrEmpty(tempStr)){
			//				CardSuffixAnncLength = Integer.parseInt(tempStr);
			//			}

			DebitOrCreditCardLength = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_DebitOrCreditCardLength);

			BINAndAccountTypes = (HashMap<String, ArrayList<String>>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_BINAndAccountTypes);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Who should handle the Report loss card feature : " + ReportLossOfCardsHandling);}
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Suffix Announcement length is : "+CardSuffixAnncLength);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit or Credit card lenght is "+ DebitOrCreditCardLength);}

			if(!util.isNullOrEmpty(BINAndAccountTypes)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bin and Account type hash map size is "+BINAndAccountTypes.size());}
			}

			callInfo.setField(Field.ReportLossOfCardsThrough, ReportLossOfCardsHandling);
			//			callInfo.setField(Field.LastNDigits, CardSuffixAnncLength);
			callInfo.setField(Field.DebitOrCreditCardLength, DebitOrCreditCardLength);
			//TODO
			//callInfo.setField(Field.BINAndAccountTypes, BINAndAccountTypes);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ReportLossCardImpl.getConfigurationParam()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ReportLossCardImpl.getConfigurationParam() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}

	}
}
