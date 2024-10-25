package com.servion.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.SecretKey;
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
import com.servion.dao.CCEntityInquiryDAO;
import com.servion.dao.KeyExAuthDAO;
import com.servion.dao.ValidateDebitCardPinDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.jce.JCEWrapper;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.apinValidation.APINCustomerProfileDetails_HostRes;
import com.servion.model.apinValidation.ValidatePIN_HostRes;
import com.servion.model.keyExAuth.KeyExAuth_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class APINValidationImpl implements IAPINValidation {
	private static Logger logger = LoggerObject.getLogger();

	private ValidateDebitCardPinDAO validateDebitCardPinDAO;
	private CCEntityInquiryDAO ccEntityInquiryDAO;
	private KeyExAuthDAO keyExAuthDAO;

	public KeyExAuthDAO getKeyExAuthDAO() {
		return keyExAuthDAO;
	}

	public void setKeyExAuthDAO(KeyExAuthDAO keyExAuthDAO) {
		this.keyExAuthDAO = keyExAuthDAO;
	}

	public ValidateDebitCardPinDAO getValidateDebitCardPinDAO() {
		return validateDebitCardPinDAO;
	}

	public void setValidateDebitCardPinDAO(ValidateDebitCardPinDAO validateDebitCardPinDAO) {
		this.validateDebitCardPinDAO = validateDebitCardPinDAO;
	}

	public CCEntityInquiryDAO getCcEntityInquiryDAO() {
		return ccEntityInquiryDAO;
	}

	public void setCcEntityInquiryDAO(CCEntityInquiryDAO ccEntityInquiryDAO) {
		this.ccEntityInquiryDAO = ccEntityInquiryDAO;
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
	public String getAPINValidate(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";
		try {
			session_ID_ = (String) callInfo.getField(Field.SESSIONID);
			logger = (Logger) callInfo.getField(Field.LOGGER);
			WriteLog.loggerInit(logger, session_ID_);
		} catch (Exception e) {
		}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO, session_ID_, "ENTER: APINValidationImpl.getAPINValidate()");
		}
		String code = Constants.EMPTY_STRING;

		try {

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Fetching the Feature Object values");
			}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if (util.isNullOrEmpty(ivr_ICEGlobalConfig)) {
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String enteredCINNumber = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"Entered CIN Number ending with " + util.getSubstring(enteredCINNumber, Constants.GL_FOUR));
			}

			String pan = enteredCINNumber;
			String processingCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_ProcessingCode);
//		int intProcessingCode = util.isNullOrEmpty(processingCode)?Constants.GL_ZERO:Integer.parseInt(processingCode);
			String amtTransaction = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AmtTransaction);

			String str_AmtSettlement = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AmtSettlement);
			BigDecimal amtSettlement = util.isNullOrEmpty(str_AmtSettlement) ? new BigDecimal(Constants.GL_ZERO)
					: new BigDecimal(str_AmtSettlement);

			String transmissionDate = (String) util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);

			String str_ConversionRate = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_ConversionRate);
			BigDecimal conversionRate = util.isNullOrEmpty(str_ConversionRate) ? new BigDecimal(Constants.GL_ZERO)
					: new BigDecimal(str_ConversionRate);

			// Following for the sequencial number generation for System trace audit number
			// for S1 systems
			String db_Code = Constants.ONE;
			int codeLength = Constants.GL_ZERO;
			String sessionId = (String) callInfo.getField(Field.SESSIONID);

			if (util.isNullOrEmpty(sessionId)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Session ID is null or empty");
				}
				throw new ServiceException("Session id is null or empty");
			}

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Calling the DB Method ");
			}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Setting the input for getSequenceNo");
			}

			String uui = (String) callInfo.getField(Field.UUI);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "UUI of the call is " + uui);
			}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			String strRefNumberOne = Constants.EMPTY_STRING;

			try {
				db_Code = dataServices.getSequenceNoS1(logger, sessionId, uui, configMap);

				if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)) {
					strRefNumberOne = (String) configMap.get(DBConstants.SEQUENCENO);

					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_,
								"The sequencial number return value " + strRefNumberOne);
					}
					codeLength = strRefNumberOne.length();

					for (int p = codeLength; p < 6; p++) {
						strRefNumberOne = Constants.ZERO + strRefNumberOne;
						strRefNumberOne.trim();
					}

				} else {
					if (logger.isInfoEnabled()) {
						WriteLog.write(WriteLog.INFO, session_ID_, "Error in the S1 sequencial DB response");
					}
					throw new ServiceException("Sequencial number DB access throwing error");
				}
			} catch (com.db.exception.ServiceException e) {
				if (logger.isInfoEnabled()) {
					WriteLog.write(WriteLog.INFO, session_ID_, "ERROR: GlobalImpl.getSequenceNumber()");
				}
				throw new ServiceException("S1 Sequencial number DB access throwing error");
				// e.printStackTrace();
			}

			String str_SysTraceAuditNo = strRefNumberOne;
//				util.getRandomNumber(999999) + Constants.EMPTY_STRING; 
//		int sysTraceAuditNo = util.isNullOrEmpty(str_SysTraceAuditNo)?Constants.GL_ZERO:Integer.parseInt(str_SysTraceAuditNo); 

			String localTransTime = (String) util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
			String localTansDate = (String) util.getTodayDateOrTime(Constants.DATEFORMAT_MMDD);
			String expirationDate = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_ExpirationDate);

			String settlementDate = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_SettlementDate);

			// XMLGregorianCalendar xml_DateConversion =
			// (XMLGregorianCalendar)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_DateConversion);
			// String dateConversion = util.convertXMLCalendarToString(xml_DateConversion,
			// Constants.DATEFORMAT_MMDD);
			String dateConversion = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_DateConversion);
			XMLGregorianCalendar xml_DateConversion = util.isNullOrEmpty(dateConversion) ? null
					: util.convertDateStringtoXMLGregCalendar(dateConversion, Constants.DATEFORMAT_MMDD);

			String str_MerchantType = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_MerchantType);
//		int merchantType = util.isNullOrEmpty(str_MerchantType)?Constants.GL_ZERO:Integer.parseInt(str_MerchantType);

			String str_PointOfServiceMode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_PointOfServiceMode);
//		int pointOfServiceMode = util.isNullOrEmpty(str_PointOfServiceMode)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServiceMode); 

			String str_CardSeqNum = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CardSeqNum);
//		int cardSeqNum = util.isNullOrEmpty(str_CardSeqNum)?Constants.GL_ZERO:Integer.parseInt(str_CardSeqNum); 

			String str_PointOfServCondCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_PointOfServCondCode);
//		int pointOfServCondCode = util.isNullOrEmpty(str_PointOfServCondCode)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServCondCode);

			String str_PointOfServCaptureCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_PointOfServCaptureCode);
//		int pointOfServCaptureCode = util.isNullOrEmpty(str_PointOfServCaptureCode)?Constants.GL_ZERO:Integer.parseInt(str_PointOfServCaptureCode); 

			String str_AuthIDRespLength = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AuthIDRespLength);
			int authIDRespLength = util.isNullOrEmpty(str_AuthIDRespLength) ? Constants.GL_ZERO
					: Integer.parseInt(str_AuthIDRespLength);

			String str_AmtSettlementFee = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AmtSettlementFee);
			BigDecimal amtSettlementFee = util.isNullOrEmpty(str_AmtSettlementFee) ? new BigDecimal(Constants.GL_ZERO)
					: new BigDecimal(str_AmtSettlementFee);

			String str_AmtSettlementProcFee = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AmtSettlementProcFee);
			BigDecimal amtSettlementProcFee = util.isNullOrEmpty(str_AmtSettlementProcFee)
					? new BigDecimal(Constants.GL_ZERO)
					: new BigDecimal(str_AmtSettlementProcFee);

			String acquiringInstitutionID = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AcquiringInstitutionID);
			String trackTwoData = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_TrackTwoData);
			String cardAccpTerminalID = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CardAccpTerminalID);
			String cardAccpIDCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CardAccpIDCode);
			String cardAccpName = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CardAccpName);
			String currCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CurrCode);
			String currCodeSettlement = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CurrCodeSettlement);

			/**
			 * Doing the process of PIN Blocking and Encryption
			 */

			// Setting the User entered APIN value
			String pin = (String) callInfo.getField(Field.APIN);
			// (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Pin);

			String apinKey = (String) callInfo.getField(Field.APINKEY);
			if (util.isNullOrEmpty(apinKey)) {
				throw new ServiceException("Apin Key stored in the callinfo is null / empty");
			}

			apinKey = apinKey.substring(Constants.GL_ZERO, Constants.GL_THIRTYTWO);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The first 32 digit key is retrieved ");
			}
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The first 32 digit key is retrieved " + apinKey);
			}

			String masterkeyValue = util.isNullOrEmpty(callInfo.getField(Field.APINMASTERKEY)) ? null
					: (String) callInfo.getField(Field.APINMASTERKEY);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Obtained Master key from the keystore / Config file");
			}

			if (util.isNullOrEmpty(masterkeyValue)) {
				throw new ServiceException("Masterkey conversion result is null or empty");
			}

			masterkeyValue = util.convertTo48BitKey(masterkeyValue);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Master key has been converted to 48 bits");
			}

			JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);
			SecretKey sKey = jceWrap.toSecretKey(masterkeyValue);
			String clearKey = jceWrap.decrypt(apinKey, sKey);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Received the clear key and the clear key is " + clearKey);
			}

			String pinBlocking = util.getISOPinBlock(pan, pin, true, false, Constants.GL_THREE, Constants.GL_ONE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Have Successfully done the pin blocking");
			}

			clearKey = util.convertTo48BitKey(clearKey);
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Clear key is converted to 48 digit and the result got "+ clearKey);}
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Clear key is converted to 48 digit and the result got ");
			}

			sKey = jceWrap.toSecretKey(clearKey);
			String encryptPIN = jceWrap.encrypt(pinBlocking, sKey);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Have Successfully done the pin encryption");
			}
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Have Successfully done the pin encryption"+ encryptPIN);}

			if (util.isNullOrEmpty(encryptPIN)) {
				throw new ServiceException("encryption PIN conversion result is null or empty");
			}
			String securityContInfo = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_SecurityContInfo);
			String additionalAmt = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AdditionalAmt);

			String str_ExtendedPaymentCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_ExtendedPaymentCode);
			int extendedPaymentCode = util.isNullOrEmpty(str_ExtendedPaymentCode) ? Constants.GL_ZERO
					: Integer.parseInt(str_ExtendedPaymentCode);

			String originalDataElement = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_OriginalDataElement);
			String payee = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Payee);
			String recvInstIDCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_RecvInstIDCode);
			String acctIdenfOne = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AcctIdenfOne);
			String acctIdenfTwo = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AcctIdenfTwo);
			String posDataCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_PosDataCode);
			String bitMap = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_BitMap);
			String checkData = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_CheckData);
			String terminalOwner = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_TerminalOwner);
			String posGeographicData = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_PosGeographicData);
			String sponsorBank = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_SponsorBank);
			String addrVerfData = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_AddrVerfData);
			String bankDetails = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_BankDetails);
			String payeeName = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_PayeeName);
			String iccData = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_IccData);
			String origData = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_OrigData);
			String macField = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_MacField);

			String lastUpdatedTimeStamp = (String) callInfo.getField(Field.LASTUPDATEDTIME);

			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String) callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			// hostReportDetails.setHostCounter(hostCounter);
			// hostReportDetails.setHostEndTime(hostEndTime);
//		String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + 
//				Constants.NA + Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
//		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
//		hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_VALIDATEDBTCRDPIN);
			// hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); // It should be in the format of 31/07/2013
																			// 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_DEBITCARDS);
			// End Reporting

			/*
			 * Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData) callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);

			/* END */

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData()) ? null
					: (ICEFeatureData) callInfo.getICEFeatureData();

			if (util.isNullOrEmpty(iceFeatureData)) {
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(
					iceFeatureData.getConfig().getParamValue(Constants.CUI_VALIDATEDBTCRDPIN_REQUESTTYPE)) ? null
							: (String) iceFeatureData.getConfig()
									.getParamValue(Constants.CUI_VALIDATEDBTCRDPIN_REQUESTTYPE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);
			}

			ValidatePIN_HostRes validatePIN_HostRes = validateDebitCardPinDAO.getAPINValidateHostRes(callInfo, pan,
					processingCode, amtTransaction, amtSettlement, transmissionDate, conversionRate,
					str_SysTraceAuditNo, localTransTime, localTansDate, expirationDate, settlementDate, dateConversion,
					str_MerchantType, str_PointOfServiceMode, str_CardSeqNum, str_PointOfServCondCode,
					str_PointOfServCaptureCode, authIDRespLength, amtSettlementFee, amtSettlementProcFee,
					acquiringInstitutionID, trackTwoData, cardAccpTerminalID, cardAccpIDCode, cardAccpName, currCode,
					currCodeSettlement, encryptPIN, securityContInfo, additionalAmt, extendedPaymentCode,
					originalDataElement, payee, recvInstIDCode, acctIdenfOne, acctIdenfTwo, posDataCode, bitMap,
					checkData, terminalOwner, posGeographicData, sponsorBank, addrVerfData, bankDetails, payeeName,
					iccData, origData, macField, lastUpdatedTimeStamp, requestType);

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "validatePIN_HostRes Object is :" + validatePIN_HostRes);
			}
			callInfo.setValidatePIN_HostRes(validatePIN_HostRes);

			code = validatePIN_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO
					+ util.maskCardOrAccountNumber((String) callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_ESBREQREFNUM + Constants.EQUALTO
					+ (util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ? "NA"
							: callInfo.getField(Field.ESBREQREFNUM));
			hostReportDetails.setHostInParams(strHostInParam);

			String hostEndTime = validatePIN_HostRes.getHostEndTime();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);
			}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = validatePIN_HostRes.getHostResponseCode();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host response code is " + hostResCode);
			}
			hostReportDetails.setHostResponse(hostResCode);

			/**
			 * Setting the APIN Status
			 */
			String apinStatus = util.isNullOrEmpty(validatePIN_HostRes.getResponseCode()) ? Constants.NA
					: validatePIN_HostRes.getResponseCode() + Constants.EMPTY_STRING;
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "APIN Status is " + apinStatus);
			}
			callInfo.setField(Field.APIN_STATUS, apinStatus);
			// END

			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam) callInfo.getICERuleParam();

			if (util.isNullOrEmpty(ruleParamObj)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"***********Rule Engine Object is null or empty*************" + ruleParamObj);
				}
			}

			String apinInactiveCode = (String) callInfo.getField(Field.APINInactiveCode);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "APIN Inactive code is " + apinInactiveCode);
			}
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"Host Response code for validate APIN is " + validatePIN_HostRes.getHostResponseCode());
			}

			// boolean isAPINInactive =
			// util.isCodePresentInTheConfigurationList(apinInactiveCode,
			// validatePIN_HostRes.getHostResponseCode());
			boolean isAPINInactive = util.isCodePresentInTheConfigurationList(validatePIN_HostRes.getHostResponseCode(),
					apinInactiveCode);

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Is APIN / CIN is inactive ? " + isAPINInactive);
			}

			if (isAPINInactive) {
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCINACTIVATED, Constants.N);
				ruleParamObj.updateIVRFields();
			} else {
				ruleParamObj.setIVRParam(Constants.RULE_ENGINE_ISCINACTIVATED, Constants.Y);
				ruleParamObj.updateIVRFields();
			}

			// END Rule Engine Updation

			String responseDesc = Constants.HOST_FAILURE;
			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc
					+ Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE + Constants.EQUALTO + hostResCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC + Constants.EQUALTO
					+ (util.isNullOrEmpty(validatePIN_HostRes.getErrorDesc()) ? "NA"
							: validatePIN_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData) callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			// End Reporting

			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"Customer APIN was validated successfully " + hostEndTime);
				}

				callInfo.setField(Field.APIN_VALIDATED, true);
			} else {

				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Got failure response for APIN validate host service");
				}
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "The original response code of host access is "
							+ validatePIN_HostRes.getHostResponseCode());
				}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_VALIDATEDBTCRDPIN,
						validatePIN_HostRes.getHostResponseCode());
			}

			if (logger.isInfoEnabled()) {
				WriteLog.write(WriteLog.INFO, session_ID_, "EXIT: APINValidationImpl.getAPINValidate()");
			}
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,
					"There was an error at APINValidationImpl.getAPINValidate() " + e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	public String getNewEncryptionKey(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";
		try {
			session_ID_ = (String) callInfo.getField(Field.SESSIONID);
			logger = (Logger) callInfo.getField(Field.LOGGER);
			WriteLog.loggerInit(logger, session_ID_);
		} catch (Exception e) {
		}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO, session_ID_, "ENTER: APINValidationImpl.getNewEncryptionKey()");
		}
		String code = Constants.EMPTY_STRING;

		try {

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Fetching the Feature Object values");
			}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if (util.isNullOrEmpty(ivr_ICEGlobalConfig)) {
				throw new ServiceException("ICEGlobalConfig object is null");
			}

//		String enteredCINNumber = (String)callInfo.getField(Field.ENTEREDCINNUMBER);
//		if(util.isNullOrEmpty(enteredCINNumber)){
//			throw new ServiceException("Entered  CIN number value is null or EMPTY");
//		}
//		
//		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered CIN Number ending with " + util.getSubstring(enteredCINNumber, Constants.GL_FOUR));}

			// String transmissionDateTime =
			// (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_TransmissionDateTime);
			String transmissionDateTime = (String) util.getTodayDateOrTime(Constants.DATEFORMAT_MMDDhhmmss);

			String localTransTime = (String) util.getTodayDateOrTime(Constants.DATEFORMAT_hhmmss);
			String localTansDate = (String) util.getTodayDateOrTime(Constants.DATEFORMAT_MMDD);

			// Following for the sequencial number generation for System trace audit number
			// for S1 systems
			String db_Code = Constants.ONE;
			int codeLength = Constants.GL_ZERO;
			String sessionId = (String) callInfo.getField(Field.SESSIONID);

			if (util.isNullOrEmpty(sessionId)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Session ID is null or empty");
				}
				throw new ServiceException("Session id is null or empty");
			}

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Calling the DB Method ");
			}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Setting the input for getSequenceNo");
			}

			String uui = (String) callInfo.getField(Field.UUI);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "UUI of the call is " + uui);
			}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			String strRefNumberOne = Constants.EMPTY_STRING;
			try {
				db_Code = dataServices.getSequenceNoS1(logger, sessionId, uui, configMap);

				if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)) {
					strRefNumberOne = (String) configMap.get(DBConstants.SEQUENCENO);

					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_,
								"The sequencial number return value " + strRefNumberOne);
					}
					codeLength = strRefNumberOne.length();

					for (int p = codeLength; p < 6; p++) {
						strRefNumberOne = Constants.ZERO + strRefNumberOne;
						strRefNumberOne.trim();
					}

				} else {
					if (logger.isInfoEnabled()) {
						WriteLog.write(WriteLog.INFO, session_ID_, "Error in the S1 sequencial DB response");
					}
					throw new ServiceException("Sequencial number DB access throwing error");
				}
			} catch (com.db.exception.ServiceException e) {
				if (logger.isInfoEnabled()) {
					WriteLog.write(WriteLog.INFO, session_ID_, "ERROR: GlobalImpl.getSequenceNumber()");
				}
				throw new ServiceException("S1 Sequencial number DB access throwing error");
				// e.printStackTrace();
			}

			String str_SysTraceAuditNo = strRefNumberOne;

			// Need to create 6 or some random number as STAN
//		String str_SysTraceAuditNo = util.getRandomNumber(999999) + Constants.EMPTY_STRING;
			// (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_KeyExAuth_SysTraceAuditNo);
//		int sysTraceAuditNo = util.isNullOrEmpty(str_SysTraceAuditNo)?Constants.GL_ZERO:Integer.parseInt(str_SysTraceAuditNo); 

			String networkInfoCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_KeyExAuth_NetworkInfoCode);
			int int_networkInfoCode = util.isNullOrEmpty(networkInfoCode) ? Constants.GL_ZERO
					: Integer.parseInt(networkInfoCode);
			String messageAuthCode = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CI_KeyExAuth_MessageAuthCode);
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String) callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			// hostReportDetails.setHostCounter(hostCounter);
			// hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO
					+ util.maskCardOrAccountNumber((String) callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_ESBREQREFNUM + Constants.EQUALTO
					+ (util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ? "NA"
							: callInfo.getField(Field.ESBREQREFNUM));
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_KEYEXAUTH);
			// hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); // It should be in the format of 31/07/2013
																			// 18:11:11

			hostReportDetails.setHostType(Constants.HOST_TYPE_DEBITCARDS);
			// End Reporting

			/*
			 * Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData) callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);

			/* END */

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData()) ? null
					: (ICEFeatureData) callInfo.getICEFeatureData();

			if (util.isNullOrEmpty(iceFeatureData)) {
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util
					.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_KEYEXAUTH_REQUESTTYPE)) ? null
							: (String) iceFeatureData.getConfig().getParamValue(Constants.CUI_KEYEXAUTH_REQUESTTYPE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);
			}

			KeyExAuth_HostRes keyExAuth_HostRes = keyExAuthDAO.getKeyExAuthHostRes(callInfo, transmissionDateTime,
					str_SysTraceAuditNo, localTransTime, localTansDate, int_networkInfoCode, messageAuthCode,
					requestType);

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "keyExAuth_HostRes Object is :" + keyExAuth_HostRes);
			}
			callInfo.setKeyExAuth_HostRes(keyExAuth_HostRes);

			code = keyExAuth_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = keyExAuth_HostRes.getHostEndTime();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);
			}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = keyExAuth_HostRes.getHostResponseCode();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host response code is " + hostResCode);
			}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				responseDesc = Constants.HOST_SUCCESS;
			}
			/**** Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + Constants.NA + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_CIN + Constants.EQUALTO
					+ util.maskCardOrAccountNumber((String) callInfo.getField(Field.ENTEREDCINNUMBER)) + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA + Constants.COMMA
					+ Constants.HOST_INPUT_PARAM_ESBREQREFNUM + Constants.EQUALTO
					+ (util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ? "NA"
							: callInfo.getField(Field.ESBREQREFNUM));
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/

			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc
					+ Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE + Constants.EQUALTO + hostResCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC + Constants.EQUALTO
					+ (util.isNullOrEmpty(keyExAuth_HostRes.getErrorDesc()) ? "NA" : keyExAuth_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData) callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			// End Reporting

			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"New key was generated successfully and the host host end time is" + hostEndTime);
				}

				/**
				 * Setting the host response information in the call info
				 */

				String macCode = keyExAuth_HostRes.getMessageAuthCode();
				String lastUpdatedTime = keyExAuth_HostRes.getLastUpdateTimeStamp();
				String apinKey = keyExAuth_HostRes.getSecurityControlInfo();

				// TODO need to hide the below logging lines
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "APIN Key retrieved from the host is " + apinKey);
				}
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "The last updated time value is " + lastUpdatedTime);
				}
				callInfo.setField(Field.APINKEY, apinKey);
				callInfo.setField(Field.LASTUPDATEDTIME, lastUpdatedTime);
				// Successfully set the value at the call info
			} else {

				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Got failure response for KeyExAuth host service");
				}
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"The original response code of host access is " + keyExAuth_HostRes.getHostResponseCode());
				}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_EXCHNGRATEINQ,
						keyExAuth_HostRes.getHostResponseCode());
			}
			if (logger.isInfoEnabled()) {
				WriteLog.write(WriteLog.INFO, session_ID_, "EXIT: APINValidationImpl.getNewEncryptionKey()");
			}
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,
					"There was an error at APINValidationImpl.getNewEncryptionKey() " + e);
			util.getFullStackTrace(e);
			throw new ServiceException(e);
		}
		return code;
	}

	public void getConfigurationParam(CallInfo callInfo) throws ServiceException {

	}

	@Override
	public String getCompleteCustomerProfile(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";
		try {
			session_ID_ = (String) callInfo.getField(Field.SESSIONID);
			logger = (Logger) callInfo.getField(Field.LOGGER);
			WriteLog.loggerInit(logger, session_ID_);
		} catch (Exception e) {
		}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO, session_ID_, "ENTER: APINValidationImpl.getCompleteCustomerProfile()");
		}
		String code = Constants.EMPTY_STRING;
		getConfigurationParam(callInfo);
		try {

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Fetching the Feature Object values");
			}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if (util.isNullOrEmpty(ivr_ICEGlobalConfig)) {
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String SelectedCardOrAcctNo = (String) callInfo.getField(Field.ENTEREDCINNUMBER);
			if (util.isNullOrEmpty(SelectedCardOrAcctNo)) {
				throw new ServiceException("Selected Card OR Acct No is empty or null");
			}
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Setting selected card or acct no as entered cin"
						+ util.getSubstring(SelectedCardOrAcctNo, Constants.GL_FOUR));
			}

			String customerID = (String) callInfo.getField(Field.CUSTOMERID);
			ArrayList<String> customerIDList = new ArrayList<String>();
			customerIDList.add(customerID);

			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			if (util.isNullOrEmpty(ivr_ICEFeatureData)) {
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String entyityInquiryType = Constants.EMPTY_STRING;
			entyityInquiryType = (String) ivr_ICEFeatureData.getConfig()
					.getParamValue(Constants.CUI_CREDITCARD_ENTITYINQ_TYPE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"The entyityInquiryType type from the entityInquiryType " + entyityInquiryType);
			}

//			String entyityInquiryType = Constants.EMPTY_STRING;
//			HashMap<String, CardAcctDtl> cardDetailMap = callInfo.getCallerIdentification_HostRes().getCardDetailMap();
//			if(cardDetailMap!=null && cardDetailMap.containsKey(SelectedCardOrAcctNo)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type is Card");}
//				entyityInquiryType = Constants.HOST_REQUEST_ENTITYINQTYPE_CARD;
//			}else{
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The entyityInquiryType type is Customer");}
//				entyityInquiryType = Constants.HOST_REQUEST_ENTITYINQTYPE_CUSTOMER;
//			}

			/**
			 * Setting the InteralCustomerID and nationalID as null value
			 */

			ArrayList<String> numberList = new ArrayList<>();

			ArrayList<String> creditCardNumList = new ArrayList<String>();
			creditCardNumList.add(SelectedCardOrAcctNo);

			ArrayList<String> prepaidCardNumList = new ArrayList<String>();
			prepaidCardNumList.add(SelectedCardOrAcctNo);
			ArrayList<String> cardAccountNumList = new ArrayList<String>();
			String cardAccountNum = null;

			if (!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())) {
				if (!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl())) {
					if (!util.isNullOrEmpty(
							callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getFirstCCAccountNo())) {
						// TODO - CC
//						cardAccountNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl().getAccountNumber();

						cardAccountNum = callInfo.getCallerIdentification_HostRes().getCustomerEntityDtl()
								.getFirstCCAccountNo();

						if (logger.isDebugEnabled()) {
							WriteLog.write(WriteLog.DEBUG, session_ID_,
									"Card Account Number is" + util.getSubstring(cardAccountNum, Constants.GL_FOUR));
						}

					}
				}
			}
			cardAccountNumList.add(cardAccountNum);

			String inquiryReference = Constants.EMPTY_STRING;
			inquiryReference = (String) ivr_ICEFeatureData.getConfig()
					.getParamValue(Constants.CUI_CREDITCARD_INQUIRYREFERENCE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The inquiryReference is " + inquiryReference);
			}

			if (!util.isNullOrEmpty(creditCardNumList)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Credit card list is " + creditCardNumList);
				}
				numberList.addAll(creditCardNumList);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as C");}
//				inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_C;
			}else if(!util.isNullOrEmpty(prepaidCardNumList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Prepaid card list is "+ prepaidCardNumList);}
				numberList.addAll(prepaidCardNumList);
			}else if(!util.isNullOrEmpty(cardAccountNumList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card account number list is "+ cardAccountNumList);}
				numberList.addAll(cardAccountNumList);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as A");}
//				inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_A;
			} else {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Customer ID list is " + customerIDList);
				}
				numberList.addAll(customerIDList);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting inquiryReference type as U");}
//				inquiryReference = Constants.HOST_REQUEST_INQUIRYREFERENCE_U;
			}

			// As per the updated wsdl we need to pass the following value to the host
			String returnReplacedCards = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_CCBALANCE_RETURNED_REPLACED_CARD);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"returnReplacedCards value configured in the ICE is " + returnReplacedCards);
			}

			/**
			 * END
			 */
//			String creditCardNum = Constants.EMPTY_STRING;

//			String cardAccountNum = Constants.EMPTY_STRING;

//			String internalCustomerID = Constants.EMPTY_STRING;

//			String nationalID = Constants.EMPTY_STRING;
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String) callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			// hostReportDetails.setHostCounter(hostCounter);
			// hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO
					+ callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER
					+ Constants.EQUALTO + Constants.NA + Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM
					+ Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ? "NA"
							: callInfo.getField(Field.ESBREQREFNUM));
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_CCENTITYINQUIRY);
			// hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); // It should be in the formate of 31/07/2013
																			// 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_DEBITCARDS);
			// End Reporting

			/*
			 * Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData) callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);

			/* END */

			String entityEnquirySize = (String) ivr_ICEGlobalConfig.getConfig()
					.getParamValue(Constants.CUI_UI_CCENTITY_ENQUIRY_SIZE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"The configured entity Enquiry size is " + entityEnquirySize);
			}

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData()) ? null
					: (ICEFeatureData) callInfo.getICEFeatureData();

			if (util.isNullOrEmpty(iceFeatureData)) {
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(
					iceFeatureData.getConfig().getParamValue(Constants.CUI_CCENTITYINQUIRY_REQUESTTYPE)) ? null
							: (String) iceFeatureData.getConfig()
									.getParamValue(Constants.CUI_CCENTITYINQUIRY_REQUESTTYPE);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);
			}

			APINCustomerProfileDetails_HostRes apinCustomerProfileDetails_HostRes = ccEntityInquiryDAO
					.getAPINValCustProfDetailsHostRes(callInfo, entyityInquiryType, inquiryReference, numberList,
							returnReplacedCards, entityEnquirySize, requestType);

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"apinCustomerProfileDetails_HostRes Object is :" + apinCustomerProfileDetails_HostRes);
			}
			callInfo.setAPINCustomerProfileDetails_HostRes(apinCustomerProfileDetails_HostRes);

			code = apinCustomerProfileDetails_HostRes.getErrorCode();
			/*
			 * For Reporting Start
			 */

			String hostEndTime = apinCustomerProfileDetails_HostRes.getHostEndTime();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);
			}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = apinCustomerProfileDetails_HostRes.getHostResponseCode();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host response code is " + hostResCode);
			}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				responseDesc = Constants.HOST_SUCCESS;
			}

			/**** Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO
					+ callInfo.getField(Field.CUSTOMERID) + Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER
					+ Constants.EQUALTO + Constants.NA + Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM
					+ Constants.EQUALTO + (util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ? "NA"
							: callInfo.getField(Field.ESBREQREFNUM));
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/

			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc
					+ Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE + Constants.EQUALTO + hostResCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC + Constants.EQUALTO
					+ (util.isNullOrEmpty(apinCustomerProfileDetails_HostRes.getErrorDesc()) ? "NA"
							: apinCustomerProfileDetails_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData) callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			// End Reporting
			if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)) {
				// Commented for EPIN CR on 04-Mar-2019
				// code = Constants.WS_FAILURE_CODE;
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Got success for CCEntity Service host access");
				}
			} else {

				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"Got failure response for APIN Customer Profile host service");
				}
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "The original response code of host access is "
							+ apinCustomerProfileDetails_HostRes.getHostResponseCode());
				}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_CCENTITYINQUIRY,
						apinCustomerProfileDetails_HostRes.getHostResponseCode());
			}
			if (logger.isInfoEnabled()) {
				WriteLog.write(WriteLog.INFO, session_ID_, "EXIT:  APINValidationImpl.getCompleteCustomerProfile()");
			}
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,
					"There was an error at APINValidationImpl.getCompleteCustomerProfile " + e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public boolean isAPINAConsecutiveNo(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";
		try {
			session_ID_ = (String) callInfo.getField(Field.SESSIONID);
			logger = (Logger) callInfo.getField(Field.LOGGER);
			WriteLog.loggerInit(logger, session_ID_);
		} catch (Exception e) {
		}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO, session_ID_, "ENTER: APINValidationImpl.isAPINAConsecutiveNo()");
		}
		boolean isAPINConsecuetive = false;
		try {

			String apin = (String) callInfo.getField(Field.APIN);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Retreived user entered APIN" + apin);
			}

			isAPINConsecuetive = util.checkConsecutive(apin);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Is APIN entered is consecuetive" + isAPINConsecuetive);
			}

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				WriteLog.writeError(WriteLog.ERROR, e,
						"There was an error at APINValidationImpl.getAPINPhrases() " + e.getMessage());
			}
			throw new ServiceException(e);
		}
		return isAPINConsecuetive;
	}

	@Override
	public String getAPINPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";
		try {
			session_ID_ = (String) callInfo.getField(Field.SESSIONID);
			logger = (Logger) callInfo.getField(Field.LOGGER);
			WriteLog.loggerInit(logger, session_ID_);
		} catch (Exception e) {
		}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO, session_ID_, "ENTER: APINValidationImpl.getAPINPhrases()");
		}
		String str_GetMessage, finalResult;

		try {
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Fetching the Feature Object values");
			}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			// Need to get the FeatureConfig Data
			String apinLength = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_APINLength);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Apin Length configured is " + apinLength);
			}

			String forgotAPIN = util.isNullOrEmpty(
					ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ENABLE_FORGOT_APIN_OPTION))
							? Constants.EMPTY_STRING
							: (String) ivr_ICEFeatureData.getConfig()
									.getParamValue(Constants.CUI_ENABLE_FORGOT_APIN_OPTION);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, " Enabled Forgot APIN option " + forgotAPIN);
			}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			// Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(apinLength);

			grammar = Constants.GRAMMAR_ENTERAPIN;
			grammar = grammar + apinLength;
			grammar = grammar + Constants.EXTENSION_GRXML;

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The grammar value for APIN length is " + grammar);
			}

			if (Constants.TRUE.equalsIgnoreCase(forgotAPIN)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"Is to add the forgot APIN Option in the dynamic list " + forgotAPIN);
				}
				dynamicValueArray.add(DynaPhraseConstants.APIN_1001);
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);

				grammar = grammar + Constants.ASTERISK;
				grammar = grammar + Constants.GRAMMAR_AGENT_ONE_DTMF + Constants.EXTENSION_GRXML;
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"The grammar Including the Press one for Forgot APIN is " + grammar);
				}
			} else {
				dynamicValueArray.add(Constants.NA);
				dynamicValueArray.add(Constants.NA);
			}

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Generated dynamic phrase list is" + dynamicValueArray);
			}

			String languageKey = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Language Key value is" + languageKey);
			}
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Locale selected is " + locale);
			}

			String menuID = MenuIDMap.getMenuID("APIN_VALIDATION");
			// String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("APIN_Validation");
			String combinedKey = languageKey + Constants.UNDERSCORE + featureID + Constants.UNDERSCORE + menuID;

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Combined Key is " + combinedKey);
			}

			Object[] object = new Object[dynamicValueArray.size()];
			for (int count = 0; count < dynamicValueArray.size(); count++) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_,
							"Adding " + count + "element: " + dynamicValueArray.get(count) + "into Object array ");
				}
				object[count] = dynamicValueArray.get(count);

			}

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "objArray  is :" + object);
			}
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);
			}

			str_GetMessage = this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE,
					locale);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"The property value for the get Message method is " + str_GetMessage);
			}

			if (str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Assigning Silence phrase as result");
				}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_,
						"Total Prompt received from the dynaproperty file is " + totalPrompt);
			}

			// Overriding the total prompts, received from the property file

			// To have the property file grammar, need to call that util method here

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The dynamic phrase key is " + dynamicPhraseKey);
			}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The dynamic message value is" + dynamicMessageValue);
			}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "The final string formation is :" + finalResult);
			}

			// Need to handle if we want to append pipe Seperator sign

			if (logger.isInfoEnabled()) {
				WriteLog.write(WriteLog.INFO, session_ID_, "EXIT: APINValidationImpl.getAPINPhrases()");
			}

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				WriteLog.writeError(WriteLog.ERROR, e,
						"There was an error at APINValidationImpl.getAPINPhrases() " + e.getMessage());
			}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public void getAndSetMasterKey(CallInfo callInfo) throws ServiceException {
		// TODO Auto-generated method stub
		String session_ID_ = "";
		try {
			session_ID_ = (String) callInfo.getField(Field.SESSIONID);
			logger = (Logger) callInfo.getField(Field.LOGGER);
			WriteLog.loggerInit(logger, session_ID_);
		} catch (Exception e) {
		}
		if (logger.isInfoEnabled()) {
			WriteLog.write(WriteLog.INFO, session_ID_, "ENTER: APINValidationImpl.getAndSetMasterKey()");
		}
		try {
			/**
			 * Getting the dynamic key and the cipher text of master key from the DB Method
			 */

			String db_Code = Constants.ONE;
			int codeLength = Constants.GL_ZERO;
			String sessionId = (String) callInfo.getField(Field.SESSIONID);

			if (util.isNullOrEmpty(sessionId)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "Session ID is null or empty");
				}
				throw new ServiceException("Session id is null or empty");
			}

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Calling the DB Method ");
			}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "Setting the input for getSequenceNo");
			}

			String uui = (String) callInfo.getField(Field.UUI);
			if (logger.isDebugEnabled()) {
				WriteLog.write(WriteLog.DEBUG, session_ID_, "UUI of the call is " + uui);
			}

			if (util.isNullOrEmpty(uui)) {
				if (logger.isDebugEnabled()) {
					WriteLog.write(WriteLog.DEBUG, session_ID_, "uui ID is null or empty");
				}
				throw new ServiceException("Session id is null or empty");
			}

			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			String apin_dynamicKey = Constants.EMPTY_STRING;
			String apin_MasterKey = Constants.EMPTY_STRING;
			String apin_StaticKey = Constants.APIN_STATIC_KEY;
			String combinedKey = Constants.EMPTY_STRING;
			try {
				db_Code = dataServices.getAPINMasterKey(logger, sessionId, uui, configMap);

				if (Constants.WS_SUCCESS_CODE.equalsIgnoreCase(db_Code)) {
					apin_dynamicKey = (String) configMap.get(DBConstants.APIN_DYNAMICKEY);
					apin_MasterKey = (String) configMap.get(DBConstants.APIN_MASTERKEY);

					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_, "Retrieved APIN Dynamic & Master keys");
					}

					if (util.isNullOrEmpty(apin_dynamicKey) || util.isNullOrEmpty(apin_MasterKey)) {
						if (logger.isDebugEnabled()) {
							WriteLog.write(WriteLog.DEBUG, session_ID_,
									"apin_dynamicKey / apin_MasterKey is null or empty");
						}
						throw new ServiceException("apin_dynamicKey / apin_MasterKey is null or empty");
					}

					if (util.isNullOrEmpty(apin_StaticKey)) {
						if (logger.isDebugEnabled()) {
							WriteLog.write(WriteLog.DEBUG, session_ID_, "apin_StaticKey is null or empty");
						}
						throw new ServiceException("apin_StaticKey is null or empty");
					}

					combinedKey = apin_StaticKey + apin_dynamicKey;
					combinedKey = combinedKey.trim();
					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_, "Formed the combined key ");
					}

					combinedKey = util.convertTo48BitKey(combinedKey);
					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_, "Combined key has been converted to 48 bits");
					}

					if (util.isNullOrEmpty(combinedKey)) {
						throw new ServiceException("Masterkey conversion result is null or empty");
					}

					JCEWrapper jceWrap = new JCEWrapper(Constants.JCEWRAPPER_FILE_LOCATION);

					SecretKey sKey = jceWrap.toSecretKey(combinedKey);
					String plainMasterKey = jceWrap.decrypt(apin_MasterKey, sKey);
//					String plainMasterKey = apin_MasterKey;
					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_, "Received the Plain master key");
					}

					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_,
								"Setting the Plain APIN Master key in the CallInfo field value");
					}
					callInfo.setField(Field.APINMASTERKEY, plainMasterKey);

					if (logger.isDebugEnabled()) {
						WriteLog.write(WriteLog.DEBUG, session_ID_, "Saved the Plain master key at the callinfo field");
					}
				} else {
					if (logger.isInfoEnabled()) {
						WriteLog.write(WriteLog.INFO, session_ID_, "Error in the S1 sequencial DB response");
					}
					throw new ServiceException("APIN master key retrieval DB access throwing error");
				}
			} catch (com.db.exception.ServiceException e) {
				if (logger.isInfoEnabled()) {
					WriteLog.write(WriteLog.INFO, session_ID_, "ERROR: APINValidationImpl.getAndSetMasterKey()");
				}
				throw new ServiceException("APIN Master key retrieval DB access throwing error");
				// e.printStackTrace();
			}

			if (logger.isInfoEnabled()) {
				WriteLog.write(WriteLog.INFO, session_ID_, "EXIT: APINValidationImpl.getAndSetMasterKey()");
			}

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				WriteLog.writeError(WriteLog.ERROR, e,
						"There was an error at APINValidationImpl.getAndSetMasterKey() " + e.getMessage());
			}
			throw new ServiceException(e);
		}

	}
	/*
	 * public static void main(String s[]){ //acdae0b06b5c08dd8aec4571d029acc9
	 * 13EC170FE76CE16A //EEB4B236CC331EBBB765ACFAB72E8466 JCEWrapper jceWrap = new
	 * JCEWrapper("d:\\parthiban\\JCEWrap_config.properties");
	 * //d31133806e546d521b637882cfa75418 SecretKey sKey =
	 * jceWrap.toSecretKey(convertTo48BitKey("5A4B3C52CDE629F1"+"8F80FC5ABEB929E0"))
	 * ; String plainMasterKey = jceWrap.decrypt("d31133806e546d521b637882cfa75418",
	 * sKey); System.out.println(plainMasterKey);
	 * 
	 * SecretKey sKey =
	 * jceWrap.toSecretKey(convertTo48BitKey("9FED8E449377530CD18BF0D4566A4BE2"));
	 * String clearKey = jceWrap.decrypt("7C5E7B7AEEFE868F4AD7FA7A9F882E41", sKey);
	 * System.out.println("Received the clear key and the clear key is "+ clearKey);
	 * 
	 * String pinBlocking = util.getISOPinBlock(pan, pin, true, false,
	 * Constants.GL_THREE, Constants.GL_ONE);
	 * System.out.println("Have Successfully done the pin blocking");
	 * 
	 * clearKey = convertTo48BitKey(clearKey); //
	 * if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,
	 * "Clear key is converted to 48 digit and the result got "+ clearKey);}
	 * System.out.println("Clear key is converted to 48 digit and the result got "
	 * +clearKey);
	 * 
	 * 
	 * }
	 */

	public static String convertTo48BitKey(String inputString) {
		// if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.INFO, "ENTER:
		// Util.convertTo48BitKey()");}
		int stringLength = Constants.GL_ZERO;
		String k1 = Constants.EMPTY_STRING;
		String k2 = Constants.EMPTY_STRING;
		String k3 = Constants.EMPTY_STRING;
		String result = Constants.EMPTY_STRING;
		try {

			stringLength = inputString.length();
			// if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "Input string
			// length is "+ stringLength);}

			if (stringLength == 16) {
				//System.out.println("Input string of length 16, so treating K1= K2= K3");
				k1 = inputString;
				k2 = inputString;
				k3 = inputString;
				result = k1 + k2 + k3;
				result = result.trim();
			}

			else if (stringLength == 32) {
				//System.out.println("Input string of length 32, so treating K1= K3");

				k1 = inputString.substring(Constants.GL_ZERO, Constants.GL_SIXTEEN);
				// if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "K1 value is "
				// + k1);}

				k2 = inputString.substring(Constants.GL_SIXTEEN, inputString.length());
				// if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "K2 value is "
				// + k2);}

				k3 = k1;
				//System.out.println("Considering the result of K1 as K3 ");

				result = k1 + k2 + k3;
				result = result.trim();
			}

			else if (stringLength == 48) {
				//System.out.println("Input string of length 48, so returning the same");

				result = inputString.trim();
			} else {

				//System.out.println("Its an invalid key returning the same");
				return inputString;
			}

			// if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "The final 48
			// bit length key is "+result);}

		} catch (Exception e) {
			System.out.println("EXCEPTION at: Util.convertTo48BitKey()" + e);
			util.getFullStackTrace(e);
		}
		//System.out.println("Result=" + result);
		return result;
	}
}
