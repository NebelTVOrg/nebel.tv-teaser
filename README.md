nebel.tv-teaser
===============

Nebel.TV teaser app has config file to have the ability to change URLs for each Top Level View.
At first start app creates folder NebelTV in the root of external storage(sdcard) of the device. That is where config file is created under the name config.xml

Config file looks like this:
```xml
<config>
    <friends_feed>http://google.com</friends_feed>
    <whats_close>http://tut.by</whats_close>
    <recently_viewed>http://mail.ru</recently_viewed>
    <whats_hot>http://yandex.ru</whats_hot>
    <pictures>http://rambler.ru</pictures>
    <recommended>http://bing.com</recommended>
</config>
```
There is a xml tag for each Top Level View and you can change the URL inside this tag.

There might be several possible errors while parsing this config: after changing config file xml tag for Top View was deleted or renamed; external storage(sdcard) was unmounted, etc. 
If there will be any error while parsing this config the default one (see above) will be used.
