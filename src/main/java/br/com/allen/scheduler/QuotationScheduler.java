package br.com.allen.scheduler;

import br.com.allen.service.QuotationService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class QuotationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuotationScheduler.class);
    @Inject
    QuotationService quotationService;

    @Transactional
    @Scheduled(every = "35s", identity = "task-job")
    void schedule() {
        LOGGER.info("Enviando requisição ao client externo");
        quotationService.getCurrencyPrice();
    }
}
