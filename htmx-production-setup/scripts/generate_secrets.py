import base64
import os
import secrets
import string

KEY_BYTES_LENGTH = 32
PASSWORD_LENGTH = 48
PASSWORD_CHARACTERS = f'{string.ascii_letters}{string.digits}'

def random_key():
    return base64.b64encode(os.urandom(KEY_BYTES_LENGTH)).decode("ascii")


def random_password():
    characters = list(PASSWORD_CHARACTERS)
    return ''.join(secrets.choice(characters) for _ in range(PASSWORD_LENGTH))


auth_token_key = random_key()
db_root_password = random_password()
db_password= random_password()

print("Go to needed directory and simply run:")
print(f"""
echo "{auth_token_key}" > auth-token-key.txt
echo "{db_root_password}" > db-root-password.txt
echo "{db_password}" > db-password.txt
""")
