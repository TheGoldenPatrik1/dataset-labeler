# Flood Dataset Labeler

This is a custom program to label datasets, implemented using [Java](https://www.java.com/en/). Although it was originally intended specifically for urban flooding, subsequent releases have made it generalizable to many use cases.

## Configuration

In order to execute the program, you must create a [JSON](https://www.json.org/json-en.html) file named `options.json` in the current working directory in which you run the program. This file specifies all of the data labeling parameters. An example is provided [here](options.example.json).

Generally speaking, the JSON file ought to be a dictionary, where the key is the name of the data field and the value is a dictionary that fits the following format:

| Key | Type | Description | Required |
| ---- | ---- | ---- | ---- |
| `description` | `string` | The description of the data field and the label that will accompany the buttons. | Yes |
| `type` | `string`; one of `"boolean"`, `"select-one"`, or `"select-many"` | The type of input that the data field accepts. | Yes |
| `required` | `boolean` | Whether or not the data field is required | No |
| `options` | `array` or `dictionary` | The options that should be displayed for `select-one` and `select-many` input types. If the options are a dictionary, the key is the option and the value is the corresponding hover tooltip. | No |
| `disabled` | `dictionary` | A list of key, value pairs, where the key is the name of another data field and the value is the value of that data field for which this input should be disabled. | No |
| `keybinds` | `dictionary` | A list of key, value pairs, where the key is a keyboard character and the value is the button option to click on that keyboard event. | No |

## Steps to Run

1. Install the latest version of [Java](https://www.oracle.com/java/technologies/downloads/).
2. Install [Maven](https://maven.apache.org/install.html).
3. Execute `make` or `make build` to compile and run the program.
   * **Note for Windows Users**: as `make` is a Linux/Unix-specific command, you can either [install WSL](https://learn.microsoft.com/en-us/windows/wsl/install) to run Linux on your Windows machine or manually run the following commands:
       1. `mvn clean package`
       2. `java -jar target/my-labeler-project-2.1.3-SNAPSHOT.jar`
4. Use the GUI to select a directory of flood images to label.

### Arguments

Both the Makefile and the Java program accept the following command line arguments:

| Makefile | Java | Description | Default |
| ---- | ---- | ---- | ---- |
| `images=` | `-i` | Path to a directory of images. | Select via GUI |
| `labels=` | `-l` | Path to a labels JSON file or where it ought to be created. | `labels.json` |
| `options=` | `-o` | Path to an options JSON file. | `options.json` |

## Changelog

- **1.0** - Initial release.
- **1.1** - Added a previous button.
- **1.2** - Added the object label selection panel.
- **1.2.1** - Added a checkbox to preserve selections and auto-populate them for the next image.
- **2.0.0** - Rewrite to support any type of labeling, based on a JSON file of options.
- **2.0.1** - Added support for command line arguments.
- **2.1.0** - Added a configuration option for keybinds.
- **2.1.1** - Improved control panel layout, making it more uniform and generalizable.
- **2.1.2** - Add optional hover tooltips to the options field.
- **2.1.3** - Add skip button.