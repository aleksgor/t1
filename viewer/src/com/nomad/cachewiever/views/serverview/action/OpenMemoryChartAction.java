package com.nomad.cachewiever.views.serverview.action;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

import com.nomad.cachewiever.utility.OpenViewUtilite;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.cachewiever.views.serverview.ServerView;

public class OpenMemoryChartAction implements IAction {
  ServerView viewer;
  public OpenMemoryChartAction(ServerView viewer) {
    this.viewer = viewer;
  }

  @Override
  public void addPropertyChangeListener(IPropertyChangeListener listener) {

  }

  @Override
  public int getAccelerator() {

    return 0;
  }

  @Override
  public String getActionDefinitionId() {
    return null;
  }

  @Override
  public String getDescription() {

    return "Description";
  }

  @Override
  public ImageDescriptor getDisabledImageDescriptor() {
    return null;
  }

  @Override
  public HelpListener getHelpListener() {
    return null;
  }

  @Override
  public ImageDescriptor getHoverImageDescriptor() {
    return null;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public IMenuCreator getMenuCreator() {
    return null;
  }

  @Override
  public int getStyle() {
    return IAction.AS_PUSH_BUTTON;
  }

  @Override
  public String getText() {

    return "Open memory chart";
  }

  @Override
  public String getToolTipText() {

    return "ConnectToolTip";
  }

  @Override
  public boolean isChecked() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled() {
    return false;
  }

  @Override
  public void removePropertyChangeListener(IPropertyChangeListener listener) {

  }

  @Override
  public void run() {

  }

  @Override
  public void runWithEvent(Event event) {
    Server server = viewer.getColleagueModel();
    try {
      OpenViewUtilite.openMemoryChartView(server);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void setActionDefinitionId(String id) {

  }

  @Override
  public void setChecked(boolean checked) {

  }

  @Override
  public void setDescription(String text) {

  }

  @Override
  public void setDisabledImageDescriptor(ImageDescriptor newImage) {

  }

  @Override
  public void setEnabled(boolean enabled) {

  }

  @Override
  public void setHelpListener(HelpListener listener) {

  }

  @Override
  public void setHoverImageDescriptor(ImageDescriptor newImage) {

  }

  @Override
  public void setId(String id) {

  }

  @Override
  public void setImageDescriptor(ImageDescriptor newImage) {

  }

  @Override
  public void setMenuCreator(IMenuCreator creator) {

  }

  @Override
  public void setText(String text) {

  }

  @Override
  public void setToolTipText(String text) {

  }

  @Override
  public void setAccelerator(int keycode) {

  }

}
