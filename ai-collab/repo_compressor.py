from os import environ, path, listdir
from pathlib import Path
import sys

SKIP_DIRS_CONTAINING = [".", "target", "dist", "node_modules"]
SKIP_FILES = ["package-lock.json"]

REPO_PATH = environ.get("REPO_PATH")
if not REPO_PATH:
    print("REPO_PATH is required but wasn't supplied!")
    sys.exit(-1)

output_file = path.join(REPO_PATH, ".repo.txt")

def walk_dir_recursively(root_dir, collected_file_paths):
    for f in listdir(root_dir):
        f_path = path.join(root_dir, f)
        if path.isdir(f_path):
            if should_process_dir(f):
                walk_dir_recursively(f_path, collected_file_paths)
            else:
                print(f"Skipping {f} dir")
        else:
            if should_process_file(f):
                collected_file_paths.append(f_path)
            else:
                print(f"Skipping {f} file")

def should_process_dir(dir_name):
    for skip in SKIP_DIRS_CONTAINING:
        if skip in dir_name:
            return False
    return True

def should_process_file(file_name):
    return file_name not in SKIP_FILES


collected_file_paths = []
repo_path = path.abspath(REPO_PATH)

walk_dir_recursively(repo_path, collected_file_paths)

with open(output_file, "w") as rf:
    for path in collected_file_paths:
        with open(path, "r") as f:
            content = f.read()

        file_headline = f"{path.replace(repo_path, "")} file:"
        rf.write(file_headline)
        rf.write("\n")
        rf.write(content)
        rf.write("\n")
