package br.com.allen.service;

import br.com.allen.client.CurrencyPriceClient;
import br.com.allen.dto.CurrencyPriceDTO;
import br.com.allen.dto.QuotationDTO;
import br.com.allen.entity.QuotationEntity;
import br.com.allen.message.KafkaEvents;
import br.com.allen.repository.QuotationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class QuotationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuotationService.class);

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
        LOGGER.info("Buscando informações do valor atual da moeda");
        CurrencyPriceDTO currencyPriceInfo = currencyPriceClient.getPriceByPair("USD-BRL");
        if (isPriceUpdateNeeded(currencyPriceInfo)) {
            LOGGER.info("Alteração no preço encontrada. Enviando evento no Kafka");
            QuotationDTO quotationDTO = createQuotationDTO(currencyPriceInfo);
            kafkaEvents.sendNewKafkaEvent(quotationDTO);
        } else {
            LOGGER.info("Sem alteração no valor da moeda");
        }
    }

    /**
     * Checks if an update to the current price is needed based on the provided currency price information.
     *
     * @param currencyPriceInfo the currency price inform
     *
     *
     *
     *                          ation to evaluate
     * @return true if an update to the current price is needed, false otherwise
     */
    private boolean isPriceUpdateNeeded(CurrencyPriceDTO currencyPriceInfo) {
        BigDecimal currentPrice = new BigDecimal(currencyPriceInfo.getUsdbrl().getBid());

        List<QuotationEntity> quotationList = quotationRepository.findAll().list();

        if (quotationList.isEmpty()) {
            LOGGER.info("Sem informações salvas. Atualizando cotação");
            saveQuotation(currencyPriceInfo);
            return true;
        } else {
            QuotationEntity lastDollarPrice = quotationList.get(quotationList.size() - 1);
            if (currentPrice.floatValue() > lastDollarPrice.getCurrencyPrice().floatValue()) {
                LOGGER.info("Salvando nova cotação");
                saveQuotation(currencyPriceInfo);
                return true;
            }
        }

        return false;
    }

    private void saveQuotation(CurrencyPriceDTO currencyPriceInfo) {
        QuotationEntity quotation = createQuotationEntity(currencyPriceInfo);
        quotationRepository.persist(quotation);
        LOGGER.info("Cotação salva: {}", quotation);
    }

    private QuotationDTO createQuotationDTO(CurrencyPriceDTO currencyPriceInfo) {
        return QuotationDTO.builder().currencyPrice(new BigDecimal(currencyPriceInfo.getUsdbrl().getBid())).date(new Date()).build();
    }

    private QuotationEntity createQuotationEntity(CurrencyPriceDTO currencyPriceInfo) {
        QuotationEntity quotation = new QuotationEntity();
        quotation.setDate(new Date());
        quotation.setCurrencyPrice(new BigDecimal(currencyPriceInfo.getUsdbrl().getBid()));
        quotation.setPctChange(currencyPriceInfo.getUsdbrl().getPctChange());
        quotation.setPair("USD-BRL");
        return quotation;
    }
}
