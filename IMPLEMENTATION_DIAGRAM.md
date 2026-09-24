# NPNG System Diagram

```mermaid
flowchart TD
    Main[Main] --> UI[ConsoleUI]
    UI --> Profile[Profile]
    UI --> Planner[WorkoutPlanner]
    Profile --> Goal[FitnessGoal]
    Planner -->|reads| Profile
    Planner -->|uses| Goal
    Planner -->|generates| Schedule[WorkoutSchedule]
    Schedule -->|stores by day| Routine[WorkoutRoutine]
    Routine -->|contains| Exercise[Exercise]
    Planner -->|selects| Exercise
    UI -->|displays| Schedule
```
