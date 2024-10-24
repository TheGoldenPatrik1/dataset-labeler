# Flood Dataset Labeler

This is a custom program to label urban flooding datasets, implemented using [Java](https://www.java.com/en/).

## Steps to Run

1. Install [Java](https://www.java.com/en/download/).
2. Install [Maven](https://maven.apache.org/install.html).
3. Create an `images` folder in the root directory of the project and populate it with the images that you would like to be labelled.
4. Execute `make` or `make build` to compile and run the program.
   * **Note for Windows Users**: as `make` is a Linux/Unix-specific command, you can either [install WSL](https://learn.microsoft.com/en-us/windows/wsl/install) to run Linx on your Windows machine or manually run the following commands:
       1. `mvn clean package`
       2. `java -jar target/my-labeler-project-1.0-SNAPSHOT.jar`