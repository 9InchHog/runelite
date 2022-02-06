/*
 * Copyright (c) 2021, BickusDiggus <https://github.com/BickusDiggus>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.splugins;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.openosrs.client.ui.OpenOSRSSplashScreen;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.MissingDependenciesException;
import net.runelite.client.plugins.OPRSExternalPluginManager;
import net.runelite.client.ui.SplashScreen;
import org.pf4j.PluginAlreadyLoadedException;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.update.PluginInfo;
import com.openosrs.client.config.OpenOSRSConfig;
import org.pf4j.update.UpdateRepository;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;

@Slf4j
@Singleton
public class sExternalPluginManager
{
    @Inject
    private OPRSExternalPluginManager pluginManager;
    @Inject
    private OpenOSRSConfig config;
    public static final String SPOONLITE = "SpoonLite";

    public void sDisablePlugin(String pluginID)
    {
        String getDisabled = config.sGetDisabledPlugins().strip();
        if (!getDisabled.contains(pluginID))
        {
            config.sSetDisabledPlugins(getDisabled + pluginID + ",");
        }
    }
    public void sEnablePlugin(String pluginID)
    {
        String getDisabled = config.sGetDisabledPlugins().strip();
        if (getDisabled.contains(pluginID))
        {
            String setDisabled = getDisabled.replace(pluginID + ",", "");
            config.sSetDisabledPlugins(setDisabled);
        }
    }
    public void sPlugins()
    {
        if (OPRSExternalPluginManager.isDevelopmentMode())
        {
            return;
        }
        OpenOSRSSplashScreen.stage(.66, "Installing external plugins");
        SplashScreen.stage(.66, null, "Installing external plugins");

        // Remove original forked repo
        pluginManager.removeRepository("gh:SpoonLite/spoon-plugins");

        //log.info(Integer.toString(pluginManager.getUpdateManager().getPlugins().size()));
        for (UpdateRepository repo : pluginManager.getUpdateManager().getRepositories())
        {
            //log.info(repo.getId());
            if (!repo.getId().equals(SPOONLITE))
            {
                continue;
            }

            if (repo.getPlugins().size() == 0)
            {
                return;
            }
        }

        sUpdate();
        sInstall();
    }

    private void sUpdate()
    {
        //log.info(pluginManager.getDisabledPlugins().toString());
        //log.info(pluginManager.getExternalPluginManager().getResolvedPlugins().toString());
        //log.info(pluginManager.getExternalPluginManager().getPlugins().toString());
        //log.info(pluginManager.getExternalPluginManager().getUnresolvedPlugins().toString());
        for (PluginWrapper installed : pluginManager.getExternalPluginManager().getPlugins())
        {
            //log.info(pluginManager.getUpdateManager().getPlugins().toString());
            if (pluginManager.getUpdateManager().getLastPluginRelease(installed.getPluginId()) != null)
            {
                String hash = pluginManager.getUpdateManager().getLastPluginRelease(installed.getPluginId()).sha512sum;
                String installedHash = OPRSExternalPluginManager.hashedPlugins.get(installed.getPluginId());
                if (installedHash != null && !installedHash.equals(hash))
                {
                    if (pluginManager.getGroups() != null && pluginManager.getGroups().getInstanceCount() > 1)
                    {
                        // Do not update when there is more than one client open -> api might contain changes
                        log.info("Not updating bexternal plugins since there is more than 1 client open");
                    }
                    else
                    {
                        try
                        {
                            pluginManager.getExternalPluginManager().deletePlugin(installed.getPluginId());
                            log.debug("Updating: '{}' Hash: '{}' Installed Hash: '{}'", installed.getPluginId(), hash, installedHash);
                        }
                        catch (PluginRuntimeException ex)
                        {
                            // This should never happen but can crash the client
                            log.warn("Cannot update plugin '{}', failed deleting outdated plugin", installed.getPluginId());
                        }
                        try
                        {
                            pluginManager.getUpdateManager().installPlugin(installed.getPluginId(), null);
                        }
                        catch (Exception ex)
                        {
                            if (ex instanceof PluginAlreadyLoadedException)
                            {
                                log.debug("Plugin already installed");
                            }
                            else if (ex instanceof MissingDependenciesException)
                            {
                                List<String> deps = ((MissingDependenciesException) ex).getDependencies();
                                Multimap<String, String> reverseDepMap = ((MissingDependenciesException) ex).getReverseDependencyMap();

                                for (String dependency : deps)
                                {
                                    Collection<String> dependentPlugins = reverseDepMap.get(dependency);

                                    log.error("Dependency {} is missing, but is required by {}, attempting install.", dependency, dependentPlugins);
                                    try
                                    {
                                        pluginManager.getUpdateManager().installPlugin(dependency, null);
                                    }
                                    catch (PluginRuntimeException ex2)
                                    {
                                        log.error("Dependency {} is missing and couldn't be installed. Disabling loading of {} as they depend on it.", dependency, dependentPlugins);
                                        dependentPlugins.forEach(s -> pluginManager.getExternalPluginManager().unloadPlugin(s));
                                    }
                                }

                                sUpdate();
                            }
                            else if (ex instanceof PluginRuntimeException)
                            {
                                // This should never happen but can crash the client
                                log.warn("Cannot update plugin '{}', failed to write file to plugin folder",installed.getPluginId());
                            }
                            else
                            {
                                log.error("Could not update plugin " + installed.getPluginId(), ex);

                            }
                        }
                    }
                }
                continue;
            }

            //if (!installed.getDescriptor().getProvider().equals(PROVIDER) && !installed.getDescriptor().getProvider().equals(EXTENDED_PROVIDER)&& !installed.getDescriptor().getProvider().equals(SpoonLite))
            if (!installed.getDescriptor().getProvider().equals(SPOONLITE))
            {
                continue;
            }

            //pluginManager.uninstall(installed.getPluginId());
            if (pluginManager.getGroups() != null && pluginManager.getGroups().getInstanceCount() > 1)
            {
                pluginManager.getGroups().sendString("STOPEXTERNAL;" + installed.getPluginId());
                pluginManager.getExternalPluginManager().unloadPlugin(installed.getPluginId());
            }
            else
            {
                try
                {
                    pluginManager.getExternalPluginManager().deletePlugin(installed.getPluginId());
                }
                catch (PluginRuntimeException ex)
                {
                    // This should never happen but can crash the client
                    log.warn("Cannot delete plugin '{}', failed deleting plugin", installed.getPluginId());
                }

            }
        }
    }
    private void sInstall()
    {
        //log.info(pluginManager.getUpdateManager().getAvailablePlugins().toString());
        for (PluginInfo plugins : pluginManager.getUpdateManager().getAvailablePlugins())
        {
            if (!plugins.provider.equals(SPOONLITE) || config.sGetDisabledPlugins().contains(plugins.id))
            {
                continue;
            }

            try
            {
                pluginManager.getUpdateManager().installPlugin(plugins.id, null);
            }
            catch (Exception ex)
            {
                if (ex instanceof PluginAlreadyLoadedException)
                {
                    log.debug("Plugin already installed");
                }
                else if (ex instanceof MissingDependenciesException)
                {
                    List<String> deps = ((MissingDependenciesException) ex).getDependencies();
                    Multimap<String, String> reverseDepMap = ((MissingDependenciesException) ex).getReverseDependencyMap();

                    for (String dependency : deps)
                    {
                        Collection<String> dependentPlugins = reverseDepMap.get(dependency);

                        log.error("Dependency {} is missing, but is required by {}, attempting install.", dependency, dependentPlugins);
                        try
                        {
                            pluginManager.getUpdateManager().installPlugin(dependency, null);
                        }
                        catch (PluginRuntimeException ex2)
                        {
                            log.error("Dependency {} is missing and couldn't be installed. Disabling loading of {} as they depend on it.", dependency, dependentPlugins);
                            dependentPlugins.forEach(s -> pluginManager.getExternalPluginManager().unloadPlugin(s));
                        }
                    }

                    sInstall();
                }
                else
                {
                    log.error("Could not install plugin " + plugins.id, ex);
                }
            }

            OpenOSRSSplashScreen.stage(.66, "Installing plugins");
            SplashScreen.stage(.66, null, "Installing plugins");
        }
    }
}
