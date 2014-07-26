Gameplay
--------
* give rotation to the ball, like Manigolf

Level design
------------
* add water gaps, with many little blue balls to simulate the water
    * when the ball falls in, it reaches the ground (put a sensor) because it's heavier
    * the player must restart from the beginning
* use the jBox2d world creator

Graphics
--------
* the angle indicator is crap, rewrite it
    * if the ball is near the edge of screen, the indicator is drawn outside window, so we should automatically draw it on the other side of the ball
* display a big message for a few seconds when it's another player's turn