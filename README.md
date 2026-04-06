# EquationFlow
EquationFlow is a Remastered version of Fitting Pipes(A Balancing addition and subtraction expressions game made for Grade School Kids). EquationFlow features an overhaul of design to make it much more minimalistic and simple for users. 

# How to Run the Game

1. Download this GitHub's Zip File and extract it all
   
2. Install IntelliJ (.exe Windows version), and must have Java 21

3. Launch IntelliJ IDEA, select Open, and choose the project root folder.
  - Press setup JDK if not done yet

4. Wait for the progress bar at the bottom right to finish. If a "Maven projects need to be imported" popup appears, click Load Maven Project.
  - If dependencies are not recognized, click the "Reload All Maven Projects" icon in the Maven tool window (top right).

5. Set Sources Root: If the folder src/main/java is not blue, right-click it and select Mark Directory as > Sources Root.

6. Execute Main Class: Navigate to src/main/java/com/example/demo1/BasicGameApp.java. Right-click the file and select Run 'BasicGameApp.main()'.

# Objective
The objective of this game is to solve mathematical puzzles by creating a continuous "pipe" that connects a Start Value to a Goal Value on two parallel number lines.

You are given a starting number (e.g., 5) on the top bar and a target number (e.g., 12) on the bottom bar. Your job is to select "number pipes" from your tray and place them into the three available slots (Top, Middle, and Bottom) so that the final position of the pipe chain matches the target number.

The UI provides clear START and GOAL indicators on the number bars to help the user visualize the required equation path.

# Mechanics
**Vector Movement:** Each pipe represents a numerical value. If you place a 5 pipe, it physically spans 5 units on the board.

**The Chain:** The pipes must flow from one to the next. The second pipe starts exactly where the first one ends, and the third starts where the second ends.

**Inversion (Subtraction):** If your chain goes too far to the right, you can click the 🔄 (Flip) button located on a pipe. This turns it into a negative value, making the pipe face left and "subtracting" from your total distance.

# Winning a Level
You win when the very end of the bottom-most pipe aligns perfectly with the vertical line of your Goal Value.

**The Visual Feedback:** When the math is correct, the UI displays "Target Reached! Correct!" in bright green.

**Efficiency:** While any combination that hits the target works, the challenge lies in managing your limited pipe options to find the exact sum needed.
