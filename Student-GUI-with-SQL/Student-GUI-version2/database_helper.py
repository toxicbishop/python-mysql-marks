import pymysql
import os
import uuid
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST'),
    'port': int(os.getenv('DB_PORT', 11624)),
    'user': os.getenv('DB_USER'),
    'password': os.getenv('DB_PASS'),
    'database': os.getenv('DB_NAME'),
    'cursorclass': pymysql.cursors.DictCursor,
    'ssl': {}
}

SUBJ_MAP = {
    "Science": 101,
    "Social": 102,
    "Maths": 103,
    "English": 104,
    "Hindi": 105,
    "Kannada": 106
}

REVERSE_SUBJ_MAP = {v: k for k, v in SUBJ_MAP.items()}

class DatabaseHelper:
    def __init__(self):
        self.conn = None

    def connect(self):
        return pymysql.connect(**DB_CONFIG)

    def save_student_marks(self, name, roll_no, marks_dict):
        conn = self.connect()
        try:
            with conn.cursor() as cursor:
                # Insert or Update Student
                cursor.execute("INSERT INTO STUDENTS (ROLL_NO, NAME) VALUES (%s, %s) ON DUPLICATE KEY UPDATE NAME=%s", (roll_no, name, name))
                
                # Insert Marks
                for sub_name, marks in marks_dict.items():
                    if marks == "": continue
                    sub_id = SUBJ_MAP.get(sub_name)
                    if sub_id:
                        unique_id = str(uuid.uuid4())
                        # Check if marks already exist for this roll_no and subj_id, if so update, else insert
                        # Note: The original code didn't have a unique constraint on (ROLL_NO, SUBJ_ID) besides the UUID ID.
                        # I'll stick to the original logic of adding new entries or I can improve it.
                        # Improvement: Delete old marks for this student/subject before inserting new ones to avoid duplicates.
                        cursor.execute("DELETE FROM MARKS WHERE ROLL_NO=%s AND SUBJ_ID=%s", (roll_no, sub_id))
                        cursor.execute("""
                            INSERT INTO MARKS (ID, ROLL_NO, SUBJ_ID, MARKS) 
                            VALUES (%s, %s, %s, %s)
                        """, (unique_id, roll_no, sub_id, int(marks)))
            conn.commit()
            return True, "Data Saved Successfully"
        except Exception as e:
            print(f"Database Connection Error: {e}")
            return False, f"Connection Failed: {str(e)}"
        finally:
            if 'conn' in locals() and conn:
                conn.close()

    def get_all_records(self):
        conn = None
        try:
            conn = self.connect()
            with conn.cursor() as cursor:
                # Pivot marks for easier display
                query = """
                SELECT s.ROLL_NO, s.NAME,
                    MAX(CASE WHEN m.SUBJ_ID = 101 THEN m.MARKS END) AS Science,
                    MAX(CASE WHEN m.SUBJ_ID = 102 THEN m.MARKS END) AS Social,
                    MAX(CASE WHEN m.SUBJ_ID = 103 THEN m.MARKS END) AS Maths,
                    MAX(CASE WHEN m.SUBJ_ID = 104 THEN m.MARKS END) AS English,
                    MAX(CASE WHEN m.SUBJ_ID = 105 THEN m.MARKS END) AS Hindi,
                    MAX(CASE WHEN m.SUBJ_ID = 106 THEN m.MARKS END) AS Kannada
                FROM STUDENTS s
                LEFT JOIN MARKS m ON s.ROLL_NO = m.ROLL_NO
                GROUP BY s.ROLL_NO, s.NAME
                """
                cursor.execute(query)
                return cursor.fetchall()
        except Exception as e:
            print(f"Error fetching records: {e}")
            return None # Return None to indicate error
        finally:
            if conn:
                conn.close()

    def delete_student(self, roll_no):
        conn = self.connect()
        try:
            with conn.cursor() as cursor:
                cursor.execute("DELETE FROM MARKS WHERE ROLL_NO=%s", (roll_no,))
                cursor.execute("DELETE FROM STUDENTS WHERE ROLL_NO=%s", (roll_no,))
            conn.commit()
            return True, "Record deleted successfully"
        except Exception as e:
            return False, str(e)
        finally:
            conn.close()
