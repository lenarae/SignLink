Documentation of the process ( Action detection LSTM ):
    1. Import and install dependencies
        -> tensorflow, opencv, scikit-learn, matplotlib 
    2. Keypoints using MediaPipe Holistic
    3. Extract Keypoint Values
    4. Setup Folders for Collection
    5. Collect Keypoint Values for Training and Testing
    6. Preprocess Data and Create Labels and Features
    7. Build and Train LSTM Neural Network
    8. Make Predictions 
    9. Save weights
    10. Evaluation using Confusion Matrix and Accuracy
    11. Test in Real Time







Side notes:
###### **Python Virtual Environment Creation steps (for reference):**


**1. Check Python Version ofc** 

&nbsp;	python --version



**2. Navigate to folder**

&nbsp;	cd C:\Users\YourName\Projects\MyApp 



**3. Create virtual environment (pip3)**

&nbsp;	python -m virtualenv venv 

&nbsp;  Folder structure will now look like:

&nbsp;	MyApp/

&nbsp;	├── venv/

&nbsp;	├── your_project_files/



**4. Activate the virtual env**

&nbsp;	venv\Scripts\activate

&nbsp;  Prompt will now look like this:

&nbsp;	(venv) C:\Users\YourName\Projects\MyApp>



**5. Download Requirements**

&nbsp;	a) Manually: 

&nbsp;		pip3 install numpy openpyxl pandas pyarrow pyjanitor ipykernel

&nbsp;	b) Using file: 

&nbsp;		pip3 install -r requirements.txt



**6. Use the env created in vs code**
On windows press , ctrl+shift+p
Choose "python:select interpreter"
Choose the virtual env you created (here, env).


