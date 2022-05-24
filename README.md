# jme-video-player

### Requirements
- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- [Minie](https://github.com/stephengold/Minie) - A physics library for JMonkeyEngine.
- JavaFX

### Youtube Videos
- [How to play a video intro for your game](https://youtu.be/5lwIorg5tbM)
- [Destructible Wall Generator](https://www.youtube.com/watch?v=Vp7nPncpZqs)

### How to build 
- Download jdk-17 from : 
https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
- Clone locally.
```bash
cd ./Gradle-Projects
git clone https://github.com/capdevon/jme-video-player.git
```
- Run gradle build script.
```bash
./gradlew build
```
- Change the `mainClassName` to your favourite demo class in the `build.gradle`.
- Now test by running the gradle run task.
```bash
./gradlew run
```
