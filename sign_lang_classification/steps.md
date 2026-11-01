Side notes:
###### **Python Virtual Environment Creation steps (for reference):**


**1. Check Python Version ofc** 

&nbsp;	python --version



**2. Navigate to folder**

&nbsp;	cd C:\Users\YourName\Projects\MyApp 



**3. Create virtual environment (pip3)**

&nbsp;	python -m virtualenv venv  / python -m venv venv


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


