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

package com.griddynamics.jagger.reporting;

import com.google.common.collect.Maps;
import com.griddynamics.jagger.exception.TechnicalException;
import com.griddynamics.jagger.extension.ExtensionRegistry;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ReportingContext implements ApplicationContextAware {

    public static final String CONTEXT_NAME = "context";

    private ResourceLoader resourceLoader;

    private ExtensionRegistry<ReportProvider> providerRegistry = new ExtensionRegistry<ReportProvider>(ReportProvider.class);
    private ExtensionRegistry<MappedReportProvider> mappedProviderRegistry = new ExtensionRegistry<MappedReportProvider>(MappedReportProvider.class);

    private Map<String, Object> parameters = Maps.newHashMap();

    private boolean removeFrame = true;

    public InputStream getResource(String location) {
        try {
            return resourceLoader.getResource(location).getInputStream();
        } catch (IOException e) {
            throw new TechnicalException(e);
        }
    }

    public OutputStream getOutputResource(String location) {
        try {
            File file = new File(resourceLoader.getResource(location).getFilename());
            return new FileOutputStream(file);
        } catch (IOException e) {
            throw new TechnicalException(e);
        }
    }

    public Map<String, ReportingContext> getAsMap() {
        Map<String, ReportingContext> map = new HashMap<String, ReportingContext>();
        map.put(CONTEXT_NAME, this);
        return map;
    }

    public ReportProvider getProvider(String name) {
        return providerRegistry.getExtension(name);
    }

    public MappedReportProvider getMappedProvider(String name) {
        return mappedProviderRegistry.getExtension(name);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public JasperReport getReport(String location) {
        try {
            return JasperCompileManager.compileReport(new ReportInputStream(resourceLoader.getResource(location).getInputStream(), removeFrame));
        } catch (JRException e) {
            throw new TechnicalException(e);
        } catch (IOException e) {
            throw new TechnicalException(e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public void setProviderRegistry(ExtensionRegistry<ReportProvider> providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    public void setMappedProviderRegistry(ExtensionRegistry<MappedReportProvider> providerRegistry) {
        this.mappedProviderRegistry = providerRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        resourceLoader = applicationContext;
    }

    public boolean isRemoveFrame() {
        return removeFrame;
    }

    public void setRemoveFrame(boolean removeFrame) {
        this.removeFrame = removeFrame;
    }
}
