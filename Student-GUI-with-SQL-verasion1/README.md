# Student Management System (Java Edition)

A professional Student Management System built with **Java Swing** and **MySQL**. This application features a modern dark-themed UI (using FlatLaf), data visualization, and export capabilities.

![Pro Student Management System](https://raw.githubusercontent.com/toxicbishop/Student-GUI-With-SQL/readme-assets/screenshot.png) *(Pending screenshot)*

## 🚀 Features

- **Modern UI**: Clean, dark-themed interface powered by FlatLaf.
- **CRUD Operations**: Add, View, Update (Re-import), and Delete student records.
- **Data Validation**: Robust input validation to prevent SQL injection and ensure data integrity.
- **Analytics Dashboard**: Real-time statistics and bar charts visualizing subject performance using JFreeChart.
- **Search & Filter**: Instant search by Name or Roll Number.
- **Export Data**: Export student records to CSV format.
- **Database Integration**: Secure MySQL connection using `dotenv` for configuration.

## 🛠 Tech Stack

- **Language**: Java 24 (JDK 24)
- **Build Tool**: Maven
- **GUI Framework**: Swing
- **Theme**: FlatLaf (Dark)
- **Database**: MySQL 8.0
- **Charts**: JFreeChart
- **Utilities**: OpenCSV, Dotenv

## 💿 Standalone Executable

For Windows users, a pre-built standalone executable is available!

1. Navigate to `build_output_final/StudentGUI/`.
2. Ensure your MySQL server is running and configured as per the `.env` file in that directory.
3. Run `StudentGUI.exe`.

---

## ⚙️ Prerequisites

- Java JDK 24 or higher (for development)
- Maven 3.6+
- MySQL Server

## 🔧 Installation & Setup (Development)

1. **Clone the Repository**

    ```bash
    git clone https://github.com/toxicbishop/Student-GUI-With-SQL.git
    cd Student-GUI-With-SQL
    git checkout Java
    ```

2. **Configure Database**
    - Create a MySQL database named `school_db`.
    - Ensure tables `STUDENTS` and `MARKS` exist (matching the schema).
    - Create a `.env` file in the project root with your credentials:

        ```properties
        DB_HOST=localhost
        DB_PORT=3306
        DB_USER=root
        DB_PASS=your_password
        DB_NAME=school_db
        ```

3. **Build and Run**

    ```bash
    # Verify dependencies and compile
    tools\apache-maven-3.9.6\bin\mvn clean compile
    
    # Run the application
    tools\apache-maven-3.9.6\bin\mvn exec:java -Dexec.mainClass="com.studentgui.ui.StudentApp"
    ```

## 📂 Project Structure

```text
Student-GUI-with-SQL/
├── src/main/java/com/studentgui/
│   ├── ui/           # Swing UI Components (StudentApp.java)
│   ├── db/           # Database Helper (JDBC)
│   └── util/         # Utilities (Input Validation)
├── src/main/resources/
│   └── .env          # Environment Variables
├── pom.xml           # Maven Dependencies
└── LICENSE           # MIT License
```

## 🤝 Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
