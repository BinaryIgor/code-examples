import os

backups_path = os.environ["BACKUPS_PATH"]
max_backups = int(os.environ["MAX_BACKUPS"])

backups = sorted(os.listdir(backups_path))
backups_len = len(backups)

to_remove = backups_len - max_backups
if to_remove > 0:
    print(f"Max number of backups ({max_backups}) was reached, removing the oldest ones...")
    to_remove_backups = backups[0:to_remove]
    for b in to_remove_backups:
        b_to_remove = os.path.join(backups_path, b)
        print(f"Removing {b_to_remove} backup...")
        os.remove(b_to_remove)
    print()
    print(f"Oldest {to_remove_backups} backups were removed from local file system")
else:
    print(f"No need to remove backups, since we have {backups_len} and {max_backups} are allowed")
