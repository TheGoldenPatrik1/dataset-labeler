# Flood Dataset Labeler

This is a custom program to label urban flooding datasets, implemented using [Java](https://www.java.com/en/).

## Steps to Run

1. Install the latest version of [Java](https://www.oracle.com/java/technologies/downloads/).
2. Install [Maven](https://maven.apache.org/install.html).
3. Execute `make` or `make build` to compile and run the program.
   * **Note for Windows Users**: as `make` is a Linux/Unix-specific command, you can either [install WSL](https://learn.microsoft.com/en-us/windows/wsl/install) to run Linux on your Windows machine or manually run the following commands:
       1. `mvn clean package`
       2. `java -jar target/my-labeler-project-1.2.1-SNAPSHOT.jar`
4. Use the GUI to select a directory of flood images to label.

## Changelog

- **1.0** - Initial release.
- **1.1** - Added a previous button.
- **1.2** - Added the object label selection panel.
- **1.2.1** - Added a checkbox to preserve selections and auto-populate them for the next image.