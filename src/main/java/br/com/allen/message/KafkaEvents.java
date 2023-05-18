package br.com.allen.message;

import br.com.allen.dto.QuotationDTO;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KafkaEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEvents.class);

    @Channel("quotation-channel")
    Emitter<QuotationDTO> quotationRequestEmitter;
    public void sendNewKafkaEvent(QuotationDTO quotation) {
        LOGGER.info("Enviando cotação para tópico kafka");
        quotationRequestEmitter.send(quotation).toCompletableFuture().join();
    }
}
