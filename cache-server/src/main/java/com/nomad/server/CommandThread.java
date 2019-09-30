package com.nomad.server;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.model.ServerModel;
import com.nomad.server.commands.CleanCommand;
import com.nomad.server.commands.ContainsCommand;
import com.nomad.server.commands.GetBlockedIdCommand;
import com.nomad.server.commands.GetCommand;
import com.nomad.server.commands.GetListModelTypeCommand;
import com.nomad.server.commands.ListCommand;
import com.nomad.server.commands.RemoveModelFromCacheCommand;

public class CommandThread implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(CommandServer.class);
    private Socket clientSocket = null;
    private final ServerModel server;
    private volatile InternalTransactDataStore store;

    public CommandThread(final Socket clientSocket, final InternalTransactDataStore store, final ServerModel server) {
        this.clientSocket = clientSocket;
        this.store = store;
        this.server=server;
    }

    @Override
    public void run() {
        InputStream input = null;
        OutputStream output = null;
        LOGGER.info("started command thread");
        try {
            input = clientSocket.getInputStream();
            output = clientSocket.getOutputStream();

            final PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            final String sessionId=UUID.randomUUID().toString();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                LOGGER.info("command:{}", inputLine);
                final String answer = tryToExecuteCommand(inputLine,sessionId);
                LOGGER.info("answer:{}", answer);
                out.println(answer);
                LOGGER.info("answer sended!");
                if (inputLine.equals("bye")) {
                    break;
                }
            }

        } catch (final SocketException e) {
            LOGGER.info(" command session closed!");
        } catch (final EOFException e) {
            LOGGER.info(" command session closed!");
        } catch (final IOException e) {
            LOGGER.error("error", e);
        } catch (final Throwable e) {
            LOGGER.error("error", e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (final Throwable e) {
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (final Throwable e) {
            }

        }
        LOGGER.info("stoped command thread");

    }

    private String tryToExecuteCommand(String s,final String sessionId) {
        s = s.trim();
        final String[] commands = s.split(" ");
        if ("get".equals(commands[0])) {
            return new GetCommand(store,server).execute(commands, sessionId);
        }else if ("list".equals(commands[0])) {
            return new ListCommand(store,server).execute(commands, sessionId);
        }else if ("clean".equals(commands[0])) {
            return new CleanCommand(store,server).execute(commands, sessionId);
        }else if ("RemoveFromCache".equals(commands[0])) {
            return new RemoveModelFromCacheCommand(store,server).execute(commands, sessionId);
        }else if ("getListModels".equals(commands[0])) {
            return new GetListModelTypeCommand(store,server).execute(commands, sessionId);
        }else if ("getGlocks".equals(commands[0])) {
            return new GetBlockedIdCommand(store,server).execute(commands, sessionId);
        }else if ("contains".equals(commands[0])) {
            return new ContainsCommand(store, server).execute(commands, sessionId);

        }else if ("stop".equals(commands[0])) {
            System.exit(0);
        }

        return "unknown command:" + commands[0];
    }


}