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
@Table(name = "invoice_line_fee_lines")
public class InvoiceLineFeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_line_id")
    private InvoiceLineEntity line;

    private String name;
    private BigDecimal amount;

    protected InvoiceLineFeeEntity() {
    }

    public InvoiceLineFeeEntity(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public InvoiceLineEntity getLine() { return line; }
    public void setLine(InvoiceLineEntity line) { this.line = line; }
    public String getName() { return name; }
    public BigDecimal getAmount() { return amount; }
}
