/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/I_CmsResourceType.java,v $
 * Date   : $Date: 2004/09/20 05:38:42 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.List;

/**
 * Defines resource type descriptors for all resources in the VFS.<p>
 * 
 * Each file in the VFS must belong to an initialized resource type.
 * The available resource type are read during system startup ftom the configuration 
 * file <code>opencms-vfs.xml</code>.<p>
 * 
 * Certain resource types may require special handling for certain operations.
 * This is usually required for write operations, or other operations that 
 * modify the VFS database.
 * Therefore, the {@link org.opencms.file.CmsObject} defers handling of this 
 * operations to implementations of this interface.<p>
 * 
 * If you implement a new resource type, it's a good idea to extend the  
 * abstract class {@link org.opencms.file.types.A_CmsResourceType}.<p>
 * 
 * Important: The {@link org.opencms.file.CmsObject} passes the {@link org.opencms.db.CmsDriverManager}
 * object to implementations of this class. Using this object correctly is key to the 
 * resource type operations. Mistakes made in the implementation of a resource type
 * can screw up the system security and the database structure, and make you unhappy. 
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public interface I_CmsResourceType extends I_CmsConfigurationParameterHandler {

    /** The name of the addMapping() method. */
    String C_ADD_MAPPING_METHOD = "addMappingType";
    
    
    /**
     * Maps a file extension to a resource type.<p>
     * 
     * When uploading files into OpenCms, they must be mapped to the different
     * OpenCms resource types. The configuration, to map which extension to which
     * resouce type is done in the OpenCms VFS configuration.
     * 
     * @param mapping the file extension mapped to the resource type
     */
    void addMappingType(
        String mapping);
    
    /**
     * Changes the project id of the resource to the current project, indicating that 
     * the resource was last modified in this project.<p>
     * 
     * This information is used while publishing. Only resources inside the 
     * project folders that are new/modified/changed <i>and</i> that "belong" 
     * to the project (i.e. have the id of the project set) are published
     * with the project.<p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong  
     * 
     * @see CmsObject#changeLastModifiedProjectId(String)
     * @see CmsDriverManager#changeLastModifiedProjectId(org.opencms.file.CmsRequestContext, CmsResource)   
     */
    void changeLastModifiedProjectId(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException;
    
    /**
     * Changes the lock of a resource to the current user,
     * that is "steals" the lock from another user.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the name of the resource to change the lock with complete path
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#changeLock(String)
     * @see CmsDriverManager#changeLock(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void changeLock(
        CmsObject cms, 
        CmsDriverManager driverManager,        
        CmsResource resource
    ) throws CmsException;

    /**
     * Changes the resource flags of a resource.<p>
     * 
     * The resource flags are used to indicate various "special" conditions
     * for a resource. Most notably the "internal only" setting which signals 
     * that a resource can not be directly requested with it's URL.<p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to change the flags for
     * @param flags the new resource flags for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chflags(String, int)
     * @see CmsDriverManager#chflags(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void chflags(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int flags
    ) throws CmsException;

    /**
     * Changes the resource type of a resource.<p>
     * 
     * OpenCms handles resource according to the resource type,
     * not the file suffix. This is e.g. why a JSP in OpenCms can have the 
     * suffix ".html" instead of ".jsp" only. Changing the resource type
     * makes sense e.g. if you want to make a plain text file a JSP resource,
     * or a binary file an image etc.<p> 
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to change the type for
     * @param type the new resource type for this resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#chtype(String, int)
     * @see CmsDriverManager#chtype(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void chtype(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int type
    ) throws CmsException;
    
    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_NEW}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_AS_SIBLING}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param source the resource to copy
     * @param destination the name of the copy destination with complete path
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResource(String, String, int)
     * @see CmsDriverManager#copyResource(org.opencms.file.CmsRequestContext, CmsResource, String, int)
     */
    void copyResource(
        CmsObject cms,
        CmsDriverManager driverManager,
        CmsResource source,
        String destination,
        int siblingMode
    ) throws CmsException;

    /**
     * Copies a resource to the current project of the user.<p>
     * 
     * This is used to extend the current users project with the
     * specified resource, in case that resource is not yet part of the project.
     * The resource is not really copied like in a regular copy operation, 
     * it is in fact only "enabled" in the current users project.<p>   
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to apply this operation to
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#copyResourceToProject(String)
     * @see CmsDriverManager#copyResourceToProject(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void copyResourceToProject(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException;

    /**
     * Creates a new resource with the provided content and properties.<p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the name of the resource to create (full path)
     * @param content the content for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createResource(String, int, byte[], List)
     * @see CmsObject#createResource(String, int)
     * @see CmsDriverManager#createResource(org.opencms.file.CmsRequestContext, String, int, byte[], List)
     */
    CmsResource createResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        byte[] content, 
        List properties
    ) throws CmsException;
    
    /**
     * Creates a new sibling of the source resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param source the resource to create a sibling for
     * @param destination the name of the sibling to create with complete path
     * @param properties the individual properties for the new sibling
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#createSibling(String, String, List)
     * @see CmsDriverManager#createSibling(org.opencms.file.CmsRequestContext, CmsResource, String, List)
     */
    void createSibling(
        CmsObject cms, 
        CmsDriverManager driverManager,
        CmsResource source, 
        String destination, 
        List properties
    ) throws CmsException;

    /**
     * Deletes a resource.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_DELETE_SIBLINGS}</code></li>
     * <li><code>{@link org.opencms.main.I_CmsConstants#C_DELETE_OPTION_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#deleteResource(String, int)
     * @see CmsDriverManager#deleteResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void deleteResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int siblingMode
    ) throws CmsException;

    /**
     * Returns the default for the <code>cache</code> property setting of this resource type.<p>
     * 
     * The <code>cache</code> property is used by the Flex cache implementation 
     * to build the cache key that controls the caching behaviour of a resource.<p>
     * 
     * If <code>null</code> is returnd, this is the same as turning the cache 
     * off by default for this resource type.<p>
     * 
     * @return the default for the <code>cache</code> property setting of this resource type
     * 
     * @see org.opencms.flex.CmsFlexCache
     * @see org.opencms.flex.CmsFlexCacheKey
     */
    String getCachePropertyDefault();

    /**
     * Returns the loader type id of this resource type.<p>
     *
     * @return the loader type id of this resource type
     */
    int getLoaderId();
    
    /**
     * Returns the file extensions mappings for this resource type.<p>
     *
     * @return a list of file extensions mappings for this resource type
     */
    List getMapping();


    /**
     * Returns the type id of this resource type.<p>
     *
     * @return the type id of this resource type
     */
    int getTypeId();

    /**
     * Returns the name of this resource type.<p>
     *
     * @return the name of this resource type
     */
    String getTypeName();

    /**
     * Imports a resource to the OpenCms VFS.<p>
     * 
     * If a resource already exists in the VFS (i.e. has the same name and 
     * same id) it is replaced by the imported resource.<p>
     * 
     * If a resource with the same name but a different id exists, 
     * the imported resource is (usually) moved to the "lost and found" folder.<p> 
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resourcename the target name (with full path) for the resource after import
     * @param resource the resource to be imported
     * @param content the content of the resource
     * @param properties the properties of the resource
     * 
     * @return the imported resource
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsDriverManager#moveToLostAndFound(org.opencms.file.CmsRequestContext, String, boolean)
     * @see CmsObject#importResource(String, CmsResource, byte[], List)
     * @see CmsDriverManager#createResource(org.opencms.file.CmsRequestContext, String, CmsResource, byte[], List, boolean)
     */
    CmsResource importResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        String resourcename, 
        CmsResource resource,
        byte[] content, 
        List properties
    ) throws CmsException;

    /**
     * Returns <code>true</code> if a resource type is direct editable.<p>
     * 
     * @return <code>true</code> if a resource type is direct editable
     */
    boolean isDirectEditable();

    /**
     * Locks a resource.<p>
     *
     * The <code>mode</code> parameter controls what kind of lock is used.
     * Possible values for this parameter are: 
     * <ul>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_COMMON}</code></li>
     * <li><code>{@link org.opencms.lock.CmsLock#C_MODE_TEMP}</code></li>
     * </ul><p>
     * 
     * @param cms the initialized CmsObject
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to lock
     * @param mode flag indicating the mode for the lock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#lockResource(String, int)
     * @see CmsDriverManager#lockResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void lockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int mode
    ) throws CmsException;

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folder in the offline
     * project, and are unable to undelete them.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to move
     * @param destination the destination resource name
     *
     * @throws CmsException if something goes wrong
     * @see CmsObject#moveResource(String, String)
     * @see CmsObject#renameResource(String, String)
     * @see CmsDriverManager#copyResource(org.opencms.file.CmsRequestContext, CmsResource, String, int)
     * @see CmsDriverManager#deleteResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void moveResource(
        CmsObject cms, 
        CmsDriverManager driverManager,
        CmsResource resource, 
        String destination
    ) throws CmsException;

    /**
     * Replaces the content, type and properties of a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the name of the resource to replace
     * @param type the new type of the resource
     * @param content the new content of the resource
     * @param properties the new properties of the resource
     *  
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#replaceResource(String, int, byte[], List)
     * @see CmsDriverManager#replaceResource(org.opencms.file.CmsRequestContext, CmsResource, int, byte[], List)
     */
    void replaceResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int type, 
        byte[] content, 
        List properties
    ) throws CmsException;

    /**
     * Restores a file in the current project with a version from the backup archive.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to restore from the archive
     * @param tag the tag (version) id to resource form the archive
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#restoreResourceBackup(String, int)
     * @see CmsDriverManager#restoreResource(org.opencms.file.CmsRequestContext, CmsResource, int)
     */
    void restoreResourceBackup(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        int tag
    ) throws CmsException;

    /**
     * Change the timestamp information of a resource.<p>
     * 
     * This method is used to set the "last modified" date
     * of a resource, the "release" date of a resource, 
     * and also the "expires" date of a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to touch
     * @param dateLastModified the new last modified date of the resource
     * @param dateReleased the new release date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param dateExpired the new expire date of the resource, 
     *      use <code>{@link org.opencms.main.I_CmsConstants#C_DATE_UNCHANGED}</code> to keep it unchanged
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#touch(String, long, long, long, boolean)
     * @see CmsDriverManager#touch(org.opencms.file.CmsRequestContext, CmsResource, long, long, long)
     */
    void touch(
        CmsObject cms,
        CmsDriverManager driverManager,
        CmsResource resource,
        long dateLastModified,
        long dateReleased,
        long dateExpired,
        boolean recursive
    ) throws CmsException;

    /**
     * Undos all changes in the resource by restoring the version from the 
     * online project to the current offline project.<p>
     * 
     * This is also used when doing an "undelete" operation.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to undo the changes for
     * @param recursive if this operation is to be applied recursivly to all resources in a folder
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#undoChanges(String, boolean)
     * @see CmsDriverManager#undoChanges(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void undoChanges(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource, 
        boolean recursive        
    ) throws CmsException;

    /**
     * Unlocks a resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to unlock
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#unlockResource(String)
     * @see CmsDriverManager#unlockResource(org.opencms.file.CmsRequestContext, CmsResource)
     */
    void unlockResource(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsResource resource
    ) throws CmsException;

    /**
     * Writes a resource, including it's content.<p>
     * 
     * Applies only to resources of type <code>{@link CmsFile}</code>
     * have a binary content attached.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to apply this operation to
     *
     * @return the written resource
     *
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writeFile(CmsFile)
     * @see CmsDriverManager#writeFile(org.opencms.file.CmsRequestContext, CmsFile)
     */
    CmsFile writeFile(
        CmsObject cms, 
        CmsDriverManager driverManager, 
        CmsFile resource
    ) throws CmsException;
    
    /**
     * Writes a property for a specified resource.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to write the property for
     * @param property the property to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObject(String, CmsProperty)
     * @see CmsDriverManager#writePropertyObject(org.opencms.file.CmsRequestContext, CmsResource, CmsProperty)
     */    
    void writePropertyObject(
        CmsObject cms, 
        CmsDriverManager driverManager,
        CmsResource resource, 
        CmsProperty property
    ) throws CmsException;
    
    /**
     * Writes a list of properties for a specified resource.<p>
     * 
     * Code calling this method has to ensure that the no properties 
     * <code>a, b</code> are contained in the specified list so that <code>a.equals(b)</code>, 
     * otherwise an exception is thrown.<p>
     * 
     * @param cms the current cms context
     * @param driverManager the initialized OpenCms driver manager
     * @param resource the resource to write the properties for
     * @param properties the list of properties to write
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see CmsObject#writePropertyObjects(String, List)
     * @see CmsDriverManager#writePropertyObjects(org.opencms.file.CmsRequestContext, CmsResource, List)
     */    
    void writePropertyObjects(
        CmsObject cms, 
        CmsDriverManager driverManager,
        CmsResource resource, 
        List properties
    ) throws CmsException;
    
}