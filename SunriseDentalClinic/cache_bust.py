import os, re

dirs = [r'd:\Sunrise Dental Clinic_New\SunriseDentalClinic\web', r'd:\Sunrise Dental Clinic_New\SunriseDentalClinic\build\web']

for d in dirs:
    for root, _, files in os.walk(d):
        for f in files:
            if f.endswith('.html'):
                path = os.path.join(root, f)
                with open(path, 'r', encoding='utf-8') as file:
                    content = file.read()
                
                new_content = re.sub(r'\.js(\?v=\d+)?"', '.js?v=7"', content)
                
                with open(path, 'w', encoding='utf-8') as file:
                    file.write(new_content)
                print(f"Updated {path}")
