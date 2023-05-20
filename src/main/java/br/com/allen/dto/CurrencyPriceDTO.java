package br.com.allen.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
@Builder
@AllArgsConstructor
public class CurrencyPriceDTO {

    @JsonProperty("USDBRL")
    public USDBRL usdbrl;
}
