// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package snapshotpolicy.sdk.sample;

import com.azure.resourcemanager.netapp.fluent.NetAppManagementClient;
import com.azure.resourcemanager.netapp.fluent.models.SnapshotPolicyInner;
import com.azure.resourcemanager.netapp.models.SnapshotPolicyPatch;
import snapshotpolicy.sdk.sample.common.Utils;

public class Update
{
    /**
     * Updates a Snapshot Policy
     * @param anfClient Azure NetApp Files Management Client
     * @param resourceGroup Name of the resource group where the Snapshot Policy will be updated
     * @param accountName Name of the Account
     * @param snapshotPolicyName Name of the Snapshot Policy being updated
     * @param policyPatch The Snapshot Policy body used in the update
     * @return The newly updated Snapshot Policy
     */
    public static SnapshotPolicyInner updateSnapshotPolicy(NetAppManagementClient anfClient, String resourceGroup, String accountName, String snapshotPolicyName, SnapshotPolicyPatch policyPatch)
    {
        SnapshotPolicyInner snapshotPolicy = anfClient.getSnapshotPolicies().beginUpdate(resourceGroup, accountName, snapshotPolicyName, policyPatch).getFinalResult();
        Utils.writeSuccessMessage("Snapshot Policy successfully updated");

        return snapshotPolicy;
    }
}
