package com.nomad.saver.adapters;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class MapEntryType {

   @XmlAttribute
   public String key;

   @XmlValue
   public String value;

}