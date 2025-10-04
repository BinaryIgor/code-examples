import json
import subprocess
from os import path

with open('config.json') as f:
    config = json.load(f)

register_lines = [];
lines_to_write = []
for c in config['components']:
    with open(path.join('src', c)) as c:
        skip_next_line = False
        for c_line in c.readlines():
            if 'customElements.define' in c_line:
                register_lines.append(c_line)
                skip_next_line = True
                continue

            if skip_next_line:
                skip_next_line = False
                continue

            if 'import' in c_line or 'register()' in c_line:
                continue

            lines_to_write.append(c_line)


def git_metadata():
    commit_hash = subprocess.check_output(["git", "rev-parse", "HEAD"]).strip().decode('utf-8')
    branch = subprocess.check_output(["git", "rev-parse", "--abbrev-ref", "HEAD"]).strip().decode('utf-8')
    return f'{branch}:{commit_hash}'
    

metadata = f'Generated from {git_metadata()}'

for o in config['outputs']:
    with open(o, "w") as of:
        of.write(f"// {metadata}\n\n")
        of.write(''.join(lines_to_write))
        of.write('\n')
        of.write('export function registerComponents() {')
        of.write('\n')
        of.write(''.join(register_lines))
        of.write('}')
