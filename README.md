---
page_type: sample
languages:
- java
products:
- azure
- azure-netapp-files
description: "This project demonstrates how to create a snapshot policy for Microsoft.NetApp resource provider using Java SDK."
---

# Azure NetAppFiles SDK Sample - Snapshot policy for Java

This project demonstrates how to use a Java sample application to create a snapshot policy for the Microsoft.NetApp
resource provider. 

In this sample application we perform the following operations:

* Creations
    * NetApp account
    * Snapshot policy
    * Capacity pool
    * Volume
* Updates
    * Snapshot policy
* Deletions
    * Volume
    * Snapshot policy
    * Capacity pool
    * NetApp account

>Note: The cleanup execution is disabled by default. If you want to run this end to end with the cleanup, simply
>change the value of Boolean variable 'cleanup' in main.java.

If you don't already have a Microsoft Azure subscription, you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212).

## Prerequisites

1. This project is built upon Maven, which must be installed in order to run the sample. See [instructions on installing Maven](https://maven.apache.org/install.html).
1. The sample is written in Java 11. The Maven compiler's target Java version is therefore Java 11, and the JAVA_HOME environment variable must be set to Java 11 or a newer version.
See [instructions on setting JAVA_HOME for windows](https://mkyong.com/java/how-to-set-java_home-on-windows-10/),
and [instructions on setting JAVA_HOME for macOS](https://mkyong.com/java/how-to-set-java_home-environment-variable-on-mac-os-x/).
1. Azure subscription
1. Subscription needs to be enabled for Azure NetApp Files. For more information, see
[Submit a waitlist request for accessing the service](https://docs.microsoft.com/azure/azure-netapp-files/azure-netapp-files-register#waitlist). 
1. Resource Group created
1. Virtual Network with a delegated subnet to Microsoft.Netapp/volumes resource. For more information, see
[Guidelines for Azure NetApp Files network planning](https://docs.microsoft.com/en-us/azure/azure-netapp-files/azure-netapp-files-network-topologies).
1. For this sample console application to work, authentication is needed. We will use Service Principal based authentication
    1. Within an [Azure Cloud Shell](https://docs.microsoft.com/en-us/azure/cloud-shell/quickstart) session, make sure
    you're logged in at the subscription where you want to be associated with the service principal by default:
        ```bash
        az account show
       ```
         If this is not the correct subscription, use:             
         ```bash
        az account set -s <subscription name or id>  
        ```
    1. Create a service principal using Azure CLI:
        ```bash
        az ad sp create-for-rbac --sdk-auth
        ```
       
       >Note: This command will automatically assign RBAC contributor role to the service principal at subscription level.
       You can narrow down the scope to the specific resource group where your tests will create the resources.

    1. Copy the output contents and paste it in a file called azureauth.json, and secure it with file system permissions. 
    1. Set an environment variable pointing to the file path you just created. Here is an example with Powershell and bash:
        
        Powershell
        ```powershell
        [Environment]::SetEnvironmentVariable("AZURE_AUTH_LOCATION", "C:\sdksample\azureauth.json", "User")
        ```
        Bash
        ```bash
        export AZURE_AUTH_LOCATION=/sdksamples/azureauth.json
        ```
    
## What does netappfiles-java-snapshot-policy-sdk-sample do?

This sample demonstrates how to create a snapshot policy using a NetApp account name in Azure NetApp Files.
Similar to other Azure NetApp Files SDK examples, the authentication method is based on a service principal. This project will first create a NetApp account and then a snapshot policy that is tied to that Account. Afterwards, it will create a capacity pool within the
account and finally a single volume that uses the newly created snapshot policy.

There is a section in the code dedicated to remove created resources. By default, this script will not remove all created resources;
this behavior is controlled by a boolean variable called 'cleanup'. If you want to erase all resources right after the
creation operations, set this variable to 'true'.

A snapshot policy uses schedules to create snapshots of volumes that can be **hourly**, **daily**, **weekly**, **monthly**.
The snapshot policy will also determine how many snapshots to keep.
The sample will create a snapshot policy with all schedules and then update a single schedule within the policy, changing
the value of the schedule's snapshots to keep.

>Note: This sample does not have a specific retrieve section, because we perform get operations in several
>places throughout the code.

## How the project is structured

The following table describes all files within this solution:

| Folder         | File Name                    | Description                                                                                                                                                                                                                                                               |
|----------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Root\\^           | main.java                   | Reads configuration, authenticates, executes all operations
| Root\\^           | Cleanup.java                | Performs the delete operations of the created resources
| Root\\^           | Creation.java               | Performs the creation operations of resources
| Root\\^           | Update.java                 | Performs the update operation of the snapshot policy
| Root\\^\common    | CommonSdk.java              | Class dedicated to common operations related to Azure NetApp Files SDK
| Root\\^\common    | ResourceUriUtils.java       | Class that exposes a few methods that help parsing URI's, building new URI's, or getting a resource name from a URI, etc
| Root\\^\common    | ServiceCredentialsAuth.java | A small support class for extracting and creating credentials from a File
| Root\\^\common    | Utils.java                  | Class that contains utility functions for writing output, retrieving AD password, etc.
>\\^ == src/main/java/snapshotpolicy/sdk/sample

## How to run the console application

1. Clone the SDK sample locally:
    ```powershell
    git clone https://github.com/Azure-Samples/netappfiles-java-snapshot-policy-sdk-sample
    ```
1. Change folder to **.\netappfiles-java-snapshot-policy-sdk-sample**.
1. Make sure you have the azureauth.json and its environment variable with the path to it defined (as previously described).
1. Make sure the JAVA_HOME environment variable is pointing to version 11 of Java or newer (see Prerequisites for instructions).
1. In the main.java class, change the values of the variables within the runAsync() function to reflect your environment:
1. Compile the console application:
    ```powershell
    mvn clean compile
    ```
1. Run the console application:
    ```powershell
    mvn exec:java -Dexec.mainClass="snapshotpolicy.sdk.sample.main"
    ```

Sample output
![e2e execution](./media/e2e-execution.png)

## References

* [Manage snapshots by using Azure NetApp Files](https://docs.microsoft.com/en-us/azure/azure-netapp-files/azure-netapp-files-manage-snapshots)
* [Resource limits for Azure NetApp Files](https://docs.microsoft.com/azure/azure-netapp-files/azure-netapp-files-resource-limits)
* [Azure Cloud Shell](https://docs.microsoft.com/azure/cloud-shell/quickstart)
* [Azure NetApp Files documentation](https://docs.microsoft.com/azure/azure-netapp-files/)
* [Download Azure SDKs](https://azure.microsoft.com/downloads/)
