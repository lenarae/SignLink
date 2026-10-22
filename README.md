# SignLink 
SignLink is an undergraduate honors project that focuses on bridging communication gaps between sign-language users and non-signers by translating sign language gestures into real-time text and speech using wearable sensors and machine learning.

The system adopts a multimodal approach, combining **surface electromyography (sEMG)** and **inertial measurement unit (IMU)** data from a wearable forearm or wrist device processed through a mobile application that enables real-time conversion of sign gestures into readable text and audible speech, improving accessibility and inclusive communication.

---

##  System Concept
SignLink follows a hybrid sensing and AI pipeline:

1. **Wearable Data Acquisition**
   * sEMG sensors capture neuromuscular activity
   * IMU sensors track hand and wrist motion
   * Optional EEG module provides cognitive or emotional context

2. **Signal Processing & Machine Learning**
   * Filtering, windowing, normalization, and feature extraction
   * Gesture recognition using classical ML and deep learning models
   * Sequence-to-sentence translation for meaningful outputs

3. **Mobile Application**
   * Bluetooth Low Energy (BLE) connectivity
   * Live translation display
   * Text-to-speech (TTS) output
   * User calibration and accessibility-focused UI

---

## UX & Human-Centred Design

The mobile application is designed using a **user-centred design approach**, guided by:
* Nielsen’s 10 Usability Heuristics
* Human–Technology Interaction (HTI) principles
* Accessibility and inclusivity considerations

Design development progressed through:
* Paper prototypes (low fidelity)
* Digital wireframes
* Medium-fidelity UI designs (Figma)

---

## Repository Structure

```
SignLink/
├── android_app/                # Android Mobile application prototype
├── datasets/           # Public + custom EMG/IMU data
├── designs/            # UX wireframes and prototypes
├── grading_and_submissions/               # Reports, HTA, references
├── sign_lang_classification/            # ML work on the datasets
├── sign_lang_vid_detection/            # Vision use to translate ASL
└── README.md
```

---

## Academic Context

* **Module**: CMP6200 / DIG6200 – Individual Undergraduate Project
* **Degree**: B.Sc. (Honors) Computer Science
* **Student**: Sombatla Truc Deydeepya Lakshmi
* **Supervisor**: Dr. Vi Chi Thanh

This project emphasizes:
* Assistive technology
* Wearable sensing
* AI & machine learning
* Human–Computer Interaction (HCI)
* Ethical and accessible system design

---

## Acknowledgements

* **Rombolabs** for inspiration and reference designs related to wearable EMG/IMU systems and experimental sensor platforms

---

## Ethics & Use
* All physiological data collection follows informed consent and anonymization practices
* No personal or identifiable user data is stored or shared

---