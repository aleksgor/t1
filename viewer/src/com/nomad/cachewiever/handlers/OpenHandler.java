package com.nomad.cachewiever.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nomad.cachewiever.editors.MyEditorInput;

public class OpenHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
//    MessageDialog.openInformation(Display.getDefault().getActiveShell(),
 //       "Annotate", "For now: write in the margins of the real book.");
    
    
    IWorkbench wb = PlatformUI.getWorkbench();
   IWorkbenchPage wbPage = wb.getActiveWorkbenchWindow().getActivePage();
     
     try {
       IEditorPart ep= wbPage.openEditor(new MyEditorInput("eee"), "com.nomad.cachewiever.editors.CacheEditor");
       wbPage.activate(ep);
     } catch (PartInitException e) {
       e.printStackTrace();
     }
    return null;
  }

}
