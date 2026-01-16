# 🎓 Student Marks Management System

A robust **Console User Interface (CUI)** application built with Python and MySQL to manage student academic records. This project demonstrates database normalization, Python-SQL connectivity, and efficient data entry workflows using a remote database server.

![Python](https://img.shields.io/badge/Python-3.10%2B-blue?style=flat&logo=python)
![MySQL](https://img.shields.io/badge/Database-MySQL-orange?style=flat&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green)

## 📖 Overview

This application serves as a data entry tool for educational institutions. It allows administrators to:
1.  Register new students or identify existing ones by Roll Number.
2.  Input marks for a predefined curriculum (Science, Social, Maths, English, Hindi, Kannada).
3.  Automatically generate unique transaction IDs (UUID) for every record.
4.  Store data securely in a normalized relational database on a remote server.

## ⚙️ Features

* **Remote Database Connection:** Capable of connecting to remote MySQL servers on custom ports.
* **Data Normalization:** Uses three separate tables (`STUDENTS`, `SUBJECTS`, `MARKS`) to reduce redundancy.
* **Smart Error Handling:** * Detects if a student already exists and automatically switches to "Update" mode.
    * Validates integer inputs to prevent crashes.
    * Handles network timeouts and SSL handshakes gracefully using `pymysql`.
* **Secure Configuration:** Uses dictionary unpacking (`**kwargs`) for clean and secure database connection management.

## 🗄️ Database Schema

The project uses a Relational Database design:

| Table | Primary Key | Description |
| :--- | :--- | :--- |
| **STUDENTS** | `ROLL_NO` | Stores student Name and Roll Number. |
| **SUBJECTS** | `SUBJ_ID` | Stores Subject IDs (101-106) and Names. |
| **MARKS** | `ID` (CHAR 36) | Links Student, Subject, and Marks together using UUIDs. |

## 🛠️ Tech Stack

* **Language:** Python 3.x
* **Database:** MySQL (Remote Server)
* **Libraries:** * `pymysql` (For robust database connectivity)
    * `uuid` (For generating unique IDs)
    * `sys` (For standard input handling)

## 🚀 Setup & Installation
You do **not** need Python installed to run this application. It is a standalone Windows executable.

### Step 1: Download
1.  Go to the [**Releases Page**](https://github.com/toxicbishop/python-mysql-marks/releases) of this repository.
2.  Find the latest version (e.g., `v1.0`).
3.  Under the **"Assets"** section, click on `gui_app.exe` to download it.

### Step 2: Run the Application
1.  Locate the downloaded `gui_app.exe` file on your computer.
2.  Double-click to launch it.

### ⚠️ Note on Windows Security
Since this is a custom application and not signed by Microsoft, Windows Defender might show a warning popup saying *"Windows protected your PC"*.

To bypass this:
1.  Click **"More info"**.
2.  Click the **"Run anyway"** button.
*(This happens because the app was built by an individual developer, not a registered corporation.)*

## ⚠️ Use the .env to store the Database Credentials only
⚠️ Important: Since the app relies on a .env file for database credentials, you keep a .env file in the same directory as the .exe.
