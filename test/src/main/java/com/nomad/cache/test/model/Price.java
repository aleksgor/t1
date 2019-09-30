package com.nomad.cache.test.model;

import java.util.Date;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class Price implements Model {

    private long id;
    private long mainId;
    private String name;
    private Date startDate;
    private Date endDate;
    private double money;
    private String valuta;
    private Identifier identifier;


    public Price() {
        super();
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public Price(PriceId identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public Identifier getIdentifier() {
        if (identifier == null) {
            identifier = new PriceId(id);
        }
        return identifier;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

  
    public long getMainId() {
        return mainId;
    }

    public void setMainId(long mainId) {
        this.mainId = mainId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getValuta() {
        return valuta;
    }

    public void setValuta(String valuta) {
        this.valuta = valuta;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getModelName() {
        return PriceId.MODEL_NAME;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + (int) (mainId ^ (mainId >>> 32));
        long temp;
        temp = Double.doubleToLongBits(money);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + ((valuta == null) ? 0 : valuta.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Price other = (Price) obj;
        if (endDate == null) {
            if (other.endDate != null)
                return false;
        } else if (!endDate.equals(other.endDate))
            return false;
        if (id != other.id)
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (mainId != other.mainId)
            return false;
        if (Double.doubleToLongBits(money) != Double.doubleToLongBits(other.money))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (startDate == null) {
            if (other.startDate != null)
                return false;
        } else if (!startDate.equals(other.startDate))
            return false;
        if (valuta == null) {
            if (other.valuta != null)
                return false;
        } else if (!valuta.equals(other.valuta))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Price [id=" + id + ", mainId=" + mainId + ", name=" + name + ", startDate=" + startDate + ", endDate=" + endDate + ", money=" + money + ", valuta=" + valuta + ", identifier=" + identifier + "]";
    }

}
