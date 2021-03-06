/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.configuration;

import org.opencms.ade.containerpage.inherited.CmsContainerConfigurationCache;
import org.opencms.ade.containerpage.inherited.CmsContainerConfigurationWriter;
import org.opencms.ade.containerpage.inherited.CmsInheritedContainerState;
import org.opencms.ade.detailpage.CmsDetailPageConfigurationWriter;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.detailpage.CmsSitemapDetailPageFinder;
import org.opencms.ade.detailpage.I_CmsDetailPageFinder;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsADECache;
import org.opencms.xml.containerpage.CmsADECacheSettings;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.Messages;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

/**
 * This is the main class used to access the ADE configuration and also accomplish some other related tasks
 * like loading/saving favorite and recent lists.<p>
 */
public class CmsADEManager {

    /** JSON property name constant. */
    protected enum FavListProp {
        /** element property. */
        ELEMENT,
        /** formatter property. */
        FORMATTER,
        /** properties property. */
        PROPERTIES;
    }

    /**
     * A status enum for the initialization status.<p>
     */
    protected enum Status {
        /** already initialized. */
        initialized,
        /** currently initializing. */
        initializing,
        /** not initialized. */
        notInitialized
    }

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_FAVORITE_LIST_SIZE = "ADE_FAVORITE_LIST_SIZE";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_RECENT_LIST_SIZE = "ADE_RECENT_LIST_SIZE";

    /** User additional info key constant. */
    public static final String ADDINFO_ADE_SEARCH_PAGE_SIZE = "ADE_SEARCH_PAGE_SIZE";

    /** The client id separator. */
    public static final String CLIENT_ID_SEPERATOR = "#";

    /** The configuration file name. */
    public static final String CONFIG_FILE_NAME = ".config";

    /** The name of the sitemap configuration file type. */
    public static final String CONFIG_FOLDER_TYPE = "content_folder";

    /** The path for sitemap configuration files relative from the base path. */
    public static final String CONFIG_SUFFIX = "/"
        + CmsADEManager.CONTENT_FOLDER_NAME
        + "/"
        + CmsADEManager.CONFIG_FILE_NAME;

    /** The name of the sitemap configuration file type. */
    public static final String CONFIG_TYPE = "sitemap_config";

    /** The content folder name. */
    public static final String CONTENT_FOLDER_NAME = ".content";

    /** Default favorite list size constant. */
    public static final int DEFAULT_FAVORITE_LIST_SIZE = 10;

    /** Default recent list size constant. */
    public static final int DEFAULT_RECENT_LIST_SIZE = 10;

    /** The name of the module configuration file type. */
    public static final String MODULE_CONFIG_TYPE = "module_config";

    /** The path to the sitemap editor JSP. */
    public static final String PATH_SITEMAP_EDITOR_JSP = "/system/modules/org.opencms.ade.sitemap/pages/sitemap.jsp";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_RECENT_LIST = "ADE_RECENT_LIST";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /** The cache instance. */
    private CmsADECache m_cache;

    /** The sitemap configuration file type. */
    private I_CmsResourceType m_configType;

    /** The detail page finder. */
    private I_CmsDetailPageFinder m_detailPageFinder = new CmsSitemapDetailPageFinder();

    /** The initialization status. */
    private Status m_initStatus = Status.notInitialized;

    /** The module configuration file type. */
    private I_CmsResourceType m_moduleConfigType;

    /** The online cache instance. */
    private CmsConfigurationCache m_offlineCache;

    /** The offline CMS context. */
    private CmsObject m_offlineCms;

    /** The offline inherited container configuration cache. */
    private CmsContainerConfigurationCache m_offlineContainerConfigurationCache;

    /** The offline cache instance. */
    private CmsConfigurationCache m_onlineCache;

    /** The online CMS context. */
    private CmsObject m_onlineCms;

    /** The online inherited container configuration cache. */
    private CmsContainerConfigurationCache m_onlineContainerConfigurationCache;

    /**
     * Creates a new ADE manager.<p>
     *
     * @param adminCms a CMS context with admin privileges 
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsADEManager(CmsObject adminCms, CmsMemoryMonitor memoryMonitor, CmsSystemConfiguration systemConfiguration) {

        // initialize the ade cache
        CmsADECacheSettings cacheSettings = systemConfiguration.getAdeCacheSettings();
        if (cacheSettings == null) {
            cacheSettings = new CmsADECacheSettings();
        }
        m_onlineCms = adminCms;
        m_cache = new CmsADECache(memoryMonitor, cacheSettings);

        // further initialization is done by the initialize() method. We don't do that in the constructor,
        // because during the setup the configuration resource types don't exist yet.
    }

    /**
     * Finds the entry point to a sitemap.<p>
     * 
     * @param cms the CMS context
     * @param openPath the resource path to find the sitemap to
     * 
     * @return the sitemap entry point
     */
    public String findEntryPoint(CmsObject cms, String openPath) {

        CmsADEConfigData configData = lookupConfiguration(cms, openPath);
        String result = configData.getBasePath();
        if (result == null) {
            return cms.getRequestContext().addSiteRoot("/");
        }
        return result;
    }

    /**
     * Gets the complete list of beans for the currently configured detail pages.<p>
     * 
     * @param cms the CMS context to use
     *   
     * @return the list of detail page infos 
     */
    public List<CmsDetailPageInfo> getAllDetailPages(CmsObject cms) {

        CmsConfigurationCache cache = cms.getRequestContext().getCurrentProject().isOnlineProject()
        ? m_onlineCache
        : m_offlineCache;
        return cache.getAllDetailPages();
    }

    /**
     * Gets the containerpage cache instance.<p> 
     * 
     * @return the containerpage cache instance 
     */
    public CmsADECache getCache() {

        return m_cache;
    }

    /**
     * Gets the configuration file type.<p>
     * 
     * @return the configuration file type 
     */
    public I_CmsResourceType getConfigurationType() {

        return m_configType;
    }

    /**
     * Reads the current element bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the element bean
     * 
     * @throws CmsException if no current element is set
     */
    public CmsContainerElementBean getCurrentElement(ServletRequest req) throws CmsException {

        CmsContainerElementBean element = CmsJspStandardContextBean.getInstance(req).getElement();
        if (element == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0));
        }
        return element;
    }

    /**
     * Gets the detail page for a content element.<p>
     * 
     * @param cms the CMS context 
     * @param pageRootPath the element's root path 
     * @param originPath the path in which the the detail page is being requested 
     * 
     * @return the detail page for the content element 
     */
    public String getDetailPage(CmsObject cms, String pageRootPath, String originPath) {

        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        CmsConfigurationCache cache = online ? m_onlineCache : m_offlineCache;
        String resType = cache.getParentFolderType(pageRootPath);
        if (resType == null) {
            return null;
        }
        String originRootPath = cms.getRequestContext().addSiteRoot(originPath);
        CmsADEConfigData configData = lookupConfiguration(cms, originRootPath);
        List<CmsDetailPageInfo> pageInfo = configData.getDetailPagesForType(resType);
        if ((pageInfo == null) || pageInfo.isEmpty()) {
            // in case no detail page is found for the base URI try to fetch it for the page root path
            configData = lookupConfiguration(cms, pageRootPath);
            pageInfo = configData.getDetailPagesForType(resType);
            if ((pageInfo == null) || pageInfo.isEmpty()) {
                return null;
            }
        }
        return pageInfo.get(0).getUri();
    }

    /**
     * Gets the detail page finder.<p>
     * 
     * @return the detail page finder 
     */
    public I_CmsDetailPageFinder getDetailPageFinder() {

        return m_detailPageFinder;
    }

    /**
     * Returns the main detail pages for a type in all of the VFS tree.<p>
     * 
     * @param cms the current CMS context 
     * @param type the resource type name 
     * @return a list of detail page root paths 
     */
    public List<String> getDetailPages(CmsObject cms, String type) {

        CmsConfigurationCache cache = cms.getRequestContext().getCurrentProject().isOnlineProject()
        ? m_onlineCache
        : m_offlineCache;
        return cache.getDetailPages(type);
    }

    /**
     * Returns the element settings for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * 
     * @return the element settings for a given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementSettings(CmsObject cms, CmsResource resource)
    throws CmsException {

        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
            Map<String, CmsXmlContentProperty> settings = CmsXmlContentDefinition.getContentHandlerForResource(
                cms,
                resource).getSettings(cms, resource);
            result.putAll(settings);
            return CmsXmlContentPropertyHelper.copyPropertyConfiguration(result);
        }
        return Collections.<String, CmsXmlContentProperty> emptyMap();
    }

    /**
     * Returns the favorite list, or creates it if not available.<p>
     *
     * @param cms the cms context
     * 
     * @return the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsContainerElementBean> getFavoriteList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);

        List<CmsContainerElementBean> favList = new ArrayList<CmsContainerElementBean>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        favList.add(elementFromJson(array.getJSONObject(i)));
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        } else {
            // save to be better next time
            saveFavoriteList(cms, favList);
        }

        return favList;
    }

    /**
     * Returns the inheritance state for the given inheritance name and resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * @param name the inheritance name
     * 
     * @return the inheritance state
     */
    public CmsInheritedContainerState getInheritedContainerState(CmsObject cms, CmsResource resource, String name) {

        String rootPath = resource.getRootPath();
        if (!resource.isFolder()) {
            rootPath = CmsResource.getParentFolder(rootPath);
        }
        CmsInheritedContainerState result = new CmsInheritedContainerState();
        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        CmsContainerConfigurationCache cache = online
        ? m_onlineContainerConfigurationCache
        : m_offlineContainerConfigurationCache;
        result.addConfigurations(cache, rootPath, name, cms.getRequestContext().getLocale());
        return result;

    }

    /**
     * Returns the inheritance state for the given inheritance name and root path.<p>
     * 
     * @param cms the current cms context
     * @param rootPath the root path
     * @param name the inheritance name
     * 
     * @return the inheritance state
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsInheritedContainerState getInheritedContainerState(CmsObject cms, String rootPath, String name)
    throws CmsException {

        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            CmsResource resource = cms.readResource(rootPath);
            return getInheritedContainerState(cms, resource, name);
        } finally {
            cms.getRequestContext().setSiteRoot(oldSiteRoot);
        }
    }

    /** 
     * Gets the maximum sitemap depth.<p>
     * 
     * @return the maximum sitemap depth 
     */
    public int getMaxSitemapDepth() {

        return 20;
    }

    /**
     * Gets the module configuration resource type.<p>
     * 
     * @return the module configuration resource type 
     */
    public I_CmsResourceType getModuleConfigurationType() {

        return m_moduleConfigType;
    }

    /**
     * Returns the favorite list, or creates it if not available.<p>
     *
     * @param cms the cms context
     * 
     * @return the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsContainerElementBean> getRecentList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_RECENT_LIST);

        List<CmsContainerElementBean> recentList = new ArrayList<CmsContainerElementBean>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        recentList.add(elementFromJson(array.getJSONObject(i)));
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        } else {
            // save to be better next time
            saveRecentList(cms, recentList);
        }

        return recentList;
    }

    /**
     * Gets the maximum length of the recent list.<p>
     * 
     * @param user the user for which to get the maximum length 
     * 
     * @return the maximum recent list size for the user 
     */
    public int getRecentListMaxSize(CmsUser user) {

        Integer maxElems = (Integer)user.getAdditionalInfo(ADDINFO_ADE_RECENT_LIST_SIZE);
        if (maxElems == null) {
            maxElems = new Integer(DEFAULT_RECENT_LIST_SIZE);
        }
        return maxElems.intValue();
    }

    /**
     * Tries to get the subsite root for a given resource root path.<p>
     * 
     * @param cms the current CMS context 
     * @param rootPath the root path for which the subsite root should be found 
     * 
     * @return the subsite root 
     */
    public String getSubSiteRoot(CmsObject cms, String rootPath) {

        CmsADEConfigData configData = lookupConfiguration(cms, rootPath);
        String basePath = configData.getBasePath();
        if (basePath == null) {
            return OpenCms.getSiteManager().getSiteRoot(rootPath);
        } else {
            return basePath;
        }
    }

    /**
     * Initializes the configuration by reading all configuration files and caching their data.<p>
     */
    public synchronized void initialize() {

        if (m_initStatus == Status.notInitialized) {
            try {
                m_initStatus = Status.initializing;
                m_configType = OpenCms.getResourceManager().getResourceType(CONFIG_TYPE);
                m_moduleConfigType = OpenCms.getResourceManager().getResourceType(MODULE_CONFIG_TYPE);
                CmsProject temp = getTempfileProject(m_onlineCms);
                m_offlineCms = OpenCms.initCmsObject(m_onlineCms);
                m_offlineCms.getRequestContext().setCurrentProject(temp);
                m_onlineCache = new CmsConfigurationCache(m_onlineCms, m_configType, m_moduleConfigType);
                m_offlineCache = new CmsConfigurationCache(m_offlineCms, m_configType, m_moduleConfigType);
                m_onlineCache.initialize();
                m_offlineCache.initialize();
                m_onlineContainerConfigurationCache = new CmsContainerConfigurationCache(m_onlineCms, "online");
                m_offlineContainerConfigurationCache = new CmsContainerConfigurationCache(m_offlineCms, "offline");
                CmsGlobalConfigurationCacheEventHandler handler = new CmsGlobalConfigurationCacheEventHandler(
                    m_onlineCms);
                handler.addCache(m_offlineCache, m_onlineCache, "ADE configuration cache");
                handler.addCache(
                    m_offlineContainerConfigurationCache,
                    m_onlineContainerConfigurationCache,
                    "Inherited container cache");
                OpenCms.getEventManager().addCmsEventListener(handler);
                m_initStatus = Status.initialized;
            } catch (CmsException e) {
                m_initStatus = Status.notInitialized;
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Checks whether the given resource is configured as a detail page.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the resource which should be tested 
     * 
     * @return true if the resource is configured as a detail page 
     */
    public boolean isDetailPage(CmsObject cms, CmsResource resource) {

        CmsConfigurationCache cache = cms.getRequestContext().getCurrentProject().isOnlineProject()
        ? m_onlineCache
        : m_offlineCache;
        return cache.isDetailPage(cms, resource);
    }

    /**
     * Checks whether the ADE manager is initialized (this should usually be the case except during the setup).<p>
     * 
     * @return true if the ADE manager is initialized 
     */
    public boolean isInitialized() {

        return m_initStatus == Status.initialized;
    }

    /**
     * Looks up the configuration data for a given sitemap path.<p>
     *
     * @param cms the current CMS context  
     * @param rootPath the root path for which the configuration data should be looked up
     *   
     * @return the configuration data 
     */
    public CmsADEConfigData lookupConfiguration(CmsObject cms, String rootPath) {

        CmsADEConfigData configData = internalLookupConfiguration(cms, rootPath);
        if (configData == null) {
            configData = new CmsADEConfigData();
            configData.initialize(cms.getRequestContext().getCurrentProject().isOnlineProject()
            ? m_onlineCms
            : m_offlineCms);
        }
        return configData;
    }

    /**
     * Reloads the configuration.<p>
     * 
     * Normally you shouldn't call this directly since the event handlers take care of updating the configuration.
     */
    public void refresh() {

        m_onlineCache.initialize();
        m_offlineCache.initialize();
    }

    /**
     * Saves a list of detail pages.<p>
     * @param cms the cms context 
     * @param rootPath the root path 
     * @param detailPages the detail pages 
     * @param newId the id to use for new detail pages without an id 
     * @return true if the detail pages could be successfully saved 
     * 
     * @throws CmsException if something goes wrong 
     */
    public boolean saveDetailPages(CmsObject cms, String rootPath, List<CmsDetailPageInfo> detailPages, CmsUUID newId)
    throws CmsException {

        CmsADEConfigData configData = lookupConfiguration(cms, rootPath);
        CmsDetailPageConfigurationWriter configWriter;
        String originalSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            if (configData.isModuleConfiguration()) {
                return false;
            }
            CmsResource configFile = configData.getResource();
            configWriter = new CmsDetailPageConfigurationWriter(cms, configFile);
            configWriter.updateAndSave(detailPages, newId);
            return true;
        } finally {
            cms.getRequestContext().setSiteRoot(originalSiteRoot);
        }
    }

    /**
     * Saves the favorite list, user based.<p>
     * 
     * @param cms the cms context
     * @param favoriteList the element list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveFavoriteList(CmsObject cms, List<CmsContainerElementBean> favoriteList) throws CmsException {

        saveElementList(cms, favoriteList, ADDINFO_ADE_FAVORITE_LIST);
    }

    /**
     * Saves the inheritance container information.<p>
     * 
     * @param cms the current cms context
     * @param pageResource the resource or parent folder
     * @param name the inheritance name
     * @param newOrder if the element have been reordered
     * @param elements the elements
     * 
     * @throws CmsException if something goes wrong
     */
    public void saveInheritedContainer(
        CmsObject cms,
        CmsResource pageResource,
        String name,
        boolean newOrder,
        List<CmsContainerElementBean> elements) throws CmsException {

        CmsContainerConfigurationWriter writer = new CmsContainerConfigurationWriter();
        writer.save(cms, name, newOrder, pageResource, elements);
    }

    /**
     * Saves the inheritance container information.<p>
     * 
     * @param cms the current cms context
     * @param sitePath the site path of the resource or parent folder
     * @param name the inheritance name
     * @param newOrder if the element have been reordered
     * @param elements the elements
     * 
     * @throws CmsException if something goes wrong
     */
    public void saveInheritedContainer(
        CmsObject cms,
        String sitePath,
        String name,
        boolean newOrder,
        List<CmsContainerElementBean> elements) throws CmsException {

        saveInheritedContainer(cms, cms.readResource(sitePath), name, newOrder, elements);
    }

    /**
     * Saves the favorite list, user based.<p>
     * 
     * @param cms the cms context
     * @param recentList the element list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveRecentList(CmsObject cms, List<CmsContainerElementBean> recentList) throws CmsException {

        saveElementList(cms, recentList, ADDINFO_ADE_RECENT_LIST);
    }

    /**
     * The method which is called when the OpenCms instance is shut down.<p>
     */
    public void shutdown() {

        // do nothing 
    }

    /**
     * Creates an element from its serialized data.<p> 
     * 
     * @param data the serialized data
     * 
     * @return the restored element bean
     * 
     * @throws JSONException if the serialized data got corrupted
     */
    protected CmsContainerElementBean elementFromJson(JSONObject data) throws JSONException {

        CmsUUID element = new CmsUUID(data.getString(FavListProp.ELEMENT.name().toLowerCase()));
        CmsUUID formatter = null;
        if (data.has(FavListProp.FORMATTER.name().toLowerCase())) {
            formatter = new CmsUUID(data.getString(FavListProp.FORMATTER.name().toLowerCase()));
        }
        Map<String, String> properties = new HashMap<String, String>();

        JSONObject props = data.getJSONObject(FavListProp.PROPERTIES.name().toLowerCase());
        Iterator<String> keys = props.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            properties.put(key, props.getString(key));
        }

        return new CmsContainerElementBean(element, formatter, properties, false);
    }

    /**
     * Converts the given element to JSON.<p>
     * 
     * @param element the element to convert
     * 
     * @return the JSON representation
     */
    protected JSONObject elementToJson(CmsContainerElementBean element) {

        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put(FavListProp.ELEMENT.name().toLowerCase(), element.getId().toString());
            if (element.getFormatterId() != null) {
                data.put(FavListProp.FORMATTER.name().toLowerCase(), element.getFormatterId().toString());
            }
            JSONObject properties = new JSONObject();
            for (Map.Entry<String, String> entry : element.getIndividualSettings().entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }
            data.put(FavListProp.PROPERTIES.name().toLowerCase(), properties);
        } catch (JSONException e) {
            // should never happen
            if (!LOG.isDebugEnabled()) {
                LOG.warn(e.getLocalizedMessage());
            }
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
        return data;
    }

    /** 
     * Gets the root path for a given resource structure id.<p>
     * 
     * @param structureId the structure id 
     * @param online if true, the resource will be looked up in the online project ,else in the offline project
     *  
     * @return the root path for the given structure id
     *  
     * @throws CmsException if something goes wrong 
     */
    protected String getRootPath(CmsUUID structureId, boolean online) throws CmsException {

        CmsConfigurationCache cache = online ? m_onlineCache : m_offlineCache;
        return cache.getPathForStructureId(structureId);
    }

    /**
     * Gets a tempfile project, creating one if it doesn't exist already.<p>
     * 
     * @param cms the CMS context to use 
     * @return the tempfile project
     *  
     * @throws CmsException if something goes wrong 
     */
    protected CmsProject getTempfileProject(CmsObject cms) throws CmsException {

        try {
            return cms.readProject(I_CmsProjectDriver.TEMP_FILE_PROJECT_NAME);
        } catch (CmsException e) {
            return cms.createTempfileProject();
        }
    }

    /**
     * Internal configuration lookup method.<p>
     * 
     * @param cms the cms context 
     * @param rootPath the root path for which to look up the configuration 
     * 
     * @return the configuration for the given path
     */
    protected CmsADEConfigData internalLookupConfiguration(CmsObject cms, String rootPath) {

        boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
        CmsConfigurationCache cache = online ? m_onlineCache : m_offlineCache;
        CmsADEConfigData result = cache.getSiteConfigData(rootPath);
        if (result == null) {
            result = cache.getModuleConfiguration();
        }
        return result;
    }

    /**
     * Saves an element list to the user additional infos.<p>
     * 
     * @param cms the cms context
     * @param elementList the element list
     * @param listKey the list key
     * 
     * @throws CmsException if something goes wrong 
     */
    private void saveElementList(CmsObject cms, List<CmsContainerElementBean> elementList, String listKey)
    throws CmsException {

        // limit the favorite list size to 100 entries to avoid the additional info size limit
        while (elementList.size() > 100) {
            elementList.remove(elementList.size() - 1);
        }
        JSONArray data = new JSONArray();
        for (CmsContainerElementBean element : elementList) {
            data.put(elementToJson(element));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        user.setAdditionalInfo(listKey, data.toString());
        cms.writeUser(user);
    }
}
