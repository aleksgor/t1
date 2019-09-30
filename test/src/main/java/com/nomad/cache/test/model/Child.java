package com.nomad.cache.test.model;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class Child implements Model {

    private long id;
    private String name;
    private Identifier identifier;

    public Child() {
        super();
    }

    public Child(ChildId identifier) {
        super();
        this.identifier = identifier;
    }

    @Override
    public Identifier getIdentifier() {
        if (identifier == null) {
            identifier = new ChildId(getId());
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Child other = (Child) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public void setIdentifier(Identifier id) {
        this.identifier = id;

    }

    @Override
    public String getModelName() {
        return new ChildId().getModelName();
    }

    @Override
    public String toString() {
        return "Child[ identifier:" + identifier + " id:" + id + " name:" + name + "]";
    }
}
