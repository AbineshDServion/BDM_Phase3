package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.beneficiarymanagementservice.BeneficiaryRegistrationResType;
import com.bankmuscat.esb.commontypes.BeneAccInfoType;
import com.bankmuscat.esb.commontypes.BeneBankInfoType;
import com.bankmuscat.esb.commontypes.BeneBillPayDtlsType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.BeneficiaryRegstDAO;
import com.servion.exception.DaoException;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.payeeRegistration.BeneficiaryRegistration_HostRes;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.BeneficiaryRegstService;
import com.servion.ws.util.DAOLayerUtils;

public class BeneficiaryRegstDAOImpl implements BeneficiaryRegstDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	BeneficiaryRegstService beneficiaryRegstService;
	
	public BeneficiaryRegstService getBeneficiaryRegstService() {
		return beneficiaryRegstService;
	}


	public void setBeneficiaryRegstService(
			BeneficiaryRegstService beneficiaryRegstService) {
		this.beneficiaryRegstService = beneficiaryRegstService;
	}
	
	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public BeneficiaryRegistration_HostRes getPayeeRegistrationHostRes(
			CallInfo callInfo, String beneficiaryID, String shortDescription,
			String customerId, String customerDebitAcctNumber,
			String channelRequired, String beneficiaryName,
			String beneficiaryAcctType, String beneficiaryAcctNo,
			String beneficiaryMobNo, String paymentType,
			String serviceProviderCode, String utilityCode,
			String creditCardNo, String billNo, String contractNo, int gsmNo,
			int telephoneNo, String studentName, String classSection,
			String bankCode, String bankName, String bankBranch,
			String bankIFSCCode, String bankLocation, String requestType) throws DaoException {
		
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryRegstDAOImpl.getPayeeRegistrationHostRes()");}
		BeneficiaryRegistration_HostRes beanResponse = new BeneficiaryRegistration_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);
			
			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");
			
			BeneficiaryRegistrationResType response = null;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callPayeeRegistrationHost");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			
			response = beneficiaryRegstService.callPayeeRegistrationHost(logger, sessionID, beneficiaryID, shortDescription, customerId, customerDebitAcctNumber, 
					channelRequired, beneficiaryName, beneficiaryAcctType, beneficiaryAcctNo, beneficiaryMobNo, paymentType, serviceProviderCode, utilityCode,
					creditCardNo, billNo, contractNo, gsmNo, telephoneNo, studentName, classSection, bankCode, bankName, bankBranch, bankIFSCCode, bankLocation, requestType, str_UUI, generateXML, callInfo);
					
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);
			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryRegistration Service is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for BeneficiaryRegistration Service is : "+ws_ResponseHeader.getEsbErrDesc());}
			
			beanResponse.setHostResponseCode(code);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryRegst HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficaryRegst_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficaryRegst_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficaryRegst_Succ_ErrorCode);
			}

			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code of BeneficiaryRegistration Service  for Application layer is "+code);}
			
			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					BeneAccInfoType beneAccInfoType = response.getBeneAccInfo();
					if(!util.isNullOrEmpty(beneAccInfoType)){
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Beneficiary ID is"+beneAccInfoType.getId());}
						beanResponse.setId(beneAccInfoType.getId());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Shor Desc is"+beneAccInfoType.getShortDescription());}
						beanResponse.setShortDesc(beneAccInfoType.getShortDescription());
						if(!util.isNullOrEmpty(beneAccInfoType.getCustomerId()))
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Customer ID ending with"+util.getSubstring(beneAccInfoType.getCustomerId(),Constants.GL_FOUR));}
						beanResponse.setCustomerId(beneAccInfoType.getCustomerId());
						if(!util.isNullOrEmpty(beneAccInfoType.getCustomerDrAccount()))
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Customer DR Acct number ending with"+util.getSubstring(beneAccInfoType.getCustomerDrAccount(),Constants.GL_FOUR));}
						beanResponse.setCustomerDrAcct(beneAccInfoType.getCustomerDrAccount());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Name is"+beneAccInfoType.getBeneficiaryName());}
						beanResponse.setBenefName(beneAccInfoType.getBeneficiaryName());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Account Type is"+beneAccInfoType.getBeneficiaryAccountType());}
						beanResponse.setBenefAcctType(beneAccInfoType.getBeneficiaryAccountType());
						if(!util.isNullOrEmpty(beneAccInfoType.getBeneficiaryAccountNumber()))
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef account Number ending wth "+util.getSubstring(beneAccInfoType.getBeneficiaryAccountNumber(),Constants.GL_FOUR));}
						beanResponse.setBenefAcctNumber(beneAccInfoType.getBeneficiaryAccountNumber());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Mobile Number is"+beneAccInfoType.getBeneficiaryMobileNumber());}
						beanResponse.setBenefMobNumber(beneAccInfoType.getBeneficiaryMobileNumber()); 	
					}
					
					BeneBillPayDtlsType beneBillPayDtlsType = response.getBeneBillPayDtls();
					if(!util.isNullOrEmpty(beneBillPayDtlsType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Payment type is"+beneBillPayDtlsType.getPaymentType());}
						beanResponse.setPaymentType(beneBillPayDtlsType.getPaymentType());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Service Provider Code is"+beneBillPayDtlsType.getServiceProviderCode());}
						beanResponse.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Utility Code is"+beneBillPayDtlsType.getUtilityCode());}
						beanResponse.setUtilityCode(beneBillPayDtlsType.getUtilityCode());
						if(!util.isNullOrEmpty(beneBillPayDtlsType.getCreditCardNumber()))
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef CC Number ending with is"+util.getSubstring(beneBillPayDtlsType.getCreditCardNumber(),Constants.GL_FOUR));}
						beanResponse.setRegisteredCCNumber(beneBillPayDtlsType.getCreditCardNumber());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Bill Number is"+beneBillPayDtlsType.getBillNumber());}
						beanResponse.setBillNumber(beneBillPayDtlsType.getBillNumber());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Contract Number is"+beneBillPayDtlsType.getContractNumber());}
						beanResponse.setContractNumber(beneBillPayDtlsType.getContractNumber());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef GSM Number is"+beneBillPayDtlsType.getGsmNumber());}
						beanResponse.setGsmNumber(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Telephone Number is"+beneBillPayDtlsType.getTelephoneNumber());}
						beanResponse.setTelephoneNumber(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY_STRING);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Student Name is"+beneBillPayDtlsType.getStudentName());}
						beanResponse.setStudentName(beneBillPayDtlsType.getStudentName());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Class Section is"+beneBillPayDtlsType.getClassSection());}
						beanResponse.setClassSection(beneBillPayDtlsType.getClassSection());
					}
					
					BeneBankInfoType beneBankInfoType = response.getBeneBankInfo();
					if(!util.isNullOrEmpty(beneBankInfoType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Bnak Code is"+beneBankInfoType.getBankCode());}
						//beneBankInfoType.setBankCode(beneBankInfoType.getBankCode());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Bank Branch is"+beneBankInfoType.getBankBranch());}
						beneBankInfoType.setBankBranch(beneBankInfoType.getBankBranch());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Bank IFSC Code is"+beneBankInfoType.getBankIFSCCode());}
						beneBankInfoType.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Bank Location is"+beneBankInfoType.getBankLocation());}
						beneBankInfoType.setBankLocation(beneBankInfoType.getBankLocation());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## RESPONSE FIELD ## Benef Bank Name is"+beneBankInfoType.getBankName());}
						beneBankInfoType.setBankName(beneBankInfoType.getBankName());
						
					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty message810Type response object so setting error code as 1");}

						beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
					}
					
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty KeyExAuthResType response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryRegstDAOImpl.getPayeeRegistrationHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryRegstDAOImpl.getPayeeRegistrationHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryRegstDAOImpl.getPayeeRegistrationHostRes()");}
		return beanResponse;
	}

}
