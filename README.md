# Workout Planner

A Java console demo that creates a weekly workout schedule based on your fitness goal, workout duration, available equipment, and physical limitations.

## Run the demo

In VS Code, open this project folder, select **Workout Planner** in **Run and Debug**, and press **F5**. This configuration compiles `Main.java` and opens the demo in the integrated terminal for input. The Java debugger extension must be installed.

Requires **JDK 17 or newer**. Maven and additional libraries are not required.
Open a terminal in this project folder in your IDE and run:

```bash
java Main.java
```

Enter the following sample values as each prompt appears. `Enter` means pressing the Enter key without typing anything.

| Prompt | Sample input |
| --- | --- |
| Fitness goal | `Strength` |
| Workout duration in minutes | `45` |
| Number of workout days (1-7) | `3` |
| Workout day 1 | `Monday` |
| Workout day 2 | `Wednesday` |
| Workout day 3 | `Friday` |
| Available equipment | `barbell, dumbbells` |
| Limitations or injuries | Enter |
| Preferred exercise | `Push Ups` |
| Custom exercise name | Enter |

The schedule assigns Push to Monday, Pull to Wednesday, and Legs to Friday, with the remaining days marked as rest days. Target muscle selection is no longer required.

- Enter `view` to see your current schedule and recorded limitations or injuries again.
- To try editing, enter `edit` → `Monday` → `remove` → `Push Ups`. The program displays `Exercise removed.` on success and shows the updated schedule automatically.
- After viewing or editing, the menu stays open. Press Enter to finish, or press `Ctrl+C` to exit at any time.
- Use full English day names. Built-in muscle groups are `Shoulders`, `Back`, `Chest`, `Legs`, and `Arms`. Singular inputs `shoulder`, `leg`, and `arm` also work, regardless of capitalization.
- Your input is not saved between runs.

## Automatic weekly splits

Inspired by [Gymshark’s workout split guide](https://www.gymshark.com/blog/article/the-best-workout-splits-for-every-goal), the app uses these defaults in calendar order:

| Days selected | Split |
| --- | --- |
| 1 | Full Body |
| 2 | Upper / Lower |
| 3 | Push / Pull / Legs |
| 4 | Upper / Lower / Upper / Lower |
| 5 | Chest / Back / Legs / Shoulders / Arms |
| 6 | Push / Pull / Legs, repeated |
| 7 | Six-day split plus a recovery day |

Push combines chest, shoulders, and triceps; Pull combines back and biceps. Lower is displayed as Legs. The one-day option and exact day-count mapping are app defaults, not a prescription from the article. Selected dates are preserved; recovery spacing is not optimized. Equipment and injury filters can leave a session shorter or empty.

## Goal-based routines

The built-in catalog contains **52 exercises**: 12 Chest, 10 Legs, 12 Back, 8 Shoulders, and 10 Arms. Options include wall and knee push-ups, floor presses, bodyweight squats, lunges, glute bridges, calf raises, prone raises, dumbbell rows, shoulder presses, lateral raises, biceps curls, triceps extensions, and band exercises. Arms includes both biceps and triceps. Shoulder and arm exercises also carry `elbow` and/or `wrist` limitation tags where applicable.

Equipment keywords are `barbell`, `dumbbells`, `bench`, `cable machine`, `resistance band`, and `pull up bar`; enter `none` for bodyweight-only routines. Pull Ups require `pull up bar`. Incline dumbbell exercises and chest-supported rows require both `dumbbells` and `bench`; pulldowns and pushdowns require `cable machine`. Exercises can have multiple limitation tags, and any matching tag excludes the exercise. These tags are demo filters, not a complete injury assessment. An empty routine is still possible when the time budget or filters exclude all candidates.

Choose one of these goals (case-insensitive):

| Goal | Sets × reps | Rest between sets | Default ordering preference |
| --- | --- | --- | --- |
| Strength | 3 × 6 | 180 seconds | Barbell exercises |
| Muscle Gain | 3 × 10 | 120 seconds | Dumbbell exercises |
| Endurance | 2 × 16 | 60 seconds | Bodyweight exercises |
| Weight Loss | 3 × 12 | 90 seconds | Bodyweight exercises |

These are demo presets, not individualized training prescriptions. Goal-specific training principles are described in the [ACSM resistance training overview](https://acsm.org/resistance-training-guidelines-update-2026/) and [ACSM progression guidance](https://pubmed.ncbi.nlm.nih.gov/19204579/); the exact presets and equipment ordering above are app design choices.

Your preferred exercise takes priority over the default ordering. Equipment and recorded limitations still filter every recommendation. Custom exercises and manual edits keep the sets and reps you enter. Weight Loss provides a resistance-training component, not a calorie-burn or weight-loss prediction.

Sessions are generated from the available time: five minutes for warm-up, 3.5 minutes per working set including rest, and one minute between exercises. There is no fixed total-set guideline. Exercise availability and injury filters may leave unused time. Entering `1` workout day immediately displays “Only one day? Is that all you've got?”; entering `7` displays “You need some rest. I'm going to add a recovery day.” The seventh day is Recovery.

## Run the tests

With Maven installed, run the JUnit tests using the following command. Maven downloads the test dependencies on the first run.

```bash
mvn test
```

## Project structure

- `Main.java`: models, workout generation logic, and interactive console demo
- `WorkoutPlannerTest.java`: JUnit tests
- `pom.xml`: Java 17 build and test configuration

## 90-minute Muscle Gain sessions

Select `Muscle Gain` and 90 minutes or more for an intermediate-style template. No RIR is displayed. Upper, Push, and Pull sessions prioritize main lifts, assistance exercises, then isolation work. The main template prioritizes eight exercises, using 3 sets for the first four and 2 sets for later exercises. Additional eligible exercises can be included if time remains; sets are reduced if needed to fit. Main compounds use 6–10 reps, other compounds 8–12, and isolation exercises 12–20. Rest is 150 seconds for compounds and 75 seconds for isolation work.

For an Upper session, enter two or four weekly workout days and equipment `barbell, dumbbells, bench, cable machine`. The template includes bench press, lat pulldown (or pull-ups if a bar is available), incline dumbbell press, chest-supported row, lateral raise, rear delt raise, triceps pushdown, and incline curl. Templates fall back to eligible catalog exercises when equipment is missing. Injury restrictions may reduce the exercise/set total; the program does not add incompatible exercises to reach a quota. Custom sets remain unchanged. This is a planning template, not a measured 90-minute session.

## Target muscle percentages and edit limits

Each exercise displays a target-muscle mix totaling 100%, for example `Chest 60%, Triceps 25%, Shoulders 15%` for bench press. These are illustrative app classification weights, not measured muscle activation. Built-in exercises use named muscle groups; unknown custom exercises default to 100% of their declared group. Percentages are shown again after viewing or editing a routine.

Automatic generation and exercise replacement enforce the estimated time budget. In the edit menu, adding a custom exercise is allowed to exceed that budget: the exercise stays in the routine and a warning shows the estimated duration. Equipment, injuries, valid inputs, duplicate names, and recovery-day restrictions still apply. Displayed rest recommendations are included in the 3.5-minute estimate, not added again. Manual/custom sets are not silently changed. Rejected replacements leave the original intact. Ending terminal input closes the console cleanly.
