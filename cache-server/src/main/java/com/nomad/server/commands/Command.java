package com.nomad.server.commands;

public interface Command {
    String execute(String[] input, String sessionId);
}
