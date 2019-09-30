package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.DataSourceNode;

public class DataSourceFigure extends Figure {
  private DataSourceNode dataSource;
  private Label text;

  public DataSourceFigure(DataSourceNode dataSource) {
    this.dataSource = dataSource;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label(getText());
    text.setToolTip(new Label(getTTip()));
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    add(text);
    setBorder(new LineBorder(1));
    setOpaque(true);
  }
  private String getText(){
    return "DataSource name:" + dataSource.getDataSource().getName()  + " threads:"
        + dataSource.getDataSource().getThreads();
  }
  private String getTTip(){
    return "name:" + dataSource.getDataSource().getName() + " threads:" + dataSource.getDataSource().getThreads() + " class:"
        + dataSource.getDataSource().getClazz();
  }
  public void setLayout(Rectangle rect) {
    text.setText(getText());
    text.setToolTip(new Label(getTTip()));
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}