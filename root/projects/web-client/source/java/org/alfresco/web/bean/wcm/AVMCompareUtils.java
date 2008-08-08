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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.util.NameMatcher;
import org.alfresco.web.app.Application;

/**
 * AVMCompare Utils
 * @author ValerySh
 *
 */
public class AVMCompareUtils
{

    /**
     * Get a difference map between two corresponding node trees.
     * @param avmSyncService AVMSyncService
     * @param srcVersion The version id for the source tree.
     * @param srcPath The avm path to the source tree.
     * @param dstVersion The version id for the destination tree.
     * @param dstPath The avm path to the destination tree.
     * @param excluder A NameMatcher used to exclude files from consideration.
     * @return list of compared objects
     */
    public static List<Map<String, String>> getComparedNodes(AVMSyncService avmSyncService, int srcVersion, String srcPath, int dstVersion, String dstPath, NameMatcher excluder)
    {
        FacesContext context = FacesContext.getCurrentInstance();
        List<AVMDifference> compare = avmSyncService.compare(srcVersion, srcPath, dstVersion, dstPath, excluder);
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (AVMDifference diff : compare)
        {
            String path = diff.getSourcePath();
            Map<String, String> node = new HashMap<String, String>();
            String sandboxPath = AVMUtil.getSandboxPath(path);
            node.put("path", path.replaceFirst(sandboxPath, ""));
            node.put("name", path.substring(path.lastIndexOf("/") + 1));

            String status;
            switch (diff.getDifferenceCode())
            {
            case AVMDifference.OLDER:
                status = Application.getMessage(context, "avm_compare_older");
                break;
            case AVMDifference.NEWER:
                status = Application.getMessage(context, "avm_compare_newer");
                break;
            case AVMDifference.SAME:
                status = Application.getMessage(context, "avm_compare_same");
                break;
            case AVMDifference.DIRECTORY:
                status = Application.getMessage(context, "avm_compare_directory");
                break;
            case AVMDifference.CONFLICT:
                status = Application.getMessage(context, "avm_compare_conflict");
                break;
            default:
                status = "";
            }
            node.put("status", status);

            result.add(node);
        }
        return result;
    }

    /**
     * checks the version of the first is accessible for Store
     * @param avmService AVMService
     * @param name The name of the AVMStore
     * @param version Version
     * @return true if version is first
     */
    public static boolean isFirstVersion(AVMService avmService, String name, int version)
    {
        boolean result = false;
        List<Integer> allVersions = getAllVersionID(avmService, name);

        if (version == Collections.min(allVersions))
            result = true;
        return result;
    }

    /**
     * checks the version of the last is accessible for Store
     * @param avmService AVMService
     * @param name The name of the AVMStore
     * @param version Version
     * @return true if version is latest
     */
    public static boolean isLatestVersion(AVMService avmService, String name, int version)
    {
        boolean result = false;
        List<Integer> allVersions = getAllVersionID(avmService, name);
        if (version == Collections.max(allVersions))
            result = true;
        return result;
    }

    /**
     * Get the versions id in an AVMStore
     * @param avmService AVMService
     * @param name The name of the AVMStore
     * @return List versions id
     */
    public static List<Integer> getAllVersionID(AVMService avmService, String name)
    {
        List<Integer> allVersions = new ArrayList<Integer>();
        List<VersionDescriptor> listVersion = avmService.getStoreVersions(name);
        for (VersionDescriptor vd : listVersion)
        {
            if ((vd.getTag() != null || AVMUtil.isUserStore(name)) && vd.getVersionID() > 2)
            {
                allVersions.add(vd.getVersionID());
            }
        }

        return allVersions;
    }

    /** Get Previous Version Id
     * @param avmService AVMService
     * @param name The name of the AVMStore
     * @param version Current version Id
     * @return Previous Version Id
     */
    public static int getPrevVersionID(AVMService avmService, String name, int version)
    {
        List<Integer> allVersions = getAllVersionID(avmService, name);
        Collections.sort(allVersions);
        int index = allVersions.indexOf(version);
        if (index == 0)
            return 0;
        return allVersions.get(index - 1);
    }

    /**
     * Receive Stores List
     * @param avmService AVMService
     * @return List Stores name
     */
    public static List<String> receiveStoresList(AVMService avmService)
    {
        List<String> result = new ArrayList<String>();
        List<AVMStoreDescriptor> storeDescs = avmService.getStores();
        for (AVMStoreDescriptor storeDesc : storeDescs)
        {
            if (!storeDesc.getCreator().equalsIgnoreCase("system") && !AVMUtil.isPreviewStore(storeDesc.getName()))
                result.add(storeDesc.getName());
        }
        return result;
    }
}
