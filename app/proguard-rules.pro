# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/ty/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep class de.measite.smack.AndroidDebugger { *; }
-keep class * implements org.jivesoftware.smack.initializer.SmackInitializer
-keep class * implements org.jivesoftware.smack.provider.IQProvider
-keep class * implements org.jivesoftware.smack.provider.PacketExtensionProvider
-keep class * extends org.jivesoftware.smack.packet.Packet
-keep class org.jivesoftware.smack.XMPPConnection
-keep class org.jivesoftware.smack.ReconnectionManager
-keep class org.jivesoftware.smack.CustomSmackConfiguration
-keep class org.jivesoftware.smackx.disco.ServiceDiscoveryManager
-keep class org.jivesoftware.smackx.xhtmlim.XHTMLManager
-keep class org.jivesoftware.smackx.muc.MultiUserChat
-keep class org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager
-keep class org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager
-keep class org.jivesoftware.smackx.filetransfer.FileTransferManager
-keep class org.jivesoftware.smackx.iqlast.LastActivityManager
-keep class org.jivesoftware.smackx.commands.AdHocCommandManager
-keep class org.jivesoftware.smackx.ping.PingManager
-keep class org.jivesoftware.smackx.privacy.PrivacyListManager
-keep class org.jivesoftware.smackx.time.EntityTimeManager
-keep class org.jivesoftware.smackx.vcardtemp.VCardManager

-dontnote org.xbill.DNS.spi.DNSJavaNameServiceDescriptor
-dontwarn org.xbill.DNS.spi.DNSJavaNameServiceDescriptor
