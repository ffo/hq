/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004-2008], Hyperic, Inc.
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

package org.hyperic.hq.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.events.MaintenanceEvent;
import org.hyperic.hq.events.server.session.MaintenanceEventManagerEJBImpl;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;


/**
 * A job to disable and enable alerts for the member resources
 * of a group during a maintenance window.
 */
public class MaintenanceEventJob implements Job {
    protected Log log = LogFactory.getLog(MaintenanceEventJob.class);

    public static final String GROUP_ID = "groupId";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String MODIFIED_TIME = "modifiedTime";
    
    private MaintenanceEvent event = null;
    
    public MaintenanceEventJob() {
    }

    public void execute(JobExecutionContext context) throws
        JobExecutionException
    {
        Trigger trigger = context.getTrigger();
        event = MaintenanceEventManagerEJBImpl.getOne()
        			.buildMaintenanceEvent(context.getJobDetail());
                
        updateAlertDefinitionsActiveStatus((trigger.getNextFireTime() == null));
    }
    
    private void updateAlertDefinitionsActiveStatus(boolean activate) {
    	if (activate) {
    		log.info("NEED TO ACTIVE ALERTS FOR GROUP " + event.getGroupId() + " AT " + new java.util.Date());
    	} else {
    		log.info("NEED TO DEACTIVATE ALERTS FOR GROUP " + event.getGroupId() + " AT " + new java.util.Date());
    		// if already deactive, don't deactivate???
    	}
    }
    
}
