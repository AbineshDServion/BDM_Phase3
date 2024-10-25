package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.beneficiarymanagementservice.BasicTelecomAcctInfo;
import com.bankmuscat.esb.beneficiarymanagementservice.SubscriberMSISDNInfoType;
import com.bankmuscat.esb.beneficiarymanagementservice.TelecomSubscriberInfoResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.TelecomSubscriberInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.TelecomSubcriberInfoBasicDetails;
import com.servion.model.billPayment.TelecomSubscriberInfo_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.TelecomSubscriberInfoService;
import com.servion.ws.util.DAOLayerUtils;

public class TelecomSubscriberInfoDAOImpl implements TelecomSubscriberInfoDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	TelecomSubscriberInfoService telecomSubscriberInfoService;
	
	public TelecomSubscriberInfoService getTelecomSubscriberInfoService() {
		return telecomSubscriberInfoService;
	}

	public void setTelecomSubscriberInfoService(
			TelecomSubscriberInfoService telecomSubscriberInfoService) {
		this.telecomSubscriberInfoService = telecomSubscriberInfoService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public TelecomSubscriberInfo_HostRes getTelecomSubscriberInfo_HostRes(
			CallInfo callInfo, String requestType, String providerType,
			String serviceProviderCode, String MSISDN) throws DaoException{
		// TODO Auto-generated method stub
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: TelecomSubscriberInfoDAOImpl.getTelecomSubscriberInfo_HostRes()");}
		
		TelecomSubscriberInfo_HostRes beanResponse = new TelecomSubscriberInfo_HostRes();

		try{

			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			
			TelecomSubscriberInfoResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method TelecomSubscriberInfo Service host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = telecomSubscriberInfoService.callTelecomSubscriberInfoResType(logger, sessionID, requestType, providerType, serviceProviderCode, MSISDN, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of TelecomSubscriberInfo is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for TelecomSubscriberInfo is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### TelecomSubscriberInfo HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_TelecomSubscriberInfo_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_TelecomSubscriberInfo_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_TelecomSubscriberInfo_Succ_ErrorCode);
			}
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
				
					
					SubscriberMSISDNInfoType subscriberMSISDNInfoType = response.getSubscriberMSISDNInfo();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  SubscriberMSISDNInfoType is "+ subscriberMSISDNInfoType);}
					
					if(!util.isNullOrEmpty(subscriberMSISDNInfoType)){
						
						beanResponse.setCustNam(subscriberMSISDNInfoType.getCustName());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getCustName() is "+ subscriberMSISDNInfoType.getCustName());}
					
						beanResponse.setCustCompanyName(subscriberMSISDNInfoType.getCustCompanyName());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getCustCompanyName() is "+ subscriberMSISDNInfoType.getCustCompanyName());}
					
						beanResponse.setCustPackage(subscriberMSISDNInfoType.getCustPackage());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getCustPackage() is "+ subscriberMSISDNInfoType.getCustPackage());}
					
						beanResponse.setActivationDate(subscriberMSISDNInfoType.getActivationDate() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getActivationDate() is "+ subscriberMSISDNInfoType.getActivationDate());}
					
						beanResponse.setDeactivationDate(subscriberMSISDNInfoType.getDeactivationDate() + Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getDeactivationDate() is "+ subscriberMSISDNInfoType.getDeactivationDate());}
					
						beanResponse.setUbccSts(subscriberMSISDNInfoType.getUbccSts());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getUbccSts() is "+ subscriberMSISDNInfoType.getUbccSts());}
					
						beanResponse.setSingleBillFlg(subscriberMSISDNInfoType.getSingleBillFlg());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getSingleBillFlg() is "+ subscriberMSISDNInfoType.getSingleBillFlg());}
					
						beanResponse.setRoamFlg(subscriberMSISDNInfoType.getRoamFlg());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getRoamFlg() is "+ subscriberMSISDNInfoType.getRoamFlg());}
						
						beanResponse.setIsdFlg(subscriberMSISDNInfoType.getIsdFlg());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  subscriberMSISDNInfoType.getIsdFlg() is "+ subscriberMSISDNInfoType.getIsdFlg());}
						
						List<BasicTelecomAcctInfo> basicTelecomAcctInfoList = subscriberMSISDNInfoType.getBasicTelecomAcctInfo();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  basicTelecomAcctInfoList is "+ basicTelecomAcctInfoList);}
						BasicTelecomAcctInfo basicTelecomAcctInfo = null;
						
						ArrayList<TelecomSubcriberInfoBasicDetails> telecomSubcriberDetailsList = new ArrayList<>();
						TelecomSubcriberInfoBasicDetails telecomSubcriberInfoBasicDetails = null;
						
						if(!util.isNullOrEmpty(basicTelecomAcctInfoList)){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  BasicTelecomAcctInfoList() is "+ basicTelecomAcctInfoList);}
							
							for(int i=0; i<basicTelecomAcctInfoList.size(); i++){
								
								telecomSubcriberInfoBasicDetails = new TelecomSubcriberInfoBasicDetails();
								basicTelecomAcctInfo = basicTelecomAcctInfoList.get(i);
								
								if(!util.isNullOrEmpty(basicTelecomAcctInfo)){
									telecomSubcriberInfoBasicDetails.setAccctType(basicTelecomAcctInfo.getAcctType());
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  basicTelecomAcctInfo.getAcctType() is "+ basicTelecomAcctInfo.getAcctType());}
									
									telecomSubcriberInfoBasicDetails.setAcctNum(basicTelecomAcctInfo.getAcctNum());
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  basicTelecomAcctInfo.getAcctNum() is "+ basicTelecomAcctInfo.getAcctNum());}
									
									telecomSubcriberInfoBasicDetails.setAcctNumSts(basicTelecomAcctInfo.getAcctNumSts());
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  basicTelecomAcctInfo.getAcctNumSts() is "+ basicTelecomAcctInfo.getAcctNumSts());}
									
									telecomSubcriberInfoBasicDetails.setMSISDN(basicTelecomAcctInfo.getMSISDN() + Constants.EMPTY_STRING);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  basicTelecomAcctInfo.getMSISDN() is "+ basicTelecomAcctInfo.getMSISDN());}
									
									telecomSubcriberInfoBasicDetails.setMSISDNStatus(basicTelecomAcctInfo.getMSISDNStatus());
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  basicTelecomAcctInfo.getMSISDNStatus() is "+ basicTelecomAcctInfo.getMSISDNStatus());}

								}
								telecomSubcriberDetailsList.add(telecomSubcriberInfoBasicDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting into the arraylist and the count is "+ telecomSubcriberDetailsList.size());}
							}
						}
						beanResponse.setTelecomSubcriberInfoBasicDetails(telecomSubcriberDetailsList);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the arraylistinto the bean object  "+ telecomSubcriberDetailsList);}
					}
					
					callInfo.setTelecomSubscriberInfo_HostRes(beanResponse);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting into the beanObject into callInfo object ");}
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at TelecomSubscriberInfoDAOImpl.TelecomSubscriberInfo() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TelecomSubscriberInfoDAOImpl.TelecomSubscriberInfo() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: TelecomSubscriberInfoDAOImpl.TelecomSubscriberInfo()");}
		return beanResponse;
	}

}
