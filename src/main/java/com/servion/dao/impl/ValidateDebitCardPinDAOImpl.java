package com.servion.dao.impl;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.Message210Type;
import com.bankmuscat.esb.cardmanagementservice.ValidateDbtCrdPinResType;
import com.bankmuscat.esb.commontypes.S1ResMessageType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.ValidateDebitCardPinDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.apinValidation.ValidatePIN_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.ValidateDebitCardPinService;
import com.servion.ws.util.DAOLayerUtils;

public class ValidateDebitCardPinDAOImpl implements ValidateDebitCardPinDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	ValidateDebitCardPinService validateDebitCardPinService;
	
	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

	
	public ValidateDebitCardPinService getValidateDebitCardPinService() {
		return validateDebitCardPinService;
	}

	public void setValidateDebitCardPinService(
			ValidateDebitCardPinService validateDebitCardPinService) {
		this.validateDebitCardPinService = validateDebitCardPinService;
	}


	@Override
	public ValidatePIN_HostRes getAPINValidateHostRes(CallInfo callInfo,
			String pan, String processingCode, String amtTransaction,
			BigDecimal amtSettlement, String transmissionDate, BigDecimal convRateSettlement,
			String systemTraceAudit, String localTransactionTime,
			String localTransactionDate, String expirationDate,
			String settlementDate, String conversionDate, String merchantType,
			String pointOfServiceEntryMode, String cardSeqNum,
			String pointOfserviceConditionCode, String pointOfServiceCaptureCode,
			int authIDRespLength, BigDecimal amtSettlementFee,
			BigDecimal amtSettlementProcessingFee, String acquInstitutionCode,
			String trackTwoData, String cardAccpTerminalID,
			String cardAccpIDCode, String cardAccpName, String currCode, String currCodeSettlement,
			String pin, String securityContrInfo, String additionalAmt,
			int extendedPaymentCode, String origDataElements, String payee,
			String recvInstitutionID, String acctIdentfOne,
			String acctIdentfTwo, String posDataCode, String bitMap,
			String checkData, String termOwner, String posGeographicData,
			String sponsorBank, String addrVerfData, String bankDetails,
			String payeeNameAddr, String iccData, String origalData,
			String MACField, String lastUpdTimeStamp, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ValidateDebitCardPinDAOImpl.getAPINValidateHostRes()");}
		
		ValidatePIN_HostRes beanResponse = new ValidatePIN_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			ValidateDbtCrdPinResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callDebitCardDetailsHost");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = validateDebitCardPinService.callValidateDbtCrdPinHost(logger, sessionID, pan, processingCode, amtTransaction, amtSettlement, transmissionDate, 
					convRateSettlement, systemTraceAudit, localTransactionTime, localTransactionDate, expirationDate, settlementDate, conversionDate, merchantType, 
					pointOfServiceEntryMode, cardSeqNum, pointOfserviceConditionCode, pointOfServiceCaptureCode, authIDRespLength, amtSettlementFee, amtSettlementProcessingFee, 
					acquInstitutionCode, trackTwoData, cardAccpTerminalID, cardAccpIDCode, cardAccpName, currCode, currCodeSettlement, pin, securityContrInfo, additionalAmt, 
					extendedPaymentCode, origDataElements, payee, recvInstitutionID, acctIdentfOne, acctIdentfTwo, posDataCode, bitMap, checkData, termOwner, 
					posGeographicData, sponsorBank, addrVerfData, bankDetails, payeeNameAddr, iccData, origalData, MACField, lastUpdTimeStamp, requestType, str_UUI, generateXML, callInfo);
			 
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of ValidateDebitCardPinService is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ValidateDbtCrdPIN_Succ_ErrorCode);
			}
			
			//Setting host response code
			beanResponse.setHostResponseCode(code);
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for ValidateDebitCardPinService is : "+ws_ResponseHeader.getEsbErrDesc());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ValidateDebitCardPin HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code of ValidateDbtCardPin for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					Message210Type message210Type = null;
					message210Type = response.getMessage210();
					
					if(!util.isNullOrEmpty(message210Type)){
						
						S1ResMessageType s1ResMessageType = message210Type.getS1ResBaseMessageType();
						
						if(!util.isNullOrEmpty(s1ResMessageType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Addr Verification Result value is "+s1ResMessageType.getField038());}
							beanResponse.setAddrVerfResult(s1ResMessageType.getField12716());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Auth ID  value is "+s1ResMessageType.getField038());}
							beanResponse.setAuthIDResponse(s1ResMessageType.getField038());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Bit Map  value is "+s1ResMessageType.getField038());}
							beanResponse.setBitMap(s1ResMessageType.getField1271());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Data Settlement value is "+s1ResMessageType.getField038());}
							beanResponse.setDateSettlement(s1ResMessageType.getField015());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Message Authentication Code value is "+s1ResMessageType.getField038());}
							beanResponse.setMessageAuthenticationCode(s1ResMessageType.getField128());
							
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Data Private value is "+message210Type.getField048());}
						beanResponse.setAdditionalDataPrivate(message210Type.getField048());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Amount value is "+message210Type.getField054());}
						beanResponse.setAdditionAmount(message210Type.getField054());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Settlement  value is "+message210Type.getField005());}
						beanResponse.setAmountSettlement(message210Type.getField005()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Settlement fee value is "+message210Type.getField029());}
						beanResponse.setAmountSettlementFee(message210Type.getField029()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Settlement Processing Fee value is "+message210Type.getField031());}
						beanResponse.setAmountSettlementProcessFee(message210Type.getField031()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Transaction Fee value is "+message210Type.getField028());}
						beanResponse.setAmountTransactionFee(message210Type.getField028()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Transaction  Processing fee value is "+message210Type.getField030());}
						beanResponse.setAmountTransactionProcessFee(message210Type.getField030()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Auth Agent ID value is "+message210Type.getField058());}
						beanResponse.setAuthAgentIDCode(message210Type.getField058());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Conversion Rate Settlement value is "+s1ResMessageType.getField038());}
						beanResponse.setConversionRateSettlement(message210Type.getField009()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Curr Code Settlement value is "+message210Type.getField050());}
						beanResponse.setCurrCodeSettlement(message210Type.getField050());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Date Conversion  value is "+message210Type.getField016());}
						beanResponse.setDateConversion(message210Type.getField016());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Response code value is "+message210Type.getField039());}
						beanResponse.setResponseCode(message210Type.getField039()+Constants.EMPTY);
						
						
						String actualSucccesssResponseCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_ValidateDbtCrdPIN_Field39SuccValue);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "UI Configured Host response value for GetDebitCardDetails is "+actualSucccesssResponseCode);}
						
						if(!util.isNullOrEmpty(actualSucccesssResponseCode) && util.isCodePresentInTheConfigurationList(beanResponse.getResponseCode(), actualSucccesssResponseCode)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Got success response code for ValidateDbtCardPIN");}
							beanResponse.setHostResponseCode(beanResponse.getResponseCode());
						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Overriding the IVR response code as 1");}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The response code value for IVR is " + beanResponse.getResponseCode());}
							code = Constants.WS_FAILURE_CODE;
							beanResponse.setHostResponseCode(beanResponse.getResponseCode());
							beanResponse.setErrorCode(code);
						}

						
						
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty message210Type response object so setting error code as 1");}
						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty response object so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ValidateDebitCardPinDAOImpl.getAPINValidateHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ValidateDebitCardPinDAOImpl.getAPINValidateHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ValidateDebitCardPinDAOImpl.getAPINValidateHostRes()");}
		return beanResponse;
	}

}
