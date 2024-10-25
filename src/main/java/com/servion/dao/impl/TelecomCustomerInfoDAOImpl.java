package com.servion.dao.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.customermgmtservice.AttributeIdType;
import com.bankmuscat.esb.customermgmtservice.TelecomCustomerInfoResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.TelecomCustomerInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.TelecomCustomerInfo_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.TelecomCustomerInfoDetailsService;
import com.servion.ws.util.DAOLayerUtils;

//public class TelecomCustomerInfoDAOImpl{
public class TelecomCustomerInfoDAOImpl implements TelecomCustomerInfoDAO{
	private static Logger logger = LoggerObject.getLogger();

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

	//TODO - CR
	@Autowired
	TelecomCustomerInfoDetailsService telecomCustomerInfoDetailsService;

	public TelecomCustomerInfoDetailsService getTelecomCustomerInfoDetailsService() {
		return telecomCustomerInfoDetailsService;
	}

	public void setTelecomCustomerInfoDetailsService(
			TelecomCustomerInfoDetailsService telecomCustomerInfoDetailsService) {
		this.telecomCustomerInfoDetailsService = telecomCustomerInfoDetailsService;
	}


	@Override
	public TelecomCustomerInfo_HostRes getTelecomCustomerInfo_HostRes(CallInfo callInfo,String requestType, String providerType, 
			String serviceProviderCode, String subscriberNumber, String contractNumber, String subscriberType) throws DaoException{
		// TODO Auto-generated method stub
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: TelecomPostpaidBalanceDetailsDAOImpl.getTelecomPostpaidBalanceDetails_HostRes()");}
		
		TelecomCustomerInfo_HostRes beanResponse = new TelecomCustomerInfo_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
		
			TelecomCustomerInfoResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callTelecomCustomerInfoResType host");}
			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = telecomCustomerInfoDetailsService.callTelecomCustomerInfoResType(logger, sessionID, requestType, providerType, serviceProviderCode, subscriberNumber, contractNumber, subscriberType, str_UUI, generateXML, callInfo);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callTelecomCustomerInfoResType is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getTelecomCustomerInfo_HostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### TelecomCustomerInfo HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_TelecomCustomerInfoDetails_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_TelecomCustomerInfoDetails_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_TelecomCustomerInfoDetails_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					if(response.getReply()!=null){
						if(response.getReply().getCustomerInfo()!=null){
							if(response.getReply().getCustomerInfo().getCustomerIDType()!=null){
								
								List<AttributeIdType> customerIDTypeList = response.getReply().getCustomerInfo().getCustomerIDType();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Customer ID Type List is "+customerIDTypeList.size());}
								
								if(customerIDTypeList.size() != 0){
									
									 AttributeIdType  attributeIdType = null;
									 String fieldType = Constants.EMPTY;
									for(int i=0; i < customerIDTypeList.size(); i++){
									
										attributeIdType = customerIDTypeList.get(i);
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Attribute type is "+attributeIdType.getFieldName());}
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Attribute Value is "+attributeIdType.getFieldName());}
										
										
										fieldType = attributeIdType.getFieldName();
										switch(fieldType){
										
										case Constants.TelecomCustomerInfo_AcctCategory:
											beanResponse.setAcctCategory(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_ColIndicator:
											beanResponse.setColIndicator(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustActiveDate:
											beanResponse.setCustActiveDate(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustAddr1:
											beanResponse.setCustAddr1(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustAddr2:
											beanResponse.setCustAddr2(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustAddr3:
											beanResponse.setCustAddr3(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustCity:
											beanResponse.setCustCity(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustCompany:
											beanResponse.setCustCompany(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustGender:
											beanResponse.setCustGender(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustId:
											beanResponse.setCustId(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustName:
											beanResponse.setCustName(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_CustNationality:
											beanResponse.setCustNationality(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_HvcFlag:
											beanResponse.setHvcFlag(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										case Constants.TelecomCustomerInfo_LanguageCode:
											beanResponse.setLanguageCode(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
//										case Constants.TelecomCustomerInfo_SiList:
//											beanResponse.setAcctCategory(attributeIdType.getFieldValue());
//											break;
											
										case Constants.TelecomCustomerInfo_TotalDues:
											beanResponse.setTotalDues(attributeIdType.getFieldValue() + Constants.EMPTY);
											break;
											
										default:
											break;
										}
										
									}
								}
							}
						}
					}
					
				}
			}
			
			callInfo.setTelecomCustomerInfo_HostRes(beanResponse);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the bean response object into the call info object"+ callInfo.getTelecomCustomerInfo_HostRes() );}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at TelecomCustomerInfo_HostRes.getTelecomCustomerInfo_HostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at TelecomCustomerInfo_HostRes.getTelecomCustomerInfo_HostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: TelecomPostpaidBalanceDetails.getTelecomPostpaidBalanceDetails_HostRes()");}
		return beanResponse;
		
	}

}
