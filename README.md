# SLAM_AR_Android

## Hint

- If `tr1/unordered_map file not found` appears when compiling `g2o`, use lower version of ndk instead.([r14b](https://developer.android.google.cn/ndk/downloads/older_releases))
- You can train your vocabulary to reduce the total size of program.


- Based on https://github.com/Martin20150405/SLAM_AR_Android
- 5.16.2022
- With p2p connection, an virtual object can be shared
- Applied a simplified coordinate system alignment menthod

- 5.25.2022
- Test socket programming for both devices
- Good demo that each device can act as host and resolver simultaneously

- 6.1.2022
- This version is the complete prototype of multi-user AR app. It supports at least 3 or more users to
- add object together. To test, first open the app. After SLAM set up, a device is set to group owner.
- Then other devices set as client and join the group. 
- First let all client send "+" request to the GO. After all client have sent, let to GO sent request "+".
- GO at this time will notify to each client with other clients address. So that when a client want to 
- add an object, it will tell the object info to all other devices.
