# Alibaba Cloud Intelligent Speech Interaction Service

## Backend Configuration
Open the Alibaba Cloud Intelligent Speech Interaction Service, create a project, and open the created App in the Project List to obtain the APP_KEY. Configure project functionality here.

In RAM (Resource Access Management), click on AccessKey to enter the Access Key Management page. Create an Access Key here to obtain ACCESS_KEY and ACCESS_KEY_SECRET.

## SDK/API Key Configuration
Configuration in AndroidManifest.xml under the application tag (can also be configured in code):

```xml
<meta-data
    android:name="ALI_IVS_ACCESS_KEY"
    android:value="${ALI_IVS_ACCESS_KEY}" />
<meta-data
    android:name="ALI_IVS_ACCESS_KEY_SECRET"
    android:value="${ALI_IVS_ACCESS_KEY_SECRET}" />
<meta-data
    android:name="ALI_IVS_APP_KEY"
    android:value="${ALI_IVS_APP_KEY}" />
```