from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pymysql
import uuid

app = FastAPI()

# --- SERVER KEEPS THE SECRETS ---
DB_CONFIG = {
    'host': 'oemr.in',
    'port': 9522,
    'user': 'school-admin',
    'password': 'School@2025',
    'database': 'school_db',
    'cursorclass': pymysql.cursors.DictCursor
}

# --- DATA MODELS (Validation) ---
class Student(BaseModel):
    name: str
    roll_no: int

class MarksEntry(BaseModel):
    roll_no: int
    subject_id: int
    marks: int

# --- DATABASE HELPER ---
def get_db_connection():
    return pymysql.connect(**DB_CONFIG)

@app.get("/")
def home():
    return {"message": "School API is Running!"}

@app.post("/add_student")
def add_student(student: Student):
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            # Check if student exists
            cursor.execute("SELECT * FROM STUDENTS WHERE ROLL_NO=%s", (student.roll_no,))
            if cursor.fetchone():
                return {"message": "Student already exists (Skipped)"}
            
            cursor.execute("INSERT INTO STUDENTS (ROLL_NO, NAME) VALUES (%s, %s)", 
                           (student.roll_no, student.name))
            conn.commit()
            return {"message": f"Student {student.name} added"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

@app.post("/add_marks")
def add_marks(entry: MarksEntry):
    conn = get_db_connection()
    try:
        with conn.cursor() as cursor:
            unique_id = str(uuid.uuid4())
            cursor.execute("""
                INSERT INTO MARKS (ID, ROLL_NO, SUBJ_ID, MARKS) 
                VALUES (%s, %s, %s, %s)
            """, (unique_id, entry.roll_no, entry.subject_id, entry.marks))
            conn.commit()
            return {"message": "Marks saved"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()