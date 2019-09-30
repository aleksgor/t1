package com.nomad.cachewiever.views.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.nomad.cache.commonClientServer.ManagementMessageImpl;
import com.nomad.cachewiever.utility.AppData.TimerContent;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.message.ManagementMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.message.MessageSenderReceiverImpl;
import com.nomad.model.ManagerCommand;
import com.nomad.model.MemoryInfo;
import com.nomad.statistic.StatisticPoint;

public class RefreshTimerTask extends TimerTask {

  private Server server;
  private TimerContent tmc;
  

  public RefreshTimerTask(Server server, TimerContent view) {

    this.server = server;
    this.tmc = view;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void run() {
    if (tmc.memoryChartView == null && tmc.modelChartView == null && tmc.chartView == null) {
      return;
    }
    Socket client = null;
    try {
      client = new Socket(server.getServerModel().getHost(), server.getServerModel().getManagementPort());
      InputStream input = client.getInputStream();
      OutputStream output = client.getOutputStream();
      MessageSenderReceiver msr = new MessageSenderReceiverImpl();
      MessageHeader header = new MessageHeader();
      header.setVersion("002");
      if (tmc.chartView != null) {
        
       final long currentTime=System.currentTimeMillis();
        header.setCommand(ManagerCommand.GetMemoryInfo.toString());
        ManagementMessage message = new ManagementMessageImpl(header, null);
        msr.assembleManagementMessage(message, output);
        message= msr.parseManagementMessage(input);
        final MemoryInfo mi = (MemoryInfo) message.getData();
        
        header.setCommand(ManagerCommand.GetObjectsCountInfo.toString());
        message = new ManagementMessageImpl(header, null);
        msr.assembleManagementMessage(message, output);
        message= msr.parseManagementMessage(input);
        final Map<String, Integer> data = (Map<String, Integer>) message.getData();

        header.setCommand(ManagerCommand.GetPoolStatistic.toString());
        message = new ManagementMessageImpl(header, new Long(currentTime));
        msr.assembleManagementMessage(message, output);
        message= msr.parseManagementMessage(input);
        final List<StatisticPoint>  connectPoolSt = (List<StatisticPoint> ) message.getData();
        
        header.setCommand(ManagerCommand.GetListenerStatistic.toString());
        message = new ManagementMessageImpl(header, new Long(currentTime));
        msr.assembleManagementMessage(message, output);
        message= msr.parseManagementMessage(input);
        final List<StatisticPoint>  listenerPoolSt = (List<StatisticPoint> ) message.getData();
        client.close();
        
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            if (tmc.chartView != null) {
              tmc.chartView.addData(currentTime, data, mi,connectPoolSt,listenerPoolSt);
            }
          }
        });

      }
      if (tmc.memoryChartView != null) {
        header.setCommand(ManagerCommand.GetMemoryInfo.toString());
        ManagementMessage message = new ManagementMessageImpl(header, null);
        msr.assembleManagementMessage(message, output);
        msr.getMessageHeader(input);
        message = msr.parseManagementMessage(input);
        final MemoryInfo mi = (MemoryInfo) message.getData();
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            if (tmc.memoryChartView != null) {
              tmc.memoryChartView.addMemoryInfo(mi);
            }
          }
        });
      }
      if (tmc.modelChartView != null) {
        header.setCommand(ManagerCommand.GetObjectsCountInfo.toString());
        ManagementMessage message = new ManagementMessageImpl(header, null);
        msr.assembleManagementMessage(message, output);
        message = msr.parseManagementMessage(input);
        final Map<String, Integer> data = (Map<String, Integer>) message.getData();
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            if (tmc.modelChartView != null) {
              tmc.modelChartView.addData(data);
            }
          }
        });
      }
    } catch (ConnectException e) {
      MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
      mb.setText("Connect problem");
      mb.setMessage(e.getMessage());
      mb.open();

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
