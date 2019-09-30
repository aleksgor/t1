package com.nomad.cachewiever.editors.model;

import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.model.CommandPluginModel;

public class CommandPluginNode extends Node {
  public static final String PROPERTY_CLASS="class";
  public static final String PROPERTY_POOLSIZE="poolSize";
  public static final String PROPERTY_CHECKDELAY="chechDelay";
  public static final String PROPERTY_TIMEOUT="timeOut";
  public static final String PROPERTY_PROP="$$property$$";
  
  private CommandPluginModel commandPluginModel;

  public CommandPluginNode(CommandPluginModel commandPluginModel) {
    this.commandPluginModel = commandPluginModel;
  }

  public CommandPluginModel getCommandPluginModel() {
    return commandPluginModel;
  }

  public void setCommandPluginModel(CommandPluginModel commandPluginModel) {
    this.commandPluginModel = commandPluginModel;
  }


  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySource.class) {
      if (propertySource == null)
        propertySource = new CommandPluginPropertySource(this);
      return propertySource;
    }
    return null;
  }

}