// Copyright (c) Microsoft and contributors.  All rights reserved.
//
// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package snapshotpolicy.sdk.sample;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.netapp.NetAppFilesManager;
import com.azure.resourcemanager.netapp.fluent.models.CapacityPoolInner;
import com.azure.resourcemanager.netapp.fluent.models.NetAppAccountInner;
import com.azure.resourcemanager.netapp.fluent.models.SnapshotPolicyInner;
import com.azure.resourcemanager.netapp.fluent.models.VolumeInner;
import com.azure.resourcemanager.netapp.models.*;
import snapshotpolicy.sdk.sample.common.CommonSdk;
import snapshotpolicy.sdk.sample.common.Utils;

import java.util.Collections;

public class main
{
    /**
     * Sample console application that executes CRUD management operations on Azure NetApp Files resources
     * Showcases how to create and use ANF Snapshot Policy
     * @param args
     */
    public static void main( String[] args )
    {
        Utils.displayConsoleAppHeader();

        try
        {
            run();
            Utils.writeConsoleMessage("Sample application successfully completed execution");
        }
        catch (Exception e)
        {
            Utils.writeErrorMessage(e.getMessage());
        }

        System.exit(0);
    }

    private static void run()
    {
        //---------------------------------------------------------------------------------------------------------------------
        // Setting variables necessary for resources creation - change these to appropriate values related to your environment
        //---------------------------------------------------------------------------------------------------------------------
        boolean cleanup = false;

        String subscriptionId = "<subscription-id>";
        String location = "<location>";
        String resourceGroupName = "<resource-group-name>";
        String vnetName = "<vnet-name>";
        String subnetName = "<subnet-name>";
        String anfAccountName = "anf-java-example-account";
        String snapshotPolicyName = "anf-java-example-snapshotpolicy";
        String capacityPoolName = "anf-java-example-pool";
        String capacityPoolServiceLevel = "Standard"; // Valid service levels are: Ultra, Premium, Standard
        String volumeName = "anf-java-example-volume";

        long capacityPoolSize = 4398046511104L;  // 4TiB which is minimum size
        long volumeSize = 107374182400L;  // 100GiB - volume minimum size

        // Instantiating a new ANF management client and authenticate
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        Utils.writeConsoleMessage("Instantiating a new Azure NetApp Files management client...");
        NetAppFilesManager manager = NetAppFilesManager
                .authenticate(credential, profile);

        //---------------------------
        // Creating ANF resources
        //---------------------------

        //---------------------------
        // Create ANF Account
        //---------------------------
        Utils.writeConsoleMessage("Creating Azure NetApp Files Account...");

        String[] accountParams = {resourceGroupName, anfAccountName};
        NetAppAccountInner anfAccount = (NetAppAccountInner) CommonSdk.getResource(manager.serviceClient(), accountParams, NetAppAccountInner.class);
        if (anfAccount == null)
        {
            NetAppAccountInner newAccount = new NetAppAccountInner();
            newAccount.withLocation(location);

            try
            {
                anfAccount = Creation.createANFAccount(manager.serviceClient(), resourceGroupName, anfAccountName, newAccount);
            }
            catch (AzureException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating account: " + e.getMessage());
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
        SnapshotPolicyInner snapshotPolicy = (SnapshotPolicyInner) CommonSdk.getResource(manager.serviceClient(), policyParams, SnapshotPolicyInner.class);
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
                snapshotPolicy = Creation.createSnapshotPolicy(manager.serviceClient(), resourceGroupName, anfAccountName, snapshotPolicyName, newPolicy);
            }
            catch (AzureException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating snapshot policy: " + e.getMessage());
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
        CapacityPoolInner capacityPool = (CapacityPoolInner) CommonSdk.getResource(manager.serviceClient(), poolParams, CapacityPoolInner.class);
        if (capacityPool == null)
        {
            CapacityPoolInner newCapacityPool = new CapacityPoolInner();
            newCapacityPool.withServiceLevel(ServiceLevel.fromString(capacityPoolServiceLevel));
            newCapacityPool.withSize(capacityPoolSize);
            newCapacityPool.withLocation(location);

            try
            {
                capacityPool = Creation.createCapacityPool(manager.serviceClient(), resourceGroupName, anfAccountName, capacityPoolName, newCapacityPool);
            }
            catch (AzureException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating capacity pool: " + e.getMessage());
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
        VolumeInner volume = (VolumeInner) CommonSdk.getResource(manager.serviceClient(), volumeParams, VolumeInner.class);
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
                volume = Creation.createVolume(manager.serviceClient(), resourceGroupName, anfAccountName, capacityPoolName, volumeName, newVolume);
            }
            catch (AzureException e)
            {
                Utils.writeConsoleMessage("An error occurred while creating volume: " + e.getMessage());
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

        HourlySchedule hourlySchedule = snapshotPolicy.hourlySchedule();
        hourlySchedule.withSnapshotsToKeep(10);

        SnapshotPolicyPatch snapshotPolicyPatch = new SnapshotPolicyPatch();
        snapshotPolicyPatch.withHourlySchedule(hourlySchedule);
        snapshotPolicyPatch.withLocation(snapshotPolicy.location());
        snapshotPolicyPatch.withEnabled(true);

        try
        {
            Update.updateSnapshotPolicy(manager.serviceClient(), resourceGroupName, anfAccountName, snapshotPolicyName, snapshotPolicyPatch);
        }
        catch (AzureException e)
        {
            Utils.writeConsoleMessage("An error occurred while updating snapshot policy: " + e.getMessage());
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
          In this case: Volume -> Capacity Pool -> Snapshot Policy -> Account
          Note that a Snapshot Policy can be used by multiple Volumes, therefore we must first delete the Volume(s)
          using the Snapshot Policy before we can delete the policy itself
        */
        if (cleanup)
        {
            Utils.writeConsoleMessage("Cleaning up all created resources");

            try
            {
                Cleanup.runCleanupTask(manager.serviceClient(), volumeParams, VolumeInner.class);
                // ARM workaround to wait for the deletion to complete
                CommonSdk.waitForNoANFResource(manager.serviceClient(), volume.id(), VolumeInner.class);
                Utils.writeSuccessMessage("Volume successfully deleted: " + volume.id());

                Cleanup.runCleanupTask(manager.serviceClient(), poolParams, CapacityPoolInner.class);
                CommonSdk.waitForNoANFResource(manager.serviceClient(), capacityPool.id(), CapacityPoolInner.class);
                Utils.writeSuccessMessage("Capacity Pool successfully deleted: " + capacityPool.id());

                Cleanup.runCleanupTask(manager.serviceClient(), policyParams, SnapshotPolicyInner.class);
                CommonSdk.waitForNoANFResource(manager.serviceClient(), snapshotPolicy.id(), SnapshotPolicyInner.class);
                Utils.writeSuccessMessage("Snapshot Policy successfully deleted: " + snapshotPolicy.id());

                Cleanup.runCleanupTask(manager.serviceClient(), accountParams, NetAppAccountInner.class);
                CommonSdk.waitForNoANFResource(manager.serviceClient(), anfAccount.id(), NetAppAccountInner.class);
                Utils.writeSuccessMessage("Account successfully deleted: " + anfAccount.id());
            }
            catch (AzureException e)
            {
                Utils.writeConsoleMessage("An error occurred while deleting resource: " + e.getMessage());
                throw e;
            }
        }
    }
}
