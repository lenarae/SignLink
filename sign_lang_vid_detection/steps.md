```
## NOTE: 
Python version used is 3.11.9.
```


# Documentation of the Process (Action Detection LSTM)

### 1. Import and Install Dependencies
* **Libraries:** `tensorflow`, `opencv-python`, `scikit-learn`, `matplotlib`, `mediapipe`

---

### 2. Keypoints Using MediaPipe Holistic
* **What are Keypoints and Landmarks?**
    * In computer vision, **keypoints** (or **landmarks**) are specific spatial coordinates $(X, Y, Z)$ that map to crucial anatomical points on the human body. 
    * **X and Y:** Represent the horizontal and vertical positions of the joint relative to the frame.
    * **Z:** Represents depth, indicating how close or far the landmark is from the camera.
    * **Visibility:** An extra score representing the probability that the coordinate is not obscured or out of frame.

* **What is MediaPipe Holistic?**
    * Instead of using separate, resource-heavy models to track hands, faces, and body positions individually, **MediaPipe Holistic** integrates these components into a single, optimized pipeline. It captures the entire human topology simultaneously from a video frame.
    
* **Keypoint Breakdown in the Code:**
    * When a frame is processed, the model extracts a total of **1,662 structural values** across four key areas:
    * **Pose Landmarks (33 points):** Tracks major joints (shoulders, elbows, wrists, hips). Each point yields $X, Y, Z,$ and visibility ($33 \times 4 = 132$ values).
    * **Face Landmarks (468 points):** Captures precise facial contours and geometry. Each point yields $X, Y, Z$ ($468 \times 3 = 1,404$ values).
    * **Left Hand Landmarks (21 points):** Tracks individual knuckles and finger joints. Each point yields $X, Y, Z$ ($21 \times 3 = 63$ values).
    * **Right Hand Landmarks (21 points):** Tracks the opposite hand's knuckles and finger joints ($21 \times 3 = 63$ values).

* **What is Done in this Step:**
    1. The MediaPipe Holistic solution is initialized with specific detection and tracking confidence thresholds.
    2. OpenCV captures the live video feed frame-by-frame, converting the color space from BGR to RGB for MediaPipe processing.
    3. The model infers the coordinates, and drawing utilities overlay these detected joints and skeletal connections back onto the video screen in real time, validating accurate tracking.

---

### 3. Extract Keypoint Values
### 4. Setup Folders for Collection
### 5. Collect Keypoint Values for Training and Testing
### 6. Preprocess Data and Create Labels and Features
### 7. Build and Train LSTM Neural Network
### 8. Make Predictions
### 9. Save Weights
### 10. Evaluation Using Confusion Matrix and Accuracy
### 11. Test in Real Time

---