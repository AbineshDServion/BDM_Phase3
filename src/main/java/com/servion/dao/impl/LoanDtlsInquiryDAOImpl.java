package com.servion.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.accountmanagementservice.AcctInfoType;
import com.bankmuscat.esb.accountmanagementservice.IntliqdAccInfoType;
import com.bankmuscat.esb.accountmanagementservice.LoanDtls;
import com.bankmuscat.esb.accountmanagementservice.LoanDtlsInquiryResType;
import com.bankmuscat.esb.commontypes.BankInfoType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.LoanDtlsInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.loanBalance.LoanBalanceDetails_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.LoanDtlsInquiryService;
import com.servion.ws.util.DAOLayerUtils;

public class LoanDtlsInquiryDAOImpl implements LoanDtlsInquiryDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	LoanDtlsInquiryService loanDtlsInquiryService;

	public LoanDtlsInquiryService getLoanDtlsInquiryService() {
		return loanDtlsInquiryService;
	}

	public void setLoanDtlsInquiryService(
			LoanDtlsInquiryService loanDtlsInquiryService) {
		this.loanDtlsInquiryService = loanDtlsInquiryService;
	}


	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public LoanBalanceDetails_HostRes getLoanBalanceHostRes(CallInfo callInfo,
			String contractID, String requestType) throws DaoException {
		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: LoanDtlsInquiryDAOImpl.getLoanBalanceHostRes()");}


		LoanBalanceDetails_HostRes beanResponse = new LoanBalanceDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			LoanDtlsInquiryResType response = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callLoanBalanceHost");}			

			
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}

			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = loanDtlsInquiryService.callLoanBalanceHost(logger, sessionID, contractID, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of getLoanBalanceHostRes is : "+code);}
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

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### LoanDtlsInquiry HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_LoanDtlsInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_LoanDtlsInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_LoanDtlsInquiry_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					AcctInfoType acctInfoType = response.getAcctInfo();
					BankInfoType bankInfoType = response.getBankInfo();
					LoanDtls loanDtlsType = response.getLoanDtls();
					IntliqdAccInfoType intliqdAccInfoType = response.getIntliqdAcctInfo();


					if(!util.isNullOrEmpty(acctInfoType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Acct ID "+ acctInfoType.getAcctId());}
						beanResponse.setAcctID(acctInfoType.getAcctId());

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Acct ID "+ acctInfoType.getAcctCcy());}
						beanResponse.setAcctCcy(acctInfoType.getAcctCcy());

					}

//					if(!util.isNullOrEmpty(bankInfoType)){
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Account officer "+ bankInfoType.getAccountOfficer());}
//						beanResponse.setAccountOfficer(bankInfoType.getAccountOfficer() + Constants.EMPTY_STRING);
//
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Branch Name "+ bankInfoType.getBranchName());}
//						beanResponse.setBranchName(bankInfoType.getBranchName() + Constants.EMPTY_STRING);
//
//					}

					if(!util.isNullOrEmpty(loanDtlsType)){

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Principal amount "+ loanDtlsType.getPrincipalAmount());}
						beanResponse.setPrincipalAmount(loanDtlsType.getPrincipalAmount() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Category "+ loanDtlsType.getCategory());}
						beanResponse.setCategory(loanDtlsType.getCategory() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Value Date "+ loanDtlsType.getValueDate());}
						beanResponse.setValueDate(loanDtlsType.getValueDate() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## PMaturity Date "+ loanDtlsType.getMaturityDate());}
						beanResponse.setMaturityDate(loanDtlsType.getMaturityDate() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ##Tneor value "+ loanDtlsType.getTenor());}
						beanResponse.setTenor(loanDtlsType.getTenor() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Limit ref "+ loanDtlsType.getLimitRef());}
						beanResponse.setLimitref(loanDtlsType.getLimitRef() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Limit amount "+ loanDtlsType.getLimitAmount());}
						beanResponse.setLimitAmount(loanDtlsType.getLimitAmount() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Contract status "+ loanDtlsType.getContractStatus());}
						beanResponse.setContractStatus(loanDtlsType.getContractStatus() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Draw down account "+ loanDtlsType.getDrawDownAccount());}
						beanResponse.setDrawDownAccount(loanDtlsType.getDrawDownAccount() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Outstanding principal "+ loanDtlsType.getOutStandingPrincipal());}
						beanResponse.setOutstandingAmount(loanDtlsType.getOutStandingPrincipal() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Repayment Date "+ loanDtlsType.getRepaymentDate());}
						beanResponse.setRepaymentDate(loanDtlsType.getRepaymentDate() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Payment amount "+ loanDtlsType.getRepaymentAmount());}
						beanResponse.setRepaymentAmount(loanDtlsType.getRepaymentAmount() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Freq value "+ loanDtlsType.getFreq());}
						beanResponse.setFreq(loanDtlsType.getFreq() + Constants.EMPTY_STRING);

						/**
						 * Below mapping for Due date is done as per T24 Devandar's confirmation
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Due Date value "+ loanDtlsType.getRepaymentDate());}
						beanResponse.setDueDate(loanDtlsType.getRepaymentDate() + Constants.EMPTY_STRING);

						/**
						 * Below mapping for Due date is done as per T24 Devandar's confirmation
						 */
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## EMI / repayment  value "+ loanDtlsType.getRepaymentAmount());}
						beanResponse.seteMI(loanDtlsType.getRepaymentAmount() + Constants.EMPTY_STRING);
					}

					if(!util.isNullOrEmpty(intliqdAccInfoType)){

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## IntLiq Acct Info Type "+ intliqdAccInfoType.getIntRateType());}
						beanResponse.setIntLiqAcct_IntRateType(intliqdAccInfoType.getIntRateType() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Freq value "+ intliqdAccInfoType.getPrinliqAcct());}
						beanResponse.setIntLiqAcct_PrinliqAcct(intliqdAccInfoType.getPrinliqAcct() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## Freq value "+ intliqdAccInfoType.getComLiqAcct());}
						beanResponse.setIntLiqAcct_ComLiqAcct(intliqdAccInfoType.getComLiqAcct() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## ChrgLiqAcct Freq value "+ intliqdAccInfoType.getChrgLiqAcct());}
						beanResponse.setIntLiqAcct_ChrgLiqAcct(intliqdAccInfoType.getChrgLiqAcct() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## AcctId Freq value "+ intliqdAccInfoType.getAcctId());}
						beanResponse.setIntLiqAcct_AcctID(intliqdAccInfoType.getComLiqAcct() + Constants.EMPTY_STRING);

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## Response Field ## tIntRate value "+ intliqdAccInfoType.getIntRate());}
						beanResponse.setIntLiqAcct_IntRate(intliqdAccInfoType.getChrgLiqAcct() + Constants.EMPTY_STRING);

					}

				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field Received null / empty message110Type response object so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at LoanBalanceDetails_HostRes.getLoanBalanceHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at LoanBalanceDetails_HostRes.getLoanBalanceHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: LoanBalanceDetails_HostRes.getLoanBalanceHostRes()");}
		return beanResponse;

	}

}
