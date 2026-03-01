import pymysql
import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST'),
    'port': int(os.getenv('DB_PORT', 11624)),
    'user': os.getenv('DB_USER'),
    'password': os.getenv('DB_PASS'),
    'database': os.getenv('DB_NAME'),
    'ssl': {},
    'connect_timeout': 10
}

sql_file_path = os.path.join(os.path.dirname(__file__), '..', 'Student-GUI-version1', 'school_db.sql')

def setup_database():
    try:
        connection = pymysql.connect(**DB_CONFIG)
        with connection.cursor() as cursor:
            with open(sql_file_path, 'r') as f:
                sql_commands = f.read().split(';')
                for command in sql_commands:
                    if command.strip():
                        cursor.execute(command)
            connection.commit()
            print("✅ Database setup successfully! All tables created.")
    except Exception as e:
        print(f"❌ Error setting up database: {e}")
    finally:
        if 'connection' in locals():
            connection.close()

if __name__ == "__main__":
    setup_database()
