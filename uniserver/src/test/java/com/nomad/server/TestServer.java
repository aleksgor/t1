package com.nomad.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.nomad.communication.udp.client.UdpClient;
import com.nomad.communication.udp.server.UdpServer;
import com.nomad.io.serializer.SerializerFactory;
import com.nomad.model.CommonClientModel;
import com.nomad.model.CommonServerModel;
import com.nomad.utility.SimpleServerContext;

@Ignore
public class TestServer {

    @BeforeClass
    public static  void start(){
        SerializerFactory.registerSerializer(StringMessage.class, StringMessageSerializer.class);
    }
    @Test
    public void testServer() throws Exception{
        final UdpServer<StringMessage,StringMessage> server=startServer() ;

        final CommonClientModel clientModel=new SimpleClientModel() ;
        clientModel.setHost("localhost");
        clientModel.setPort(7648);
        final UdpClient<StringMessage,StringMessage> client=new UdpClient<>(clientModel,null);
        final StringMessage result=client.sendMessage(new StringMessage("Message"));
        assertEquals("Response: Message", result);
        client.close();
        server.close();
    }
    @Test
    public void testLongMessageServer() throws Exception {
        final UdpServer<StringMessage,StringMessage> server=startServer() ;

        final CommonClientModel clientModel=new SimpleClientModel() ;
        clientModel.setHost("localhost");
        clientModel.setPort(7648);

        final StringMessage message=new StringMessage(getLongMessage("Messageee;",1000));
        final UdpClient<StringMessage,StringMessage> client=new UdpClient<>(clientModel,null);
        StringMessage result=client.sendMessage(message);
        assertEquals(message.length()+10, result.length());
        assertEquals("Response: "+ message, result);

        result= client.sendMessage(message);
        assertEquals(message.length()+10, result.length());
        assertEquals("Response: "+ message, result);
        result= client.sendMessage(message);
        assertEquals(message.length()+10, result.length());
        assertEquals("Response: "+ message, result);
        result= client.sendMessage(message);
        assertEquals(message.length()+10, result.length());
        assertEquals("Response: "+ message, result);

        assertEquals(0, client.checkClean());
        assertEquals(0, server.checkClean());


        client.close();
        server.close();
    }

    private String getLongMessage (final String template,final int count) {
        String message="";
        for(int i=0;i<count;i++){
            message+=template;
        }
        return message;
    }
    @SuppressWarnings("unused")
    @Test
    public void testMultiThread() throws Exception {
        final UdpServer<StringMessage,StringMessage> server=startServer();
        final int count = 10;

        final String message=getLongMessage("Messageee;",1000);

        final CommonClientModel clientModel=new SimpleClientModel() ;
        clientModel.setHost("localhost");
        clientModel.setPort(7648);

        final ThreadContainer [] threads= new ThreadContainer[count];
        for(int i=0;i<count;i++){
            threads[i]=new ThreadContainer();
            threads[i].runner=new ThreadRunner(clientModel,i,message);
            threads[i].thread=new Thread(threads[i].runner);
        }

        for(int i=0;i<count;i++){
            threads[i].thread.start();
        }
        Thread.currentThread();
        Thread.sleep(10000);
        int result=0;
        for(int i=0;i<count;i++){
            threads[i].runner.stop();
            result+=threads[i].runner.getCount();
        }
        server.close();
    }

    private UdpServer<StringMessage,StringMessage> startServer() throws Exception{
        final CommonServerModel serverModel= new SimpleServerModel();
        serverModel.setHost("localhost");
        serverModel.setPort(7648);

        final UdpServer<StringMessage,StringMessage> server= new UdpServer<>(serverModel, new SimpleServerContext(),"",new TestMessageExecutorFactory());

        new Thread(server).start();
        return server;
    }
    private class ThreadContainer{
        Thread thread;
        ThreadRunner runner;
    }
    private class ThreadRunner implements Runnable{
        private final UdpClient<StringMessage,StringMessage> client;
        private volatile boolean stop=false;
        private final int index;
        private final String message;
        private volatile int count;
        private volatile int error;


        public ThreadRunner(final CommonClientModel clientModel, final int index,final String message) throws Exception{
            client=new UdpClient<StringMessage,StringMessage>(clientModel, null);
            this.index=index;
            this.message=message;
        }
        @Override
        public void run() {
            while(!stop){
                StringMessage result;
                try {
                    result = client.sendMessage(new StringMessage(index+message));
                    count++;
                    if( !result.equals("Response: "+index+message)){
                        error++;
                    }
                    //Thread.sleep();
                } catch (final Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }
            client.close();

        }
        public void stop(){
            stop=true;
        }
        public int getCount() {
            return count;
        }
        @SuppressWarnings("unused")
        public int getError() {
            return error;
        }

    }
}
