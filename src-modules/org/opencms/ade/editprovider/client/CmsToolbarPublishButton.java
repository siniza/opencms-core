/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/editprovider/client/Attic/CmsToolbarPublishButton.java,v $
 * Date   : $Date: 2011/04/21 11:50:17 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.editprovider.client;

import org.opencms.gwt.client.ui.A_CmsToolbarButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDomUtil;

/**
 * A simplified publish button for the Toolbar direct edit provider.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarPublishButton extends A_CmsToolbarButton<CmsDirectEditToolbarHandler> {

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     */
    public CmsToolbarPublishButton(CmsDirectEditToolbarHandler handler) {

        super(I_CmsButton.ButtonData.PUBLISH, handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        // triggering a mouse-out event, as it won't be fired once the dialog has opened (the dialog will capture all events)
        CmsDomUtil.ensureMouseOut(getElement());
        setEnabled(false);
        getHandler().showPublishDialog();
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        setEnabled(true);
    }

}