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

package org.hyperic.hq.ui.action.resource.group.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hyperic.hq.appdef.shared.AppdefGroupValue;
import org.hyperic.hq.authz.shared.PermissionException;
import org.hyperic.hq.bizapp.shared.AppdefBoss;
import org.hyperic.hq.bizapp.shared.AuthzBoss;
import org.hyperic.hq.bizapp.shared.ControlBoss;
import org.hyperic.hq.grouping.shared.GroupEntry;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.Portal;
import org.hyperic.hq.ui.action.resource.ResourceForm;
import org.hyperic.hq.ui.action.resource.common.control.ResourceControlController;
import org.hyperic.hq.ui.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A <code>ResourceControllerAction</code> that sets up group control portals.
 */
public class GroupControllerAction
    extends ResourceControlController {
    protected Log log = LogFactory.getLog(GroupControllerAction.class.getName());
    private final Properties map = getKeyMethodMap();

    @Autowired
    public GroupControllerAction(AppdefBoss appdefBoss, AuthzBoss authzBoss, ControlBoss controlBoss) {
        super(appdefBoss, authzBoss, controlBoss);
        map.setProperty(Constants.MODE_CRNT_DETAIL, "currentControlStatusDetail");
    }

    public ActionForward currentControlStatus(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        List<String> portlets = new ArrayList<String>();
        Portal portal = new Portal();

        portlets.add(".resource.group.control.list.detail");
        portal.setName("resource.group.Control.PageTitle.New");
        portal.addPortlets(portlets);

        checkGroupHasMembers(mapping, form, request, response);

        super.currentControlStatus(mapping, form, request, response, portal);

        return null;
    }

    /**
     * Method to call when mode=crntDetail
     **/
    public ActionForward currentControlStatusDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                                    HttpServletResponse response) throws Exception {
        List<String> portlets = new ArrayList<String>();
        Portal portal = new Portal();

        portlets.add(".resource.group.control.list.current.detail");
        portal.setName("resource.group.Control.PageTitle.CurrentDetail");
        portal.addPortlets(portlets);
        portal.setDialog(true);

        request.setAttribute(Constants.PORTAL_KEY, portal);
        setResource(request, response);

        checkGroupHasMembers(mapping, form, request, response);

        return null;
    }

    /**
     * Method to call when mode=history
     */
    public ActionForward controlStatusHistory(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        List<String> portlets = new ArrayList<String>();
        Portal portal = new Portal();

        portlets.add(".resource.group.control.list.history");
        portal.setName("resource.group.Control.PageTitle.New");
        portal.addPortlets(portlets);

        checkGroupHasMembers(mapping, form, request, response);

        super.controlStatusHistory(mapping, form, request, response, portal);

        return null;
    }

    /**
     * Method to call when mode=hstDetail.
     */
    public ActionForward controlStatusHistoryDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                                    HttpServletResponse response) throws Exception {
        List<String> portlets = new ArrayList<String>();
        Portal portal = new Portal();

        portlets.add(".resource.group.control.list.history.detail");
        portal.setName("resource.group.Control.PageTitle.New");
        portal.addPortlets(portlets);
        portal.setDialog(true);

        checkGroupHasMembers(mapping, form, request, response);

        super.controlStatusHistoryDetail(mapping, form, request, response, portal);

        return null;
    }

    public ActionForward newScheduledControlAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                                   HttpServletResponse response) throws Exception {
        List<String> portlets = new ArrayList<String>();
        Portal portal = new Portal();

        portlets.add(".resource.group.control.new");
        portal.setName("resource.group.Control.PageTitle.New");
        portal.addPortlets(portlets);
        portal.setDialog(true);

        checkGroupHasMembers(mapping, form, request, response);

        super.newScheduledControlAction(mapping, form, request, response, portal);

        return null;
    }

    public ActionForward editScheduledControlAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                                    HttpServletResponse response) throws Exception {
        Portal portal = Portal.createPortal("resource.group.Control.PageTitle.Edit", ".resource.group.control.edit");

        portal.setDialog(true);

        checkGroupHasMembers(mapping, form, request, response);

        super.editScheduledControlAction(mapping, form, request, response, portal);

        return null;
    }

    protected void checkGroupHasMembers(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                        HttpServletResponse response) throws Exception {
        ResourceForm addForm = (ResourceForm) form;
        Integer groupId = null;

        if (form == null) {
            groupId = RequestUtils.getResourceId(request);
        } else {
            groupId = addForm.getRid();
        }

        try {
            Integer sessionId = RequestUtils.getSessionId(request);
            AppdefGroupValue group = appdefBoss.findGroup(sessionId.intValue(), groupId);
            List<GroupEntry> entries = group.getGroupEntries();

            if (entries != null && entries.size() < 1) {
                RequestUtils.setError(request, "resource.common.control.error.NoResourcesInGroup");
            }
        } catch (PermissionException pe) {
            log.warn(pe);
        } catch (ServletException se) {
            log.warn(se);
        }
    }
}
