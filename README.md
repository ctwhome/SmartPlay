
# SmartPlay: A WearOS App for Measuring Play

Welcome to **SmartPlay**! SmartPlay is a smartwatch application for Android OS Wear designed to collect real-time data on children’s play experiences, using a data-driven interdisciplinary approach. Developed in collaboration with researchers from developmental psychology and human-media interaction, SmartPlay aims to support research in play behaviors, health, and more.

![Slide 16_9 - 6](https://github.com/user-attachments/assets/fdcf84f2-49d2-49a5-b39f-731e0face6a6)

- [Download the APK](./relesases)
- [Installation and user manual instructions](./INSTALL.md)
- [Data visualization dashboard](https://ctwhome.github.io/SmartPlay)

### Project Goal
The primary objective of SmartPlay is to **collect subjective experiences from children during playtime**, providing valuable insights into their behavior and environment. This app is geared towards helping researchers combine interdisciplinary knowledge to capture and analyze data in a user-friendly way.

### Features
#### 1. **Sensor Data Collection**
SmartPlay uses the sensors available on WearOS smartwatches, such as:
- **GPS Location** to collect location data, despite its known lag issues
- **Bluetooth receiver** for localized triangulation, which helps compensate for the limited precision of GPS sensors
- **Heart rate** to monitor physical exertion and stress
- **Gyroscope** and **Accelerometer** to monitor movement
- **Magnetometer** for orientation tracking
- **Steps counting** to track physical activity levels
- **Audio recording** to capture environmental sounds
- **dB intensity** to measure noise levels
![Slide 16_9 - 5](https://github.com/user-attachments/assets/c8b4a8ad-3f52-4f98-a966-10316fa9e3f0)
![Slide 16_9 - 7](https://github.com/user-attachments/assets/e988bcb7-1487-4923-b71a-77c12f034b81)
![Slide 16_9 - 8](https://github.com/user-attachments/assets/65aa2e74-b520-4f3d-9a22-1f27eb586e52)



These sensors provide a robust dataset to analyze play behavior, track activities, and study the interaction of children with their surroundings.

#### 2. **Customizable Alert Workflows**
The app allows researchers to create **custom workflows** in JSON format. These workflows determine when and how notifications are sent to the child’s watch, prompting them to register data about aspects like stress levels, energy, and emotions.

Key functionality includes:
- **Flexible notifications**: Researchers can customize the timing and frequency of alerts to suit specific study requirements.
- **Interactive responses**: Children can use the watch to respond to prompts, providing subjective data about their experiences.

#### 3. **Privacy and Data Safety**
Data privacy is a core concern in SmartPlay. The app ensures that the data is:
- **Stored locally** in a private directory of the smartwatch, inaccessible to third-party apps unless explicitly permitted.
- Extractable through **offline methods** only (e.g., using ADB commands), maintaining the privacy of children’s data throughout the research.
![dashboard](https://github.com/user-attachments/assets/0d121818-197c-4a37-85b1-bd5f5b2aa919)

#### 4. **User-Centric Design**
The app is designed to be **simple, glanceable, and easy to interact with**, ensuring a seamless user experience for children. Its interface minimizes complexity to make it accessible to a younger audience.

### Sustainability and Device Support
SmartPlay is compatible with Android WearOS devices, including but not limited to:
- **Google Pixel Watch**
- **Samsung Galaxy Watch**
- **Oppo Watch**
- **Mobvoi TicWatch**

Efforts are being made to add support for more modern sensors and devices to broaden its applicability.

### Beyond the Project
SmartPlay has broader potential applications beyond developmental psychology, including:
- **Sports Science**: Measuring physical activity and player engagement.
- **General Health**: Non-medical monitoring of physical and emotional wellbeing.
- **Elderly Care**: Detecting falls or providing reminders.

### Impact
By taking a **user-friendly approach**, SmartPlay aims to enhance research quality and cost-effectiveness, with potential future applications in various disciplines beyond its initial scope. The combination of precise data collection and real-time subjective input makes it a valuable tool for researchers.

### Contributors
- **Lead Applicant**: Prof. Dr. Carolien Rieffe, University of Twente (Human-Media Interaction)
- **Lead RSE**: Jesus Garcia Gonzalez, eScience Center (Social Sciences and Humanities)
- **PhD Candidate**: Maedeh Nasri, University of Leiden (Developmental and Educational Psychology)
- **Program Manager**: Dr. Pablo Lopez-Tarifa, eScience Center

### Get Involved
For more information on how to collaborate or to see examples of workflows and data collected by SmartPlay check the [Installation and user manual instructions](./INSTALL.md). 

### Useful Commands
Here are some ADB commands you might need during setup:
- **Connect to Device**: `adb connect <ip:port>`
- **Install App**: `adb -s <device_id> install /path/to/your/smartplay.apk`
- **Push workflows.json**: `adb -s <device_id> push path/to/workflows.json /sdcard/Android/data/com.example.smartplay/files/`
- **Retrieve Data**: `adb -s <device_id> pull /sdcard/Android/data/com.example.smartplay/files/Documents ./`

---

We hope SmartPlay makes your research more effective and insightful. If you have any questions, feel free to reach out or contribute to our GitHub!




