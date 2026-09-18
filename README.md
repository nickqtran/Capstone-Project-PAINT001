# Painless Prep Application
Description: Painless Prep is an application being developed to help reduce the time and energy investment needed for painters during the process of measuring windows so that they can be properly covered during the painting process. 

Notable Features:<br/>
    &nbsp;&nbsp;&nbsp;- Distance measurement between Aruco markers<br/>
    &nbsp;&nbsp;&nbsp;- Camera calibration for Aruco detection<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* Calibration data is saved between application usage<br/>
    &nbsp;&nbsp;&nbsp;- Mesurement capturing, saving, and exporting to CSV files.<br/>

# Group Members
Jocelyn Horn<br/>
Josef Stanek<br/>
Kurtis Kathol<br/>

# Release notes
    - V 1.0.0-Alpha
        - Added "Generate_Aruco.py", an AruCo generation script that allows for easy creation of AruCo markers 
        - Added "MarkerDetection.py", the main script that detects, and roughly estimates the distance between AruCo markers
        - Added "calibrate_camera.py", a camera calibration script that allows for more accurate distance estimation.
        - Removed Test.py, TestVideo.py, VideoCapture.py, as those files were generic tests that provided no use to the release

    - V 1.0.4-Alpha (04.16.2026)
        - Switched main branch to master, a kotlin focused build that runs on android devices.
        - Added the ability to capture measurements via the "capture" button. 
        - Added the ability to name captured measurements, and store them in a CSV file "measurements.csv".
        - Refined measurement data to measure to a 16th of an inch.
        
        
