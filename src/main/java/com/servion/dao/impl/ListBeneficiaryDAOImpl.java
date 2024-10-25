package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.beneficiarymanagementservice.BeneAcctType;
import com.bankmuscat.esb.beneficiarymanagementservice.BeneficiaryListInqResType;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.dao.ListBeneficiaryDAO;
import com.servion.dao.TelecomSubscriberInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.TelecomSubcriberInfoBasicDetails;
import com.servion.model.billPayment.TelecomSubscriberInfo_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetailList_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryShortDetails;
import com.servion.model.billPayment.Utility_BenfPayeeDetails_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BeneficiaryDetailList_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BeneficiaryShortDetails;
import com.servion.model.fundsTransfer.FT_BeneficiaryDetailList_HostRes;
import com.servion.model.fundsTransfer.FT_BeneficiaryShortDetails;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.thirdPartyRemittance.TPR_BeneficiaryShortDetails;
import com.servion.model.thirdPartyRemittance.TPR_RetrieveBenfPayeeList_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.ListBeneficiaryService;
import com.servion.ws.util.DAOLayerUtils;

public class ListBeneficiaryDAOImpl implements ListBeneficiaryDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	ListBeneficiaryService listBeneficiaryService;

	public ListBeneficiaryService getListBeneficiaryService() {
		return listBeneficiaryService;
	}

	public void setListBeneficiaryService(
			ListBeneficiaryService listBeneficiaryService) {
		this.listBeneficiaryService = listBeneficiaryService;
	}


	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();



	@Override
	public FT_BeneficiaryDetailList_HostRes getFTTWBMBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)
			throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getFTTWBMBenfListHostRes()");}

		FT_BeneficiaryDetailList_HostRes beanResponse = new FT_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			ICEGlobalConfig ivr_ICEGlobalConfig =  util.isNullOrEmpty(callInfo.getICEGlobalConfig())? null: (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			
			
			beanResponse.setHostResponseCode(code);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					//						ArrayList<String> charityCodeList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					FT_BeneficiaryShortDetails ft_BeneficiaryShortDetails = null;
					HashMap<String, FT_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					if(!util.isNullOrEmpty(benefAcctTypeList) && benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){

								ft_BeneficiaryShortDetails = new FT_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								ft_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								ft_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								ft_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								ft_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}

								ft_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								ft_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								ft_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								//									charityCodeList.add(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								ft_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}

							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), ft_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					//						beanResponse.setCharityCodeList(charityCodeList);
					//	beanResponse(benefIDList);
					//	beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);


				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getFTTWBMBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getFTTWBMBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getFTTWBMBenfListHostRes()");}
		return beanResponse;
	}

	@Override
	public FT_BeneficiaryDetailList_HostRes getFTTOBMBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getFTTOBMBenfListHostRes()");}

		FT_BeneficiaryDetailList_HostRes beanResponse = new FT_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}
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
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+ code + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					FT_BeneficiaryShortDetails ft_BeneficiaryShortDetails = null;
					HashMap<String, FT_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					if(!util.isNullOrEmpty(benefAcctTypeList) && benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){

								ft_BeneficiaryShortDetails = new FT_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}
								ft_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								ft_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								ft_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								ft_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}

								ft_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								ft_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								ft_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								ft_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}

							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), ft_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					//	beanResponse(benefIDList);
					//	beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);


				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getFTTOBMBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getFTTOBMBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getCallerIdentificationHostRes()");}
		return beanResponse;
	}

	@Override
	public FT_BeneficiaryDetailList_HostRes getFTCharityBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getFTCharityBenfListHostRes()");}

		FT_BeneficiaryDetailList_HostRes beanResponse = new FT_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficirary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}
			beanResponse.setHostResponseCode(code);

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					FT_BeneficiaryShortDetails ft_BeneficiaryShortDetails = null;
					HashMap<String, FT_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){

								ft_BeneficiaryShortDetails = new FT_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}
								ft_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								ft_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								ft_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								ft_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}

								ft_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								ft_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								ft_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								ft_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}

							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), ft_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					//	beanResponse(benefIDList);
					//	beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);


				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getFTCharityBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getFTCharityBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getFTCharityBenfListHostRes()");}
		return beanResponse;
	}

	@Override
	public CCPayment_BeneficiaryDetailList_HostRes getCCPaymentTWBMBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getCCPaymentTWBMBenfListHostRes()");}

		CCPayment_BeneficiaryDetailList_HostRes beanResponse = new CCPayment_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			
			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+ code + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
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
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					ArrayList<String> utilityCodeList = new ArrayList<String>();
					ArrayList<String> serviceProviderList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					CCPayment_BeneficiaryShortDetails ccPayment_BeneficiaryShortDetails = null;
					HashMap<String, CCPayment_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){

								ccPayment_BeneficiaryShortDetails = new CCPayment_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								ccPayment_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								ccPayment_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								ccPayment_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								serviceProviderList.add(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}

								ccPayment_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								ccPayment_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								ccPayment_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								utilityCodeList.add(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								ccPayment_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}

							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), ccPayment_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setServiceProviderList(serviceProviderList);
					beanResponse.setUtilityCodeList(utilityCodeList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);


				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getCCPaymentTWBMBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getCCPaymentTWBMBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getCCPaymentTWBMBenfListHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BeneficiaryDetailList_HostRes getMobBroadBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType, BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getMobBroadBenfListHostRes()");}

		Utility_BeneficiaryDetailList_HostRes beanResponse = new Utility_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+ code + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
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

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					ArrayList<String> utilityCodeList = new ArrayList<String>();
					ArrayList<String> serviceProviderList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					Utility_BeneficiaryShortDetails utily_BeneficiaryShortDetails = null;
					HashMap<String, Utility_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					/**
					 * For Utility and Service Provider Code handling
					 */
					HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>> serviceProviderCodeMap = beanResponse.getServiceCodeMap();
					HashMap<String, ArrayList<String>> utilityCodeMap = beanResponse.getUtilityCodeMap();
					ArrayList<Utility_BeneficiaryShortDetails> utilityShorDtlsList = null;
					ArrayList<String> serviceProviderCodeList = null;
					String strTempCode = Constants.EMPTY_STRING;
					String strServCode = Constants.EMPTY_STRING;
					//END
					
					/*
					 * 
					 */
					ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
					
					if(util.isNullOrEmpty(iceFeatureData)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "ICEFeature Data object is null or empty");}	
					}
					String ooredooServProviderCodes = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_NAWRAS_PROVIDERS);
					String mobileCodes = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){
								
								/*
								 * 
								 */
								try{
								if((!util.isNullOrEmpty(beneAcctType.getServiceProviderCode())) 
										&& (!util.isNullOrEmpty(beneAcctType.getUtilityCode()))
										&& (!util.isNullOrEmpty(ooredooServProviderCodes))
										&& util.isCodePresentInTheConfigurationList(beneAcctType.getServiceProviderCode(), ooredooServProviderCodes)
										&& (!util.isNullOrEmpty(mobileCodes))
										&& util.isCodePresentInTheConfigurationList(beneAcctType.getUtilityCode(), mobileCodes)
										&& (!util.isNullOrEmpty(beneAcctType.getBeneficiaryId()))
										){
									String combinedKey = beneAcctType.getUtilityCode() + Constants.UNDERSCORE + beneAcctType.getServiceProviderCode() + Constants.UNDERSCORE + Constants.CUI_IS_A_ONLINE_SERVICEPROVIDER;
						  			combinedKey = combinedKey.trim();
						  			
						  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"Combined key is " + combinedKey);}
						  			
						  			String isSelectedUtilityCodeOnline = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(combinedKey)) ? Constants.FALSE : (String)ivr_ICEFeatureData.getConfig().getParamValue(combinedKey);
						  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, sessionID,"Selected Utility Code is a Online or Offline " + isSelectedUtilityCodeOnline);}
						  			
						  			isSelectedUtilityCodeOnline = util.isTrue(isSelectedUtilityCodeOnline) ? Constants.TRUE : Constants.FALSE;
						  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Updating the boolean value as TRUE / FALSE and the result is " + isSelectedUtilityCodeOnline);}
						  			
						  			if(isSelectedUtilityCodeOnline.equalsIgnoreCase(Constants.TRUE)){
						  				ArrayList<String> beneficiaryIdList = new ArrayList<String>();
						  				beneficiaryIdList.add(beneAcctType.getBeneficiaryId());
						  				Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes = getBenfPayeeDetails(callInfo, customerID, sessionID, beneficiaryIdList, beneficiaryDtlsInquiryDAO);
						  				String mobileNumber = utility_BenfPayeeDetails_HostRes.getUtility_BeneficiaryDetailsMap().get(beneAcctType.getBeneficiaryId()).getBenefTelephoneNo();
						  				if(!util.isNullOrEmpty(mobileNumber)){
						  					mobileNumber = utility_BenfPayeeDetails_HostRes.getUtility_BeneficiaryDetailsMap().get(beneAcctType.getBeneficiaryId()).getBenefMobileNo();
						  				}
						  				if(!util.isNullOrEmpty(mobileNumber)){
						  					
						  				}
						  			}
								boolean isOoredooService = false;
								String serviceProviderCode = beneAcctType.getServiceProviderCode();
								
								
								}
								}catch(Exception e){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,sessionID, "Exception: " + e);}
								}
								

								/**
								 * For Utility and Service Provider Code handling
								 */

								if(serviceProviderCodeMap == null){
									serviceProviderCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>>();
								}

								if(utilityCodeMap == null){
									utilityCodeMap = new HashMap<String, ArrayList<String>>();
								}

								//END



								utily_BeneficiaryShortDetails = new Utility_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								utily_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								utily_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								utily_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								utily_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(serviceProviderList!=null && !serviceProviderList.contains(beneAcctType.getServiceProviderCode())){
									serviceProviderList.add(beneAcctType.getServiceProviderCode());
								}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}
								
								
								utily_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								utily_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								utily_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());

								if(utilityCodeList!=null && !utilityCodeList.contains(beneAcctType.getUtilityCode())){
									utilityCodeList.add(beneAcctType.getUtilityCode());
								}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								utily_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}


								/**
								 * For Utility and Service Provider Code handling
								 */

								strTempCode = utily_BeneficiaryShortDetails.getUtilityCode();
								strServCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility code is "+ strTempCode);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strServCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									serviceProviderCodeList = utilityCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility related Service provider code list is "+ serviceProviderCodeList);}

									if(util.isNullOrEmpty(serviceProviderCodeList)){
										serviceProviderCodeList = new ArrayList<String>();
									}

									//Added on 09-Apr-2013 - to neglect duplicate service provider code from the list
									if(!serviceProviderCodeList.contains(strServCode)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding new service provider code "+ strServCode);}
										serviceProviderCodeList.add(strServCode);
									}
									//END
									
									
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Service provider code " +strServCode+" for the utility code is"+strTempCode);}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the service provide code list object  " +serviceProviderCodeList+" detail into Utility Code Map"+utilityCodeMap);}
									utilityCodeMap.put(strTempCode, serviceProviderCodeList);

								}



								strTempCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strTempCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									utilityShorDtlsList = serviceProviderCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Short Detail list is "+ utilityShorDtlsList);}

									if(util.isNullOrEmpty(utilityShorDtlsList)){
										utilityShorDtlsList = new ArrayList<Utility_BeneficiaryShortDetails>();
									}

									utilityShorDtlsList.add(utily_BeneficiaryShortDetails);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary short deatils " +utily_BeneficiaryShortDetails+" for the Service provider code is"+utily_BeneficiaryShortDetails.getServiceProviderCode());}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility short details arraylist object  " +utilityShorDtlsList+" detail into serviceprovidermap"+serviceProviderCodeMap);}
									serviceProviderCodeMap.put(strTempCode, utilityShorDtlsList);

								}
								//END 
							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), utily_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setServiceProviderList(serviceProviderList);
					beanResponse.setUtilityCodeList(utilityCodeList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);

					/**
					 * For Utility and Service Provider Code handling
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Code Map Object is "+utilityCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider Code Map Object is "+serviceProviderCodeMap);}

					beanResponse.setUtilityCodeMap(utilityCodeMap);
					beanResponse.setServiceCodeMap(serviceProviderCodeMap);
					//END

				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getMobBroadBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getMobBroadBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getMobBroadBenfListHostRes()");}
		return beanResponse;
	}
	
	public Utility_BenfPayeeDetails_HostRes getBenfPayeeDetails(CallInfo callInfo, String customerID, String session_ID_, ArrayList<String> beneficiaryIdList,
			BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO){
		String code = Constants.EMPTY_STRING;
		Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes = null;
		try{
		/**
		 * For Reporting Purpose
		 */
		HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

		String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
		hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
		String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
		hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY);
		//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
		hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
		hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
		hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
		hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

		hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
		hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
		//End Reporting
		
		/*
		 *  Setting NA values
		 */
		hostReportDetailsForSecHost.setHostEndTime(util.getCurrentDateTime());
		hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
		hostReportDetailsForSecHost.setHostResponse(Constants.NA);
		
		callInfo.setHostReportDetails(hostReportDetailsForSecHost);
		IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);
		callInfo.insertHostDetails(ivrdataForSecHost);
		
		/* END */
		
		ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
		
		if(util.isNullOrEmpty(iceFeatureData)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "iceFeatureData object is null or empty" + iceFeatureData);}
			return utility_BenfPayeeDetails_HostRes;
		}
		
		String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined beneficiary id list is " + beneficiaryIdList);}


		utility_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getMobBroadBandBenfDelsHostRes(callInfo, beneficiaryIdList, requestType);

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility_BenfPayeeDetails_HostRes Object is :"+ utility_BenfPayeeDetails_HostRes);}
		callInfo.setUtility_BenfPayeeDetails_HostRes(utility_BenfPayeeDetails_HostRes);
		code = utility_BenfPayeeDetails_HostRes.getErrorCode();

		/*
		 * For Reporting Start
		 */
		String hostEndTimeForSecHost = utility_BenfPayeeDetails_HostRes.getHostEndTime();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
		hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

		String hostResCodeForSecHost = utility_BenfPayeeDetails_HostRes.getHostResponseCode();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
		hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

		String responseDescForSecHost = Constants.HOST_FAILURE;
		if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
			responseDescForSecHost = Constants.HOST_SUCCESS;
		}
		
		/****Duplicate RRN Fix 25012016 *****/
		strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
		/************************************/
		
		String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
				+ Constants.EQUALTO + hostResCodeForSecHost
		+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(utility_BenfPayeeDetails_HostRes.getErrorDesc()) ?"NA" :utility_BenfPayeeDetails_HostRes.getErrorDesc());
		hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

		callInfo.setHostReportDetails(hostReportDetailsForSecHost);
		ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

		callInfo.updateHostDetails(ivrdataForSecHost);
		//End Reporting
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_, "Exception: " + e);}
		}
		return utility_BenfPayeeDetails_HostRes;
	}
	
	public TelecomSubscriberInfo_HostRes telecomSubscriberInfo_HostRes(CallInfo callInfo, String MSISDN, 
			String session_ID_, String requestType, String providerType, String serviceProviderCode, TelecomSubscriberInfoDAO telecomSubscriberInfoDAO){
		String code = Constants.EMPTY_STRING;
		TelecomSubscriberInfo_HostRes telecomSubscriberInfo_HostRes = null;
		try{
		/**
		 * For Reporting Purpose
		 */
		HostReportDetails hostReportDetails = new HostReportDetails();

		String featureId = (String)callInfo.getField(Field.FEATUREID);
		hostReportDetails.setHostActiveMenu(featureId);
		//hostReportDetails.setHostCounter(hostCounter);
		//hostReportDetails.setHostEndTime(hostEndTime);
		String strHostInParam = Constants.HOST_INPUT_PARAM_MOBILE_NO + Constants.EQUALTO + MSISDN
				+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
		hostReportDetails.setHostInParams(strHostInParam);
		hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO);
		hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMSUBSCRIBERINFO);
		//hostReportDetails.setHostOutParams(hostOutParams);
		hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
		hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

		String startTime = util.getCurrentDateTime();
		hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
		hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
		//End Reporting


		/*
		 *  Setting NA values
		 */
		hostReportDetails.setHostEndTime(Constants.NA);
		hostReportDetails.setHostOutParams(Constants.NA);
		hostReportDetails.setHostResponse(Constants.NA);

		callInfo.setHostReportDetails(hostReportDetails);
		IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
		callInfo.insertHostDetails(ivrdata);
		
		telecomSubscriberInfo_HostRes = telecomSubscriberInfoDAO.getTelecomSubscriberInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, MSISDN);
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomSubscriberInfo_HostRes);}
		callInfo.setTelecomSubscriberInfo_HostRes(telecomSubscriberInfo_HostRes);

	code = telecomSubscriberInfo_HostRes.getErrorCode();
	/**
	 * Added on 18-Apr-2017 for Ooredoo change
	 */
	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes response code:" + code);}
	if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
		code = Constants.WS_FAILURE_CODE;
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to verify the prepaid number");}
		ArrayList<TelecomSubcriberInfoBasicDetails> telecomSubscriberInfoBasicDetailsList = telecomSubscriberInfo_HostRes.getTelecomSubcriberInfoBasicDetails();
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails value:" + telecomSubscriberInfoBasicDetailsList);}
		if(telecomSubscriberInfoBasicDetailsList != null){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails list size:" + telecomSubscriberInfoBasicDetailsList.size());}
			for(TelecomSubcriberInfoBasicDetails telecomSubcriberInfoBasicDetails : telecomSubscriberInfoBasicDetailsList){
				if(telecomSubcriberInfoBasicDetails != null && telecomSubcriberInfoBasicDetails.getMSISDNStatus().equalsIgnoreCase(Constants.Ooredoo_MSISDNStatus_Active)
						&& (telecomSubcriberInfoBasicDetails.getAccctType().equalsIgnoreCase(Constants.Ooredoo_AccType_GSM_PREPAID) || telecomSubcriberInfoBasicDetails.getAccctType().equalsIgnoreCase(Constants.Ooredoo_AccType_GSM_FIXED_PREPAID))){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails Ooredo_MSISDNStatus_Active:" + telecomSubcriberInfoBasicDetails.getMSISDNStatus());}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfoBasicDetails Ooredo_AccountType:" + telecomSubcriberInfoBasicDetails.getAccctType());}
					code = Constants.WS_SUCCESS_CODE;
					break;
				}
			}
		}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomSubscriberInfo_HostRes Object is :"+ telecomSubscriberInfo_HostRes);}
	}
	/***Ooredo change END****/
	/*
	 * For Reporting Start
	 */
	
	String hostEndTime = telecomSubscriberInfo_HostRes.getHostEndTime();
	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
	hostReportDetails.setHostEndTime(hostEndTime);

	String hostResCode = telecomSubscriberInfo_HostRes.getHostResponseCode();
	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
	hostReportDetails.setHostResponse(hostResCode);

	String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}

	String responseDesc = Constants.HOST_FAILURE;
	if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
		responseDesc = Constants.HOST_SUCCESS;
	}
	
	/****Duplicate RRN Fix 25012016 *****/
	strHostInParam = Constants.HOST_INPUT_PARAM_MOBILE_NO + Constants.EQUALTO + MSISDN
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
	hostReportDetails.setHostInParams(strHostInParam);
	/************************************/
	
	String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
			+ Constants.EQUALTO + hostResCode
	+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(telecomSubscriberInfo_HostRes.getErrorDesc()) ?"NA" :telecomSubscriberInfo_HostRes.getErrorDesc());
	hostReportDetails.setHostOutParams(hostOutputParam);

	callInfo.setHostReportDetails(hostReportDetails);
	ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

	callInfo.updateHostDetails(ivrdata);
	//End Reporting
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_, "Exception: " + e);}
		}
	return telecomSubscriberInfo_HostRes;
	}

	@Override
	public Utility_BeneficiaryDetailList_HostRes getElecBillBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getElecBillBenfListHostRes()");}

		Utility_BeneficiaryDetailList_HostRes beanResponse = new Utility_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
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
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			

			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					ArrayList<String> utilityCodeList = new ArrayList<String>();
					ArrayList<String> serviceProviderList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					Utility_BeneficiaryShortDetails utily_BeneficiaryShortDetails = null;
					HashMap<String, Utility_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}


					/**
					 * For Utility and Service Provider Code handling
					 */
					HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>> serviceProviderCodeMap = beanResponse.getServiceCodeMap();
					HashMap<String, ArrayList<String>> utilityCodeMap = beanResponse.getUtilityCodeMap();
					ArrayList<Utility_BeneficiaryShortDetails> utilityShorDtlsList = null;
					ArrayList<String> serviceProviderCodeList = null;
					String strTempCode = Constants.EMPTY_STRING;
					String strServCode = Constants.EMPTY_STRING;
					//END

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){


								/**
								 * For Utility and Service Provider Code handling
								 */

								if(serviceProviderCodeMap == null){
									serviceProviderCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>>();
								}

								if(utilityCodeMap == null){
									utilityCodeMap = new HashMap<String, ArrayList<String>>();
								}

								//END


								utily_BeneficiaryShortDetails = new Utility_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								utily_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								utily_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								utily_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								utily_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}

								if(serviceProviderList!=null && !serviceProviderList.contains(beneAcctType.getServiceProviderCode())){
									serviceProviderList.add(beneAcctType.getServiceProviderCode());
								}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}


								utily_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								utily_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								utily_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								if(utilityCodeList!=null && !utilityCodeList.contains(beneAcctType.getUtilityCode())){
									utilityCodeList.add(beneAcctType.getUtilityCode());
								}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}


								utily_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}



								/**
								 * For Utility and Service Provider Code handling
								 */

								strTempCode = utily_BeneficiaryShortDetails.getUtilityCode();
								strServCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility code is "+ strTempCode);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strServCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									serviceProviderCodeList = utilityCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code list is "+ serviceProviderCodeList);}

									if(util.isNullOrEmpty(serviceProviderCodeList)){
										serviceProviderCodeList = new ArrayList<String>();
									}

									
									//Added on 09-Apr-2013 - to neglect duplicate service provider code from the list
									if(!serviceProviderCodeList.contains(strServCode)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding new service provider code "+ strServCode);}
										serviceProviderCodeList.add(strServCode);
									}
									//END
									
									
//									serviceProviderCodeList.add(strServCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Service Provider code " +strServCode+" for the utility code is"+strTempCode);}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Service Provider code arraylist object  " +serviceProviderCodeList+" detail into Utility Code Map"+utilityCodeMap);}
									utilityCodeMap.put(strTempCode, serviceProviderCodeList);

								}



								strTempCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strTempCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									utilityShorDtlsList = serviceProviderCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Short Detail list is "+ utilityShorDtlsList);}

									if(util.isNullOrEmpty(utilityShorDtlsList)){
										utilityShorDtlsList = new ArrayList<Utility_BeneficiaryShortDetails>();
									}

									utilityShorDtlsList.add(utily_BeneficiaryShortDetails);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary short deatils " +utily_BeneficiaryShortDetails+" for the Service provider code is"+utily_BeneficiaryShortDetails.getServiceProviderCode());}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility short details arraylist object  " +utilityShorDtlsList+" detail into serviceprovidermap"+serviceProviderCodeMap);}
									serviceProviderCodeMap.put(strTempCode, utilityShorDtlsList);

								}
								//END 



							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), utily_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);
					beanResponse.setServiceProviderList(serviceProviderList);
					beanResponse.setUtilityCodeList(utilityCodeList);

					/**
					 * For Utility and Service Provider Code handling
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Code Map Object is "+utilityCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider Code Map Object is "+serviceProviderCodeMap);}

					beanResponse.setUtilityCodeMap(utilityCodeMap);
					beanResponse.setServiceCodeMap(serviceProviderCodeMap);
					//END

				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getElecBillBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getElecBillBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getMobBroadBenfListHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BeneficiaryDetailList_HostRes getWaterBillBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getWaterBillBenfListHostRes()");}

		Utility_BeneficiaryDetailList_HostRes beanResponse = new Utility_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+code + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
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
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					ArrayList<String> utilityCodeList = new ArrayList<String>();
					ArrayList<String> serviceProviderList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					Utility_BeneficiaryShortDetails utily_BeneficiaryShortDetails = null;
					HashMap<String, Utility_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					/**
					 * For Utility and Service Provider Code handling
					 */
					HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>> serviceProviderCodeMap = beanResponse.getServiceCodeMap();
					HashMap<String, ArrayList<String>> utilityCodeMap = beanResponse.getUtilityCodeMap();
					
					ArrayList<Utility_BeneficiaryShortDetails> utilityShorDtlsList = null;
					ArrayList<String> serviceProviderCodeList = null;
					String strTempCode = Constants.EMPTY_STRING;
					String strServCode = Constants.EMPTY_STRING;
					//END

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){
								/**
								 * For Utility and Service Provider Code handling
								 */

								if(serviceProviderCodeMap == null){
									serviceProviderCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>>();
								}

								if(utilityCodeMap == null){
									utilityCodeMap = new HashMap<String, ArrayList<String>>();
								}

								//END

								utily_BeneficiaryShortDetails = new Utility_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								utily_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								utily_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								utily_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								utily_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}

								utily_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}
								if(serviceProviderList!=null && !serviceProviderList.contains(beneAcctType.getServiceProviderCode())){
									serviceProviderList.add(beneAcctType.getServiceProviderCode());
								}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}


								utily_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								utily_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}
								if(utilityCodeList!=null && !utilityCodeList.contains(beneAcctType.getUtilityCode())){
									utilityCodeList.add(beneAcctType.getUtilityCode());
								}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}

								utily_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}

								/**
								 * For Utility and Service Provider Code handling
								 */

								strTempCode = utily_BeneficiaryShortDetails.getUtilityCode();
								strServCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility code is "+ strTempCode);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service code is "+ strServCode);}
								
								if(!util.isNullOrEmpty(strTempCode)){

									serviceProviderCodeList = utilityCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code list is "+ serviceProviderCodeList);}

									if(util.isNullOrEmpty(serviceProviderCodeList)){
										serviceProviderCodeList = new ArrayList<String>();
									}

									//Added on 09-Apr-2013 - to neglect duplicate service provider code from the list
									if(!serviceProviderCodeList.contains(strServCode)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding new service provider code "+ strServCode);}
										serviceProviderCodeList.add(strServCode);
									}
									//END
									
//									serviceProviderCodeList.add(strServCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the Service provide code " +strServCode+" for the utility code is"+strTempCode);}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Service Provider details arraylist object  " +serviceProviderCodeList+" detail into Utility Code Map"+utilityCodeMap);}
									utilityCodeMap.put(strTempCode, serviceProviderCodeList);

								}



								strTempCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strTempCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									utilityShorDtlsList = serviceProviderCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Short Detail list is "+ utilityShorDtlsList);}

									if(util.isNullOrEmpty(utilityShorDtlsList)){
										utilityShorDtlsList = new ArrayList<Utility_BeneficiaryShortDetails>();
									}

									utilityShorDtlsList.add(utily_BeneficiaryShortDetails);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary short deatils " +utily_BeneficiaryShortDetails+" for the Service provider code is"+utily_BeneficiaryShortDetails.getServiceProviderCode());}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility short details arraylist object  " +utilityShorDtlsList+" detail into serviceprovidermap"+serviceProviderCodeMap);}
									serviceProviderCodeMap.put(strTempCode, utilityShorDtlsList);

								}

								//END 
							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), utily_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}
					beanResponse.setServiceProviderList(serviceProviderList);
					beanResponse.setUtilityCodeList(utilityCodeList);
					beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);

					/**
					 * For Utility and Service Provider Code handling
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Code Map Object is "+utilityCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider Code Map Object is "+serviceProviderCodeMap);}

					beanResponse.setUtilityCodeMap(utilityCodeMap);
					beanResponse.setServiceCodeMap(serviceProviderCodeMap);
					//END 
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getWaterBillBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getWaterBillBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getWaterBillBenfListHostRes()");}
		return beanResponse;
	}

	@Override
	public Utility_BeneficiaryDetailList_HostRes getSchoolBillBenfListHostRes(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getSchoolBillBenfListHostRes()");}

		Utility_BeneficiaryDetailList_HostRes beanResponse = new Utility_BeneficiaryDetailList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+code + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
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
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}

			

			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					ArrayList<String> utilityCodeList = new ArrayList<String>();
					ArrayList<String> serviceProviderList = new ArrayList<String>();
					BeneAcctType beneAcctType = null;
					Utility_BeneficiaryShortDetails utily_BeneficiaryShortDetails = null;
					HashMap<String, Utility_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}


					/**
					 * For Utility and Service Provider Code handling
					 */
					HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>> serviceProviderCodeMap = beanResponse.getServiceCodeMap();
					HashMap<String, ArrayList<String>> utilityCodeMap = beanResponse.getUtilityCodeMap();
					ArrayList<Utility_BeneficiaryShortDetails> utilityShorDtlsList = null;
					ArrayList<String>  serviceProviderCodeList = null;
					String strTempCode = Constants.EMPTY_STRING;
					String strServCode = Constants.EMPTY_STRING;
					//END

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){



								/**
								 * For Utility and Service Provider Code handling
								 */

								if(serviceProviderCodeMap == null){
									serviceProviderCodeMap = new HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>>();
								}

								if(utilityCodeMap == null){
									utilityCodeMap = new HashMap<String, ArrayList<String>>();
								}

								//END




								utily_BeneficiaryShortDetails = new Utility_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								utily_BeneficiaryShortDetails.setBeneficiaryId(beneAcctType.getBeneficiaryId());
								
								utily_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								utily_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								utily_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}
								if(serviceProviderList!=null && !serviceProviderList.contains(beneAcctType.getServiceProviderCode())){
									serviceProviderList.add(beneAcctType.getServiceProviderCode());
								}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}


								utily_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								utily_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								utily_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}
								if(utilityCodeList!=null && !utilityCodeList.contains(beneAcctType.getUtilityCode())){
									utilityCodeList.add(beneAcctType.getUtilityCode());
								}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}


								utily_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}



								/**
								 * For Utility and Service Provider Code handling
								 */

								strTempCode = utily_BeneficiaryShortDetails.getUtilityCode();
								strServCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility code is "+ strTempCode);}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strServCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									serviceProviderCodeList = utilityCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service provider code list is "+ serviceProviderCodeList);}

									if(util.isNullOrEmpty(serviceProviderCodeList)){
										serviceProviderCodeList = new ArrayList<String>();
									}
									
									//Added on 09-Apr-2013 - to neglect duplicate service provider code from the list
									if(!serviceProviderCodeList.contains(strServCode)){
										if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding new service provider code "+ strServCode);}
										serviceProviderCodeList.add(strServCode);
									}
									//END
									
//									serviceProviderCodeList.add(strServCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the service provider code " +strServCode+" for the utility code is"+ strTempCode);}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Service Provider code arraylist object  " +serviceProviderCodeList+" detail into Utility Code Map"+utilityCodeMap);}
									utilityCodeMap.put(strTempCode, serviceProviderCodeList);

								}



								strTempCode = utily_BeneficiaryShortDetails.getServiceProviderCode();
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider code is "+ strTempCode);}

								if(!util.isNullOrEmpty(strTempCode)){

									utilityShorDtlsList = serviceProviderCodeMap.get(strTempCode);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Short Detail list is "+ utilityShorDtlsList);}

									if(util.isNullOrEmpty(utilityShorDtlsList)){
										utilityShorDtlsList = new ArrayList<Utility_BeneficiaryShortDetails>();
									}

									utilityShorDtlsList.add(utily_BeneficiaryShortDetails);
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the beneficiary short deatils " +utily_BeneficiaryShortDetails+" for the Service provider code is"+utily_BeneficiaryShortDetails.getServiceProviderCode());}

									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting the Utility short details arraylist object  " +utilityShorDtlsList+" detail into serviceprovidermap"+serviceProviderCodeMap);}
									serviceProviderCodeMap.put(strTempCode, utilityShorDtlsList);

								}

								//END 



							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), utily_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setBenefShortDescDetailMap(shortDescDetailMap);
					beanResponse.setServiceProviderList(serviceProviderList);
					beanResponse.setUtilityCodeList(utilityCodeList);



					/**
					 * For Utility and Service Provider Code handling
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Utility Code Map Object is "+utilityCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Service Provider Code Map Object is "+serviceProviderCodeMap);}

					beanResponse.setUtilityCodeMap(utilityCodeMap);
					beanResponse.setServiceCodeMap(serviceProviderCodeMap);
					//END

				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getSchoolBillBenfListHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getSchoolBillBenfListHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getSchoolBillBenfListHostRes()");}
		return beanResponse;
	}

	@Override
	public TPR_RetrieveBenfPayeeList_HostRes getTPRBeneficiaryPayeeList(
			CallInfo callInfo, String customerID, String paymentType, String requestType)
					throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getTPRBeneficiaryPayeeList()");}

		TPR_RetrieveBenfPayeeList_HostRes beanResponse = new TPR_RetrieveBenfPayeeList_HostRes();
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			BeneficiaryListInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method BeneficiaryListInq host service");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = listBeneficiaryService.callBeneficiaryListHost(logger, sessionID, customerID, paymentType, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of BeneficiaryListInq is : "+code);}
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
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ListBeneficiary HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC" + ws_ResponseHeader.getEsbErrDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ListBeneficiary_Succ_ErrorCode);
			}


			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);

			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					List<BeneAcctType> benefAcctTypeList = new ArrayList<BeneAcctType>();
					ArrayList<String> benefIDList = new ArrayList<String>();
					ArrayList<String> utilityCodeList = new ArrayList<String>();
					ArrayList<String> serviceProviderList = new ArrayList<String>();

					BeneAcctType beneAcctType = null;
					TPR_BeneficiaryShortDetails TPR_BeneficiaryShortDetails = null;
					HashMap<String, TPR_BeneficiaryShortDetails>shortDescDetailMap = new HashMap<String, TPR_BeneficiaryShortDetails>();
					benefAcctTypeList = response.getBeneAcct();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Obtained BenefAccount Type list is :"+benefAcctTypeList);}

					if(!util.isNullOrEmpty(benefAcctTypeList)&& benefAcctTypeList.size() > Constants.GL_ZERO){
						for(int count=0; count < benefAcctTypeList.size(); count++){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, count + "Index of Benef Account type list is"+benefAcctTypeList.get(count).getBeneficiaryId());}

							beneAcctType = benefAcctTypeList.get(count);

							if(!util.isNullOrEmpty(beneAcctType)){

								TPR_BeneficiaryShortDetails = new TPR_BeneficiaryShortDetails();
								benefIDList.add(beneAcctType.getBeneficiaryId());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary id  is "+ beneAcctType.getBeneficiaryId());}

								TPR_BeneficiaryShortDetails.setBenefAcctNumber(beneAcctType.getBeneficiaryAccountNumber());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Account number is "+ beneAcctType.getBeneficiaryAccountNumber());}

								TPR_BeneficiaryShortDetails.setMnemonics(beneAcctType.getMnemonic());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Mneumonic is "+ beneAcctType.getMnemonic());}

								TPR_BeneficiaryShortDetails.setServiceProviderCode(beneAcctType.getServiceProviderCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}
								if(serviceProviderList!=null && !serviceProviderList.contains(beneAcctType.getServiceProviderCode())){
									serviceProviderList.add(beneAcctType.getServiceProviderCode());
								}
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider code is "+ beneAcctType.getServiceProviderCode());}


								TPR_BeneficiaryShortDetails.setServiceProviderDesc(beneAcctType.getServiceProviderDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Service Provider desc is "+ beneAcctType.getServiceProviderDescription());}

								TPR_BeneficiaryShortDetails.setShorDescription(beneAcctType.getShortDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Shor Desc is "+ beneAcctType.getShortDescription());}

								TPR_BeneficiaryShortDetails.setUtilityCode(beneAcctType.getUtilityCode());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}
								if(utilityCodeList!=null && !utilityCodeList.contains(beneAcctType.getUtilityCode())){
									utilityCodeList.add(beneAcctType.getUtilityCode());
								}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code is "+ beneAcctType.getUtilityCode());}


								TPR_BeneficiaryShortDetails.setUtilityDescription(beneAcctType.getUtilityDescription());
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Beneficiary Utility Code Desc is "+ beneAcctType.getUtilityDescription());}

							}
							shortDescDetailMap.put(beneAcctType.getBeneficiaryId(), TPR_BeneficiaryShortDetails);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserted the Beneficiary Short Description map in the bean Object"+shortDescDetailMap);}

						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The total beneficiary ID available is "+benefIDList.size());}

					beanResponse.setBeneficiaryIdList(benefIDList);
					//	beanResponse(benefIDList);
					//	beanResponse.setBeneficiaryIdList(benefIDList);
					beanResponse.setTPR_BeneficiaryShortDetails(shortDescDetailMap);
					beanResponse.setServiceProviderList(serviceProviderList);
					beanResponse.setUtilityCodeList(utilityCodeList);


				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received null / empty List<BeneAcctType> response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Received Failure response so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}
		catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ListBeneficiaryDAOImpl.getTPRBeneficiaryPayeeList() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ListBeneficiaryDAOImpl.getTPRBeneficiaryPayeeList() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ListBeneficiaryDAOImpl.getTPRBeneficiaryPayeeList()");}
		return beanResponse;
	}

}
