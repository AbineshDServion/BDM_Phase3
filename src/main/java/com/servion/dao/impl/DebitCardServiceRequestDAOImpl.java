package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.DbtCrdSvcResType;
import com.bankmuscat.esb.cardmanagementservice.Message610Type;
import com.bankmuscat.esb.commontypes.S1ResMessageType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.DebitCardServiceRequestDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.debitCardActivation.ActivateCard_HostRes;
import com.servion.model.reportLostCard.LostStolenCard_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.DebitCardServiceRequestService;
import com.servion.ws.util.DAOLayerUtils;

public class DebitCardServiceRequestDAOImpl implements DebitCardServiceRequestDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	DebitCardServiceRequestService debitCardServiceRequestService;
	
	public DebitCardServiceRequestService getDebitCardServiceRequestService() {
		return debitCardServiceRequestService;
	}

	public void setDebitCardServiceRequestService(
			DebitCardServiceRequestService debitCardServiceRequestService) {
		this.debitCardServiceRequestService = debitCardServiceRequestService;
	}
	
	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public LostStolenCard_HostRes getReportLostCardHostRes(CallInfo callInfo,
			String requestType, String primaryAccountNum, String processingCode, String amtTransaction,
			String transactionDate, String sysTraceAuditNumber, String localTime,
			String localDate, String expirationDate, String settlementDate,
			String merchantType, String serviceEntryMode, String cardSequenceNum,
			String serviceCondCode, String serviceCaptureCode,
			String institutionIdnefCode, String trackTwoData,
			String cardAcceptorTerminalID, String cardAcceptorIDCode,
			String cardAcceptorName, String pin, String securityControlInfo,
			String mesgReasonCode, String originalDataElement,
			String institutionIDCode, String accountIdentification,
			String posDataCode, String bitMap, String terminalOwner,
			String posGeographicData, String sponsorBank, String addrVerfData,
			String iccData, String originalNode, String mesgAuthCode, String transCurrCode, String structureData, String extendedTransType)
			throws DaoException {
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: DebitCardServiceRequestDAOImpl.getReportLostCardHostRes()");}
		LostStolenCard_HostRes beanResponse = new LostStolenCard_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			DbtCrdSvcResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callDebitCardServiceHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = debitCardServiceRequestService.callDebitCardServiceHost(logger, sessionID, requestType, primaryAccountNum, processingCode, amtTransaction, transactionDate, sysTraceAuditNumber,
					localTime, localDate, expirationDate, settlementDate, merchantType, serviceEntryMode, cardSequenceNum, serviceCondCode, serviceCaptureCode, institutionIdnefCode, 
					trackTwoData, cardAcceptorTerminalID, cardAcceptorIDCode, cardAcceptorName, pin, securityControlInfo, mesgReasonCode, originalDataElement, institutionIDCode, 
					accountIdentification, posDataCode, bitMap, terminalOwner, posGeographicData, sponsorBank, addrVerfData, iccData, originalNode, mesgAuthCode,transCurrCode, structureData, extendedTransType, str_UUI, generateXML, callInfo);
					
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of DebitCardServiceRequest is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			
			beanResponse.setHostResponseCode(code);
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for DebitCardServiceRequest is : "+ws_ResponseHeader.getEsbErrDesc());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### DebitCardService HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_Succ_ErrorCode);
			}
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code of DebitCardServiceRequest for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					Message610Type message610Type = null;
					message610Type = response.getMessage610();
					
					if(!util.isNullOrEmpty(message610Type)){
						
						S1ResMessageType s1ResMessageType = message610Type.getS1ResBaseMessage();
						if(!util.isNullOrEmpty(s1ResMessageType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Processing code is"+s1ResMessageType.getField003());}
							beanResponse.setProcessingCode(s1ResMessageType.getField003()+Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Date Settlement is"+s1ResMessageType.getField015());}
							beanResponse.setDateSettlement(s1ResMessageType.getField015());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Authorization ID code is"+s1ResMessageType.getField038());}
							beanResponse.setAuthorizationIDCode(s1ResMessageType.getField038());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Response code is"+s1ResMessageType.getField039());}
							beanResponse.setResponseCode(s1ResMessageType.getField039());
					
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Bit Map is"+s1ResMessageType.getField1271());}
							beanResponse.setBitMap(s1ResMessageType.getField1271());
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## AddrVerf Result code is"+s1ResMessageType.getField12716());}
							beanResponse.setAddrVerfResult(s1ResMessageType.getField12716());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Message Authentication code is *******");}
							beanResponse.setMessageAuthenticationCode(s1ResMessageType.getField128());
							
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Response code is"+message610Type.getField059());}
						beanResponse.setEchoData(message610Type.getField059());
						
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty messag610Type response object so setting error code as 1");}
						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
					
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty DebitCardServiceRequest service response object so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at DebitCardServiceRequestDAOImpl.getReportLostCardHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at DebitCardServiceRequestDAOImpl.getReportLostCardHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: DebitCardServiceRequestDAOImpl.getReportLostCardHostRes()");}
		return beanResponse;
	}

	@Override
	public ActivateCard_HostRes getDebitCardActivationHostRes(
			CallInfo callInfo, String requestType, String primaryAccountNum, String processingCode, String amtTransaction,
			String transactionDate, String sysTraceAuditNumber, String localTime,
			String localDate, String expirationDate, String settlementDate,
			String merchantType, String serviceEntryMode, String cardSequenceNum,
			String serviceCondCode, String serviceCaptureCode,
			String institutionIdnefCode, String trackTwoData,
			String cardAcceptorTerminalID, String cardAcceptorIDCode,
			String cardAcceptorName, String pin, String securityControlInfo,
			String mesgReasonCode, String originalDataElement,
			String institutionIDCode, String accountIdentification,
			String posDataCode, String bitMap, String terminalOwner,
			String posGeographicData, String sponsorBank, String addrVerfData,
			String iccData, String originalNode, String mesgAuthCode, String transCurrCode, String structureData, String extendedTransType)
			throws DaoException {
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: DebitCardServiceRequestDAOImpl.getDebitCardActivationHostRes()");}
		ActivateCard_HostRes beanResponse = new ActivateCard_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			DbtCrdSvcResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callDebitCardServiceHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = debitCardServiceRequestService.callDebitCardServiceHost(logger, sessionID, requestType, primaryAccountNum, processingCode, amtTransaction, transactionDate, sysTraceAuditNumber,
					localTime, localDate, expirationDate, settlementDate, merchantType, serviceEntryMode, cardSequenceNum, serviceCondCode, serviceCaptureCode, institutionIdnefCode, 
					trackTwoData, cardAcceptorTerminalID, cardAcceptorIDCode, cardAcceptorName, pin, securityControlInfo, mesgReasonCode, originalDataElement, institutionIDCode, 
					accountIdentification, posDataCode, bitMap, terminalOwner, posGeographicData, sponsorBank, addrVerfData, iccData, originalNode, mesgAuthCode, transCurrCode, structureData, extendedTransType, str_UUI, generateXML, callInfo);
					
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of DebitCardServiceRequest is : "+code);}
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for DebitCardServiceRequest is : "+ws_ResponseHeader.getEsbErrDesc());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### DebitCardService HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_DebitCardServiceRequest_Succ_ErrorCode); 
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code of DebitCardServiceRequest for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					Message610Type message610Type = null;
					message610Type = response.getMessage610();
					
					if(!util.isNullOrEmpty(message610Type)){
						
						S1ResMessageType s1ResMessageType = message610Type.getS1ResBaseMessage();
						if(!util.isNullOrEmpty(s1ResMessageType)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Processing code is"+s1ResMessageType.getField003());}
							beanResponse.setProcessingCode(s1ResMessageType.getField003()+Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Date Settlement is"+s1ResMessageType.getField015());}
							beanResponse.setDateSettlement(s1ResMessageType.getField015());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Authorization ID code is"+s1ResMessageType.getField038());}
							beanResponse.setAuthorizationIDCode(s1ResMessageType.getField038());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Response code is"+s1ResMessageType.getField039());}
							beanResponse.setResponseCode(s1ResMessageType.getField039());
					
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Bit Map is"+s1ResMessageType.getField1271());}
							beanResponse.setBitMap(s1ResMessageType.getField1271());
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## AddrVerf Result code is"+s1ResMessageType.getField12716());}
							beanResponse.setAddrVerfResult(s1ResMessageType.getField12716());
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Message Authentication code is *******");}
							beanResponse.setMessageAuthenticationCode(s1ResMessageType.getField128());
							
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Response code is"+message610Type.getField059());}
						beanResponse.setEchoData(message610Type.getField059());
						
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty messag610Type response object so setting error code as 1");}
						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
					
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty DebitCardServiceRequest service response object so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at DebitCardServiceRequestDAOImpl.getDebitCardActivationHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at DebitCardServiceRequestDAOImpl.getDebitCardActivationHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: DebitCardServiceRequestDAOImpl.getDebitCardActivationHostRes()");}
		return beanResponse;
	}

}
