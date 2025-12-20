---- Architecture ----

* migrate from collectAsState to collectAsStateWithLifecycle.
* Apply the ON_RESUME data refresh pattern to all screens displaying dynamic data to ensure UI is
  always up-to-date.'
* look into coroutines, scope, and snackbar


* make dark theme look better
* Have the add "REPS" in goalstage say the unit that is in the goal stage instead of reps
  ---- UI ----
  Implement loading bar for all pages
  Restrusture to have a repository layer

* When switching tabs from bottom navbar, the TopNavBar and BottomNavBar show way earlier than the
  actual page, it's very clunky.

* -- Bottom Nav Bar --
* Make navigation appear to slide left and right instead of fade in and out.

* -- Side Navigation Bar --

* -- MovesList --
* refactor a moveItem to a component in it's own file

  -- EditMove --

-- Moves by Tag

* Ui looks greyed out, fix it
  -- Combo Generator --

-- Saved Combos --

* should be renamed everywhere to Practice Combos

-- Edit Saved Combo --

-- Goals Screen --

* Should have a title "Goals"

  -- Edit Goal --
* Change what the goal stage items look like
* they should have
  -- Add/Edit Goal Stage --
* typing numbers in target count focuses keyboard on StageName each key press.
* When saving a goal stage in here, navigating back to the edit Goal screen, the stages list should
  be updated but it's not
* I think it's fine that it saves the edits to Goal Title and description, I think it definitly
  should have live data for the stages tho.
* So when I add a goal stage to an unsaved goal, it saves the goal. then when I save a goal stage
  and it brings me back to goal page, I can save again and it will create a NEW goal. It should ask
  you to save changes before hitting add goal stage. and hitting add goal stage should not save the
  goal, if the goal doesn't exist then do nothing but show a snackbar message to save the goal.
* Change Goal Title to get rid of the asterisk, but it should show a red alert around the box as a
  required field before saving.


* When adding a goal
    * title should be Add Goal instead of Edit Goal
    * Should not see archive or delete button since goal was not created yet.
* Progress bar should not appear when there are no goal stages
* A progress bar at zero has this weird dot on the right end for some reason
* When a goal stage is added, the goal itself should be saved. Otherwise a user can exit early and
  there will be a goalstage pointing to a goal that was never saved.
* So Apparently a goal with the same goal tag DOES exist, but unnamed. I think that before adding a
  goal stage, Goal Name should be required. How should we fix this?
* Have three dot menu or edit button on the goals in the list.

* -- Timer Screen --
* Have it display milliseconds too
* I like the idea of scrolling through numbers similar to android clock stopwatch. This is better UX
  than having to type in a number.
* I also want a circle around the time that will fill up to indicate the time left.

* -- Battle Combo Page --

* -- Edit Battle Combo --

* -- Archived Goals --

Ask AI Agents to "search for edge cases that could break my code or won't work as intended and
report to me"