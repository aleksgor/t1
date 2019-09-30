package com.nomad.cache.test.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class MainTestModel implements Model {

    private long id;
    private String name;
    private Date date;
    private long childId;
    private long secondChildId;
    private long thirdChildId;
    private Child child;
    private Child secondChild;
    private Child thirdChild;
    private byte[] blob;
    private Identifier identifier;
    private double money;
    private final List<Price> prices = new ArrayList<>();


    public MainTestModel() {
        super();
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public MainTestModel(MainTestModelId identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public Identifier getIdentifier() {
        if (identifier == null) {
            identifier = new MainTestModelId(id);
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getChildId() {
        return childId;
    }

    public void setChildId(long childId) {
        this.childId = childId;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    @Override
    public void setIdentifier(Identifier id) {
        identifier = id;

    }

    @Override
    public String getModelName() {
        return new MainTestModelId().getModelName();
    }

    public long getSecondChildId() {
        return secondChildId;
    }

    public void setSecondChildId(long secondChildId) {
        this.secondChildId = secondChildId;
    }

    public long getThirdChildId() {
        return thirdChildId;
    }

    public void setThirdChildId(long thirdChildId) {
        this.thirdChildId = thirdChildId;
    }

    public Child getSecondChild() {
        return secondChild;
    }

    public void setSecondChild(Child secondChild) {
        this.secondChild = secondChild;
    }

    public Child getThirdChild() {
        return thirdChild;
    }

    public void setThirdChild(Child thirdChild) {
        this.thirdChild = thirdChild;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    
    public List<Price> getPrices() {
        return prices;
    }
    
    public void setPrices(List<Price> prices) {
         this.prices.clear();
         this.prices.addAll(prices);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(blob);
        result = prime * result + ((child == null) ? 0 : child.hashCode());
        result = prime * result + (int) (childId ^ (childId >>> 32));
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        long temp;
        temp = Double.doubleToLongBits(money);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((secondChild == null) ? 0 : secondChild.hashCode());
        result = prime * result + (int) (secondChildId ^ (secondChildId >>> 32));
        result = prime * result + ((thirdChild == null) ? 0 : thirdChild.hashCode());
        result = prime * result + (int) (thirdChildId ^ (thirdChildId >>> 32));
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
        MainTestModel other = (MainTestModel) obj;
        if (!Arrays.equals(blob, other.blob))
            return false;
        if (child == null) {
            if (other.child != null)
                return false;
        } else if (!child.equals(other.child))
            return false;
        if (childId != other.childId)
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (id != other.id)
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (Double.doubleToLongBits(money) != Double.doubleToLongBits(other.money))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (secondChild == null) {
            if (other.secondChild != null)
                return false;
        } else if (!secondChild.equals(other.secondChild))
            return false;
        if (secondChildId != other.secondChildId)
            return false;
        if (thirdChild == null) {
            if (other.thirdChild != null)
                return false;
        } else if (!thirdChild.equals(other.thirdChild))
            return false;
        if (thirdChildId != other.thirdChildId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MainTestModel [id=" + id + ", name=" + name + ", date=" + date + ", childId=" + childId + ", secondChildId=" + secondChildId + ", thirdChildId=" + thirdChildId + ", child=" + child + ", secondChild=" + secondChild
                + ", thirdChild=" + thirdChild + ", blob=" + Arrays.toString(blob) + ", identifier=" + identifier + ", money=" + money + ", prices=" + prices + "]";
    }

 
}
