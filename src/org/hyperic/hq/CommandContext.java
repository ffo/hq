package org.hyperic.hq;

import org.hyperic.hq.bizapp.shared.CommandHandlerUtil;
import org.hyperic.hq.bizapp.shared.CommandHandlerLocal;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/*
* NOTE: This copyright does *not* cover user programs that use HQ
* program services by normal system calls through the application
* program interfaces provided as part of the Hyperic Plug-in Development
* Kit or the Hyperic Client Development Kit - this is merely considered
* normal use of the program, and does *not* fall under the heading of
* "derived work".
* 
* Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
* This file is part of HQ.
* 
* HQ is free software; you can redistribute it and/or modify
* it under the terms version 2 of the GNU General Public License as
* published by the Free Software Foundation. This program is distributed
* in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU General Public License for more
* details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
* USA.
*/
/**
 * for command execution Context
 */
public class CommandContext implements Serializable {
    private Object result;
    private int executionTime;
    private ArrayList commands = new ArrayList();
    private HashMap params = new HashMap();

    public static CommandContext createContext() {
        return new CommandContext();
    }

    public static CommandContext createContext(Command command) {
        CommandContext context = new CommandContext();
        context.addCommand(command);
        return context;
    }

    public static CommandContext createContext(List commands) {
        CommandContext context = new CommandContext();
        context.getCommands().addAll(commands);
        return context;
    }

    private static CommandHandlerLocal commandHandler;
    static {
        try {
            commandHandler = CommandHandlerUtil.getLocalHome().create();
        } catch (CreateException e) {
            commandHandler = null;
            throw new RuntimeException(
                "Can't instantiate CommandHander Session Bean");
        } catch (NamingException e) {
            commandHandler = null;
            throw new RuntimeException(
                "Can't lookup CommandHander Session Bean");
        }
    }

    protected CommandContext() {
    }

    public void execute(Command command) {
        clear();
        addCommand(command);
        execute();
    }

    public void execute(List commands) {
        clear();
        addCommand(commands);
        execute();
    }

    public void execute() {
        if (commandHandler == null) {
            throw new IllegalStateException("CommandHandler not initialized");
        }
        commandHandler.executeHandler(this);
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

    public List getCommands() {
        return commands;
    }

    protected void setCommands(ArrayList commands) {
        this.commands = commands;
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void addCommand(List commandList) {
        commands.addAll(commandList);
    }

    public Object getParameter(Object key) {
        return params.get(key);
    }

    public void setParameter(Object key, Object value) {
        params.put(key, value);
    }

    public void clear() {
        getCommands().clear();
        params.clear();
        setResult(null);
    }
}

