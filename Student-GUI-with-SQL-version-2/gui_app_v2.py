import tkinter as tk
from tkinter import ttk, messagebox
import customtkinter as ctk
from database_helper import DatabaseHelper
from input_validator import validate_student_data, validate_search_term, sanitize_string
import pandas as pd
from datetime import datetime
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

# Configure Appearance
ctk.set_appearance_mode("Dark")
ctk.set_default_color_theme("blue")

class StudentAppPro(ctk.CTk):
    def __init__(self):
        super().__init__()

        self.db = DatabaseHelper()
        self.title("🎓 Pro Student Management System")
        self.geometry("1100x750")

        # Grid configuration
        self.grid_columnconfigure(1, weight=1)
        self.grid_rowconfigure(0, weight=1)

        # --- SIDEBAR ---
        self.sidebar_frame = ctk.CTkFrame(self, width=200, corner_radius=0)
        self.sidebar_frame.grid(row=0, column=0, sticky="nsew")
        self.sidebar_frame.grid_rowconfigure(4, weight=1)

        self.logo_label = ctk.CTkLabel(self.sidebar_frame, text="SMS PRO", font=ctk.CTkFont(size=24, weight="bold"))
        self.logo_label.grid(row=0, column=0, padx=20, pady=(20, 10))

        self.add_btn = ctk.CTkButton(self.sidebar_frame, text="✚ Add Student", command=self.show_add_frame, fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"), anchor="w")
        self.add_btn.grid(row=1, column=0, padx=20, pady=10, sticky="ew")

        self.view_btn = ctk.CTkButton(self.sidebar_frame, text="📋 View Records", command=self.show_view_frame, fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"), anchor="w")
        self.view_btn.grid(row=2, column=0, padx=20, pady=10, sticky="ew")

        self.stats_btn = ctk.CTkButton(self.sidebar_frame, text="📊 Performance", command=self.show_stats_frame, fg_color="transparent", text_color=("gray10", "gray90"), hover_color=("gray70", "gray30"), anchor="w")
        self.stats_btn.grid(row=3, column=0, padx=20, pady=10, sticky="ew")

        self.appearance_mode_label = ctk.CTkLabel(self.sidebar_frame, text="Appearance Mode:", anchor="w")
        self.appearance_mode_label.grid(row=5, column=0, padx=20, pady=(10, 0))
        self.appearance_mode_optionemenu = ctk.CTkOptionMenu(self.sidebar_frame, values=["Light", "Dark", "System"], command=self.change_appearance_mode_event)
        self.appearance_mode_optionemenu.grid(row=6, column=0, padx=20, pady=(10, 20))
        self.appearance_mode_optionemenu.set("Dark")

        # --- MAIN CONTENT AREA ---
        self.main_frame = ctk.CTkFrame(self, corner_radius=0, fg_color="transparent")
        self.main_frame.grid(row=0, column=1, sticky="nsew", padx=20, pady=20)
        self.main_frame.grid_columnconfigure(0, weight=1)
        self.main_frame.grid_rowconfigure(0, weight=1)

        # Initialize Frames
        self.add_frame = self.create_add_frame()
        self.view_frame = self.create_view_frame()
        self.stats_frame = self.create_stats_frame()

        # Show initial frame
        self.show_add_frame()

    def change_appearance_mode_event(self, new_appearance_mode: str):
        ctk.set_appearance_mode(new_appearance_mode)

    def show_add_frame(self):
        self.hide_all_frames()
        self.add_frame.grid(row=0, column=0, sticky="nsew")
        self.add_btn.configure(fg_color=("gray75", "gray25"))

    def show_view_frame(self):
        self.hide_all_frames()
        self.view_frame.grid(row=0, column=0, sticky="nsew")
        self.view_btn.configure(fg_color=("gray75", "gray25"))
        self.refresh_table()

    def show_stats_frame(self):
        self.hide_all_frames()
        self.stats_frame.grid(row=0, column=0, sticky="nsew")
        self.stats_btn.configure(fg_color=("gray75", "gray25"))
        self.update_stats()

    def hide_all_frames(self):
        self.add_frame.grid_forget()
        self.view_frame.grid_forget()
        self.stats_frame.grid_forget()
        self.add_btn.configure(fg_color="transparent")
        self.view_btn.configure(fg_color="transparent")
        self.stats_btn.configure(fg_color="transparent")

    # --- ADD STUDENT FRAME ---
    def create_add_frame(self):
        frame = ctk.CTkFrame(self.main_frame, fg_color="transparent")
        
        title = ctk.CTkLabel(frame, text="Add Student Marks", font=ctk.CTkFont(size=24, weight="bold"))
        title.pack(pady=(0, 20), anchor="w")

        form = ctk.CTkFrame(frame)
        form.pack(fill="both", expand=True, padx=10, pady=10)

        # Grid for form
        inner_form = ctk.CTkFrame(form, fg_color="transparent")
        inner_form.pack(padx=40, pady=40)

        ctk.CTkLabel(inner_form, text="Student Name:", font=ctk.CTkFont(size=14)).grid(row=0, column=0, padx=10, pady=10, sticky="e")
        self.name_entry = ctk.CTkEntry(inner_form, width=250, placeholder_text="Enter Full Name")
        self.name_entry.grid(row=0, column=1, padx=10, pady=10)

        ctk.CTkLabel(inner_form, text="Roll Number:", font=ctk.CTkFont(size=14)).grid(row=1, column=0, padx=10, pady=10, sticky="e")
        self.roll_entry = ctk.CTkEntry(inner_form, width=250, placeholder_text="Enter Roll No.")
        self.roll_entry.grid(row=1, column=1, padx=10, pady=10)

        # Marks Grid
        marks_section = ctk.CTkLabel(inner_form, text="Subject Marks", font=ctk.CTkFont(size=16, weight="bold"))
        marks_section.grid(row=2, column=0, columnspan=2, pady=(20, 10))

        self.subjects = ["Science", "Social", "Maths", "English", "Hindi", "Kannada"]
        self.entries = {}

        for i, sub in enumerate(self.subjects):
            row = 3 + (i // 2)
            col = (i % 2) * 2
            ctk.CTkLabel(inner_form, text=f"{sub}:").grid(row=row, column=col, padx=10, pady=5, sticky="e")
            entry = ctk.CTkEntry(inner_form, width=100, placeholder_text="0-100")
            entry.grid(row=row, column=col+1, padx=10, pady=5, sticky="w")
            self.entries[sub] = entry

        save_btn = ctk.CTkButton(inner_form, text="💾 Save Record", command=self.save_data, height=40, font=ctk.CTkFont(size=14, weight="bold"))
        save_btn.grid(row=7, column=0, columnspan=2, pady=30, sticky="ew")

        return frame

    def save_data(self):
        name = self.name_entry.get()
        roll = self.roll_entry.get()

        # Collect marks as strings for validation
        marks_input = {}
        for sub, entry in self.entries.items():
            marks_input[sub] = entry.get()

        # Validate all input data using the input validator
        is_valid, error_msg, validated_data = validate_student_data(name, roll, marks_input)
        
        if not is_valid:
            messagebox.showerror("Validation Error", error_msg)
            return
        
        # Use validated and sanitized data
        success, msg = self.db.save_student_marks(
            validated_data['name'], 
            validated_data['roll_no'], 
            validated_data['marks']
        )
        if success:
            messagebox.showinfo("Success", "Student record saved successfully!")
            self.clear_form()
        else:
            messagebox.showerror("Database Error", msg)

    def clear_form(self):
        self.name_entry.delete(0, tk.END)
        self.roll_entry.delete(0, tk.END)
        for e in self.entries.values():
            e.delete(0, tk.END)

    # --- VIEW RECORDS FRAME ---
    def create_view_frame(self):
        frame = ctk.CTkFrame(self.main_frame, fg_color="transparent")
        
        title_row = ctk.CTkFrame(frame, fg_color="transparent")
        title_row.pack(fill="x", pady=(0, 20))

        title = ctk.CTkLabel(title_row, text="Student Records", font=ctk.CTkFont(size=24, weight="bold"))
        title.pack(side="left")

        self.search_entry = ctk.CTkEntry(title_row, placeholder_text="Search by Name/Roll...", width=250)
        self.search_entry.pack(side="right", padx=10)
        self.search_entry.bind("<KeyRelease>", lambda e: self.filter_table())

        # Styling for Treeview
        style = ttk.Style()
        style.theme_use("default")
        style.configure("Treeview", background="#2b2b2b", foreground="white", fieldbackground="#2b2b2b", borderwidth=0, rowheight=30)
        style.map("Treeview", background=[('selected', '#1f538d')])
        style.configure("Treeview.Heading", background="#333333", foreground="white", relief="flat", font=('Arial', 10, 'bold'))

        table_container = ctk.CTkFrame(frame)
        table_container.pack(fill="both", expand=True)

        cols = ("Roll No", "Name", "Science", "Social", "Maths", "English", "Hindi", "Kannada", "Total", "Average")
        self.tree = ttk.Treeview(table_container, columns=cols, show="headings")

        for col in cols:
            self.tree.heading(col, text=col)
            self.tree.column(col, width=80, anchor="center")
        self.tree.column("Name", width=150, anchor="w")

        # Scrollbar
        scrollbar = ttk.Scrollbar(table_container, orient="vertical", command=self.tree.yview)
        self.tree.configure(yscrollcommand=scrollbar.set)
        
        self.tree.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

        # Action Buttons
        btn_row = ctk.CTkFrame(frame, fg_color="transparent")
        btn_row.pack(fill="x", pady=10)

        ctk.CTkButton(btn_row, text="🗑 Delete Selected", fg_color="#d32f2f", hover_color="#b71c1c", command=self.delete_record).pack(side="right", padx=10)
        ctk.CTkButton(btn_row, text="📥 Export to Excel", command=self.export_excel).pack(side="right", padx=10)
        ctk.CTkButton(btn_row, text="🔄 Refresh", command=self.refresh_table).pack(side="right", padx=10)

        return frame

    def refresh_table(self):
        records = self.db.get_all_records()
        if records is None:
            messagebox.showerror("Error", "Could not connect to database to fetch records.")
            return
        self.update_table_data(records)

    def update_table_data(self, records):
        self.tree.delete(*self.tree.get_children())
        for row in records:
            # Calculate total and avg
            marks = [row[s] for s in self.subjects if row[s] is not None]
            total = sum(marks) if marks else 0
            avg = round(total / len(marks), 2) if marks else 0
            
            val_list = [row['ROLL_NO'], row['NAME']] + [row[s] if row[s] is not None else "-" for s in self.subjects] + [total, avg]
            self.tree.insert("", "end", values=val_list)

    def filter_table(self):
        search_term = self.search_entry.get()
        
        # Validate search term to prevent injection
        is_valid, error_msg = validate_search_term(search_term)
        if not is_valid:
            messagebox.showwarning("Warning", error_msg)
            return
        
        search_term = sanitize_string(search_term).lower()
        records = self.db.get_all_records()
        if records is None:
            return
        filtered = [r for r in records if search_term in str(r['ROLL_NO']).lower() or search_term in r['NAME'].lower()]
        self.update_table_data(filtered)

    def delete_record(self):
        selected = self.tree.selection()
        if not selected:
            messagebox.showwarning("Warning", "Please select a record to delete")
            return
        
        if messagebox.askyesno("Confirm", "Are you sure you want to delete this record?"):
            for item in selected:
                roll_no = self.tree.item(item)['values'][0]
                self.db.delete_student(roll_no)
            self.refresh_table()

    def export_excel(self):
        records = self.db.get_all_records()
        if not records:
            messagebox.showwarning("Warning", "No data to export")
            return
        
        df = pd.DataFrame(records)
        filename = f"student_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"
        df.to_excel(filename, index=False)
        messagebox.showinfo("Success", f"Data exported to {filename}")

    # --- STATISTICS FRAME ---
    def create_stats_frame(self):
        frame = ctk.CTkFrame(self.main_frame, fg_color="transparent")
        
        ctk.CTkLabel(frame, text="Performance Analytics", font=ctk.CTkFont(size=24, weight="bold")).pack(pady=(0, 20), anchor="w")

        self.stats_container = ctk.CTkFrame(frame, fg_color="transparent")
        self.stats_container.pack(fill="x", pady=10)

        # We'll create 3 cards for stats
        self.card_total = self.create_stat_card(self.stats_container, "Total Students", "0", 0)
        self.card_avg = self.create_stat_card(self.stats_container, "Class Average", "0.0", 1)
        self.card_topper = self.create_stat_card(self.stats_container, "Top Performer", "-", 2)

        # Chart Area
        self.chart_frame = ctk.CTkFrame(frame)
        self.chart_frame.pack(fill="both", expand=True, pady=10)
        
        return frame

    def create_stat_card(self, parent, title, value, col):
        card = ctk.CTkFrame(parent, width=250, height=120)
        card.grid(row=0, column=col, padx=10, pady=10)
        card.grid_propagate(False)

        ctk.CTkLabel(card, text=title, font=ctk.CTkFont(size=14)).pack(pady=(15, 5))
        val_label = ctk.CTkLabel(card, text=value, font=ctk.CTkFont(size=24, weight="bold"), text_color="#1f538d")
        val_label.pack(pady=5)
        return val_label

    def update_stats(self):
        records = self.db.get_all_records()
        if records is None:
            # Clear stats if DB fails
            self.card_total.configure(text="Err")
            self.card_avg.configure(text="Err")
            self.card_topper.configure(text="Err")
            return
        if not records:
            return

        df = pd.DataFrame(records)
        if df.empty: return

        total_students = len(df)
        
        # Calculate individual student averages
        marks_cols = self.subjects
        df_marks = df[marks_cols].apply(pd.to_numeric, errors='coerce')
        df['Avg'] = df_marks.mean(axis=1)
        
        class_avg = round(df['Avg'].mean(), 2)
        top_student = df.loc[df['Avg'].idxmax()]['NAME'] if not df.empty else "-"

        self.card_total.configure(text=str(total_students))
        self.card_avg.configure(text=str(class_avg))
        self.card_topper.configure(text=str(top_student))

        # Clear previous chart
        for widget in self.chart_frame.winfo_children():
            widget.destroy()

        # Create Subject Averages Chart
        subj_avgs = df_marks.mean()
        
        fig, ax = plt.subplots(figsize=(8, 4), dpi=100)
        fig.patch.set_facecolor('#2b2b2b')
        ax.set_facecolor('#2b2b2b')
        
        bars = ax.bar(subj_avgs.index, subj_avgs.values, color='#1f538d', width=0.6)
        
        ax.set_title("Average Score per Subject", color='white', fontsize=14, pad=20)
        ax.tick_params(axis='x', colors='white')
        ax.tick_params(axis='y', colors='white')
        ax.set_ylim(0, 100)
        
        # Add labels on top of bars
        for bar in bars:
            yval = round(bar.get_height(), 1)
            ax.text(bar.get_x() + bar.get_width()/2, yval + 2, yval, ha='center', va='bottom', color='white', fontsize=10)

        # Remove spines
        for spine in ax.spines.values():
            spine.set_visible(False)

        plt.tight_layout()

        canvas = FigureCanvasTkAgg(fig, master=self.chart_frame)
        canvas.draw()
        canvas.get_tk_widget().pack(fill="both", expand=True)

if __name__ == "__main__":
    app = StudentAppPro()
    app.mainloop()
