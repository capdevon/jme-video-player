# jme-video-player

### Requirements
- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- [Minie](https://github.com/stephengold/Minie) - A physics library for JMonkeyEngine.
- JavaFX
- JDK-11 (minimum), JDK-17 (preferred)

### Youtube Videos
- [How to play a video intro for your game](https://youtu.be/5lwIorg5tbM)
- [Destructible Wall Generator](https://www.youtube.com/watch?v=Vp7nPncpZqs)

### How to build on Mac/Linux
- Download jdk-17 (preferred) or jdk-11 (minimum) from : 
    - [JDK-11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
    - [JDK-17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Clone locally.
```bash
cd ./Gradle-Projects
git clone https://github.com/capdevon/jme-video-player.git
```
- Run gradle build script:
```bash
./gradlew build
```
- Change the `mainClassName` to your favourite demo class in the `build.gradle`.
- Now test by running the gradle run task:
```bash
./gradlew run
```
- To visualize the stacktrace, add `--stacktrace` to the above commands:
```bash
./gradlew build --stacktrace
```
```bash
./gradlew run --stacktrace
```
- To stop the daemon process, use stop:
```bash
./gradlew -stop
```
### How to build on Windows
- Use the same commands with the same steps, but change `./gradlew` with `gradle`:
```bash
gradle build --stacktrace
```
```bash
gradle run --stacktrace
```
```bash
gradle -stop
```
