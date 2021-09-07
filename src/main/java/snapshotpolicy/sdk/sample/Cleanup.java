// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package snapshotpolicy.sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import snapshotpolicy.sdk.sample.common.Utils;

public class Cleanup
{
    /**
     * Deletes all created resources
     * @param anfClient Azure NetApp Files Management Client
     * @param params String array containing account name, pool name, etc, needed to delete resource
     * @param clazz Which resource is being deleted
     */
    public static <T> void runCleanupTask(NetAppManagementClient anfClient, String[] params, Class<T> clazz)
    {
        switch (clazz.getSimpleName())
        {
            case "VolumeInner":
                Utils.writeConsoleMessage("Deleting Volume...");
                anfClient.getVolumes().beginDelete(
                        params[0],
                        params[1],
                        params[2],
                        params[3]).getFinalResult();
                break;

            case "SnapshotPolicyInner":
                Utils.writeConsoleMessage("Deleting Snapshot Policy...");
                anfClient.getSnapshotPolicies().beginDelete(
                        params[0],
                        params[1],
                        params[2]).getFinalResult();
                break;

            case "CapacityPoolInner":
                Utils.writeConsoleMessage("Deleting Capacity Pool...");
                anfClient.getPools().beginDelete(
                        params[0],
                        params[1],
                        params[2]).getFinalResult();
                break;

            case "NetAppAccountInner":
                Utils.writeConsoleMessage("Deleting Account...");
                anfClient.getAccounts().beginDelete(
                        params[0],
                        params[1]).getFinalResult();
                break;

        }
    }
}
