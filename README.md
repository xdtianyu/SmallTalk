## SmallTalk

Small Talk is an android instant message client.


## Server side example
1\. Install Prosody

    apt-get install prosody

2\. Prosody configuration

    vi /etc/prosody/prosody.cfg.lua
    VirtualHost "192.168.4.134"
        enabled = true

3\. Restart service and add new user

    service prosody restart
    prosodyctl register 10000 192.168.4.134 123456
    prosodyctl register 10001 192.168.4.134 123456

## Thanks to

Asmack, an android xmpp library

Prosody, a Lightweight Jabber/XMPP server.

