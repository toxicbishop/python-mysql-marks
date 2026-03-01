# Student Management System (Python Edition)

A comprehensive Student Management System built with **Python**, **Tkinter**, and **MySQL**. This repository contains two versions of the application, representing the evolution from a basic CRUD tool to a more advanced system with data visualization.

## 📁 Versions

### [Version 1](./Student-GUI-with-SQL-version-1/)

- **Focus**: Core CRUD operations.
- **UI**: Standard Tkinter interface.
- **Features**: Add, View, and Delete student records with persistent MySQL storage.

### [Version 2](./Student-GUI-with-SQL-version-2/)

- **Focus**: Advanced Features & Analytics.
- **UI**: Enhanced Tkinter interface with improved layout.
- **Features**:
  - Full CRUD support.
  - Data visualization (Bar Charts) using `matplotlib`.
  - CSV Export functionality.
  - Robust search and filtering.

## 🚀 Features

- **Tkinter GUI**: Intuitive desktop interface for easy record management.
- **MySQL Integration**: Persistent storage for all student data and marks.
- **Analytics**: Visualize student performance across different subjects (v2).
- **Export**: Save records to CSV for external reporting (v2).

## 🛠 Tech Stack

- **Language**: Python 3.x
- **GUI Framework**: Tkinter
- **Database**: MySQL
- **Data Visualization**: Matplotlib
- **Utilities**: Connector/Python, OpenCSV (equivalent logic)

## ⚙️ Prerequisites

- Python 3.8+
- MySQL Server
- Required Libraries:

    ```bash
    pip install mysql-connector-python matplotlib
    ```

## 🔧 Setup & Usage

1. **Configure Database**
    - Create a MySQL database named `school_db`.
    - Ensure your credentials are set correctly in the source files (or `.env` if configured).

2. **Run Version 1**

    ```bash
    cd Student-GUI-with-SQL-version-1
    python main.py
    ```

3. **Run Version 2**

    ```bash
    cd Student-GUI-with-SQL-version-2
    python main.py
    ```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
