package br.com.allen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Data
@Builder
@AllArgsConstructor
public class UsdBrl {

    public String code;
    public String codein;
    public String name;
    public String high;
    public String low;
    public String varBid;
    public String variation;
    public String bid;
    public String ask;
    public String timestamp;
    public String createDate;

}
