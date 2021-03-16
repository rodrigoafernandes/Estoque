package br.com.alura.estoque.model;

import java.math.BigDecimal;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Produto {

    @PrimaryKey(autoGenerate = true)
    private final long id;
    private final String name;
    private final BigDecimal price;
    private final int quantity;

    public Produto(long id, String name, BigDecimal price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }

    public int getQuantity() {
        return quantity;
    }

}
