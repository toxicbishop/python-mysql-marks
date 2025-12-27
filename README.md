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

* **Remote Database Connection:** Capable of connecting to remote MySQL servers (e.g., `oemr.in`) on custom ports (e.g., `9522`).
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

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/yourusername/student-marks-system.git](https://github.com/yourusername/student-marks-system.git)
    cd student-marks-system
    ```

2.  **Install dependencies:**
    This project requires the `pymysql` driver.
    ```bash
    pip install pymysql
    ```

3.  **Database Setup:**
    Ensure your MySQL server has the required tables (`STUDENTS`, `SUBJECTS`, `MARKS`) created.

## 📝 Configuration

Open the `main.py` file and update the `DB_CONFIG` dictionary with your database credentials. 

```python
DB_CONFIG = {
    'host': 'oemr.in',             # Remote Server Host
    'port': 9522,                  # Custom Port
    'user': 'school-admin',        # Username
    'password': 'YourPassword',    # Password
    'database': 'school_db',       # Database Name
    'connect_timeout': 10,
    'cursorclass': pymysql.cursors.DictCursor
}
