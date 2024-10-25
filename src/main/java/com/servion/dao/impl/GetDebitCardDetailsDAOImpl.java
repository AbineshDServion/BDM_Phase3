package com.servion.dao.impl;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.GetDbtCrdDetailsResType;
import com.bankmuscat.esb.cardmanagementservice.Message110Type;
import com.bankmuscat.esb.commontypes.S1ResMessageType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.GetDebitCardDetailsDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.callerIdentification.CallerIdenf_DebitCardDetails;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.GetDebitCardDetailsService;
import com.servion.ws.util.DAOLayerUtils;

public class GetDebitCardDetailsDAOImpl implements GetDebitCardDetailsDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	GetDebitCardDetailsService getDebitCardDetailsService;

	public GetDebitCardDetailsService getGetDebitCardDetailsService() {
		return getDebitCardDetailsService;
	}

	public void setGetDebitCardDetailsService(
			GetDebitCardDetailsService getDebitCardDetailsService) {
		this.getDebitCardDetailsService = getDebitCardDetailsService;
	}

	//	//Object will initialized through Spring BEAN Injection
	//	private WS_RequestHeader ws_RequestHeader;
	//	
	//	public WS_RequestHeader getWs_RequestHeader() {
	//		return ws_RequestHeader;
	//	}
	//
	//	public void setWs_RequestHeader(WS_RequestHeader ws_RequestHeader) {
	//		this.ws_RequestHeader = ws_RequestHeader;
	//	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public CallerIdenf_DebitCardDetails getCallerIdenfCustomerIDHostRes(
			CallInfo callInfo, String pan, String processingCode,
			String amtTransaction, BigDecimal amtSettlement, String transmissionDate,
			int conversionRate, String sysTraceAuditNo, String localTransTime,
			String localTansDate, String expirationDate, String settlementDate,
			XMLGregorianCalendar dateConversion, String merchantType,
			String pointOfServiceMode, String cardSeqNum, String pointOfServCondCode,
			String pointOfServCaptureCode, int authIDRespLength,
			BigDecimal amtSettlementFee, BigDecimal amtSettlementProcFee,
			String AcquiringInstitutionID, String trackTwoData,
			String cardAccpTerminalID, String cardAccpIDCode,
			String cardAccpName, String currCode, String currCodeSettlement,
			String pin, String securityContInfo, String additionalAmt,
			int extendedPaymentCode, String originalDataElement, String payee,
			String recvInstIDCode, String acctIdenfOne, String acctIdenfTwo,
			String posDataCode, String bitMap, String switchKey,
			String checkData, String terminalOwner, String posGeographicData,
			String sponsorBank, String addrVerfData, String bankDetails,
			String payeeName, String iccData, String origData, String macField, String structureData, String extendedTransType, String requestType)
					throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: GetDebitCardDetailsDAOImpl.getCallerIdentificationHostRes()");}


		CallerIdenf_DebitCardDetails beanResponse = new CallerIdenf_DebitCardDetails();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			GetDbtCrdDetailsResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callDebitCardDetailsHost");}

			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = getDebitCardDetailsService.callDebitCardDetailsHost(logger, sessionID, 
					pan, processingCode, amtTransaction, amtSettlement, transmissionDate, conversionRate,
					sysTraceAuditNo,  localTransTime,  localTansDate, expirationDate,  settlementDate,
					dateConversion,  merchantType, pointOfServiceMode,  cardSeqNum,  pointOfServCondCode,
					pointOfServCaptureCode,  authIDRespLength, amtSettlementFee,  amtSettlementProcFee,
					AcquiringInstitutionID,  trackTwoData, cardAccpTerminalID,  cardAccpIDCode,
					cardAccpName,  currCode,  currCodeSettlement, pin,  securityContInfo,  additionalAmt,
					extendedPaymentCode,  originalDataElement,  payee, recvInstIDCode,  acctIdenfOne,  acctIdenfTwo,
					posDataCode,  bitMap,  switchKey, checkData, terminalOwner,  posGeographicData,
					sponsorBank,  addrVerfData,  bankDetails, payeeName,  iccData,  origData,  macField, structureData, extendedTransType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of GetDetbitCardDetails is : "+code);}
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

			beanResponse.setHostResponseCode(code);

			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for GetDetbitCardDetails is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### GetDbtCardDtls HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetDebitCardDetails_Succ_ErrorCode);
			}
			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			String strCustomerID = Constants.EMPTY_STRING;
			String strExpiryDate = Constants.EMPTY_STRING;
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					Message110Type message110Type = null;
					message110Type = response.getMessage110();

					String str_CUICustomerIDStartKey = Constants.EMPTY_STRING;
					String str_CUICustomerIDEndKey = Constants.EMPTY_STRING;
					int int_CUICustomerIDKeyLength =  Constants.GL_ZERO;
					
					String str_CUIExpiryDateStartKey = Constants.EMPTY_STRING;
					String str_CUIExpiryDateEndKey = Constants.EMPTY_STRING;
					int int_CUIExpiryDateKeyLength =  Constants.GL_ZERO;

					if(!util.isNullOrEmpty(message110Type)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "##Response Field ## Received valid message110Type response object");}
						S1ResMessageType s1ResMessageType = message110Type.getS1ResBaseMessage();

						if(!util.isNullOrEmpty(s1ResMessageType)){

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Processing code is "+s1ResMessageType.getField003());}
							beanResponse.setProcessingCode(s1ResMessageType.getField003()+"");

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Date settlement value is"+s1ResMessageType.getField015());}
							beanResponse.setDateSettlement(s1ResMessageType.getField015());

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Field ID value is"+s1ResMessageType.getField011());}
							beanResponse.setSystemTraceNumber(s1ResMessageType.getField011());

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Authorization identification response value is "+s1ResMessageType.getField038());}
							beanResponse.setAuthIdentificationCode(s1ResMessageType.getField038());

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Response code is "+s1ResMessageType.getField039());}
							beanResponse.setResponseCode(s1ResMessageType.getField039());

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field BitMap value is "+s1ResMessageType.getField1271());}
							beanResponse.setBitMap(s1ResMessageType.getField1271());

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Address Verfication Result "+s1ResMessageType.getField12716());}
							beanResponse.setAddrVerfResult(s1ResMessageType.getField12716());

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Message Authentication Code is "+s1ResMessageType.getField128());}
							beanResponse.setMessageAuthenticationCode(s1ResMessageType.getField128());

							String actualSucccesssResponseCode = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_Field39SuccValue);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "UI Configured Host response value for GetDebitCardDetails is "+actualSucccesssResponseCode);}
							
							if(!util.isNullOrEmpty(actualSucccesssResponseCode) && util.isCodePresentInTheConfigurationList(beanResponse.getResponseCode(), actualSucccesssResponseCode)){
								
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Customer ID  "+s1ResMessageType.getField12722());}
								strCustomerID = s1ResMessageType.getField12722();

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The configured structed data Customer id is "+ str_CUICustomerIDStartKey);}

								str_CUICustomerIDStartKey = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_CustomerIDStartTag);
								int_CUICustomerIDKeyLength = util.isNullOrEmpty(str_CUICustomerIDStartKey)?Constants.GL_ZERO : str_CUICustomerIDStartKey.length();

								str_CUICustomerIDEndKey= (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_CustomerIDEndTag);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The configured structed data Customer id is "+ str_CUICustomerIDStartKey);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Ending Key Length :"+ str_CUICustomerIDEndKey);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The length of the configured Structured data customer id is "+ int_CUICustomerIDKeyLength);}

								strCustomerID = strCustomerID.substring(strCustomerID.indexOf(str_CUICustomerIDStartKey)+int_CUICustomerIDKeyLength);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The result of first substring the customer id is "+ strCustomerID);}

								strCustomerID = strCustomerID.substring(Constants.GL_ZERO, strCustomerID.indexOf(str_CUICustomerIDEndKey));
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The result of Second substring result is "+ strCustomerID);}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The customer id is "+ strCustomerID);}

								beanResponse.setCustomerID(strCustomerID);
								
								strExpiryDate = s1ResMessageType.getField12722();

								//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The configured structed data Customer id is "+ str_CUIExpiryDateStartKey);}

								str_CUIExpiryDateStartKey = (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_ExpiryDateStartTag);
								int_CUIExpiryDateKeyLength = util.isNullOrEmpty(str_CUIExpiryDateStartKey)?Constants.GL_ZERO : str_CUIExpiryDateStartKey.length();

								str_CUIExpiryDateEndKey= (String) ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_UI_GetDebitCardDetails_ExpiryDateEndTag);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The configured structed data Customer id is "+ str_CUIExpiryDateStartKey);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Ending Key Length :"+ str_CUIExpiryDateEndKey);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The length of the configured Structured data customer id is "+ int_CUIExpiryDateKeyLength);}

								strExpiryDate = strExpiryDate.substring(strExpiryDate.indexOf(str_CUIExpiryDateStartKey)+int_CUIExpiryDateKeyLength);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The result of first substring the customer id is "+ strExpiryDate);}

								strExpiryDate = strExpiryDate.substring(Constants.GL_ZERO, strExpiryDate.indexOf(str_CUIExpiryDateEndKey));
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The result of Second substring result is "+ strExpiryDate);}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Expiry Date is "+ strExpiryDate);}

								beanResponse.setExpiryDate(strExpiryDate);

								
							}else{
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Overriding the IVR response code as 1");}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Actual response code for IVR is " + s1ResMessageType.getField039());}
								code = Constants.WS_FAILURE_CODE;
								beanResponse.setHostResponseCode(s1ResMessageType.getField039());
								beanResponse.setErrorCode(code);
							}

						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount settlement value is "+message110Type.getField005());}
						beanResponse.setAmountSettlement(message110Type.getField005()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Conversion rate settlement value is "+message110Type.getField009());}
						beanResponse.setConversionDateSettlement(message110Type.getField009()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Date Conversion value is "+message110Type.getField016());}
						beanResponse.setDateConversion(message110Type.getField016());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount transaction fee"+message110Type.getField028());}
						beanResponse.setAmountTransactionFee(message110Type.getField028()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Settlement fee"+message110Type.getField029());}
						beanResponse.setAmountSettlementFee(message110Type.getField029()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Transaction Processing fee"+message110Type.getField030());}
						beanResponse.setAmountTransProcessFee(message110Type.getField030()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Transaction Processing fee"+message110Type.getField031());}
						beanResponse.setAmountSettlementProcessFee(message110Type.getField031()+Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Addition data private values is "+message110Type.getField048());}
						beanResponse.setAdditionalDataPrivate(message110Type.getField048());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Currency code settlement is "+message110Type.getField050());}
						beanResponse.setCurrCodeSettlement(message110Type.getField050());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Amount is "+message110Type.getField054());}
						beanResponse.setAdditionalAmount(message110Type.getField054());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Authorization AgentID code is "+message110Type.getField058());}
						beanResponse.setAuthAgentIDCode(message110Type.getField058());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Echo data is "+message110Type.getField059());}
						beanResponse.setEchoData(message110Type.getField059());	

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field POS data code is "+message110Type.getField123());}
						beanResponse.setPosDataCode(message110Type.getField123());


						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Originator / Authorizer data settlement value "+message110Type.getField12720());}
						beanResponse.setAuthorizerDataSettlement(message110Type.getField12720());

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty message110Type response object so setting error code as 1");}

						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty message110Type response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at GetDebitCardDetailsDAOImpl.getCallerIdentificationHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GetDebitCardDetailsDAOImpl.getCallerIdentificationHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: GetDebitCardDetailsDAOImpl.getCallerIdentificationHostRes()");}
		return beanResponse;
	}
	/*
	@Override
	public EnterCard_CallerIdentification_HostRes getEnterDbtCallerIdenfCustIDfHostRes(
			CallInfo callinfo, String pan, String processingCode,
			String amtTransaction, int amtSettlement, String transmissionDate,
			int conversionRate, int sysTraceAuditNo, String localTransTime,
			String localTansDate, String expirationDate, String settlementDate,
			XMLGregorianCalendar dateConversion, int merchantType,
			int pointOfServiceMode, int cardSeqNum, int pointOfServCondCode,
			int pointOfServCaptureCode, int authIDRespLength,
			int amtSettlementFee, int amtSettlementProcFee,
			String AcquiringInstitutionID, String trackTwoData,
			String cardAccpTerminalID, String cardAccpIDCode,
			String cardAccpName, String currCode, String currCodeSettlement,
			String pin, String securityContInfo, String additionalAmt,
			int extendedPaymentCode, String originalDataElement, String payee,
			String recvInstIDCode, String acctIdenfOne, String acctIdenfTwo,
			String posDataCode, String bitMap, String switchKey,
			String checkData, String terminalOwner, String posGeographicData,
			String sponsorBank, String addrVerfData, String bankDetails,
			String payeeName, String iccData, String origData, String macField)
			throws DaoException {
		// TODO Auto-generated method stub
		return null;
	}
	 */
}
