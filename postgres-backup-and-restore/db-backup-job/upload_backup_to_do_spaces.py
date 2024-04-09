from os import environ
import boto3

region = environ["DO_REGION"]
access_key_id = environ["DO_SPACES_KEY"]
access_key = environ["DO_SPACES_SECRET"]
do_spaces_bucket = environ["DO_SPACES_BUCKET"]
do_spaces_bucket_folder = environ["DO_SPACES_BUCKET_FOLDER"]

backup_local_path = environ["BACKUP_LOCAL_PATH"]
backup_name = environ["BACKUP_NAME"]

max_backups = int(environ["MAX_BACKUPS"])

client = boto3.client('s3',
                      region_name=region,
                      endpoint_url=f'https://{region}.digitaloceanspaces.com',
                      aws_access_key_id=access_key_id,
                      aws_secret_access_key=access_key)

do_spaces_backup_key = f"{do_spaces_bucket_folder}/{backup_name}"

print(f"Uploading {backup_local_path} to {do_spaces_backup_key} on {do_spaces_bucket} space...")

client.upload_file(backup_local_path, do_spaces_bucket, do_spaces_backup_key)

print("Backup uploaded to DO spaces!")

print("Checking if we should remove old ones...")

backup_objects = client.list_objects_v2(Bucket=do_spaces_bucket, Prefix=do_spaces_bucket_folder).get("Contents", [])

sorted_backups = sorted(b['Key'] for b in backup_objects)
backups_len = len(sorted_backups)

backups_to_delete = backups_len - max_backups

if backups_to_delete > 0:
    print(f"Max number of backups ({max_backups}) was reached, deleting the oldest ones...")
    
    to_delete_backups = sorted_backups[0:backups_to_delete]

    for b in to_delete_backups:
        print(f"Deleting {b} backup...")
        client.delete_object(Bucket=do_spaces_bucket, Key=b)

    print()
    print(f"Oldest {to_delete_backups} backups were deleted from DO spaces")
else:
    print(f"There are less backups ({len(sorted_backups)}) than max allowed ({max_backups}), skipping deletion")