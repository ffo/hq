/*
 * Generated by XDoclet - Do not edit!
 */
package org.hyperic.hq.bizapp.shared;

import java.util.Properties;

import org.hyperic.hq.auth.shared.SessionNotFoundException;
import org.hyperic.hq.auth.shared.SessionTimeoutException;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.common.ApplicationException;
import org.hyperic.util.ConfigPropertyException;

/**
 * Local interface for ConfigBoss.
 */
public interface ConfigBoss {
    /**
     * Get the top-level configuration properties
     */
    public Properties getConfig() throws ConfigPropertyException;

    /**
     * Get the configuration properties for a specified prefix
     */
    public Properties getConfig(String prefix) throws ConfigPropertyException;

    /**
     * Set the top-level configuration properties
     */
    public void setConfig(int sessId, Properties props) throws ApplicationException, ConfigPropertyException;

    /**
     * Set the configuration properties for a prefix
     */
    public void setConfig(int sessId, String prefix, Properties props) throws ApplicationException,
        ConfigPropertyException;

    /**
     * Restart the config Service
     */
    public void restartConfig();

    /**
     * Perform routine database maintenance. Must have admin permissions.
     * @return The time it took to vaccum, in milliseconds, or -1 if the
     *         database is not PostgreSQL.
     */
    public long vacuum(int sessionId) throws SessionTimeoutException, SessionNotFoundException, PermissionException;

}
