import tkinter as tk
from tkinter import messagebox, ttk
import pymysql
import uuid

# --- CONFIGURATION ---
DB_CONFIG = {
    'host': localhost,
    'port': 3306,
    'user': root,
    'password': root,
    'database': 'school_db',
    'cursorclass': pymysql.cursors.DictCursor
}
class StudentApp:
    def __init__(self, root):
        self.root = root
        self.root.title("🎓 Student Management System")
        self.root.geometry("600x500")

        # Title Label
        title_label = tk.Label(root, text="Student Marks Entry", font=("Arial", 18, "bold"))
        title_label.pack(pady=10)

        # --- FORM FRAME ---
        form_frame = tk.Frame(root)
        form_frame.pack(pady=10)

        # Name
        tk.Label(form_frame, text="Student Name:").grid(row=0, column=0, padx=5, pady=5)
        self.name_entry = tk.Entry(form_frame)
        self.name_entry.grid(row=0, column=1, padx=5, pady=5)

        # Roll No
        tk.Label(form_frame, text="Roll Number:").grid(row=1, column=0, padx=5, pady=5)
        self.roll_entry = tk.Entry(form_frame)
        self.roll_entry.grid(row=1, column=1, padx=5, pady=5)

        # --- MARKS FRAME ---
        marks_frame = tk.LabelFrame(root, text="Subject Marks")
        marks_frame.pack(fill="both", expand=True, padx=20, pady=10)

        self.subjects = ["Science", "Social", "Maths", "English", "Hindi", "Kannada"]
        self.entries = {}

        # Create input boxes for all subjects dynamically
        for i, sub in enumerate(self.subjects):
            tk.Label(marks_frame, text=sub).grid(row=i, column=0, padx=10, pady=5)
            entry = tk.Entry(marks_frame)
            entry.grid(row=i, column=1, padx=10, pady=5)
            self.entries[sub] = entry

        # Submit Button
        submit_btn = tk.Button(root, text="💾 Save to Database", command=self.save_data, 
                               bg="green", fg="white", font=("Arial", 12))
        submit_btn.pack(pady=20)

        # Status Bar
        self.status_var = tk.StringVar()
        self.status_var.set("Ready to connect...")
        tk.Label(root, textvariable=self.status_var, bd=1, relief=tk.SUNKEN, anchor=tk.W).pack(side=tk.BOTTOM, fill=tk.X)

    def save_data(self):
        name = self.name_entry.get()
        roll_txt = self.roll_entry.get()

        if not name or not roll_txt:
            messagebox.showerror("Error", "Please fill Name and Roll Number")
            return

        try:
            roll_no = int(roll_txt)
        except ValueError:
            messagebox.showerror("Error", "Roll Number must be an integer")
            return

        # Connect to Database
        conn = None
        try:
            self.status_var.set("Connecting to server...")
            self.root.update_idletasks() # Force UI update
            
            conn = pymysql.connect(**DB_CONFIG)
            cursor = conn.cursor()

            # 1. Insert Student
            try:
                cursor.execute("INSERT INTO STUDENTS (ROLL_NO, NAME) VALUES (%s, %s)", (roll_no, name))
            except pymysql.err.IntegrityError as e:
                # If error is 1062 (Duplicate), we just ignore it and move to marks
                if e.args[0] != 1062:
                    raise e

            # 2. Insert Marks
            # Map names to IDs
            sub_ids = {"Science": 101, "Social": 102, "Maths": 103, "English": 104, "Hindi": 105, "Kannada": 106}

            for sub_name in self.subjects:
                val_txt = self.entries[sub_name].get()
                if not val_txt: continue # Skip empty boxes
                
                marks = int(val_txt)
                sub_id = sub_ids[sub_name]
                unique_id = str(uuid.uuid4())

                cursor.execute("""
                    INSERT INTO MARKS (ID, ROLL_NO, SUBJ_ID, MARKS) 
                    VALUES (%s, %s, %s, %s)
                """, (unique_id, roll_no, sub_id, marks))

            conn.commit()
            messagebox.showinfo("Success", f"Data saved for {name}!")
            self.status_var.set("Data Saved Successfully.")
            
            # Clear form
            self.name_entry.delete(0, tk.END)
            self.roll_entry.delete(0, tk.END)
            for e in self.entries.values():
                e.delete(0, tk.END)

        except Exception as e:
            messagebox.showerror("Database Error", str(e))
            self.status_var.set("Error occurred.")
        finally:
            if conn: conn.close()

if __name__ == "__main__":
    root = tk.Tk()
    app = StudentApp(root)

    root.mainloop()
