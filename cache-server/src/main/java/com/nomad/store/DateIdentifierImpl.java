package com.nomad.store;

import com.nomad.model.Identifier;
import com.nomad.server.DateIdentifier;

public class DateIdentifierImpl implements DateIdentifier {

  private final Identifier identifier;
  private long time;

  public DateIdentifierImpl(final Identifier identifier, final long time) {
    super();
    this.identifier = identifier;
    this.time = time;
  }

  @Override
  public Identifier getIdentifier() {
    this.time = System.currentTimeMillis();
    return identifier;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public String toString() {
    return "DateIdentifierImpl [identifier=" + identifier + ", time=" + time + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DateIdentifierImpl other = (DateIdentifierImpl) obj;
    if (identifier == null) {
      if (other.identifier != null) {
        return false;
      }
    } else if (!identifier.equals(other.identifier)) {
      return false;
    }
    return true;
  }

}
