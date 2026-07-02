# Running La Safor - IPC Project

![JavaFX](https://img.shields.io/badge/JavaFX-11+-orange.svg)
![IDE](https://img.shields.io/badge/IDE-NetBeans-blue.svg)
![Grade](https://img.shields.io/badge/Grade-9.9%2F10-brightgreen.svg)
![Course](https://img.shields.io/badge/Course-Human--Computer_Interaction_(IPC)-blue.svg)
![University](https://img.shields.io/badge/University-UPV-red.svg)

## Overview
**Running La Safor** is a desktop application developed in JavaFX using the NetBeans IDE. It was created as the final project for the **Human-Computer Interaction (Interacción Persona-Computador)** course in the 2nd year of the Computer Engineering degree at the Universitat Politècnica de València (UPV).

The development heavily prioritizes UI/UX design, applying the principles of Norman, Nielsen, and Wickens to deliver an intuitive and robust user experience. The conceptual and physical design phases were documented iteratively before any codebase was established.

*Note: This project received a final grade of 9.9/10*

## Features
- **User Management:** User registration, authentication, and profile editing.
- **Session History:** Import, process, and view sport activities derived from GPX files.
- **Interactive Maps:** Route visualization over georeferenced maps with optimized zooming and panning via JavaFX `ScrollPane` and `Group` structures.
- **Advanced Metrics:** Elevation profiles, speed-over-route tracking, and cumulative activity statistics.
- **Custom Annotations:** Interface for users to add graphical annotations directly onto their activity maps.

## Tech Stack
- **Language:** Java
- **UI Framework:** JavaFX (FXML, CSS)
- **IDE:** NetBeans
- **Database:** SQLite (Local storage management)
- **Design Methodology:** User-centered design, hierarchical task analysis, low-fidelity wireframing, and iterative prototyping.

## Project Structure
The repository is structured following a strict MVC pattern for the JavaFX implementation:
- **Views:** Independent FXML files for modular interface loading.
- **Controllers:** Logic separation handling user events and state.
- **Models:** Core application data entities.
