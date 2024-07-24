# Conway's Game Of Life (v1.0-beta, not finished yet)
Conway's game of life implementation in Java with the JavaFX framework

# Rules
A cell can be alive (white) or dead (black). On each generation the following rules are applied:
1. Any alive cell with less than 2 neighbors is dead
2. Any alive cell with more than 3 neighbors is dead
3. Any alive cell with 2 or 3 neighbors keeps alive
4. Any dead cell with exactly 3 neighbors is alive

# Keys
* `SPACE` to pause/resume
* `R` to reset
* `G` to show/hide grid
* `C` to create backup
* `V` to load last backup
* `L` to load a .cells plaintext file
* `S` to save to a .cells plaintext file
* `Q` to generate random alive cells

# Screenshot
![Screenshot from 2024-07-24 14-42-23](https://github.com/user-attachments/assets/385d9cc5-8c06-4a3e-999b-7de0d1669b75)
![GIF_20240724_145657_245](https://github.com/user-attachments/assets/2538f265-696f-4c65-94d8-abab43950d72)
