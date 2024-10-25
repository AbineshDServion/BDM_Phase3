package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.beneficiarymanagementservice.BeneficiaryDetailsEnquiryResType;
import com.bankmuscat.esb.commontypes.BeneAccInfoType;
import com.bankmuscat.esb.commontypes.BeneBankInfoType;
import com.bankmuscat.esb.commontypes.BeneBillPayDtlsType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.Utility_BeneficiaryDetails;
import com.servion.model.billPayment.Utility_BenfPayeeDetails_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BenefDetails;
import com.servion.model.creditCardPayment.CCPayment_BenfPayeeDetails_HostRes;
import com.servion.model.fundsTransfer.FT_BeneficiaryDetails;
import com.servion.model.fundsTransfer.FT_BenfPayeeDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_BeneficiaryDetails;
import com.servion.model.thirdPartyRemittance.TPR_BenfPayeeDetails_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.BeneficiaryDtlsInquiryService;
import com.servion.ws.util.DAOLayerUtils;

public class BeneficiaryDtlsInquiryDAOImpl implements BeneficiaryDtlsInquiryDAO{
	private static Logger logger = LoggerObject.getLogger();
	
	@Autowired
	BeneficiaryDtlsInquiryService beneficiaryDtlsInquiryService;


	public BeneficiaryDtlsInquiryService getBeneficiaryDtlsInquiryService() {
		return beneficiaryDtlsInquiryService;
	}

	public void setBeneficiaryDtlsInquiryService(
			BeneficiaryDtlsInquiryService beneficiaryDtlsInquiryService) {
		this.beneficiaryDtlsInquiryService = beneficiaryDtlsInquiryService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public FT_BenfPayeeDetails_HostRes getFTTWBMBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes()");}

		FT_BenfPayeeDetails_HostRes beanResponse = new FT_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
				
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}

				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryDetailsMap = null;
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryCharityDetailsMap = null;
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryAcctDetailsMap = null;
				ArrayList<String> charityCodeList = null;


				FT_BeneficiaryDetails ft_BeneficiaryDetails = null;

				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;
				
				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);

					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END
					
					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}
					WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
					
					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					
					fT_BeneficiaryDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;
					fT_BeneficiaryCharityDetailsMap = beanResponse.getFT_BeneficiaryCharityDetailsMap() ;
					fT_BeneficiaryAcctDetailsMap = beanResponse.getFT_BeneficiaryAcctDetailsMap() ;
					charityCodeList = beanResponse.getCharityCodeList();


					if(util.isNullOrEmpty(fT_BeneficiaryDetailsMap)){
						fT_BeneficiaryDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(fT_BeneficiaryCharityDetailsMap)){
						fT_BeneficiaryCharityDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(fT_BeneficiaryAcctDetailsMap)){
						fT_BeneficiaryAcctDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(charityCodeList)){
						charityCodeList = new ArrayList<String>();
					}


					/*fT_BeneficiaryDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;
					fT_BeneficiaryCharityDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;
					fT_BeneficiaryAcctDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;*/

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							ft_BeneficiaryDetails = new FT_BeneficiaryDetails();
							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID"+benefID);}
							ft_BeneficiaryDetails.setBeneficiaryID(benefID);
							
							if(!util.isNullOrEmpty(beneAccInfoType)){
								/*if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								ft_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());*/

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								ft_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								ft_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								ft_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								ft_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								ft_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								ft_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								charityCodeList.add(beneBillPayDtlsType.getUtilityCode());
								ft_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								ft_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								ft_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								ft_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								ft_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								ft_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								ft_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								ft_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//Issue: CCPayment Failure CTI-CRM Changed below due to new WSDL specification from 10.6.4 to 20.9 on 27June2018
								//ft_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								ft_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								ft_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());

							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+ft_BeneficiaryDetails);}

							fT_BeneficiaryDetailsMap.put(benefID, ft_BeneficiaryDetails);
							fT_BeneficiaryAcctDetailsMap.put(beneBillPayDtlsType.getBeneficaryAccountNumber(), ft_BeneficiaryDetails);
							fT_BeneficiaryCharityDetailsMap.put(beneBillPayDtlsType.getUtilityCode(), ft_BeneficiaryDetails);

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}

							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary detail map size is "+fT_BeneficiaryDetailsMap.size());}
						beanResponse.setFT_BeneficiaryDetailsMap(fT_BeneficiaryDetailsMap);
						beanResponse.setFT_BeneficiaryAcctDetailsMap(fT_BeneficiaryAcctDetailsMap);
						beanResponse.setFT_BeneficiaryCharityDetailsMap(fT_BeneficiaryCharityDetailsMap);
						beanResponse.setCharityCodeList(charityCodeList);
					}

				}

			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public FT_BenfPayeeDetails_HostRes getFTTOBMBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType) throws DaoException  {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "ENTER: BeneficiaryDtlsInquiryDAOImpl.getFTTOBMBenfDelsHostRes()");}

		FT_BenfPayeeDetails_HostRes beanResponse = new FT_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
//				String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);

				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}


				FT_BeneficiaryDetails ft_BeneficiaryDetails = null;
				ArrayList<String> charityCodeList = null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;

				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryDetailsMap = null;
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryCharityDetailsMap = null ;
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryAcctDetailsMap = null;

				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);
					
					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END

					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI,generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}

					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					fT_BeneficiaryDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;
					fT_BeneficiaryCharityDetailsMap = beanResponse.getFT_BeneficiaryCharityDetailsMap() ;
					fT_BeneficiaryAcctDetailsMap = beanResponse.getFT_BeneficiaryAcctDetailsMap() ;
					charityCodeList = beanResponse.getCharityCodeList();


					if(util.isNullOrEmpty(fT_BeneficiaryDetailsMap)){
						fT_BeneficiaryDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(fT_BeneficiaryCharityDetailsMap)){
						fT_BeneficiaryCharityDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(fT_BeneficiaryAcctDetailsMap)){
						fT_BeneficiaryAcctDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(charityCodeList)){
						charityCodeList = new ArrayList<String>();
					}


					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							ft_BeneficiaryDetails = new FT_BeneficiaryDetails();


							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID"+benefID);}
							ft_BeneficiaryDetails.setBeneficiaryID(benefID);

							if(!util.isNullOrEmpty(beneAccInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								ft_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								ft_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								ft_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								ft_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								ft_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								ft_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								ft_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								charityCodeList.add(beneBillPayDtlsType.getUtilityCode());
								ft_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								ft_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								ft_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								ft_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								ft_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								ft_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								ft_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								ft_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//ft_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								ft_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								ft_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());

							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+ft_BeneficiaryDetails);}

							fT_BeneficiaryDetailsMap.put(benefID, ft_BeneficiaryDetails);
							fT_BeneficiaryAcctDetailsMap.put(beneBillPayDtlsType.getBeneficaryAccountNumber(), ft_BeneficiaryDetails);
							fT_BeneficiaryCharityDetailsMap.put(beneBillPayDtlsType.getUtilityCode(), ft_BeneficiaryDetails);
						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}

							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}

						beanResponse.setFT_BeneficiaryDetailsMap(fT_BeneficiaryDetailsMap);
						beanResponse.setFT_BeneficiaryCharityDetailsMap(fT_BeneficiaryCharityDetailsMap);
						beanResponse.setFT_BeneficiaryAcctDetailsMap(fT_BeneficiaryAcctDetailsMap);
						beanResponse.setCharityCodeList(charityCodeList);
					}


				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public FT_BenfPayeeDetails_HostRes getFTCharityBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes()");}

		FT_BenfPayeeDetails_HostRes beanResponse = new FT_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
				
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				
				
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryDetailsMap = null;
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryCharityDetailsMap = null ;
				HashMap<String, FT_BeneficiaryDetails>fT_BeneficiaryAcctDetailsMap = null;


				FT_BeneficiaryDetails ft_BeneficiaryDetails = null;
				ArrayList<String> charityCodeList = null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;

				
				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);

					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END
					
					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}

					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					fT_BeneficiaryDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap();
					fT_BeneficiaryCharityDetailsMap = beanResponse.getFT_BeneficiaryCharityDetailsMap();
					fT_BeneficiaryAcctDetailsMap = beanResponse.getFT_BeneficiaryAcctDetailsMap();
					charityCodeList = beanResponse.getCharityCodeList();


					if(util.isNullOrEmpty(fT_BeneficiaryDetailsMap)){
						fT_BeneficiaryDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(fT_BeneficiaryCharityDetailsMap)){
						fT_BeneficiaryCharityDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(fT_BeneficiaryAcctDetailsMap)){
						fT_BeneficiaryAcctDetailsMap = new HashMap<String, FT_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(charityCodeList)){
						charityCodeList = new ArrayList<String>();
					}

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							ft_BeneficiaryDetails = new FT_BeneficiaryDetails();
							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID"+benefID);}
							ft_BeneficiaryDetails.setBeneficiaryID(benefID);

							if(!util.isNullOrEmpty(beneAccInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								ft_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								ft_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								ft_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								ft_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								ft_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								ft_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								ft_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								charityCodeList.add(beneBillPayDtlsType.getUtilityCode());
								ft_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								ft_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								ft_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								ft_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								ft_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								ft_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								ft_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								ft_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//ft_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								ft_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								ft_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());

							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+ft_BeneficiaryDetails);}

							fT_BeneficiaryDetailsMap.put(benefID, ft_BeneficiaryDetails);
							fT_BeneficiaryAcctDetailsMap.put(beneBillPayDtlsType.getBeneficaryAccountNumber(), ft_BeneficiaryDetails);
							fT_BeneficiaryCharityDetailsMap.put(beneBillPayDtlsType.getUtilityCode(), ft_BeneficiaryDetails);
							beanResponse.setCharityCodeList(charityCodeList);
						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}

							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary Detail map size is "+fT_BeneficiaryDetailsMap.size());}

						beanResponse.setFT_BeneficiaryDetailsMap(fT_BeneficiaryDetailsMap);
						beanResponse.setFT_BeneficiaryAcctDetailsMap(fT_BeneficiaryAcctDetailsMap);
						beanResponse.setFT_BeneficiaryCharityDetailsMap(fT_BeneficiaryCharityDetailsMap);
					}
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getFTTWBMBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public CCPayment_BenfPayeeDetails_HostRes getCCPaymentTPWBMBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getCCPaymentTPWBMBenfDelsHostRes()");}

		CCPayment_BenfPayeeDetails_HostRes beanResponse = new CCPayment_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
				
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				
				
				
				HashMap<String, CCPayment_BenefDetails>ccPayment_BeneficiaryDetailsMap = null;
				HashMap<String, ArrayList<CCPayment_BenefDetails>>ccPayment_ServiceProviderMap = null;
				HashMap<String, ArrayList<CCPayment_BenefDetails>>ccPayment_UtilityCodeMap = null;
				ArrayList<CCPayment_BenefDetails> tempArrayList = null;


				CCPayment_BenefDetails ccPayment_BenefDetails =null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;
				ArrayList<String> utilityCodeList = new ArrayList<String>();
				
				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);

					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END
					
					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}

					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
					
					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					ccPayment_BeneficiaryDetailsMap = beanResponse.getCCPayment_BenefDetailsMap() ;
					ccPayment_ServiceProviderMap = beanResponse.getCCPayment_serviceProviderMap();
					ccPayment_UtilityCodeMap = beanResponse.getCCPayment_utilityCodeMap();

					utilityCodeList = beanResponse.getUtilityCodeList();
					if(util.isNullOrEmpty(utilityCodeList)){
						utilityCodeList = new ArrayList<String>();
					}


					if(util.isNullOrEmpty(ccPayment_BeneficiaryDetailsMap)){
						ccPayment_BeneficiaryDetailsMap = new HashMap<String, CCPayment_BenefDetails>();
					}
					if(util.isNullOrEmpty(ccPayment_ServiceProviderMap)){
						ccPayment_ServiceProviderMap = new HashMap<String, ArrayList<CCPayment_BenefDetails>>();
					}

					if(util.isNullOrEmpty(ccPayment_UtilityCodeMap)){
						ccPayment_UtilityCodeMap = new HashMap<String, ArrayList<CCPayment_BenefDetails>>();
					}

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							ccPayment_BenefDetails = new CCPayment_BenefDetails();

							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							ccPayment_BenefDetails.setBeneficiaryID(benefID);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the beneficiary id into the utility_beneficiaryDtsl list"+benefID);}
							if(!util.isNullOrEmpty(beneAccInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								ccPayment_BenefDetails.setCustomerID(beneAccInfoType.getCustomerId());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								ccPayment_BenefDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								ccPayment_BenefDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								ccPayment_BenefDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								ccPayment_BenefDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								ccPayment_BenefDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								ccPayment_BenefDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								utilityCodeList.add(beneBillPayDtlsType.getUtilityCode());
								ccPayment_BenefDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								ccPayment_BenefDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								ccPayment_BenefDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								ccPayment_BenefDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								ccPayment_BenefDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								ccPayment_BenefDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								ccPayment_BenefDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								ccPayment_BenefDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//ccPayment_BenefDetails.setBankCode(beneBankInfoType.getBankCode());
								ccPayment_BenefDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								ccPayment_BenefDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());
							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+ccPayment_BeneficiaryDetailsMap);}
							ccPayment_BeneficiaryDetailsMap.put(benefID, ccPayment_BenefDetails);

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getServiceProviderCode())){

								tempArrayList = ccPayment_ServiceProviderMap.get(beneBillPayDtlsType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the service provider" +beneBillPayDtlsType.getServiceProviderCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<CCPayment_BenefDetails>();
								}

								tempArrayList.add(ccPayment_BenefDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +ccPayment_BenefDetails+" for the service provider id "+beneBillPayDtlsType.getServiceProviderCode());}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the service provider code " +beneBillPayDtlsType.getServiceProviderCode()+" detail into serviceprovidermap"+ccPayment_BenefDetails);}
								ccPayment_ServiceProviderMap.put(beneBillPayDtlsType.getServiceProviderCode(), tempArrayList);
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getUtilityCode())){

								tempArrayList = ccPayment_UtilityCodeMap.get(beneBillPayDtlsType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the Utility coder" +beneBillPayDtlsType.getServiceProviderCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<CCPayment_BenefDetails>();
								}

								tempArrayList.add(ccPayment_BenefDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +ccPayment_BenefDetails+" for the utility code is"+beneBillPayDtlsType.getServiceProviderCode());}


								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility code " +beneBillPayDtlsType.getUtilityCode()+" detail into serviceprovidermap"+ccPayment_BenefDetails);}
								ccPayment_UtilityCodeMap.put(beneBillPayDtlsType.getUtilityCode(), tempArrayList);
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}
							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary Map size is " + ccPayment_BeneficiaryDetailsMap.size());}
						beanResponse.setCCPayment_BenefDetailsMap(ccPayment_BeneficiaryDetailsMap);
						beanResponse.setCCPayment_serviceProviderMap(ccPayment_ServiceProviderMap);
						beanResponse.setCCPayment_utilityCodeMap(ccPayment_UtilityCodeMap);
						beanResponse.setUtilityCodeList(utilityCodeList);
					}
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getCCPaymentTPWBMBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getCCPaymentTPWBMBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getCCPaymentTPWBMBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BenfPayeeDetails_HostRes getMobBroadBandBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getMobBroadBandBenfDelsHostRes()");}

		Utility_BenfPayeeDetails_HostRes beanResponse = new Utility_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}

				
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				
				HashMap<String, Utility_BeneficiaryDetails>utility_BeneficiaryDetailsMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_ServiceProviderMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_UtilityCodeMap = null;
				ArrayList<Utility_BeneficiaryDetails> tempArrayList = null;


				Utility_BeneficiaryDetails utility_BeneficiaryDetails = null;
				ArrayList<String> utilityCodeList = null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;

				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);
					
					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END

					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}

					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
					
					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					utility_BeneficiaryDetailsMap = beanResponse.getUtility_BeneficiaryDetailsMap() ;

					utility_ServiceProviderMap = beanResponse.getUtility_serviceProviderMap();
					utility_UtilityCodeMap = beanResponse.getUtility_utilityCodeMap();


					utilityCodeList = beanResponse.getUtilityCodeList();
					if(util.isNullOrEmpty(utilityCodeList)){
						utilityCodeList = new ArrayList<String>();
					}

					if(util.isNullOrEmpty(utility_BeneficiaryDetailsMap)){
						utility_BeneficiaryDetailsMap = new HashMap<String, Utility_BeneficiaryDetails>();
					}
					if(util.isNullOrEmpty(utility_ServiceProviderMap)){
						utility_ServiceProviderMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					if(util.isNullOrEmpty(utility_UtilityCodeMap)){
						utility_UtilityCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							utility_BeneficiaryDetails = new Utility_BeneficiaryDetails();

							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							utility_BeneficiaryDetails.setBeneficiaryID(benefID);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Setting the beneficiary id into the utility_beneficiaryDtsl list"+benefID);}
							if(!util.isNullOrEmpty(beneAccInfoType)){

								//vijay
								/*if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								utility_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());*/

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								utility_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								utility_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								utility_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								utility_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								utility_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								utility_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								utility_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());
								utilityCodeList.add(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								utility_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								utility_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								utility_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								utility_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								utility_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								utility_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());
							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+utility_BeneficiaryDetails);}
							utility_BeneficiaryDetailsMap.put(benefID, utility_BeneficiaryDetails);

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getServiceProviderCode())){

								tempArrayList = utility_ServiceProviderMap.get(beneBillPayDtlsType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the service provider" +beneBillPayDtlsType.getServiceProviderCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the service provider id "+beneBillPayDtlsType.getServiceProviderCode());}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the service provider code " +beneBillPayDtlsType.getServiceProviderCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_ServiceProviderMap.put(beneBillPayDtlsType.getServiceProviderCode(), tempArrayList);
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getUtilityCode())){

								tempArrayList = utility_UtilityCodeMap.get(beneBillPayDtlsType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the Utility coder" +beneBillPayDtlsType.getUtilityCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the utility code is"+beneBillPayDtlsType.getUtilityCode());}


								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility code " +beneBillPayDtlsType.getUtilityCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_UtilityCodeMap.put(beneBillPayDtlsType.getUtilityCode(), tempArrayList);
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}
							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary Details map size is  " + utility_BeneficiaryDetailsMap.size());}
						beanResponse.setUtility_BeneficiaryDetailsMap(utility_BeneficiaryDetailsMap);
						beanResponse.setUtility_serviceProviderMap(utility_ServiceProviderMap);
						beanResponse.setUtility_utilityCodeMap(utility_UtilityCodeMap);
						beanResponse.setUtilityCodeList(utilityCodeList);
					}
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getMobBroadBandBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getMobBroadBandBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getMobBroadBandBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BenfPayeeDetails_HostRes getElecBillBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getElecBillBenfDelsHostRes()");}

		Utility_BenfPayeeDetails_HostRes beanResponse = new Utility_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
				
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}

				HashMap<String, Utility_BeneficiaryDetails>utility_BeneficiaryDetailsMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_ServiceProviderMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_UtilityCodeMap = null;
				ArrayList<Utility_BeneficiaryDetails> tempArrayList = null;

				ArrayList<String> utilityCodeList = null;
				Utility_BeneficiaryDetails utility_BeneficiaryDetails = null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;

				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);

					
					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END
					
					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType,str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}


					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
					
					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					utility_BeneficiaryDetailsMap = beanResponse.getUtility_BeneficiaryDetailsMap() ;
					utility_UtilityCodeMap = beanResponse.getUtility_utilityCodeMap();
					utility_ServiceProviderMap = beanResponse.getUtility_serviceProviderMap();

					utilityCodeList = beanResponse.getUtilityCodeList();
					if(util.isNullOrEmpty(utilityCodeList)){
						utilityCodeList = new ArrayList<String>();
					}

					if(util.isNullOrEmpty(utility_UtilityCodeMap)){
						utility_UtilityCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					if(util.isNullOrEmpty(utility_BeneficiaryDetailsMap)){
						utility_BeneficiaryDetailsMap = new HashMap<String, Utility_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(utility_ServiceProviderMap)){
						utility_ServiceProviderMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}


					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							utility_BeneficiaryDetails = new Utility_BeneficiaryDetails();

							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							utility_BeneficiaryDetails.setBeneficiaryID(benefID);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID"+benefID);}
							
							if(!util.isNullOrEmpty(beneAccInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								utility_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								utility_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								utility_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								utility_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								utility_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								utility_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								utility_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								utility_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());
								utilityCodeList.add(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								utility_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								utility_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								utility_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								utility_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								utility_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								utility_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());
							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+utility_BeneficiaryDetails);}
							utility_BeneficiaryDetailsMap.put(benefID, utility_BeneficiaryDetails);

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getUtilityCode())){

								tempArrayList = utility_UtilityCodeMap.get(beneBillPayDtlsType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the Utility coder" +beneBillPayDtlsType.getUtilityCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the utility code is"+beneBillPayDtlsType.getUtilityCode());}


								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility code " +beneBillPayDtlsType.getUtilityCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_UtilityCodeMap.put(beneBillPayDtlsType.getUtilityCode(), tempArrayList);
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getServiceProviderCode())){

								tempArrayList = utility_ServiceProviderMap.get(beneBillPayDtlsType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the service provider" +beneBillPayDtlsType.getServiceProviderCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the service provider id "+beneBillPayDtlsType.getServiceProviderCode());}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the service provider code " +beneBillPayDtlsType.getServiceProviderCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_ServiceProviderMap.put(beneBillPayDtlsType.getServiceProviderCode(), tempArrayList);
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}
							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary Details Map " +utility_BeneficiaryDetailsMap.size());}

						beanResponse.setUtility_BeneficiaryDetailsMap(utility_BeneficiaryDetailsMap);
						beanResponse.setUtility_utilityCodeMap(utility_UtilityCodeMap);
						beanResponse.setUtility_serviceProviderMap(utility_ServiceProviderMap);
						beanResponse.setUtilityCodeList(utilityCodeList);
					}
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getElecBillBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getElecBillBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getElecBillBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BenfPayeeDetails_HostRes getWaterBillBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException  {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getWaterBillBenfDelsHostRes()");}

		Utility_BenfPayeeDetails_HostRes beanResponse = new Utility_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
			
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				
				
				
				HashMap<String, Utility_BeneficiaryDetails>utility_BeneficiaryDetailsMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_ServiceProviderMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_UtilityCodeMap = null;
				ArrayList<Utility_BeneficiaryDetails> tempArrayList = null;

				Utility_BeneficiaryDetails utility_BeneficiaryDetails = null;
				ArrayList<String> utilityCodeList = null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;
				
				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}
				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);

					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END
					
					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}


					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
					
					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					utility_BeneficiaryDetailsMap = beanResponse.getUtility_BeneficiaryDetailsMap() ;

					if(util.isNullOrEmpty(utility_BeneficiaryDetailsMap)){
						utility_BeneficiaryDetailsMap = new HashMap<String, Utility_BeneficiaryDetails>();
					}
					
					utility_UtilityCodeMap = beanResponse.getUtility_utilityCodeMap();
					utility_ServiceProviderMap = beanResponse.getUtility_serviceProviderMap();

					utilityCodeList = beanResponse.getUtilityCodeList();
					if(util.isNullOrEmpty(utilityCodeList)){
						utilityCodeList = new ArrayList<String>();
					}

					if(util.isNullOrEmpty(utility_UtilityCodeMap)){
						utility_UtilityCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					if(util.isNullOrEmpty(utility_ServiceProviderMap)){
						utility_ServiceProviderMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							utility_BeneficiaryDetails = new Utility_BeneficiaryDetails();

							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							utility_BeneficiaryDetails.setBeneficiaryID(benefID);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID"+benefID);}
							

							if(!util.isNullOrEmpty(beneAccInfoType)){
								//vijay
								/*
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								utility_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());*/

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								utility_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								utility_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								utility_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								utility_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								utility_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								utility_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								utility_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());
								utilityCodeList.add(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								utility_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								utility_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								utility_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								utility_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								utility_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								utility_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());
							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+utility_BeneficiaryDetails);}
							utility_BeneficiaryDetailsMap.put(benefID, utility_BeneficiaryDetails);
							
							if(!util.isNullOrEmpty(beneBillPayDtlsType.getUtilityCode())){

								tempArrayList = utility_UtilityCodeMap.get(beneBillPayDtlsType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the Utility coder" +beneBillPayDtlsType.getUtilityCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the utility code is"+beneBillPayDtlsType.getUtilityCode());}


								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility code " +beneBillPayDtlsType.getUtilityCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_UtilityCodeMap.put(beneBillPayDtlsType.getUtilityCode(), tempArrayList);
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getServiceProviderCode())){

								tempArrayList = utility_ServiceProviderMap.get(beneBillPayDtlsType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the service provider" +beneBillPayDtlsType.getServiceProviderCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the service provider id "+beneBillPayDtlsType.getServiceProviderCode());}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the service provider code " +beneBillPayDtlsType.getServiceProviderCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_ServiceProviderMap.put(beneBillPayDtlsType.getServiceProviderCode(), tempArrayList);
							}


						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}
							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary Details Map " + utility_BeneficiaryDetailsMap.size());}

					beanResponse.setUtility_BeneficiaryDetailsMap(utility_BeneficiaryDetailsMap);
					beanResponse.setUtility_utilityCodeMap(utility_UtilityCodeMap);
					beanResponse.setUtility_serviceProviderMap(utility_ServiceProviderMap);
					beanResponse.setUtilityCodeList(utilityCodeList);
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getWaterBillBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getWaterBillBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getWaterBillBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BenfPayeeDetails_HostRes getSchoolBillBenfDelsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getSchoolBillBenfDelsHostRes()");}

		Utility_BenfPayeeDetails_HostRes beanResponse = new Utility_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}
				
//				String hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				/**
				 * Following changes are done by Vinoth on 12-Aug-2014 for School bean 
				 */
				
				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}

				HashMap<String, Utility_BeneficiaryDetails>utility_BeneficiaryDetailsMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_ServiceProviderMap = null;
				HashMap<String, ArrayList<Utility_BeneficiaryDetails>>utility_UtilityCodeMap = null;
				ArrayList<Utility_BeneficiaryDetails> tempArrayList = null;

				ArrayList<String> utilityCodeList = null;
				Utility_BeneficiaryDetails utility_BeneficiaryDetails = null;
				BeneAccInfoType beneAccInfoType = null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;

				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				

				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);

					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END
					
					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}


					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
				
					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					utility_BeneficiaryDetailsMap = beanResponse.getUtility_BeneficiaryDetailsMap() ;

					utilityCodeList = beanResponse.getUtilityCodeList();
					if(util.isNullOrEmpty(utilityCodeList)){
						utilityCodeList = new ArrayList<String>();
					}


					if(util.isNullOrEmpty(utility_BeneficiaryDetailsMap)){
						utility_BeneficiaryDetailsMap = new HashMap<String, Utility_BeneficiaryDetails>();
					}

					utility_UtilityCodeMap = beanResponse.getUtility_utilityCodeMap();

					if(util.isNullOrEmpty(utility_UtilityCodeMap)){
						utility_UtilityCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					utility_ServiceProviderMap = beanResponse.getUtility_serviceProviderMap();

					if(util.isNullOrEmpty(utility_ServiceProviderMap)){
						utility_ServiceProviderMap = new HashMap<String, ArrayList<Utility_BeneficiaryDetails>>();
					}

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							utility_BeneficiaryDetails = new Utility_BeneficiaryDetails();
							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							utility_BeneficiaryDetails.setBeneficiaryID(benefID);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID"+benefID);}
						
							
							if(!util.isNullOrEmpty(beneAccInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								utility_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								utility_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								utility_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								utility_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								utility_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								utility_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								utility_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								utility_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());
								utilityCodeList.add(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								utility_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								utility_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								utility_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								utility_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								utility_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								utility_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								utility_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								utility_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());
							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+utility_BeneficiaryDetails);}
							utility_BeneficiaryDetailsMap.put(benefID, utility_BeneficiaryDetails);

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getUtilityCode())){

								tempArrayList = utility_UtilityCodeMap.get(beneBillPayDtlsType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the Utility coder" +beneBillPayDtlsType.getUtilityCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the utility code is"+beneBillPayDtlsType.getUtilityCode());}


								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility code " +beneBillPayDtlsType.getUtilityCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_UtilityCodeMap.put(beneBillPayDtlsType.getUtilityCode(), tempArrayList);
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType.getServiceProviderCode())){

								tempArrayList = utility_ServiceProviderMap.get(beneBillPayDtlsType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exisiting beneficiary detail list of the service provider" +beneBillPayDtlsType.getServiceProviderCode()+" is " +tempArrayList);}

								if(tempArrayList == null){
									tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();
								}

								tempArrayList.add(utility_BeneficiaryDetails);
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary deatils " +utility_BeneficiaryDetails+" for the service provider id "+beneBillPayDtlsType.getServiceProviderCode());}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the service provider code " +beneBillPayDtlsType.getServiceProviderCode()+" detail into serviceprovidermap"+utility_BeneficiaryDetails);}
								utility_ServiceProviderMap.put(beneBillPayDtlsType.getServiceProviderCode(), tempArrayList);
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}
							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary Details Map size is " + utility_BeneficiaryDetailsMap.size());}

						beanResponse.setUtility_BeneficiaryDetailsMap(utility_BeneficiaryDetailsMap);
						beanResponse.setUtility_utilityCodeMap(utility_UtilityCodeMap);
						beanResponse.setUtility_serviceProviderMap(utility_ServiceProviderMap);
						beanResponse.setUtilityCodeList(utilityCodeList);
					}
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getSchoolBillBenfDelsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getSchoolBillBenfDelsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getSchoolBillBenfDelsHostRes()");}
		return beanResponse;
	}

	@Override
	public TPR_BenfPayeeDetails_HostRes getTPRBeneficiaryDetailsHostRes(
			CallInfo callInfo, ArrayList<String> benefIDList, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getTPRBeneficiaryDetailsHostRes()");}

		TPR_BenfPayeeDetails_HostRes beanResponse = new TPR_BenfPayeeDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryDetailsEnquiryResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callBeneficiaryDetailHost host");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list is"+ benefIDList);}
			if(benefIDList !=null){
				String code = Constants.EMPTY_STRING;
				String benefID = Constants.EMPTY_STRING;

				ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
				if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
					throw new ServiceException("ICEGlobalConfig object is null");
				}

				ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
				if(util.isNullOrEmpty(ivr_ICEFeatureData)){
					throw new ServiceException("ivr_ICEFeatureData object is null");
				}
				
				
				String hostErrorCodeList = Constants.EMPTY_STRING;
				
				if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
					hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode); 
				}
				else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
					hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_BeneficiaryDtsInquriy_Succ_ErrorCode);
				}
				
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary ID list size is"+ benefIDList.size());}

				HashMap<String, TPR_BeneficiaryDetails>tpr_BeneficiaryDetailsMap = null;
				HashMap<String, TPR_BeneficiaryDetails>tpr_BeneficiaryCharityDetailsMap = null;
				HashMap<String, TPR_BeneficiaryDetails>tpr_BeneficiaryAcctDetailsMap = null;
				ArrayList<String> charityCodeList = null;

				TPR_BeneficiaryDetails tpr_BeneficiaryDetails = null;

				BeneAccInfoType beneAccInfoType =null;
				BeneBillPayDtlsType beneBillPayDtlsType = null;
				BeneBankInfoType beneBankInfoType = null;

				String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
				

				for(int count = 0; count < benefIDList.size(); count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count+ " Beneficiary ID is"+ benefIDList.get(count));}
					benefID = benefIDList.get(count);
					
					//Newly added to disable or enable host response files
					String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
					//END

					response = beneficiaryDtlsInquiryService.callBeneficiaryDetailHost(logger, sessionID, benefID, requestType, str_UUI, generateXML, callInfo);

					ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
					code = ws_ResponseHeader.getEsbErrCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callBeneficiaryDetailHost is : "+code);}

					beanResponse.setHostResponseCode(code);

					beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for callBeneficiaryDetailHost is : "+ws_ResponseHeader.getEsbErrDesc());}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### BeneficiaryDtlsInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
					
					code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
					beanResponse.setErrorCode(code);
					String hostEndTime = util.getCurrentDateTime();
					beanResponse.setHostEndTime(hostEndTime);

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}
					
					//Setting the ESB request reference number for reporting
					String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
					callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
					//END
					
					/*fT_BeneficiaryDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;
					fT_BeneficiaryCharityDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;
					fT_BeneficiaryAcctDetailsMap = beanResponse.getFT_BeneficiaryDetailsMap() ;*/

					tpr_BeneficiaryDetailsMap = beanResponse.getTPR_BeneficiaryDetailsMap();
					tpr_BeneficiaryCharityDetailsMap = beanResponse.getTPR_BeneficiaryCharityDetailsMap();
					tpr_BeneficiaryAcctDetailsMap = beanResponse.getTPR_BeneficiaryAcctDetailsMap();
					charityCodeList = beanResponse.getCharityCodeList();


					if(util.isNullOrEmpty(tpr_BeneficiaryDetailsMap)){
						tpr_BeneficiaryDetailsMap = new HashMap<String, TPR_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(tpr_BeneficiaryCharityDetailsMap)){
						tpr_BeneficiaryCharityDetailsMap = new HashMap<String, TPR_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(tpr_BeneficiaryAcctDetailsMap)){
						tpr_BeneficiaryAcctDetailsMap = new HashMap<String, TPR_BeneficiaryDetails>();
					}

					if(util.isNullOrEmpty(charityCodeList)){
						charityCodeList = new ArrayList<String>();
					}

					if(Constants.WS_SUCCESS_CODE.equals(code)){
						if(!util.isNullOrEmpty(response)){

							tpr_BeneficiaryDetails = new TPR_BeneficiaryDetails();
							beneAccInfoType = response.getBeneAccInfo();
							beneBillPayDtlsType = response.getBeneBillPayDtls();
							beneBankInfoType = response.getBeneBankInfo();

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary ID is "+benefID);}
							tpr_BeneficiaryDetails.setBeneficiaryID(benefID);
							
							if(!util.isNullOrEmpty(beneAccInfoType)){
								/*if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer ID"+beneAccInfoType.getCustomerId());}
								ft_BeneficiaryDetails.setCustomerID(beneAccInfoType.getCustomerId());*/

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Customer Dr Account"+beneAccInfoType.getCustomerDrAccount());}
								tpr_BeneficiaryDetails.setCustomerDrAcctNo(beneAccInfoType.getCustomerDrAccount());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Type "+ beneAccInfoType.getBeneficiaryAccountType());}
								tpr_BeneficiaryDetails.setBenefAccountType(beneAccInfoType.getBeneficiaryAccountType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## beneficiary Mobile number "+beneAccInfoType.getBeneficiaryMobileNumber());}
								tpr_BeneficiaryDetails.setBenefMobileNo(beneAccInfoType.getBeneficiaryMobileNumber());
							}

							if(!util.isNullOrEmpty(beneBillPayDtlsType)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Type "+ beneBillPayDtlsType.getPaymentType());}
								tpr_BeneficiaryDetails.setBenefPaymentType(beneBillPayDtlsType.getPaymentType());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Beneficiary Account Number"+ beneBillPayDtlsType.getBeneficaryAccountNumber());}
								tpr_BeneficiaryDetails.setBenefAccountNo(beneBillPayDtlsType.getBeneficaryAccountNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Service Provider Code "+beneBillPayDtlsType.getServiceProviderCode());}
								tpr_BeneficiaryDetails.setServiceProviderCode(beneBillPayDtlsType.getServiceProviderCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ##  Utility Code"+beneBillPayDtlsType.getUtilityCode());}
								charityCodeList.add(beneBillPayDtlsType.getUtilityCode());
								tpr_BeneficiaryDetails.setUtilityCode(beneBillPayDtlsType.getUtilityCode());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card number "+beneBillPayDtlsType.getCreditCardNumber());}
								tpr_BeneficiaryDetails.setBenefCreditCardNo(beneBillPayDtlsType.getCreditCardNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Number "+beneBillPayDtlsType.getBillNumber());}
								tpr_BeneficiaryDetails.setBenefBillNo(beneBillPayDtlsType.getBillNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Contract Number"+beneBillPayDtlsType.getContractNumber());}
								tpr_BeneficiaryDetails.setBenefContractNo(beneBillPayDtlsType.getContractNumber());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## GSM Number"+beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);}
								tpr_BeneficiaryDetails.setBenefGSMNo(beneBillPayDtlsType.getGsmNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Telephone Number"+beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);}
								tpr_BeneficiaryDetails.setBenefTelephoneNo(beneBillPayDtlsType.getTelephoneNumber()+Constants.EMPTY);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Student name"+beneBillPayDtlsType.getStudentName());}
								tpr_BeneficiaryDetails.setBenefStudentName(beneBillPayDtlsType.getStudentName());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Class Section"+beneBillPayDtlsType.getClassSection());}
								tpr_BeneficiaryDetails.setBenefClassSection(beneBillPayDtlsType.getClassSection());
							}

							if(!util.isNullOrEmpty(beneBankInfoType)){

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank Code "+beneBankInfoType.getBankCode());}
								//tpr_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode());
								tpr_BeneficiaryDetails.setBankCode(beneBankInfoType.getBankCode().get(0));

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bank IFSC Code"+beneBankInfoType.getBankIFSCCode());}
								tpr_BeneficiaryDetails.setBankIFSCCode(beneBankInfoType.getBankIFSCCode());

							}

							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted Beneficiary detail map "+tpr_BeneficiaryDetails);}

							tpr_BeneficiaryDetailsMap.put(benefID, tpr_BeneficiaryDetails);
							tpr_BeneficiaryAcctDetailsMap.put(beneBillPayDtlsType.getBeneficaryAccountNumber(), tpr_BeneficiaryDetails);
							tpr_BeneficiaryCharityDetailsMap.put(beneBillPayDtlsType.getUtilityCode(), tpr_BeneficiaryDetails);

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## BeneficiaryDtlsInquiry Service Response field Received null / empty so setting error code as 1");}

							beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
						}

					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Beneficiary detail map size is "+tpr_BeneficiaryDetailsMap.size());}
					beanResponse.setTPR_BeneficiaryDetailsMap(tpr_BeneficiaryDetailsMap);
					beanResponse.setTPR_BeneficiaryAcctDetailsMap(tpr_BeneficiaryAcctDetailsMap);
					beanResponse.setTPR_BeneficiaryCharityDetailsMap(tpr_BeneficiaryCharityDetailsMap);
					beanResponse.setCharityCodeList(charityCodeList);
				}
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getTPRBeneficiaryDetailsHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BeneficiaryDtlsInquiryDAOImpl.getTPRBeneficiaryDetailsHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: BeneficiaryDtlsInquiryDAOImpl.getTPRBeneficiaryDetailsHostRes()");}
		return beanResponse;
	}

}
