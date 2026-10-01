# Soul Heart (Fabric, server-side only)

Currently updated for 26.2

Testing checklist
- /give yourself a soul_heart, eat it: message to you only, blue heart in tab for everyone.
- Try eating a second one: refused, action bar message.
- Die: inventory kept, XP drops, broadcast "<name>'s soul was protected", heart gone from tab.
- Relog/restart while protected: still protected.
- Craft both recipes (heart: shard + breeze rod + blaze rod + glow berries, any arrangement); check ancient city loot (raise SHARD_CHANCE to 1.0 to test).
