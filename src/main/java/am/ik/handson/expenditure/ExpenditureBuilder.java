package am.ik.handson.expenditure;

import java.time.LocalDate;

public class ExpenditureBuilder {

    private int price;

    private LocalDate expenditureDate;

    private Integer expenditureId;

    private String expenditureName;

    private int quantity;

    public Expenditure createExpenditure() {
        return new Expenditure(expenditureId, expenditureName, price, quantity, expenditureDate);
    }

    public ExpenditureBuilder withPrice(int price) {
        this.price = price;
        return this;
    }

    public ExpenditureBuilder withExpenditureDate(LocalDate expenditureDate) {
        this.expenditureDate = expenditureDate;
        return this;
    }

    public ExpenditureBuilder withExpenditureId(Integer expenditureId) {
        this.expenditureId = expenditureId;
        return this;
    }

    public ExpenditureBuilder withExpenditureName(String expenditureName) {
        this.expenditureName = expenditureName;
        return this;
    }

    public ExpenditureBuilder withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }
}