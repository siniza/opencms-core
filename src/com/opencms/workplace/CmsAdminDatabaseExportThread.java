/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminDatabaseExportThread.java,v $
* Date   : $Date: 2002/05/24 12:51:09 $
* Version: $Revision: 1.12 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.report.*;
import java.util.*;
import java.io.*;

/**
 * Thread for create a new project.
 * Creation date: (13.10.00 14:39:20)
 * @author: Hanjo Riege
 */

public class CmsAdminDatabaseExportThread extends Thread implements I_CmsConstants {

    private CmsObject m_cms;

    private String m_fileName;

    private String[] m_exportPaths;

    private String[] m_exportModules;

    private boolean m_excludeSystem;

    private boolean m_excludeUnchanged;

    private boolean m_exportUserdata;

    private I_CmsSession m_session;

    private boolean m_moduledataExport;

    // the object to send the information to the workplace.
    private CmsReport m_report;

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */

    public CmsAdminDatabaseExportThread(CmsObject cms, String fileName,
            String[] exportPaths, boolean excludeSystem, boolean excludeUnchanged,
            boolean exportUserdata, I_CmsSession session) {
        m_cms = cms;
        m_exportPaths = exportPaths;
        m_fileName = fileName;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_session = session;
        m_report = new CmsReport(new String[]{"<br>"});
        m_moduledataExport = false;
    }

    /**
     * Insert the method's description here.
     * Creation date: (13.09.00 09:52:24)
     */

    public CmsAdminDatabaseExportThread(CmsObject cms, String fileName,
            String[] exportChannels, String[] exportModules, I_CmsSession session) {
        m_cms = cms;
        m_exportPaths = exportChannels;
        m_exportModules = exportModules;
        m_fileName = fileName;
        m_session = session;
        m_moduledataExport = true;
    }

    public void run() {
         // Dont try to get the session this way in a thread!
         // It will result in a NullPointerException sometimes.
         // !I_CmsSession session = m_cms.getRequestContext().getSession(true);
        try {
            // do the export
            if(m_moduledataExport){
                m_cms.exportModuledata(m_fileName, m_exportPaths, m_exportModules);
            } else {
                m_cms.exportResources(m_fileName, m_exportPaths, m_excludeSystem, m_excludeUnchanged, m_exportUserdata, m_report);
            }
        }
        catch(CmsException e) {
            m_session.putValue(C_SESSION_THREAD_ERROR, Utils.getStackTrace(e));
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }

    /**
     * returns the part of the report that is ready.
     */
    public String getReportUpdate(){
        return m_report.getReportUpdate();
    }
}
