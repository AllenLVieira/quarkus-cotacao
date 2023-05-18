package br.com.allen.service;

import br.com.allen.client.CurrencyPriceClient;
import br.com.allen.dto.CurrencyPriceDTO;
import br.com.allen.dto.QuotationDTO;
import br.com.allen.entity.QuotationEntity;
import br.com.allen.message.KafkaEvents;
import br.com.allen.repository.QuotationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@ApplicationScoped
@AllArgsConstructor
public class QuotationService {

    @Inject
    @RestClient
    CurrencyPriceClient currencyPriceClient;

    @Inject
    QuotationRepository quotationRepository;

    @Inject
    KafkaEvents kafkaEvents;

    /**
     * Retrieves the currency price information and performs necessary actions if an update is needed.
     * This method fetches the currency price information from the CurrencyPriceClient for the "USD-BRL" pair.
     * If an update to the current price is required based on the retrieved information, a new QuotationDTO is created
     * and sent as a Kafka event using KafkaEvents. The update check is done by invoking the isPriceUpdateNeeded method.
     */
    public void getCurrencyPrice() {
        CurrencyPriceDTO currencyPriceInfo = currencyPriceClient.getPriceByPair("USD-BRL");

        if (isPriceUpdateNeeded(currencyPriceInfo)) {
            QuotationDTO quotationDTO = createQuotationDTO(currencyPriceInfo);
            kafkaEvents.sendNewKafkaEvent(quotationDTO);
        }
    }

    /**
     * Checks if an update to the current price is needed based on the provided currency price information.
     *
     * @param currencyPriceInfo the currency price information to evaluate
     * @return true if an update to the current price is needed, false otherwise
     */
    private boolean isPriceUpdateNeeded(CurrencyPriceDTO currencyPriceInfo) {
        BigDecimal currentPrice = new BigDecimal(currencyPriceInfo.getUSDBRL().getBid());

        List<QuotationEntity> quotationList = quotationRepository.findAll().list();

        if (quotationList.isEmpty()) {
            saveQuotation(currencyPriceInfo);
            return true;
        } else {
            QuotationEntity lastDollarPrice = quotationList.get(quotationList.size() - 1);
            if (currentPrice.floatValue() > lastDollarPrice.getCurrencyPrice().floatValue()) {
                saveQuotation(currencyPriceInfo);
                return true;
            }
        }

        return false;
    }

    private void saveQuotation(CurrencyPriceDTO currencyPriceInfo) {
        QuotationEntity quotation = createQuotationEntity(currencyPriceInfo);
        quotationRepository.persist(quotation);
    }

    private QuotationDTO createQuotationDTO(CurrencyPriceDTO currencyPriceInfo) {
        return QuotationDTO.builder()
                .currencyPrice(new BigDecimal(currencyPriceInfo.getUSDBRL().getBid()))
                .date(new Date())
                .build();
    }

    private QuotationEntity createQuotationEntity(CurrencyPriceDTO currencyPriceInfo) {
        QuotationEntity quotation = new QuotationEntity();
        quotation.setDate(new Date());
        quotation.setCurrencyPrice(new BigDecimal(currencyPriceInfo.getUSDBRL().getBid()));
        quotation.setPctChange(currencyPriceInfo.getUSDBRL().getPctChange());
        quotation.setPair("USD-BRL");
        return quotation;
    }
}
