package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.paymentmanagementservice.GetUtilityBillInfoResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.GetUtilityBillInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.GetUtilityBillInfo_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.GetUtilityBillInfoService;
import com.servion.ws.util.DAOLayerUtils;

public class GetUtilityBillInfoDAOImpl implements GetUtilityBillInfoDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	GetUtilityBillInfoService getUtilityBillInfoService;
	
	public GetUtilityBillInfoService getGetUtilityBillInfoService() {
		return getUtilityBillInfoService;
	}


	public void setGetUtilityBillInfoService(
			GetUtilityBillInfoService getUtilityBillInfoService) {
		this.getUtilityBillInfoService = getUtilityBillInfoService;
	}


	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public GetUtilityBillInfo_HostRes getGetUtilityBillInfo_HostRes(
			CallInfo callInfo, String requestType, String providerType,
			String serviceProviderCode, String utilityCode, String contractNo) throws DaoException{
		// TODO Auto-generated method stub
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: GetUtilityBillInfoDAOImpl.getGetUtilityBillInfo_HostRes()");}
		
		GetUtilityBillInfo_HostRes beanResponse = new GetUtilityBillInfo_HostRes();

		try{

			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			
			GetUtilityBillInfoResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method GetUtilityBillInfo Service host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = getUtilityBillInfoService.callGetUtilityBillInfoResType(logger, sessionID, requestType, providerType, serviceProviderCode, utilityCode, contractNo, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of GetUtilityBillInfo is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for GetUtilityBillInfo is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### GetUtilityBillInfo HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_GetUtilityBillInfo_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_GetUtilityBillInfo_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_GetUtilityBillInfo_Succ_ErrorCode);
			}
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					String recCount = response.getRecCount();
					if(!util.isNullOrEmpty(recCount)){
						try{
						if(Integer.parseInt(recCount)> 0){
							beanResponse.setRecCount(Integer.parseInt(recCount));
						}
						}catch(NumberFormatException e){
							WriteLog.write(WriteLog.ERROR, sessionID, "Invalid recCount value =="+recCount);
						}
					}
					
					String inqRefNo = response.getInqRefNo();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  response.getInqRefNo() is "+ inqRefNo);}
					if(!util.isNullOrEmpty(inqRefNo)){
						beanResponse.setInqRefNo(inqRefNo);
					}
					
					for(int x=0;x<response.getUtilityInfo().size(); x++){
						beanResponse.setPartialPayFlag(response.getUtilityInfo().get(x).getBillAddnlDtls().isPartialPayFlag());
						for(int y=0;y<response.getUtilityInfo().get(x).getAcctResInfo().size(); y++){
							if(contractNo.equalsIgnoreCase(response.getUtilityInfo().get(x).getAcctResInfo().get(y).getAcctNo())){
								beanResponse.setDueBalance(response.getUtilityInfo().get(x).getBillDueInfo().get(y).getTotalAmt());
								beanResponse.setBillNo(response.getUtilityInfo().get(x).getAcctResInfo().get(y).getBillNo());
								beanResponse.setBillStatus(response.getUtilityInfo().get(x).getBillAddnlDtls().getBillStatus());
							}
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  response.getUtilityInfo().get(x).getBillDueInfo().get(y).getTotalAmt() is "+ beanResponse.getDueBalance());}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  response.getUtilityInfo().get(x).getBillDueInfo().get(y).getBillNo() is "+ beanResponse.getBillNo());}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  response.getUtilityInfo().get(x).getBillAddnlDtls().getBillStatus() is "+ beanResponse.getBillStatus());}
					
					callInfo.setGetUtilityBillInfo_HostRes(beanResponse);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting into the beanObject into callInfo object ");}
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at GetUtilityBillInfoDAOImpl.getUtilityBillInfo() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GetUtilityBillInfoDAOImpl.getUtilityBillInfo() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: GetUtilityBillInfoDAOImpl.GetUtilityBillInfo()");}
		return beanResponse;
	}

}
