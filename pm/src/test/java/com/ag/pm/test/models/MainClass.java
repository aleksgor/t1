package com.nomad.pm.test.models;

import java.util.Arrays;
import java.util.Date;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class MainClass implements Model {

    private long mainId;
    private String mainName;
    private Date mainDate;
    private long childId;
    private long secondChildId;
    private Child child;
    private Child secondChild;
    private Child thirdChild;
    private Identifier identifier;
    private byte[] blob;

    public MainClass() {
        super();
    }

    public MainClass(TestId identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
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
        this.identifier = id;

    }

    @Override
    public String getModelName() {
        return new TestId().getModelName();
    }

    public long getMainId() {
        return mainId;
    }

    public void setMainId(long mainId) {
        this.mainId = mainId;
    }

    public String getMainName() {
        return mainName;
    }

    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    public Date getMainDate() {
        return mainDate;
    }

    public void setMainDate(Date mainDate) {
        this.mainDate = mainDate;
    }

    public long getSecondChildId() {
        return secondChildId;
    }

    public void setSecondChildId(long secondChildId) {
        this.secondChildId = secondChildId;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(blob);
        result = prime * result + ((child == null) ? 0 : child.hashCode());
        result = prime * result + (int) (childId ^ (childId >>> 32));
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((mainDate == null) ? 0 : mainDate.hashCode());
        result = prime * result + (int) (mainId ^ (mainId >>> 32));
        result = prime * result + ((mainName == null) ? 0 : mainName.hashCode());
        result = prime * result + ((secondChild == null) ? 0 : secondChild.hashCode());
        result = prime * result + (int) (secondChildId ^ (secondChildId >>> 32));
        result = prime * result + ((thirdChild == null) ? 0 : thirdChild.hashCode());
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
        MainClass other = (MainClass) obj;
        if (!Arrays.equals(blob, other.blob))
            return false;
        if (child == null) {
            if (other.child != null)
                return false;
        } else if (!child.equals(other.child))
            return false;
        if (childId != other.childId)
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (mainDate == null) {
            if (other.mainDate != null)
                return false;
        } else if (!mainDate.equals(other.mainDate))
            return false;
        if (mainId != other.mainId)
            return false;
        if (mainName == null) {
            if (other.mainName != null)
                return false;
        } else if (!mainName.equals(other.mainName))
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
        return true;
    }

    @Override
    public String toString() {
        return "MainClass [mainId=" + mainId + ", mainName=" + mainName + ", mainDate=" + mainDate + ", childId=" + childId + ", secondChildId="
                + secondChildId + ", child=" + child + ", secondChild=" + secondChild + ", identifier=" + identifier + "]";
    }

}
