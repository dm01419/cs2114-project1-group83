# cs2114-project1-group83 — NPNG Workout Planner

A console workout planner. The user enters a fitness goal, workout days,
duration, equipment, injuries, and exercise preferences; the planner builds a
weekly schedule of routines that fit their equipment, avoid their injuries,
and match their available time.

## Requirements

- Java 11 or newer
- JUnit 5 for the tests (Maven downloads it; Eclipse has it built in)

## Run

In Eclipse: right-click `Main.java` → Run As → Java Application.

From a terminal in this folder:

```bash
javac Main.java
java Main
```

## Tests

In Eclipse: right-click the project → Run As → JUnit Test.

With Maven: `mvn test`

## Classes

| Class | Owner | Job |
|---|---|---|
| `Profile` | Pratyay | Stores the user's goal, duration, limitations, equipment, days, preferences |
| `ConsoleUI` | Pratyay | Reads and validates input, shows routines and the schedule |
| `Exercise` | Ishaan | One exercise: muscles, equipment, sets/reps, injuries it conflicts with |
| `WorkoutRoutine` | Randy | The ordered exercises for one session |
| `WorkoutSchedule` | Randy | Which days are workout days, their muscle group and routine |
| `WorkoutPlanner` | David | Generates routines using the rules; adds/removes/changes exercises |
| `Main` | David | Starts the program |

Each class has a matching `...Test.java` with a normal and a bad-input case
for every public method.
