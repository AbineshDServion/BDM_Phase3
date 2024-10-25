package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.exchangeRates.ExchangeRateInquiry_HostRes;
import com.servion.model.fundsTransfer.FT_ExchangeRateDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_ExchangeRateDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface ExchngRateInqDAO {

	/**
	 * @param currencyCode
	 * @return
	 * @throws DaoException
	 */
	FT_ExchangeRateDetails_HostRes getFTExchangeRateHostRes (CallInfo callInfo, String currencyCode, String customerID, String ccyMarket, String requestType) throws DaoException;

	/**
	 * @param currencyCode
	 * @return
	 * @throws DaoException
	 */
	TPR_ExchangeRateDetails_HostRes getTPRemittanceExchangeHostRes (CallInfo callInfo, String currencyCode, String customerID, String ccyMarket, String requestType) throws DaoException;

	/**
	 * @param currencyCode
	 * @return
	 * @throws DaoException
	 */
	ExchangeRateInquiry_HostRes getExchangeRatesHostRes (CallInfo callInfo, String currencyCode, String customerID, String ccyMarket, String requestType) throws DaoException;

}
