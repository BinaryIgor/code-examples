import json
import subprocess
from os import path, listdir, environ

components_input_dir=environ["COMPONENTS_INPUT_DIR"]
components_output_path=environ["COMPONENTS_OUTPUT_PATH"]

content_to_write = ""

for c in listdir(components_input_dir):
    if not c.endswith(".js"):
        continue

    with open(path.join(components_input_dir, c)) as c:
        content_to_write += (c.read() + "\n\n")


with open(components_output_path, "w") as f:
    f.write(content_to_write.strip())


