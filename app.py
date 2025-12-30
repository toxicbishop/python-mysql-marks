import streamlit as st
import pymysql
import uuid

# --- CONFIGURATION ---
DB_CONFIG = {
    'host': 'oemr.in',
    'port': 9522,
    'user': 'school-admin',
    'password': 'School@2025',
    'database': 'school_db',
    'cursorclass': pymysql.cursors.DictCursor
}

# --- DATABASE FUNCTION ---
def run_query(query, params=None, fetch=False):
    conn = None
    try:
        conn = pymysql.connect(**DB_CONFIG)
        with conn.cursor() as cursor:
            cursor.execute(query, params)
            if fetch:
                return cursor.fetchall()
            conn.commit()
    except Exception as e:
        st.error(f"Database Error: {e}")
    finally:
        if conn: conn.close()

# --- THE WEB UI ---
st.set_page_config(page_title="Student Portal", page_icon="🎓")

st.title("🎓 Student Marks Management")
st.write("Connected to: **oemr.in**")

# Tabs for different actions
tab1, tab2 = st.tabs(["➕ Add Student & Marks", "📊 View Records"])

# TAB 1: ENTRY
with tab1:
    st.header("New Entry")
    with st.form("entry_form"):
        col1, col2 = st.columns(2)
        name = col1.text_input("Student Name")
        roll_no = col2.number_input("Roll Number", min_value=1, step=1)
        
        st.subheader("Subject Marks")
        c1, c2, c3 = st.columns(3)
        sci = c1.number_input("Science", 0, 100)
        soc = c2.number_input("Social", 0, 100)
        math = c3.number_input("Maths", 0, 100)
        
        c4, c5, c6 = st.columns(3)
        eng = c4.number_input("English", 0, 100)
        hin = c5.number_input("Hindi", 0, 100)
        kan = c6.number_input("Kannada", 0, 100)
        
        submitted = st.form_submit_button("Save to Database")
        
        if submitted and name:
            # 1. Add Student
            try:
                run_query("INSERT INTO STUDENTS (ROLL_NO, NAME) VALUES (%s, %s)", (roll_no, name))
            except:
                pass # Ignore duplicate student error
            
            # 2. Add Marks
            subjects = [
                (101, sci), (102, soc), (103, math),
                (104, eng), (105, hin), (106, kan)
            ]
            
            for sub_id, score in subjects:
                uid = str(uuid.uuid4())
                run_query("""
                    INSERT INTO MARKS (ID, ROLL_NO, SUBJ_ID, MARKS) 
                    VALUES (%s, %s, %s, %s)
                """, (uid, roll_no, sub_id, score))
            
            st.success(f"✅ Records saved for {name}!")

# TAB 2: VIEW DATA
with tab2:
    st.header("Database Records")
    if st.button("Refresh Data"):
        # SQL Join to make it readable
        sql = """
        SELECT s.NAME, m.ROLL_NO, sub.SUBJ_NAME, m.MARKS 
        FROM MARKS m
        JOIN STUDENTS s ON m.ROLL_NO = s.ROLL_NO
        JOIN SUBJECTS sub ON m.SUBJ_ID = sub.SUBJ_ID
        ORDER BY m.ROLL_NO
        """
        data = run_query(sql, fetch=True)
        if data:
            st.dataframe(data)
        else:
            st.info("No data found.")