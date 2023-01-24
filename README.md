# GatherContent AEM integration v2.0
[![Image of Integration](https://github.com/AntoniBertel/GatherContentAssets/blob/master/GC-AEM.png)](https://gathercontent.com/)

<img alt="GitHub contributors" src="https://img.shields.io/github/contributors/axamit/gathercontent-aem-integration">&nbsp;
<img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/axamit/gathercontent-aem-integration">&nbsp;
<img alt="GitHub Release Date" src="https://img.shields.io/github/release-date/axamit/gathercontent-aem-integration">&nbsp;
<img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr/axamit/gathercontent-aem-integration">&nbsp;
<img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr-raw/axamit/gathercontent-aem-integration">&nbsp;
<img alt="GitHub release (latest by date)" src="https://img.shields.io/github/downloads/axamit/gathercontent-aem-integration/latest/total">&nbsp;
<img alt="GitHub top language" src="https://img.shields.io/github/languages/top/axamit/gathercontent-aem-integration">&nbsp;

### 🛠 &nbsp;Tech Stack

![Adobe](https://img.shields.io/badge/adobe-%23FF0000.svg?style=for-the-badge&logo=adobe&logoColor=white)&nbsp;
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)&nbsp;
![JavaScript](https://img.shields.io/badge/-JavaScript-05122A?style=flat&logo=javascript)&nbsp;

GatherContent’s AEM integration allows content editors to import and update content from GatherContent to AEM. Editors are able to specify import mappings, defining which templates and fields should be mapped and then imported or updated. The integration also allows content editors to update the GatherContent workflow status for all items that are successfully imported or updated.

- [x] Migrate (import) content from GatherContent into AEM
- [x] Update migrated (imported) content in AEM


|  **`AEM 6.0`**   |  **`AEM 6.1`** | **`AEM 6.2`** | **`AEM 6.3`** | **`AEM 6.4`** | **`AEM 6.5`** |
|-------------------|----------------------|------------------|------------------|------------------|------------------|
|:ok_hand: Supported |:ok_hand: Supported |:ok_hand: Supported |:ok_hand: Supported |:ok_hand: Supported |:ok_hand: Supported |
Note: Latest versions of tool are tested on AEM 6.5. Previous versions may require some adaptation work.

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs as well as Hobbes-tests

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with  

    mvn clean install -PautoInstallPackage
    
Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish
    
Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

## Testing

v2.0 is in open beta.

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
    
## Produced by
[![Axamit](https://github.com/AntoniBertel/GatherContentAssets/blob/master/Axamit.png)](https://axamit.com/)

## Changelog
20/01/2023 [4.0.0-beta API migration](https://github.com/axamit/gathercontent-aem-integration/releases/tag/4.0.0-beta "Package attached")

23/09/2021 [2.0.9-beta API migration](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.9-beta "Package attached")

06/09/2019 [2.0.8-beta Tech Changes + Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.8-beta "Package attached")

07/12/2018 [2.0.7-beta Tech Changes](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.7-beta "Package attached")

24/05/2018 [2.0.6-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.6-beta "Package attached")

12/04/2018 [2.0.5-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.5-beta "Package attached")

03/10/2017 [2.0.4-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.4-beta "Package attached")

08/09/2017 [2.0.3-beta Bug Fix + Tech Changes](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.3-beta "Package attached")

15/08/2017 [2.0.2-beta Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.2-beta "Package attached")

12/08/2017 [2.0.1-beta Major Release 2](https://github.com/axamit/gathercontent-aem-integration/releases/tag/2.0.1-beta "Package attached")

27/02/2017 [1.0.38.1 Bug Fix](https://github.com/axamit/gathercontent-aem-integration/releases/tag/1.0.38.1 "Package attached")

28/11/2016 [1.0.38 Major Release 1](https://github.com/axamit/gathercontent-aem-integration/releases/tag/1.0.38 "Package attached")
