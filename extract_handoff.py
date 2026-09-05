import os

def extract_files(handoff_path, target_dir):
    with open(handoff_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all file blocks
    # We look for "----- BEGIN COMPLETE FILE: <filepath> -----"
    # and "----- END COMPLETE FILE: <filepath> -----"
    
    import re
    
    pattern = r"----- BEGIN COMPLETE FILE: (.*?) -----\n(.*?)----- END COMPLETE FILE: \1 -----"
    matches = re.finditer(pattern, content, re.DOTALL)
    
    extracted_count = 0
    for match in matches:
        filepath = match.group(1).strip()
        filecontent = match.group(2)
        
        full_path = os.path.join(target_dir, filepath)
        
        # ensure dir exists
        os.makedirs(os.path.dirname(full_path), exist_ok=True)
        
        with open(full_path, 'w', encoding='utf-8') as out_f:
            out_f.write(filecontent)
            
        print(f"Extracted: {filepath}")
        extracted_count += 1
        
    print(f"Total files extracted: {extracted_count}")

if __name__ == '__main__':
    extract_files(
        r'P:\projects\WHO Standard health Tracker\WHO_Health_Tracker_Full_Implementation_Handoff.txt',
        r'P:\projects\WHO Standard health Tracker'
    )
