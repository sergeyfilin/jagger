/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.griddynamics.jagger.monitoring.reporting;

import com.google.common.collect.Maps;
import com.griddynamics.jagger.engine.e1.aggregator.workload.model.WorkloadData;
import com.griddynamics.jagger.master.SessionIdProvider;
import com.griddynamics.jagger.monitoring.model.PerformedMonitoring;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;
import java.util.Map;

// todo mairbek: should we use delegation instead of inheritance??
public class AbstractMonitoringReportProvider extends HibernateDaoSupport {
    private SessionIdProvider sessionIdProvider;

    private Map<String, String> monitoringMap;

    public void clearCache() {
        monitoringMap = null;
    }

    protected void loadMonitoringMap() {
        if (monitoringMap != null) {
            return;
        }


        String sessionId = getSessionIdProvider().getSessionId();
        List<PerformedMonitoring> list = getHibernateTemplate().find("select pf from PerformedMonitoring pf where pf.sessionId =? and pf.parentId is not null", sessionId);
        Map<String, String> result = Maps.newHashMap();

        for (PerformedMonitoring performedMonitoring : list) {
            result.put(performedMonitoring.getParentId(), performedMonitoring.getMonitoringId());
        }

        monitoringMap = result;
    }

    protected String relatedMonitoringTask(String taskId) {
        String parentId = parentOf(taskId);

        if (parentId == null) {
            return null;
        }

        return monitoringMap.get(parentId);

    }


    protected String parentOf(String workloadTaskId) {
        String sessionId = getSessionIdProvider().getSessionId();
        List<WorkloadData> list = getHibernateTemplate().find("select wd from WorkloadData wd where wd.sessionId =? and wd.taskId =? and wd.parentId is not null", sessionId, workloadTaskId);

        if (list.size() == 1) {
            return list.get(0).getParentId();
        }

        return null;
    }


    @Required
    public void setSessionIdProvider(SessionIdProvider sessionIdProvider) {
        this.sessionIdProvider = sessionIdProvider;
    }

    protected SessionIdProvider getSessionIdProvider() {
        return sessionIdProvider;
    }
}
