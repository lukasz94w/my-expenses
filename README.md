# my-finances

This repository contains application code for managing daily expenses and control household budget. The program was written with the use of Java for devices equipped with the Android operating system. The operation of the application has been tested on phones with Android 9 and 10.
The program allows, inter alia, to:
- adding transactions (expenses or incomes assigned to appropriate categories, such as e.g. sports, health, hobby),
- editing or deleting existing operations,
- filtering expenses according to the mentioned categories or, for example, sorting them by date,
- searching for transactions by name,
- setting limits (when they are exceeded, appropriate warning messages will appear),
- viewing transactions in the form of pie charts or bar charts,
- export of operations to a CSV file.

Technologies / libraries used:
- Android
- Java
- SQLite
- MPAndroidChart
- Gradle
- XML

Below are the screenshots of the application where it can be noticed some of the listed functionalities:

![1st_row](https://user-images.githubusercontent.com/53697813/160177812-59082bd7-f9a4-4fbc-ac86-9ca42feece5c.jpg)

![2nd_row](https://user-images.githubusercontent.com/53697813/160178212-3abd6487-15ee-403a-8bcc-42c17aadefe8.jpg)

![3rd_row](https://user-images.githubusercontent.com/53697813/160178936-431179da-ac15-44bc-a4ad-a19d9e9f9516.jpg)

![4th_row](https://user-images.githubusercontent.com/53697813/160179321-55435301-20e6-4958-beef-e06d41fd2322.jpg)

## Improvements
The following fixes can be made to the application code:
- refactor it by moving redundant code to methods (there is some boilerplate code),
- fix the error that occurs when you try to add an expense in a different month than the one currently viewed, for example, when viewing expenses from April and trying to add an expense in March, an error will occur and the application will be closed.
