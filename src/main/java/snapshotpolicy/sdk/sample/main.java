// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package snapshotpolicy.sdk.sample;

import com.ea.async.Async;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.netapp.v2020_06_01.*;
import com.microsoft.azure.management.netapp.v2020_06_01.implementation.*;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import snapshotpolicy.sdk.sample.common.CommonSdk;
import snapshotpolicy.sdk.sample.common.ServiceCredentialsAuth;
import snapshotpolicy.sdk.sample.common.Utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

import static com.ea.async.Async.await;

public class main
{
    /**
     * Sample console application that executes CRUD management operations on Azure NetApp Files resources
     * The volume created will use the SMB/CIFS protocol
     * @param args
     */
    public static void main( String[] args )
    {
        Utils.displayConsoleAppHeader();

        try
        {
            Async.init();
            runAsync();
            Utils.writeConsoleMessage("Sample application successfully completed execution");
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage(e.getMessage());
        }

        System.exit(0);
    }

    private static CompletableFuture<Void> runAsync()
    {
        //---------------------------------------------------------------------------------------------------------------------
        // Setting variables necessary for resources creation - change these to appropriate values related to your environment
        //---------------------------------------------------------------------------------------------------------------------
        boolean cleanup = false;

        String subscriptionId = "<subscription id>";
        String location = "eastus";
        String resourceGroupName = "anf01-rg";
        String vnetName = "vnet";
        String subnetName = "anf-sn";
        String anfAccountName = "anfaccount01";
        String snapshotPolicyName = "snapshotpolicy01";
        String capacityPoolName = "pool01";
        String capacityPoolServiceLevel = "Standard"; // Valid service levels are: Ultra, Premium, Standard
        String volumeName = "volume01";

        long capacityPoolSize = 4398046511104L;  // 4TiB which is minimum size
        long volumeSize = 107374182400L;  // 100GiB - volume minimum size

        // Authenticating using service principal, refer to README.md file for requirement details
        ServiceClientCredentials credentials = ServiceCredentialsAuth.getServicePrincipalCredentials(System.getenv("AZURE_AUTH_LOCATION"));
        if (credentials == null)
        {
            return CompletableFuture.completedFuture(null);
        }

        // Instantiating a new ANF management client
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        AzureNetAppFilesManagementClientImpl anfClient = new AzureNetAppFilesManagementClientImpl(credentials);
        anfClient.withSubscriptionId(subscriptionId);
        Utils.writeConsoleMessage("Api Version: " + anfClient.apiVersion());

        //---------------------------
        // Creating ANF resources
        //---------------------------

        //---------------------------
        // Create ANF Account
        //---------------------------
        Utils.writeConsoleMessage("Creating Azure NetApp Files Account...");

        String[] accountParams = {resourceGroupName, anfAccountName};
        NetAppAccountInner anfAccount = await(CommonSdk.getResourceAsync(anfClient, accountParams, NetAppAccountInner.class));
        if (anfAccount == null)
        {
            NetAppAccountInner newAccount = new NetAppAccountInner();
            newAccount.withLocation(location);

            try
            {
                anfAccount = await(Creation.createANFAccount(anfClient, resourceGroupName, anfAccountName, newAccount));
            }
            catch (CloudException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating account: " + e.body().message());
                throw e;
            }
        }
        else
        {
            Utils.writeConsoleMessage("Account already exists");
        }

        //---------------------------
        // Create Snapshot Policy
        //---------------------------
        Utils.writeConsoleMessage("Creating Snapshot Policy...");

        String[] policyParams = {resourceGroupName, anfAccountName, snapshotPolicyName};
        SnapshotPolicyInner snapshotPolicy = await(CommonSdk.getResourceAsync(anfClient, policyParams, SnapshotPolicyInner.class));
        if (snapshotPolicy == null)
        {
            HourlySchedule hourlySchedule = new HourlySchedule();
            hourlySchedule.withSnapshotsToKeep(5);
            hourlySchedule.withMinute(50); // arbitrary sample numbers

            DailySchedule dailySchedule = new DailySchedule();
            dailySchedule.withSnapshotsToKeep(5);
            dailySchedule.withHour(15);
            dailySchedule.withMinute(30);

            WeeklySchedule weeklySchedule = new WeeklySchedule();
            weeklySchedule.withSnapshotsToKeep(5);
            weeklySchedule.withDay("Monday");
            weeklySchedule.withHour(12);
            weeklySchedule.withMinute(30);

            MonthlySchedule monthlySchedule = new MonthlySchedule();
            monthlySchedule.withSnapshotsToKeep(5);
            monthlySchedule.withDaysOfMonth("10,11,12");
            monthlySchedule.withHour(14);
            monthlySchedule.withMinute(50);

            SnapshotPolicyInner newPolicy = new SnapshotPolicyInner();
            newPolicy.withHourlySchedule(hourlySchedule);
            newPolicy.withDailySchedule(dailySchedule);
            newPolicy.withWeeklySchedule(weeklySchedule);
            newPolicy.withMonthlySchedule(monthlySchedule);
            newPolicy.withLocation(location);
            newPolicy.withEnabled(true);

            try
            {
                snapshotPolicy = await(Creation.createSnapshotPolicy(anfClient, resourceGroupName, anfAccountName, snapshotPolicyName, newPolicy));
            }
            catch (CloudException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating snapshot policy: " + e.body().message());
                throw e;
            }
        }
        else
        {
            Utils.writeConsoleMessage("Snapshot Policy already exists");
        }

        //---------------------------
        // Create Capacity Pool
        //---------------------------
        Utils.writeConsoleMessage("Creating Capacity Pool...");

        String[] poolParams = {resourceGroupName, anfAccountName, capacityPoolName};
        CapacityPoolInner capacityPool = await(CommonSdk.getResourceAsync(anfClient, poolParams, CapacityPoolInner.class));
        if (capacityPool == null)
        {
            CapacityPoolInner newCapacityPool = new CapacityPoolInner();
            newCapacityPool.withServiceLevel(ServiceLevel.fromString(capacityPoolServiceLevel));
            newCapacityPool.withSize(capacityPoolSize);
            newCapacityPool.withLocation(location);

            try
            {
                capacityPool = await(Creation.createCapacityPool(anfClient, resourceGroupName, anfAccountName, capacityPoolName, newCapacityPool));
            }
            catch (CloudException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating capacity pool: " + e.body().message());
                throw e;
            }
        }
        else
        {
            Utils.writeConsoleMessage("Capacity Pool already exists");
        }

        //---------------------------
        // Create Volume
        //---------------------------
        Utils.writeConsoleMessage("Creating Volume with attached Snapshot Policy...");

        String[] volumeParams = {resourceGroupName, anfAccountName, capacityPoolName, volumeName};
        VolumeInner volume = await(CommonSdk.getResourceAsync(anfClient, volumeParams, VolumeInner.class));
        if (volume == null)
        {
            String subnetId = "/subscriptions/" + subscriptionId + "/resourceGroups/" + resourceGroupName +
                    "/providers/Microsoft.Network/virtualNetworks/" + vnetName + "/subnets/" + subnetName;

            VolumeSnapshotProperties snapshotProperties = new VolumeSnapshotProperties();
            snapshotProperties.withSnapshotPolicyId(snapshotPolicy.id());

            VolumePropertiesDataProtection dataProtection = new VolumePropertiesDataProtection();
            dataProtection.withSnapshot(snapshotProperties);

            VolumeInner newVolume = new VolumeInner();
            newVolume.withLocation(location);
            newVolume.withServiceLevel(ServiceLevel.fromString(capacityPoolServiceLevel));
            newVolume.withCreationToken(volumeName);
            newVolume.withSubnetId(subnetId);
            newVolume.withUsageThreshold(volumeSize);
            newVolume.withDataProtection(dataProtection);
            newVolume.withProtocolTypes(Collections.singletonList("NFSv3"));

            try
            {
                volume = await(Creation.createVolume(anfClient, resourceGroupName, anfAccountName, capacityPoolName, volumeName, newVolume));
            }
            catch (CloudException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating volume: " + e.body().message());
                throw e;
            }
        }
        else
        {
            Utils.writeConsoleMessage("Volume already exists");
        }

        //---------------------------
        // Update Snapshot Policy
        //---------------------------
        Utils.writeConsoleMessage("Updating Snapshot Policy");

        // Updating number of snapshots to keep for hourly schedule

        LinkedHashMap hourlySchedule = (LinkedHashMap) snapshotPolicy.hourlySchedule();
        hourlySchedule.replace("snapshotsToKeep", 10);

        SnapshotPolicyPatch snapshotPolicyPatch = new SnapshotPolicyPatch();
        snapshotPolicyPatch.withHourlySchedule(hourlySchedule);
        snapshotPolicyPatch.withLocation(snapshotPolicy.location());
        snapshotPolicyPatch.withEnabled(true);

        try
        {
            await(Update.updateSnapshotPolicy(anfClient, resourceGroupName, anfAccountName, snapshotPolicyName, snapshotPolicyPatch));
        }
        catch (CloudException e)
        {
            Utils.writeConsoleMessage("An error occurred while updating snapshot policy: " + e.body().message());
            throw e;
        }

        Utils.writeConsoleMessage("Wait a few seconds for snapshot policy to complete update operation before deleting resources...");
        Utils.threadSleep(5000);

        //---------------------------
        // Cleaning up resources
        //---------------------------

        /*
          Cleanup process. For this process to take effect please change the value of
          the boolean variable 'cleanup' to 'true'
          The cleanup process starts from the innermost resources down in the hierarchy chain.
          In this case: Volume -> Snapshot Policy -> Capacity Pool -> Account
          Note that a Snapshot Policy can be used by multiple Volumes, therefore we must first delete the Volume(s)
          using the Snapshot Policy before we can delete the policy itself
        */
        if (cleanup)
        {
            Utils.writeConsoleMessage("Cleaning up all created resources");

            try
            {
                await(Cleanup.runCleanupTask(anfClient, volumeParams, VolumeInner.class));
                // ARM workaround to wait for the deletion to complete
                CommonSdk.waitForNoANFResource(anfClient, volume.id(), VolumeInner.class);
                Utils.writeSuccessMessage("Volume successfully deleted: " + volume.id());

                await(Cleanup.runCleanupTask(anfClient, policyParams, SnapshotPolicyInner.class));
                CommonSdk.waitForNoANFResource(anfClient, snapshotPolicy.id(), SnapshotPolicyInner.class);
                Utils.writeSuccessMessage("Snapshot Policy successfully deleted: " + volume.id());

                await(Cleanup.runCleanupTask(anfClient, poolParams, CapacityPoolInner.class));
                CommonSdk.waitForNoANFResource(anfClient, capacityPool.id(), CapacityPoolInner.class);
                Utils.writeSuccessMessage("Capacity Pool successfully deleted: " + volume.id());

                await(Cleanup.runCleanupTask(anfClient, accountParams, NetAppAccountInner.class));
                CommonSdk.waitForNoANFResource(anfClient, anfAccount.id(), NetAppAccountInner.class);
                Utils.writeSuccessMessage("Account successfully deleted: " + volume.id());
            }
            catch (CloudException e)
            {
                Utils.writeConsoleMessage("An error occurred while deleting resource: " + e.body().message());
                throw e;
            }
        }

        return CompletableFuture.completedFuture(null);
    }
}
