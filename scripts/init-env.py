#!/usr/bin/env python3
"""Create the local .env once from .env.example; never print generated secrets."""
import os
import re
import secrets
from pathlib import Path

root = Path(__file__).resolve().parents[1]
template = (root / ".env.example").read_text()
# Each GENERATE_ME gets its own random value (>= 32 bytes, enough for HS256).
text = re.sub("GENERATE_ME", lambda _: secrets.token_urlsafe(48), template)
try:
    fd = os.open(root / ".env", os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
except FileExistsError:
    raise SystemExit(".env already exists; kept unchanged")
with os.fdopen(fd, "w") as out:
    out.write(text)
print("Created development .env")
