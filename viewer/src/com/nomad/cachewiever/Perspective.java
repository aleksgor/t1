package com.nomad.cachewiever;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
    layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		layout.getEditorArea();
    IFolderLayout folderLeft = layout.createFolder("LEFT" ,IPageLayout.LEFT,0.25f,IPageLayout.ID_EDITOR_AREA);
    layout.createFolder("BTN" ,IPageLayout.BOTTOM ,0.75f,IPageLayout.ID_EDITOR_AREA);
    folderLeft.addView("com.nomad.CacheWiever.ServerView");
//    bottom.addView("com.nomad.cachewiever.views.MemoryChartView");
	}

}
