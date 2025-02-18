package app.trademapper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Trade {
    private String name;
    private String date;
    private Integer productId;
    private String currency;
    private Double price;

    public Trade(String date, int productId, String currency, double price) {
        this.date = date;
        this.productId = productId;
        this.currency = currency;
        this.price = price;
    }
}
