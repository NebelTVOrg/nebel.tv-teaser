nebel.tv-teaser
===============

Nebel.TV teaser app has config file to have the ability to change URLs for each Top Level View.
At first start app creates folder NebelTV in the root of external storage(sdcard) of the device. That is where config file is created under the name config.xml

-------WARNING--------

Before updating to a new version of the app is is recommended to erase config file from external storage(sd-card) in order to avoid bugs regarding structural config changes.

---------END----------

Config file looks like this:
```xml
<config>
    <mood name="family">
    	<friends_feed>file:///android_asset/html/friends_feed.html</friends_feed>
    	<whats_close>file:///android_asset/html/whats_close.html</whats_close>
    	<recently_viewed>file:///android_asset/html/recently_viewed.html</recently_viewed>
    	<whats_hot>file:///android_asset/html/whats_hot.html</whats_hot>
    	<pictures>file:///android_asset/html/pictures.html</pictures>
    	<recommended>file:///android_asset/html/recommended.html</recommended>
    </mood>
    <mood name="kids">
    	<friends_feed>file:///android_asset/html/friends_feed.html</friends_feed>
    	<whats_close>file:///android_asset/html/whats_close.html</whats_close>
    	<recently_viewed>file:///android_asset/html/recently_viewed.html</recently_viewed>
    	<whats_hot>file:///android_asset/html/whats_hot.html</whats_hot>
    	<pictures>file:///android_asset/html/pictures.html</pictures>
    	<recommended>file:///android_asset/html/recommended.html</recommended>
    </mood>
    <mood name="romance">
    	<friends_feed>file:///android_asset/html/friends_feed.html</friends_feed>
    	<whats_close>file:///android_asset/html/whats_close.html</whats_close>
    	<recently_viewed>file:///android_asset/html/recently_viewed.html</recently_viewed>
    	<whats_hot>file:///android_asset/html/whats_hot.html</whats_hot>
    	<pictures>file:///android_asset/html/pictures.html</pictures>
    	<recommended>file:///android_asset/html/recommended.html</recommended>
    </mood>
</config>
```
For each mood there is a xml tag for each Top Level View and you can change the URL inside this tag.

There might be several possible errors while parsing this config: after changing config file xml tag for Top View was deleted or renamed; external storage(sdcard) was unmounted, etc. 
If there will be any error while parsing this config the default one (see above) will be used.
