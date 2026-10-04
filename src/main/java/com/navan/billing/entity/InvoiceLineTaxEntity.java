package com.navan.billing.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line_tax_lines")
public class InvoiceLineTaxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_line_id")
    private InvoiceLineEntity line;

    private String name;
    private BigDecimal rate;
    private BigDecimal amount;

    protected InvoiceLineTaxEntity() {
    }

    public InvoiceLineTaxEntity(String name, BigDecimal rate, BigDecimal amount) {
        this.name = name;
        this.rate = rate;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public InvoiceLineEntity getLine() { return line; }
    public void setLine(InvoiceLineEntity line) { this.line = line; }
    public String getName() { return name; }
    public BigDecimal getRate() { return rate; }
    public BigDecimal getAmount() { return amount; }
}
