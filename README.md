# Student Management System (Multi-Language)

A comprehensive Student Management System implemented in both **Python** and **Java**. This repository demonstrates the evolution of a GUI application from basic CRUD to advanced analytics in two different programming ecosystems.

## Repository Structure

### [Python Version](./Python-Version/)

- **Student-GUI-v1**: Core CRUD operations using standard Tkinter.
- **Student-GUI-v2**: Advanced version with Matplotlib charts, CSV export, and enhanced layout.
- **Tech Stack**: Python 3.x, Tkinter, MySQL, Matplotlib.

### [Java Version](./Java-Version/)

- **Student-GUI-v2**: A professional Java Swing application with a modern dark theme (FlatLaf).
- **Features**: Data visualization via JFreeChart, robust input validation, and standalone executable support.
- **Tech Stack**: Java JDK 24, Maven, Swing, FlatLaf, MySQL.

---

## Key Features (Both Versions)

- **Tkinter / Swing GUI**: Intuitive desktop interfaces for student record management.
- **MySQL Integration**: Persistent storage for student data and marks.
- **Database Analytics**: Visualize performance across subjects via bar charts.
- **Data Export**: Export records to CSV for reporting.
- **Search & Filter**: Quickly find student records by Roll Number or Name.

## Prerequisites

- **Python Edition**: Python 3.8+, `pip install mysql-connector-python matplotlib`
- **Java Edition**: JDK 24+, Maven 3.6+
- **Database**: MySQL Server (local or cloud-hosted)

## Setup

1. **Configure Database**: Create a database named `school_db` and ensure credentials match your `.env` configuration.
2. **Environment Variables**: Copy `.env.example` to `.env` in the respective project folders and update your database details.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
