// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package snapshotpolicy.sdk.sample;

import com.microsoft.azure.management.netapp.v2020_06_01.SnapshotPolicyPatch;
import com.microsoft.azure.management.netapp.v2020_06_01.implementation.AzureNetAppFilesManagementClientImpl;
import com.microsoft.azure.management.netapp.v2020_06_01.implementation.SnapshotPolicyInner;
import snapshotpolicy.sdk.sample.common.Utils;

import java.util.concurrent.CompletableFuture;

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
    public static CompletableFuture<SnapshotPolicyInner> updateSnapshotPolicy(AzureNetAppFilesManagementClientImpl anfClient, String resourceGroup,
                                                                              String accountName, String snapshotPolicyName, SnapshotPolicyPatch policyPatch)
    {
        SnapshotPolicyInner snapshotPolicy = anfClient.snapshotPolicies().update(resourceGroup, accountName, snapshotPolicyName, policyPatch);
        Utils.writeSuccessMessage("Snapshot Policy successfully updated");

        return CompletableFuture.completedFuture(snapshotPolicy);
    }
}
