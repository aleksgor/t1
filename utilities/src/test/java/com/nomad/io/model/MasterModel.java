package com.nomad.io.model;

import java.util.Arrays;
import java.util.Date;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class MasterModel implements Model {

    private long id;
    private String name;
    private Date date;
    private long childId;
    private long secondChildId;
    private ChildModel child;
    private ChildModel secondChild;
    private Identifier identifier;
    private byte[] blob;

    public MasterModel() {
        super();
    }

    public MasterModel(MasterModelId identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public Identifier getIdentifier() {
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

    public ChildModel getChild() {
        return child;
    }

    public void setChild(ChildModel child) {
        this.child = child;
    }

    @Override
    public void setIdentifier(Identifier id) {
        this.identifier = id;

    }

    @Override
    public String getModelName() {
        return new MasterModelId().getModelName();
    }

    public ChildModel getSecondChild() {
        return secondChild;
    }

    public void setSecondChild(ChildModel secondChild) {
        this.secondChild = secondChild;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }

    public long getSecondChildId() {
        return secondChildId;
    }

    public void setSecondChildId(long secondChildId) {
        this.secondChildId = secondChildId;
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((secondChild == null) ? 0 : secondChild.hashCode());
        result = prime * result + (int) (secondChildId ^ (secondChildId >>> 32));
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
        MasterModel other = (MasterModel) obj;
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
        return true;
    }

    @Override
    public String toString() {
        return "MasterModel [id=" + id + ", name=" + name + ", date=" + date + ", childId=" + childId + ", secondChildId=" + secondChildId + ", child=" + child + ", secondChild="
                + secondChild + ", identifier=" + identifier + ", blob=" + Arrays.toString(blob) + "]";
    }

}
