This is an android wear app that controls 3 timers.
I want to add a new feature.

Typical views of the app consists of 4 quadrants.
* top-left: dark mode toggle & edit mode toggle
* top-right: timer0
* bottom-left: timer1
* bottom-right: timer2

There are 2 line segments separating the 3 timers. These are not named explicitly in the code, but I am going to give them names here for clartiy.
* segment_0_1 separates timer0 from timer1, it is drawn from the center of the watch face to the right edge.
* segment_1_2 separates timer1 from timer2, it is drawn from the center of the watch face to the bottom edge.

I want to add the ability to execute timers in sequence, pomodoro-style.

When a user clicks the edit mode toggle (indicated by ⚙️) the view changes slightly to indicate they're in that mode. In that mode, I want to add 2 new button-type artifacts.

These new buttons should be circular. I do not yet know what their radius should be.

Let's also give these buttons names.
* transition_button_0_1 should be placed such that its center is at the midpoint of segment_0_1
* transition_button_1_2 should be placed such that its center is at the midpoint of segment_1_2

These new buttons should have 3 states each. Each tap of a button should transition that button to the next state.
* transition_button_0_1
  * default_0_1: a circle, bisected horizontally by a line that exactly matches the color & thickness of segment_0_1
  * transition_0_1 appearance: the same circle described by `default appearance` but instead of being bisected horizontally, it is bisected vertically and, most importantly, that bisecting line has an arrow head pointing DOWNWARDS. This indicates that when timer0 finishes, timer1 should begin executing.
  * transition_1_0 appearance: the same circle described by `default appearance` but instead of being bisected horizontally, it is bisected vertically and, most importantly, that bisecting line has an arrow head pointing UPWARDS. This indicates that when timer1 finishes, timer0 should begin executing.
  * transition_0_1_repeat appearance: the same circle described by `default appearance` but instead of being bisected horizontally, it is bisected vertically and, most importantly, that bisecting line has one arrow head pointing UPWARDS and one arrow head pointing DOWNWARDS. This indicates that
    * when timer1 finishes, timer0 should begin executing
    * when timer0 finishes, timer1 should begin executing
* transition_button_1_2
  * default_1_2: a circle, bisected vertically by a line that exactly matches the color & thickness of segment_1_2
  * transition_1_2 appearance: the same circle described by `default appearance` but instead of being bisected vertically, it is bisected horizontally and, most importantly, that bisecting line has an arrow head pointing LEFT. This indicates that when timer1 finishes, timer2 should begin executing.
  * transition_2_1 appearance: the same circle described by `default appearance` but instead of being bisected vertically, it is bisected horizontally and, most importantly, that bisecting line has an arrow head pointing RIGHT. This indicates that when timer2 finishes, timer1 should begin executing.
  * transition_1_2_repeat appearance: the same circle described by `default appearance` but instead of being bisected vertically, it is bisected horizontally and, most importantly, that bisecting line has one arrow head pointing LEFT and one arrow head pointing RIGHT. This indicates that
    * when timer1 finishes, timer2 should begin executing
    * when timer2 finishes, timer1 should begin executing

Coordinating the logic for these transitions may be tricky and should be done very carefully. Here are the dangers I can think of right away, but there will be more lurking.
  * both transition buttons cannot be in the `_repeat` state as timer2 would not know which timer to transition to next. If both transition buttons end up in the `_repeat` state, the more recently tapped button should take precedence and the other transition button should revert to the `default_` state.
  * individual timers have their own "repeat" functionality. When in edit mode for an individual timer, that repeat functionality is represented by a button with a `↻` symbol. I believe this configuration value should not be changed by the transition button logic, but it should perhaps be ignored if the transition button points _away_ from a given timer with individual repeat enabled.
