/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.developerstudio.eclipse.carbonserver44microei12.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Map;
import java.nio.file.Paths;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jst.server.generic.core.internal.GenericServer;
import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.developerstudio.eclipse.carbon.server.model.util.CarbonServerCommonConstants;
import org.wso2.developerstudio.eclipse.carbon.server.model.util.CarbonServerCommonUtils;
import org.wso2.developerstudio.eclipse.carbon.server.model.util.CarbonServerXUtils;
import org.wso2.developerstudio.eclipse.carbonserver.base.manager.CarbonServerManager;
import org.wso2.developerstudio.eclipse.carbonserver44microei12.Activator;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.utils.file.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.consensys.cava.toml.Toml;

import net.consensys.cava.toml.TomlParseResult;

@SuppressWarnings("restriction")
public class CarbonServer44eiUtils implements CarbonServerXUtils {

    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    @Override
    public String getServerVersion() {
        return CarbonServerCommonConstants.getCarbonVersion(Activator.PLUGIN_ID);
    }

    @Override
    public boolean updateTransportPorts(IServer server) {
        return true;
    }

    @Override
    public URL getServerURL(IServer server) {
        return null;
    }

    @Override
    public ServerPort[] getServerPorts(String serverHome) {
        return null;
    }

    @Override
    public String getWebContextRoot(IServer server) {
        return null;
    }

    @Override
    public NamespaceContext getCarbonNamespace() {
        return null;
    }

    @Override
    public void setTrustoreProperties(IServer server) {
        // TODO update with deployment.toml if needed
        String transportsXml = FileUtils.addNodesToPath(CarbonServerManager.getServerHome(server).toOSString(),
                new String[] { "conf", "server.xml" });
        XPathFactory factory = XPathFactory.newInstance();
        File xmlDocument = new File(transportsXml);
        try {
            InputSource inputSource = new InputSource(new FileInputStream(xmlDocument));
            XPath xPath = factory.newXPath();
            XPathExpression xPathExpression = xPath.compile("/Server/Security/KeyStore/Location");
            String evaluate = xPathExpression.evaluate(inputSource);
            String trustoreLocation = resolveProperties(server, evaluate);
            inputSource = new InputSource(new FileInputStream(xmlDocument));
            xPathExpression = xPath.compile("/Server/Security/KeyStore/Password");
            evaluate = xPathExpression.evaluate(inputSource);
            String trustStorePassword = resolveProperties(server, evaluate);
            System.setProperty("javax.net.ssl.trustStore", trustoreLocation);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        } catch (FileNotFoundException | XPathExpressionException e) {
            log.error(e);
        }

    }

    @Override
    public ServerPort[] getServerPorts(IServer server) {
        return server.getServerPorts(new NullProgressMonitor());
    }

    @Override
    public String getRepositoryPath(String serverXmlPath) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        String nodeValue = "";
        try {
            docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.parse(serverXmlPath);

            NodeList nodeList = doc.getElementsByTagName("RepositoryLocation");
            Node node = nodeList.item(0);
            nodeValue = node.getFirstChild().getNodeValue();

        } catch (ParserConfigurationException | SAXException | IOException | TransformerFactoryConfigurationError e) {
            log.error(e);
        }
        return nodeValue;
    }

    @Override
    public boolean updateAndSaveCarbonXml(String serverXmlPath, String repoPath, IServer server) {
        return true;
    }

    @Override
    public String getServerXmlPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(getConfPathFromLocalWorkspaceRepo(workspaceRepo),
                new String[] { "carbon.xml" });
    }

    @Override
    public String getCatelinaXmlPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(getConfPathFromLocalWorkspaceRepo(workspaceRepo),
                new String[] { "tomcat", "catalina-server.xml" });
    }

    @Override
    public String getConfPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(workspaceRepo, new String[] { "conf" });
    }

    @Override
    public String getRepositoryPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(workspaceRepo, new String[] { "repository", "deployment", "server" });
    }

    @Override
    public String getTransportsXmlPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(getConfPathFromLocalWorkspaceRepo(workspaceRepo),
                new String[] { "mgt-transports.xml" });
    }

    @Override
    public String getCarbonXmlPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(getConfPathFromLocalWorkspaceRepo(workspaceRepo),
                new String[] { "carbon.xml" });
    }

    @Override
    public String getAxis2XmlPathFromLocalWorkspaceRepo(String workspaceRepo) {
        return FileUtils.addNodesToPath(getConfPathFromLocalWorkspaceRepo(workspaceRepo),
                new String[] { "axis2", "axis2.xml" });
    }

    @Override
    public String resolveProperties(IServer server, String property) {
        if (CarbonServerCommonUtils.getServerConfigMapValue(server, property) != null) {
            return CarbonServerCommonUtils.getServerConfigMapValue(server, property).toString();
        }
        GenericServer gserver = (GenericServer) server.loadAdapter(ServerDelegate.class, null);
        if (gserver == null || gserver.getServerDefinition() == null
                || gserver.getServerDefinition().getResolver() == null)
            return null;
        if (!property.startsWith("${"))
            property = "${" + property + "}";
        ServerRuntime serverDefinition = gserver.getServerDefinition();
        return serverDefinition.getResolver().resolveProperties(property);
    }

    @Override
    public boolean updateAndSaveAxis2Ports(String axis2Xml, IServer server) {
        return true;
    }

    @Override
    public File getCappMonitorBundle() {
        URL resource = Platform.getBundle(Activator.PLUGIN_ID)
                .getResource("lib/org.wso2.carbon.capp.monitor-3.0.0.jar");
        IPath path = Activator.getDefault().getStateLocation();
        IPath libFolder = path.append("lib");
        String[] paths = resource.getFile().split("\\" + File.separator);
        IPath library = libFolder.append(paths[paths.length - 1]);
        File libraryFile = new File(library.toOSString());
        if (libraryFile.exists()) {
            return libraryFile;
        }
        try {
            CarbonServerCommonUtils.writeToFile(libraryFile, resource.openStream());
        } catch (IOException e) {
            log.error(e);
            return null;
        }
        return libraryFile;
    }

    @Override
    public String getPortId(String name) {
        String id = "carbon.https";
        if (name.equalsIgnoreCase(CarbonServerCommonConstants.getEsbConsoleHttpsDesc()))
            id = CarbonServerCommonConstants.getEsbConsoleHttps();
        if (name.equalsIgnoreCase(CarbonServerCommonConstants.getEsbTransportHttpDesc()))
            id = CarbonServerCommonConstants.getEsbTransportHttp();
        if (name.equalsIgnoreCase(CarbonServerCommonConstants.getEsbTransportHttpsDesc()))
            id = CarbonServerCommonConstants.getEsbTransportHttps();
        return id;
    }

    @Override
    public void loadServerInstanceProperties(IServer server) {
        GenericServer gserver = (GenericServer) server.getAdapter(GenericServer.class);
        if (gserver == null) {
            return;
        }
        ObjectInputStream obj_in = null;
        try {
            String serverLocalWorkspacePath = CarbonServerManager.getServerLocalWorkspacePath(server);
            String objConfigPath = FileUtils.addNodesToPath(serverLocalWorkspacePath,
                    new String[] { "repository", "conf", "config" });
            if (new File(objConfigPath).exists()) {
                FileInputStream f_in = new FileInputStream(objConfigPath);
                obj_in = new ObjectInputStream(f_in);
                Map<String, String> obj = (Map<String, String>) obj_in.readObject();
                gserver.getServerInstanceProperties().putAll(obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error(e);
        } finally {
            if (obj_in != null) {
                try {
                    obj_in.close();
                } catch (IOException e) {
                    log.error("Error while closing stream", e);
                }
            }
        }
    }

    @Override
    public boolean updateAxis2XML(IServer server) {
        String serverLocalWorkspacePath = CarbonServerManager.getServerLocalWorkspacePath(server);
        return updateAndSaveAxis2Ports(getAxis2XmlPathFromLocalWorkspaceRepo(serverLocalWorkspacePath), server);
    }

    @Override
    public boolean updateAndSaveTransportsPorts(String carbonXml, String catelinaXml, IServer server) {
        return true;
    }

    public TomlParseResult getTomlResults(String carbonHomePath) throws IOException {
        String tomlFilePath = FileUtils.addNodesToPath(getConfPathFromLocalWorkspaceRepo(carbonHomePath),
                new String[] { "deployment.toml" });
        return Toml.parse(Paths.get(tomlFilePath));
    }

    private String getStringOfTomlObject(Object object) {
        if (object == null) {
            return "";
        }

        if (object instanceof Long) {
            return String.valueOf(object);
        }

        if (object instanceof String) {
            return ((String) object);
        }

        if (object instanceof Boolean) {
            return object.toString();
        }

        return "";
    }

    public String readTomlValue(TomlParseResult tomlResults, String key, String defaultValue) {
        String tomlValue = "";
        Object offsetObject = tomlResults.get(key);
        tomlValue = getStringOfTomlObject(offsetObject);
        if (tomlValue.equals("")) {
            return defaultValue;
        }
        return tomlValue;
    }
}