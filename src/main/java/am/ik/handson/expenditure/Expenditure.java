package am.ik.handson.expenditure;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Either;

import java.time.LocalDate;

public class Expenditure {

    private Integer expenditureId;

    private String expenditureName;

    private int price;

    private int quantity;

    private LocalDate expenditureDate;
    
    private static Validator<Expenditure> validator = ValidatorBuilder.of(Expenditure.class)
        .constraint(Expenditure::getExpenditureId, "expenditureId", c -> c.isNull())
        .constraint(Expenditure::getExpenditureName, "expenditureName", c -> c.notEmpty().lessThan(255))
        .constraint(Expenditure::getPrice, "price", c -> c.greaterThan(0))
        .constraint(Expenditure::getQuantity, "quantity", c -> c.greaterThan(0))
        .constraintOnObject(Expenditure::getExpenditureDate, "expenditureDate", c -> c.notNull())
        .build();

    Expenditure() {
    }

    Expenditure(Integer expenditureId, String expenditureName, int price, int quantity, LocalDate expenditureDate) {
        this.expenditureId = expenditureId;
        this.expenditureName = expenditureName;
        this.price = price;
        this.quantity = quantity;
        this.expenditureDate = expenditureDate;
    }

    public Integer getExpenditureId() {
        return expenditureId;
    }

    public void setExpenditureId(Integer expenditureId) {
        this.expenditureId = expenditureId;
    }

    public String getExpenditureName() {
        return expenditureName;
    }

    public void setExpenditureName(String expenditureName) {
        this.expenditureName = expenditureName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpenditureDate() {
        return expenditureDate;
    }

    public void setExpenditureDate(LocalDate expenditureDate) {
        this.expenditureDate = expenditureDate;
    }

    public Either<ConstraintViolations, Expenditure> validate() {
        return validator.validateToEither(this);
    }

    @Override
    public String toString() {
        return "Expenditure{" +
            "expenditureId=" + expenditureId +
            ", expenditureName='" + expenditureName + '\'' +
            ", price=" + price +
            ", quantity=" + quantity +
            ", expenditureDate=" + expenditureDate +
            '}';
    }
}
