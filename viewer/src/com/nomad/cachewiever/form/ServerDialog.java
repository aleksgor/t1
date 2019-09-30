package com.nomad.cachewiever.form;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ServerDialog extends Dialog {

  Shell parent;

  Shell dialog;

  Text host;

  Text port;
  

  boolean ok = false;

  public ServerDialog(Shell parent) {
    super(parent, SWT.APPLICATION_MODAL);
    this.parent = parent;
    this.setText("Connect to server");
  }

  public ServerDialog(Shell parent, int stat, String type) {
    super(parent, stat);
    this.parent = parent;
  }

  protected void init(Shell shell, String host, int port) {
    FormLayout gl = new FormLayout();
    shell.setLayout(gl);
    gl.marginHeight = 10;
    gl.marginLeft = 10;
    gl.marginWidth = 10;
    Label l = new Label(shell, SWT.NONE);
    l.setText("Host");
    FormData data1 = new FormData();
    data1.left = new FormAttachment(0, 5);
    data1.width=100;
    data1.top = new FormAttachment(0, 10);
    l.setLayoutData(data1);

    Label lp = new Label(shell, SWT.NONE);
    lp.setText("Port");

    FormData data2 = new FormData();
    data2.left = new FormAttachment(0, 5);
    data2.width=100;
    data2.top = new FormAttachment(l, 10);
    lp.setLayoutData(data2);

    
    this.host = new Text(shell, SWT.BORDER);
    this.host.setText(host);
    FormData data11 = new FormData();
    data11.left = new FormAttachment(lp, 5);
    data11.top = new FormAttachment(0, 5);
    data11.width = 250;
    this.host.setLayoutData(data11);

    this.port = new Text(shell, SWT.BORDER);

    this.port.addListener(SWT.Verify, new Listener() {
      public void handleEvent(Event event) {
        String text = event.text;
        for (int i = 0; i < text.length(); i++) {
          char ch = text.charAt(i);
          if (!('0' <= ch && ch <= '9')) {
            event.doit = false;
            return;
          }
        }
      }
    });

  
    
 
    
    this.port.setText(Integer.toString(port));
    this.port.setTextLimit(5);
    FormData data21 = new FormData();
    data21.left = new FormAttachment(lp, 5);
    data21.top = new FormAttachment(l, 5);
    data21.width = 250;
    this.port.setLayoutData(data21);

    Button b = new Button(shell, SWT.PUSH);
    b.setText("Ok");
    b.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    });
    FormData data4 = new FormData();
    data4.top = new FormAttachment(lp, 10);
    b.setLayoutData(data4);

    Button b1 = new Button(shell, SWT.PUSH);
    b1.setText("Cancel");
    b1.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    });
    FormData data6 = new FormData();
    data6.left = new FormAttachment(b, 5);
    data6.top = new FormAttachment(lp, 10);
    b1.setLayoutData(data6);

  }

  private void cancel() {
    ok = false;
    dialog.dispose();
  }

  /**
   * return array of string 0- name 1-type
   * 
   * @return
   */
  public String[] open(String host, int port) {
    if (host == null) {
      host = "";
    }

    try {
      dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
      Rectangle rc = parent.getBounds();
      // int w = 400; int h = 300;
      init(dialog, host, port);
      dialog.pack();
      int w = dialog.getBounds().width;
      int h = dialog.getBounds().height;
      Rectangle ps = new Rectangle(rc.x, rc.y, w, h);
      ps.x = rc.x + (rc.width / 2) - (w / 2);
      ps.y = rc.y + (rc.height / 2) - (h / 2);
      dialog.setBounds(ps);
      dialog.open();
      Display display = parent.getDisplay();
      while (!dialog.isDisposed()) {
        if (!display.readAndDispatch())
          display.sleep();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return ret;
  }

  String[] ret;

  private void ok() {
    ok = true;
    ret = new String[2];
    ret[0] = this.host.getText();
    ret[1] = this.port.getText();

    dialog.dispose();
  }

  public static void main(String[] args) {
    Shell shell = new Shell();
    ServerDialog dialog = new ServerDialog(shell);
    dialog.open("u", 1);
  }

}
