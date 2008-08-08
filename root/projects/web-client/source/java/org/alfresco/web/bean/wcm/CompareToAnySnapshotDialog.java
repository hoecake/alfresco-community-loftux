/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;

/**
 * Class for compareToAnySnapshot dialog
 *
 * @author Dmitry Velichkevich
 * @author Dmitry Lazurkin
 */
public class CompareToAnySnapshotDialog extends CompareSnapshotDialog
{
    private static final long serialVersionUID = 5483432383286687197L;

    private static final String COMPARE_TO_ANY_SNAPSHOT_DESCRIPTION_MESSAGE_TEXT_ID = "snapshot_compare_to_any_description";

    public static final String MSG_ERROR_VERSION_NOT_VALID = "error_version_validate";

    private int userSpecifiedVersion;

    private boolean storeChanged;
    private String userSpecifiedStore;

    private String userSpecifiedRoot;

    private List<Integer> availableVersionNumbers;
    private int curAvailableVersionNumber;

    private boolean compare;

    /**
     * Builds list of available version numbers
     */
    private void buildAvailibleVersionNumbers()
    {
        this.curAvailableVersionNumber = -1;
        this.availableVersionNumbers = AVMCompareUtils.getAllVersionID(getAvmService(), userSpecifiedStore);
    }

    @Override
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);
        userSpecifiedStore = sandbox;
        userSpecifiedRoot = storeRoot;
        userSpecifiedVersion = -1;
        buildAvailibleVersionNumbers();
        this.compare = true;
        this.storeChanged = false;
    }

    /**
     * @return true if snapshot's version is correct
     */
    private boolean isCorrectVersion(int userSpecVersion)
    {
        return userSpecVersion == -1 || availableVersionNumbers.contains(userSpecVersion);
    }

    @Override
    public List<Map<String, String>> getComparedNodes()
    {
        if (compare)
        {
            this.compare = false;

            List<Map<String, String>> nodes = null;

            if (isCorrectVersion(userSpecifiedVersion))
            {
                nodes = AVMCompareUtils.getComparedNodes(getAvmSyncService(), version, storeRoot, userSpecifiedVersion, userSpecifiedRoot, null);
            }

            return nodes;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    /**
     * @return list of stores select items
     */
    public List<SelectItem> getStoresList()
    {
        List<String> stores = AVMCompareUtils.receiveStoresList(getAvmService());

        List<SelectItem> result = new ArrayList<SelectItem>();

        for (String itemValue : stores)
        {
            result.add(new SelectItem(itemValue, itemValue, itemValue, false));
        }

        return result;
    }

    /**
     * Action listener method that sets flag for starting compare
     *
     * @param event action event
     */
    public void refreshComparePanel(ActionEvent event)
    {
        this.compare = true;
    }

    /**
     * Getter for user specified version
     *
     * @return userSpecifiedVersion
     */
    public int getUserSpecifiedVersion()
    {
        return userSpecifiedVersion;
    }

    /**
     * Setter for user specified version
     *
     * @param userSpecifiedVersion user specified version
     */
    public void setUserSpecifiedVersion(int userSpecifiedVersion)
    {
        if (this.storeChanged == false)
        {
            if (this.userSpecifiedVersion != userSpecifiedVersion)
            {
                if (userSpecifiedVersion != -1)
                {
                    int index = availableVersionNumbers.indexOf(userSpecifiedVersion);
                    if (index != -1)
                    {
                        this.curAvailableVersionNumber = index;
                        this.userSpecifiedVersion = userSpecifiedVersion;
                    }
                    else
                    {
                        Utils.addErrorMessage(Application.getMessage(FacesContext.getCurrentInstance(), MSG_ERROR_VERSION_NOT_VALID));
                        FacesContext.getCurrentInstance().renderResponse();
                    }
                }
                else
                {
                    this.curAvailableVersionNumber = -1;
                    this.userSpecifiedVersion = -1;
                }
            }
        }
        else
        {
            this.storeChanged = false;
        }
    }

    /**
     * Getter for user specified Store
     *
     * @return userSpecifiedStore user specified store
     */
    public String getUserSpecifiedSnapshot()
    {
        return userSpecifiedStore;
    }

    /**
     * Setter for user specified snapshot
     *
     * @param setUserSpecifiedSnapshot user specified snapshot
     */
    public void setUserSpecifiedSnapshot(String userSpecifiedSnapshot)
    {
        if (userSpecifiedSnapshot.equals(userSpecifiedStore) == false)
        {
            this.storeChanged = true;
            this.userSpecifiedStore = userSpecifiedSnapshot;
            this.userSpecifiedRoot = AVMUtil.buildSandboxRootPath(this.userSpecifiedStore);
            this.userSpecifiedVersion = -1;
            buildAvailibleVersionNumbers();
        }
    }

    @Override
    public String getSandbox()
    {
        return this.userSpecifiedStore;
    }

    @Override
    protected String getDescription()
    {
        return COMPARE_TO_ANY_SNAPSHOT_DESCRIPTION_MESSAGE_TEXT_ID;
    }

    /**
     * @return true if increment button for version is disabled
     */
    public boolean isIncrementVersionButtonDisabled()
    {
        return (this.curAvailableVersionNumber + 1) == this.availableVersionNumbers.size();
    }

    /**
     * @return true if decrement button for version is disabled
     */
    public boolean isDecrementVersionButtonDisabled()
    {
        return this.curAvailableVersionNumber == -1;
    }

    /**
     * Increments version number
     *
     * @param event action event
     */
    public void incrementVersion(ActionEvent event)
    {
        this.curAvailableVersionNumber++;
        this.userSpecifiedVersion = this.availableVersionNumbers.get(this.curAvailableVersionNumber);
    }

    /**
     * Decrements version number
     *
     * @param event action event
     */
    public void decrementVersion(ActionEvent event)
    {
        this.curAvailableVersionNumber--;
        this.userSpecifiedVersion = (this.curAvailableVersionNumber != -1) ? (this.availableVersionNumbers.get(this.curAvailableVersionNumber)) : (-1);
    }

}