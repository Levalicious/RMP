# RMP
RMP (Reliable Message Protocol), a (mostly?) reliable connectionless UDP-based networking protocol.

Currently retransmits every 3 seconds until it receives an ack, up to 10 retransmissions. May change either the retransmit delay or the number of retransmissions provided I discover some reason to do so.
