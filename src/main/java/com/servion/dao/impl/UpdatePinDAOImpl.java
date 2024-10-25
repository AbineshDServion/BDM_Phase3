package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.Message610Type;
import com.bankmuscat.esb.cardmanagementservice.UpdatePINResType;
import com.bankmuscat.esb.commontypes.S1ResMessageType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.UpdatePinDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.updatePin.UpdatePIN_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.UpdatePinService;
import com.servion.ws.util.DAOLayerUtils;

public class UpdatePinDAOImpl implements UpdatePinDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	UpdatePinService updatePinService;
	
	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

	
	public UpdatePinService getUpdatePinService() {
		return updatePinService;
	}

	public void setUpdatePinService(
			UpdatePinService updatePinService) {
		this.updatePinService = updatePinService;
	}


	@Override
	public UpdatePIN_HostRes getUpdatePINHostRes(
			CallInfo callInfo,
			String pan,
			String processingCode,
			String transmissionDate,
			String systemTraceAudit,
			String localTransactionTime,
			String localTransactionDate,
			String expirationDate,
			String pointOfServiceEntryMode,
			String cardSeqNum,
			String pointOfserviceConditionCode,
			String pointOfServiceCaptureCode,
			String cardAccpTerminalID,
			String cardAccpIDCode,
			String cardAccpName,
			String pin,
			String recvInstitutionID,
			String posDataCode,
			String structureData, String extendedTransType, String requestType
			) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: UpdatePinDAOImpl.getUpdatePINHostRes()");}
		
		UpdatePIN_HostRes beanResponse = new UpdatePIN_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			UpdatePINResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callDebitCardDetailsHost");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "UpdatePinDAOImpl updatePinService: "+updatePinService);}
			
			response = updatePinService.callUpdatePinHost(logger, sessionID, pan, processingCode, transmissionDate, systemTraceAudit, 
					localTransactionTime, localTransactionDate, expirationDate, pointOfServiceEntryMode, cardSeqNum, pointOfserviceConditionCode, 
					pointOfServiceCaptureCode, cardAccpTerminalID, cardAccpIDCode, cardAccpName, pin, recvInstitutionID, posDataCode, structureData, 
					extendedTransType, requestType, str_UUI, generateXML, callInfo);
			 
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of UpdatePinService is : "+code);}
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
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_UpdatePIN_Succ_ErrorCode);
			}
			
			//Setting host response code
			beanResponse.setHostResponseCode(code);
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for UpdatePinService is : "+ws_ResponseHeader.getEsbErrDesc());}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### UpdatePin HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code of UpdatePin for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					Message610Type message610Type = null;
					message610Type = response.getMessage610();
					
					
					if(!util.isNullOrEmpty(message610Type)){
						
						S1ResMessageType s1ResMessageType = message610Type.getS1ResBaseMessage();
						
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
						/*if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Data Private value is "+s1ResMessageType.getField048());}
						beanResponse.setAdditionalDataPrivate(s1ResMessageType.getField048());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Amount value is "+s1ResMessageType.getField054());}
						beanResponse.setAdditionAmount(s1ResMessageType.getField054());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Settlement  value is "+s1ResMessageType.getField005());}
						beanResponse.setAmountSettlement(s1ResMessageType.getField005()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Additional Settlement fee value is "+s1ResMessageType.getField029());}
						beanResponse.setAmountSettlementFee(s1ResMessageType.getField029()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Settlement Processing Fee value is "+s1ResMessageType.getField031());}
						beanResponse.setAmountSettlementProcessFee(s1ResMessageType.getField031()+Constants.EMPTY);
						*/
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Transaction Fee value is "+s1ResMessageType.getField028());}
						beanResponse.setAmountTransactionFee(s1ResMessageType.getField028()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Amount Transaction  Processing fee value is "+s1ResMessageType.getField030());}
						beanResponse.setAmountTransactionProcessFee(s1ResMessageType.getField030()+Constants.EMPTY);
						
						/*if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Auth Agent ID value is "+s1ResMessageType.getField058());}
						beanResponse.setAuthAgentIDCode(s1ResMessageType.getField058());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Conversion Rate Settlement value is "+s1ResMessageType.getField038());}
						beanResponse.setConversionRateSettlement(s1ResMessageType.getField009()+Constants.EMPTY);
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Curr Code Settlement value is "+s1ResMessageType.getField050());}
						beanResponse.setCurrCodeSettlement(s1ResMessageType.getField050());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Date Conversion  value is "+s1ResMessageType.getField016());}
						beanResponse.setDateConversion(s1ResMessageType.getField016());
						*/
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Response code value is "+s1ResMessageType.getField039());}
						beanResponse.setResponseCode(s1ResMessageType.getField039()+Constants.EMPTY);
						
						
						String actualSucccesssResponseCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UI_UpdatePIN_Field39SuccValue);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "UI Configured Host response value for UpdatePIN is "+actualSucccesssResponseCode);}
						
						if(!util.isNullOrEmpty(actualSucccesssResponseCode) && util.isCodePresentInTheConfigurationList(beanResponse.getResponseCode(), actualSucccesssResponseCode)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Got success response code for UpdatePIN");}
							beanResponse.setHostResponseCode(beanResponse.getResponseCode());
						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Overriding the IVR response code as 1");}
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The response code value for IVR is " + beanResponse.getResponseCode());}
							code = Constants.WS_FAILURE_CODE;
							beanResponse.setHostResponseCode(beanResponse.getResponseCode());
							beanResponse.setErrorCode(code);
						}

						
						
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty message610Type response object so setting error code as 1");}
						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty response object so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at UpdatePinDAOImpl.getUpdatePINHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UpdatePinDAOImpl.getUpdatePINHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: UpdatePinDAOImpl.getUpdatePINHostRes()");}
		return beanResponse;
	}

}
