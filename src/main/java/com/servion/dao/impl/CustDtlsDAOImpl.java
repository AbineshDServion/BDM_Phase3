package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.customermgmtservice.CustDtlsResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CustDtlsDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.CustDtls.CustDtls_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.CustDtlsService;
import com.servion.ws.util.DAOLayerUtils;

public class CustDtlsDAOImpl implements CustDtlsDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	CustDtlsService custDtls;
	//CustDtlsService custDtlsService;
	
		
	  public CustDtlsService getCustDtls() { return custDtls; }
	  
	  public void setCustDtls(CustDtlsService custDtls) { this.custDtls = custDtls;
	  }
	 
	

	public WS_ResponseHeader getWs_ResponseHeader() {
		return ws_ResponseHeader;
	}

	/*
	 * public CustDtlsService getCustDtlsService() { return custDtlsService; }
	 * 
	 * public void setCustDtlsService(CustDtlsService custDtlsService) {
	 * this.custDtlsService = custDtlsService; }
	 */

	public void setWs_ResponseHeader(WS_ResponseHeader ws_ResponseHeader) {
		this.ws_ResponseHeader = ws_ResponseHeader;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public CustDtls_HostRes getCustDtlsHostRes(CallInfo callInfo, String cardNo, String customerId, String requestType) throws DaoException {
		

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: FetchCardServiceHistoryDAOImpl.getFetchCardServiceHistoryHostRes()");}
		
		CustDtls_HostRes beanResponse = new CustDtls_HostRes();
		//Initialized to fetch Variables from LegalDocumentDetailsType host 
				String bmLgDocName;
				String legalDocId;
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Into getCustDtlsHostRes CardNo-"+cardNo+"customer Id-"+customerId+"Request Type-"+requestType);}
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CustDtlsResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCustDtlsHost host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			if (custDtls != null) {
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Cust Dtls object is not null");}
			} else {
			    logger.error("custDtls object is null");
			    
			}
			if(getCustDtls() !=null)
			{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "getCustDtls object is not null");}
			}
			 else {
				    logger.error("getCustDtls() object is null");
				    
				}
				/*
				 * CustDtlsServiceImpl custDtlsObj=new CustDtlsServiceImpl(); if(custDtlsObj
				 * !=null) {
				 * if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,
				 * "custDtlsObj is not null-"+custDtlsObj);} }
				 * 
				 * response = custDtlsObj.callCustDtlsHost(logger, sessionID, cardNo,
				 * customerId, requestType, str_UUI, generateXML, callInfo);
				 */
			response = getCustDtls().callCustDtlsHost(logger, sessionID, cardNo,
					 customerId, requestType, str_UUI, generateXML, callInfo);
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCustDtlsHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callCustDtlsHost is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCGrpInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CustDtls_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CustDtls_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CustDtls_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response) && 
						!util.isNullOrEmpty(response.getCustInfo()) && response.getCustInfo().size() > 0 
						&& !util.isNullOrEmpty(response.getCustInfo().get(0).getPersonInfo()) && response.getCustInfo().get(0).getPersonInfo().size() > 0
						){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "SMS Country Code :"+response.getCustInfo().get(0).getPersonInfo().get(0).getSMSCountryCode());}
					beanResponse.setIsdCode(response.getCustInfo().get(0).getPersonInfo().get(0).getSMSCountryCode());
					if (response != null && response.getLglDocDtls() != null && !response.getLglDocDtls().isEmpty()) {
				        bmLgDocName = response.getLglDocDtls().get(0).getBMLglDocName();
				        legalDocId = response.getLglDocDtls().get(0).getLegalDocId();
				        if (logger.isDebugEnabled()) {
				        	 WriteLog.write(WriteLog.DEBUG, sessionID, "--Cust Dtls Host Response--");
				            WriteLog.write(WriteLog.DEBUG, sessionID, "legal DocId="+legalDocId+" bmLgDocName="+bmLgDocName);
				        }
				        
				        // Additional null checks for individual fields
				        if (bmLgDocName == null) {
				        	  if (logger.isDebugEnabled()) {
						            WriteLog.write(WriteLog.DEBUG, sessionID, "As bmLgDocName is null");
						        }
				            bmLgDocName = "NID";
				        }
				        if (legalDocId == null) {
				        	 if (logger.isDebugEnabled()) {
						            WriteLog.write(WriteLog.DEBUG, sessionID, "As legalDocId is null");
						        }
				            legalDocId = "86141351";
				        }
				    } else {
				        if (logger.isDebugEnabled()) {
				            WriteLog.write(WriteLog.DEBUG, sessionID, "Response or LglDocDtls is null or empty. Using default values.");
				        }
				        bmLgDocName="NID";
				        legalDocId="86141351";
				        
				    }
					if (logger.isDebugEnabled()) {
			            WriteLog.write(WriteLog.DEBUG, sessionID, "Setting LegalDocId and bmLgDocName values to BeanResponse");
			        }
					beanResponse.setLegalDocId(legalDocId);
					beanResponse.setBmLgDocName(bmLgDocName);
					

				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CustDtls Response field Received null / empty so setting error code as 1");}
					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CustDtls Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CustDtlsDAO.getCustDtlsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CustDtlsDAO.getCustDtlsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: FetchCardServiceHistoryDAOImpl.getFetchCardServiceHistoryHostRes()");}
		return beanResponse;
	}

}
