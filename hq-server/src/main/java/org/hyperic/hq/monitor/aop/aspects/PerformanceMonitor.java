/**
 * NOTE: This copyright does *not* cover user programs that use Hyperic
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 *  "derived work".
 *
 *  Copyright (C) [2009-2010], VMware, Inc.
 *  This file is part of Hyperic.
 *
 *  Hyperic is free software; you can redistribute it and/or modify
 *  it under the terms version 2 of the GNU General Public License as
 *  published by the Free Software Foundation. This program is distributed
 *  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 *  In addition, as a special exception, the copyright holders give permission to link the code of portions 
 *  of this program with AspectJ under certain conditions as described in each individual source file, 
 *  and distribute linked combinations including the two.
 *  
 *  You must obey the GNU General Public License in all respects for all of the code used other than AspectJ.  
 *  If you modify file(s) with this exception, you may extend this exception to your version of the file(s), 
 *  but you are not obligated to do so.  
 *  If you do not wish to do so, delete this exception statement from your version. 
 *
 */
package org.hyperic.hq.monitor.aop.aspects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * PerformanceMonitor
 * 
 * @author Helena Edelson
 */
@Aspect
public class PerformanceMonitor {

    private final Log logger = LogFactory.getLog(this.getClass().getName());

    private long maximumDuration;

    private String warningMessage;

    @PostConstruct
    public void initialize() {
        Assert.notNull("warningMessage should not be null", warningMessage);
        Assert.isTrue(maximumDuration > 0, "threshold for maximumDuration should be > 0");
    }

    /**
     * We could bind the @Transactional annotation to the context but there's no
     * way to know if the method or the type is annotated, causing a false
     * negative.
     * 
     * @param pjp
     */
    @Around("org.hyperic.hq.monitor.aop.MonitorArchitecture.serviceLayerOperationDuration()")
    public Object monitorServiceMethod(ProceedingJoinPoint pjp) throws Throwable {
        logger.debug("\nMonitoring " + buildStartMessage(pjp));
        Object invocation = null;

        StopWatch sw = new StopWatch();
        sw.start(pjp.getSignature().getName() + UUID.randomUUID().toString());

        try {
            invocation = pjp.proceed();
        } finally {
            sw.stop();
        }

        long endTime = sw.getTotalTimeMillis();

        if (exceededThreshold(endTime)) {
            logger.warn("\n" + warningMessage + " " + endTime);
        } else {
            logger.debug("\n" + buildStartMessage(pjp) + buildEndMessage(sw));
        }

        return invocation;

    }

    /**
     * Tests if method execution exceeds the injected SLA time. If it does, will
     * return true.
     * 
     * @param duration
     * @return
     */
    private boolean exceededThreshold(long duration) {
        return duration > maximumDuration;
    }

    /**
     * 
     * @param pjp
     * @return
     */
    private String buildStartMessage(ProceedingJoinPoint pjp) {
        return new StringBuilder().append(pjp.getTarget()).append(".").append(
            pjp.getSignature().getName()).toString();
    }

    /**
     * Builds the message to log
     * 
     * @param sw
     * @return
     */
    private String buildEndMessage(StopWatch sw) {
        return new StringBuilder().append(" executed in ").append(sw.getTotalTimeSeconds()).append(
            ":s, ").append(sw.getTotalTimeMillis()).append(":ms").toString();
    }

    /**
     * 
     * @param warningMessage
     */
    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    /**
     * Optional - injected from properties file.
     * @param maximumDuration
     */
    public void setMaximumDuration(long maximumDuration) {
        this.maximumDuration = maximumDuration;
    }
}
