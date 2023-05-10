# NaveCure

This project was created using the latest version of Android Studio Dolphin. It is continously being upgraded during development, the latest version will ensure the least bugs when cloning the project. 

A Pixel 4 emulator is recommended as it has playstore capabilities and seems to be fairly stable when testing. Target API for Pixel 4 should be 32 for the app to work.

A Google Maps API Key is required, I will share it with you guys in one of our messsaging groups.

To clone the project, open Android Studio and make sure it is linked to your GitHub. If a project is already open in Android Studio, go to 'File' and 'Close Project'. This should take you to the screen for selecting a new or current project. Otherwise, the screen for selecting a new or current project should be the first thing that loads on your screen.

In the project selection screen, click on 'Get From VCS', it should open the 'Get from Version Control' window. On the left window pane, your GitHub screen name should be visible. Click on it, the right window pane should populate the different repositories available.

<img width="592" alt="image" src="https://user-images.githubusercontent.com/80980080/196011661-86fb63ef-570c-4284-84d5-d0fb11d5d6f9.png">

<img width="603" alt="image" src="https://user-images.githubusercontent.com/80980080/196011692-1bcb1f10-cf28-4d30-895f-c2ca504c7b7a.png">


Select 'NaveCure' from the right pane of the 'Get from Version Control' window. Make sure the directory where the project will be cloned is where you want it to be. Click the clone button on the bottom right of screen. After the project is cloned, the gradle will not compile because the API key mentioned above will be missing. It is kept from being uploaded to GitHub so that it many not be stolen. It is visible in the image below but since repo is private and they key is part of the image, it's not very likely a web crawler will steal it. 


In Android Studio, after cloning project, click the 'Project' tab on the left hand edged of screen. The directory 'app' should be open, look for 'Gradle Scripts' and expand if not already expanded. 

Look for 'local.properties' file and double click on it. The file will open on the editor.

At the end of file, declare a variable, MAPS_API_KEY = "API_KEY_GOES_HERE"
Replace "API_KEY_GOES_HERE" with your API key obtained from google. This will meet the requirement for a Google Maps API Key and this error should not be an issue going forward. Other issues will still have to be debugged if necessary i.e. missing dependencies, etc.





