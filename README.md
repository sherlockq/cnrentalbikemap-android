Yongjiu Rental Bicycal Map
=======================

Thus application is made at beginning to fulfill the developer's own needs that the position and status of parking stations of Yongjiu Rental Bicycle could be accessible at any time. The related information is from Yongjiu's official website: www.chinarmb.com. Somehow due to the inaccuracy of some station's location data, they could be modified by the developer. Any advice on the information or the application is welcomed.

E-mail:sherlockq@gmail.com

== build procedure ==
1. you need android-maps-extensions (https://code.google.com/p/android-maps-extensions/), actionbarsherlock (http://actionbarsherlock.com/) referenced to build this app. android-support is not required because those two are its extension. However the android-support-v4.jar in each project's lib may have different fingerprint, just make them the same version.
2. add a string resource file under /res/values to supply google map api, such like:

<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string name="google_map_api_key_release">Release Key</string>
    <string name="google_map_api_key_debug">Debug Key</string>

</resources>

== Licence and Acknowledgement ==
"Yongjiu" brand, text and icons are properties of Shanghai Yongjiu Bicycle Co.Ltd.

The names, locations and other data regarding stations are from www.chinarmb.com while location of some could be modified. The realtime status of stations are directly from www.chinarmb.com

This application is under DBAD licence. Refer to http://www.dbad-license.org；Source code is published at https://github.com/sherlockq/cnrentalbikemap-android

This application used or modified codes, modules and images from below sources (May not be complete)
-android-maps-extensions: Provides extension for Google Maps API，espcially the clustering of markders
-ActionBarSherlock: Provides support of ActionBar for Android below 3.0
-Vector of bicycle from unknown source and port image from chinarmb.com are used to make application icon
-Vector images from http://www.yay.se/resources/android-native-icons are used to make icons for ActionBar
