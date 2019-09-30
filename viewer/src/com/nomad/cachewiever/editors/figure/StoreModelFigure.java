package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.StoreModelNode;

public class StoreModelFigure extends Figure {
  private StoreModelNode storeModel;
  private Label text;
  public StoreModelFigure(StoreModelNode storeModel) {
    this.storeModel = storeModel;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label("Model:" + storeModel.getStoreModel().getModel() + " pkg:" + storeModel.getStoreModel().getPkg());
    text.setToolTip(new Label("Model:" + storeModel.getStoreModel().getModel() + " pkg:" + storeModel.getStoreModel().getPkg()));
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    add(text);
    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("Model:" + storeModel.getStoreModel().getModel() + " pkg:" + storeModel.getStoreModel().getPkg());
    text.setToolTip(new Label("Model:" + storeModel.getStoreModel().getModel() + " pkg:" + storeModel.getStoreModel().getPkg()));
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}