package com.nomad.server.service.commandplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.CommandPluginModel;
import com.nomad.server.CommandPlugin;
import com.nomad.server.ServerContext;
import com.nomad.server.plugin.ProxyPluginClassPool;

public class CommandPluginService  {
    private static Logger LOGGER = LoggerFactory.getLogger(CommandPluginService.class);

    private final Map<String, ProxyPluginClassPool> proxy = new HashMap<>();

    public void loadPlugins(final List<CommandPluginModel> serverPlugins, final ServerContext context) {
        for (final CommandPluginModel serverPlugin : serverPlugins) {
            loadPlugin(serverPlugin, context);
        }

    }

    public void loadPlugin(final CommandPluginModel serverPlugin, final ServerContext context) {

        try {
            final ProxyPluginClassPool poolImpl = new ProxyPluginClassPool(serverPlugin, context);

            final CommandPlugin plugin = poolImpl.getObject();
            try {
                final List<String> commands = plugin.getCommands();
                for (final String command : commands) {
                    if (proxy.containsKey(command) && !proxy.get(command).getClass().getName().equals(poolImpl.getClass().getName())) {
                        LOGGER.error("Plugin conflict between {} and {}", proxy.get(command).getClass().getName(), poolImpl.getClass().getName());
                    }
                    proxy.put(command, poolImpl);
                }
            } finally {
                if (plugin != null) {
                    plugin.freeObject();
                }
            }

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);

        }

    }

    public CommandPlugin getPlugin(final String command) {
        final ProxyPluginClassPool pool = proxy.get(command);
        if (pool == null) {
            return null;
        }
        return pool.getObject();
    }

    public void stop(){
        for (final ProxyPluginClassPool pool : proxy.values()) {
            pool.close();
        }
    }
}
