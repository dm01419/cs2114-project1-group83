# Workout Planner MVP

A Java console application that creates weekly workout routines from a fitness goal, available time, workout days, equipment, and physical limitations. Data stays in memory and is not saved between runs.

## Requirements

- JDK 17 or newer (Maven compiles for Java 17)
- Maven for JUnit tests; the demo needs no additional libraries

## Compile and run

From the project folder:

```bash
javac Main.java
java Main
```

Alternatively, run `java Main.java`. In VS Code, select **Workout Planner** in **Run and Debug** and press **F5**. Answer prompts in the terminal.

Example inputs, in order:

| Prompt | Input |
| --- | --- |
| Fitness goal | `Strength` |
| Minutes per workout | `45` |
| Workout days per week | `3` |
| Workout days | `Monday`, `Wednesday`, `Friday` (one per prompt) |
| Equipment | `barbell, dumbbells` |
| Limitations | Enter to skip |
| Preferred exercise | `Push Ups` |
| Custom exercise | Enter to skip |

The example generates Push, Pull, and Legs sessions. Enter `view` to see the plan again, `edit` to add/remove/change an exercise, or press Enter to finish. For a removal example, enter `edit`, `Monday`, `remove`, then `Push Ups`.

## Run tests

```bash
mvn test
```

Maven downloads JUnit 5 and build plugins on the first run.

## Implemented features

- Goals: Strength, Muscle Gain, Endurance, and Weight Loss, with different sets, reps, and ordering.
- Automatic splits: one day Full Body; two/four days Upper/Lower; three/six days Push/Pull/Legs; five days a body-part split; seven days adds Recovery.
- 52 built-in exercises covering Chest, Back, Legs, Shoulders, and Arms, plus custom exercises.
- Custom arm exercises can specify `Biceps` or `Triceps` for Push/Pull splits; generic `Arms` exercises remain in Arms and Full Body/Upper routines.
- Equipment checks, physical-limitation tags, exercise preferences, duplicate prevention, and routine editing.
- Time estimates: five minutes of warm-up, 3.5 minutes per working set including rest, and one minute between exercises. Generation and replacement respect the budget. Manual additions may exceed it with a warning; recovery days cannot be edited.
- Non-positive workout durations and missing fitness goals are rejected by workout generation. Invalid console inputs are re-prompted.
- Estimated target-muscle percentages for each exercise. These are illustrative classification weights, not measured muscle activation; limitation tags are not a complete injury assessment.

## Project structure

- `Main.java`: model classes, planner logic, and the existing interactive console demo
- `WorkoutPlannerTest.java`: JUnit tests
- `pom.xml`: Maven build and Java 17 configuration
- `.vscode/`: compile/run settings
- `DavidMoon.java`, `IshaanSrichitturi.java`, `PratyayApparaju.java`: team member files

The assignment diagram `Npng_System_Diagram_Clear.drawio` is not present in this repository and must be added separately.
