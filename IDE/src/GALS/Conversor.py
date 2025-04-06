import re

def main():
    # Open and read the Java file
    with open("ScannerConstants.java", "r") as file:
        content = file.read()
    
    # Use regex to capture everything between the '=' and the closing '};'
    match = re.search(r'int\[\]\[\] SCANNER_TABLE\s*=\s*\{(.*?)\};', content, re.DOTALL)
    if not match:
        print("SCANNER_TABLE not found in the file.")
        return

    # Extract the array contents
    array_content = match.group(1)
    
    # Remove curly braces and line breaks to simplify splitting
    cleaned = array_content.replace('{', '').replace('}', '').replace('\n', '')
    
    # Split by comma and convert each non-empty part to an integer
    try:
        numbers = [int(num.strip()) for num in cleaned.split(",") if num.strip()]
    except ValueError as e:
        print("Error converting values to integers:", e)
        return

    # Check if we have an even number of integers to form pairs
    if len(numbers) % 2 != 0:
        print("Warning: The number of integers is not even. Some values may be missing to form a complete pair.")
    
    # Write pairs of integers to the output file, one pair per line
    with open("arquivo.txt", "w") as out_file:
        for i in range(0, len(numbers) - 1, 2):
            # Write two integers separated by a space, no extra spaces on the left
            out_file.write(f"{numbers[i]} {numbers[i+1]}\n")

if __name__ == "__main__":
    main()
