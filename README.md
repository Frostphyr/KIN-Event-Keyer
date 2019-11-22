# Manual

## Environment

KIN Event Keyer is compiled with and is targeted to run on Java SE 6 which can be downloaded [here](https://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html).

**NOTE**: Machines on the Kmart network should already have Java SE 6 installed.

## File Format

* The event file must an "xlsx" file.
* It requires four columns: Division, Category, Type, and Change. It expects a row that contains a cell containing each of those names. If not found, it will prompt the user to specify the correct names for the columns. If multiple columns are present containing the same names, it will prompt the user which columns to use.
* The Division and Category columns both expect a numeric value followed by a period.
* The Type columns expects a valid percent.
* The Change columns expects "Change" for any category that should be keyed.

## Instructions

1. Run the executable file "KIN Event Keyer-*.jar".
2. Locate and select the "xlsx" file that contains the event.
3. If the file contains more than one sheet, it will prompt the user to select the sheet to use.
4. If the required columns are not found, it will prompt the user to enter the correct names of the columns. It will continue to prompt for the columns names until they are found.
5. If multiple columns containing the correct names exist, it will prompt the user to select the columns to use.
6. At the main window, if there were any errors reading any rows, it will display them at the top. These should be manually keyed if needed.
7. Below that, or at the top of the window, shows the current row to key. If only part of the event is to be keyed, the starting row can be changed.
8. Next are the settings for the keyer:
    1. Clearance: This lets the user decide whether the percent should be added for non-clearance, clearance, or both.
    2. Initial delay: This is the time, in milliseconds, between when the keyer is started and when it starts typing. This gives the user time to focus the KIN window. The recommended time is 1000-2000.
    3. Key delay: This is the time, in milliseconds, between each keystroke. This can vary based on the speed of the machine as slower machines may not be able to keep up with faster keystrokes. The recommended time is 50-100.
    4. Online order reminder: This will cause the keyer to pause for 2 minutes right before the scheduled online order reminder every 15 minutes and will dismiss the reminder before continuing. If online orders are still active, this should be checked.
9. Before starting the keyer, KIN should be at the appropriate screen to begin keying the event.
10. The keyer can be started by pressing the Start button or by pressing F11. Once started, the KIN window should be focused within the initial delay. The keyer will continue to run until finished or can be stopped by pressing the Stop button or by pressing F11.