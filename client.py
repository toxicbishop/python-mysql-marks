import requests
import sys

# The URL where your API is running
# If running locally, use localhost. If on server, use the server IP.
API_URL = "http://127.0.0.1:8000"

def main():
    print("\n--- CUI CLIENT (SECURE MODE) ---")
    
    # 1. Get Student Info
    name = input("Enter Student Name: ")
    roll_no = int(input("Enter Roll No: "))

    # Send Data to Server (No DB connection here!)
    student_data = {"name": name, "roll_no": roll_no}
    
    try:
        response = requests.post(f"{API_URL}/add_student", json=student_data)
        if response.status_code == 200:
            print(f"Server says: {response.json()['message']}")
        else:
            print(f"Error: {response.text}")
            return
    except requests.exceptions.ConnectionError:
        print("[CRITICAL] Cannot reach server. Is api.py running?")
        return

    # 2. Enter Marks
    print(f"\nEnter marks for {name}:")
    subjects = [
        ("Science", 101), ("Social", 102), ("Maths", 103),
        ("Eng", 104), ("Hindi", 105), ("Kannada", 106)
    ]

    for sub_name, sub_id in subjects:
        marks = int(input(f"{sub_name}: "))
        
        marks_data = {
            "roll_no": roll_no,
            "subject_id": sub_id,
            "marks": marks
        }
        
        requests.post(f"{API_URL}/add_marks", json=marks_data)

    print("\n[SUCCESS] All data sent to server successfully.")

if __name__ == "__main__":
    main()