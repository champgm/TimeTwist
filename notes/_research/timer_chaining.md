# Timer Chaining

## Persisted State
- `transition_0_2` stores one of `DEFAULT`, `ZERO_TO_TWO`, `TWO_TO_ZERO`, or `ZERO_TWO_REPEAT`.
- `transition_1_2` stores one of `DEFAULT`, `ONE_TO_TWO`, `TWO_TO_ONE`, or `ONE_TWO_REPEAT`.
- Invalid or missing persisted values decode to `DEFAULT`.

## Runtime Rules
- Only one transition button may remain in a `_REPEAT` state after any tap sequence.
- If a tap would place both buttons in `_REPEAT`, the newly tapped button wins and the other resets to `DEFAULT`.
- Timer chaining is resolved in `TimerViewModel` when a running timer completes.
- If a completed timer has an outgoing transition, that transition takes precedence over the timer's saved self-repeat setting.
- Saved per-timer `↻` settings are not rewritten by chaining logic.

## Routing Semantics
- `ZERO_TO_TWO` routes `timer0 -> timer2`.
- `TWO_TO_ZERO` routes `timer2 -> timer0`.
- `ZERO_TWO_REPEAT` alternates `timer0 <-> timer2`.
- `ONE_TO_TWO` routes `timer1 -> timer2`.
- `TWO_TO_ONE` routes `timer2 -> timer1`.
- `ONE_TWO_REPEAT` alternates `timer1 <-> timer2`.

## Alert Policy
- Manual timer starts keep the existing startup small alert.
- Transition-triggered starts are immediate and suppress the next timer's startup small alert.
- The finishing timer still emits its completion big alert.
